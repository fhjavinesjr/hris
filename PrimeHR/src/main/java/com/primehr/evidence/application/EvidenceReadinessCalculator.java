package com.primehr.evidence.application;

import com.primehr.evidence.domain.*;
import com.primehr.evidence.application.EvidenceReadinessData.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EvidenceReadinessCalculator {
    public Calculation calculate(EvidenceAssessmentCase assessmentCase, EvidenceAssessmentCycle cycle,
            List<EvidenceRequirement> requirements, List<EvidenceArtifact> artifacts,
            List<EvidenceFinding> findings, List<EvidenceCorrectiveAction> actions,
            LocalDate asOf, LocalDate expiringThrough) {
        Objects.requireNonNull(assessmentCase, "assessmentCase");
        Objects.requireNonNull(cycle, "cycle");
        Objects.requireNonNull(asOf, "asOf");
        if (expiringThrough == null || expiringThrough.isBefore(asOf)) {
            throw new IllegalArgumentException("expiringThrough cannot precede asOf");
        }
        Map<String, EvidenceFinding> findingByRequirement = findings.stream()
                .filter(EvidenceFinding::isCurrentRecord)
                .collect(Collectors.toMap(EvidenceFinding::getRequirementId, Function.identity(), (left, right) -> right));
        Map<String, List<EvidenceArtifact>> evidenceByRequirement = artifacts.stream()
                .filter(EvidenceArtifact::isCurrentRecord)
                .collect(Collectors.groupingBy(EvidenceArtifact::getRequirementId));
        List<RequirementFact> facts = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        int excluded = 0;
        int assessed = 0;
        int covered = 0;
        int expiring = 0;
        for (EvidenceRequirement requirement : requirements) {
            EvidenceFinding finding = findingByRequirement.get(requirement.getId());
            boolean notApplicable = finding != null && finding.getOutcome() == EvidenceFinding.Outcome.NOT_APPLICABLE;
            boolean decided = finding != null && finding.getOutcome() != EvidenceFinding.Outcome.NEEDS_REVIEW;
            if (notApplicable) excluded++;
            if (decided) assessed++;
            List<EvidenceArtifact> current = evidenceByRequirement.getOrDefault(requirement.getId(), List.of());
            List<EvidenceArtifact> valid = current.stream().filter(value -> valid(value, asOf)).toList();
            boolean requirementCovered = !notApplicable && !valid.isEmpty();
            if (requirementCovered) covered++;
            expiring += (int) valid.stream().filter(value -> value.getReviewDueDate() != null
                    && !value.getReviewDueDate().isBefore(asOf)
                    && !value.getReviewDueDate().isAfter(expiringThrough)).count();
            List<String> blockers = new ArrayList<>();
            if (finding == null) blockers.add("Missing human finding");
            else if (finding.getOutcome() == EvidenceFinding.Outcome.NEEDS_REVIEW) blockers.add("Human finding still needs review");
            if (!notApplicable && valid.isEmpty()) {
                if (current.isEmpty()) blockers.add("No current evidence");
                else current.forEach(value -> blockers.add(evidenceBlocker(value, asOf)));
            }
            if (!blockers.isEmpty()) missing.add(requirement.getCode() + ": " + String.join("; ", blockers));
            facts.add(new RequirementFact(requirement.getId(), requirement.getCode(), requirement.getTitle(),
                    requirement.isRequired(), finding == null ? null : finding.getOutcome().name(), notApplicable,
                    current.size(), valid.size(), requirementCovered, List.copyOf(blockers)));
        }
        int total = requirements.size();
        if (total == 0) throw new IllegalStateException("Published framework has no evidence requirements");
        int applicable = total - excluded;
        int overdue = (int) actions.stream().filter(value -> value.getTargetDate().isBefore(asOf)
                && value.getStatus() != EvidenceCorrectiveAction.Status.VERIFIED
                && value.getStatus() != EvidenceCorrectiveAction.Status.CANCELLED).count();
        return new Calculation(assessmentCase.getId(), assessmentCase.getCycleId(),
                assessmentCase.getFrameworkVersionId(), cycle.getFrameworkDefinitionVersion(),
                cycle.getFrameworkContentRevision(), cycle.getFrameworkFingerprint(),
                assessmentCase.getContentRevision(), assessmentCase.getSubmissionRevision(), asOf,
                expiringThrough, total, excluded, applicable, assessed, covered,
                percent(assessed, total), percent(covered, applicable), missing.size(), expiring, overdue,
                List.copyOf(facts), List.copyOf(missing), EvidenceReadinessSnapshot.NON_OFFICIAL_DISCLAIMER);
    }

    public long overdueDays(EvidenceCorrectiveAction action, LocalDate asOf) {
        return Math.max(0, ChronoUnit.DAYS.between(action.getTargetDate(), asOf));
    }

    private static boolean valid(EvidenceArtifact value, LocalDate asOf) {
        return value.getVerificationStatus() == EvidenceArtifact.VerificationStatus.VERIFIED
                && (value.getReviewDueDate() == null || !value.getReviewDueDate().isBefore(asOf));
    }

    private static String evidenceBlocker(EvidenceArtifact value, LocalDate asOf) {
        if (value.getVerificationStatus() != EvidenceArtifact.VerificationStatus.VERIFIED) {
            return value.getRequirementCode() + " evidence is " + value.getVerificationStatus().name();
        }
        if (value.getReviewDueDate() != null && value.getReviewDueDate().isBefore(asOf)) {
            return value.getRequirementCode() + " evidence expired on " + value.getReviewDueDate();
        }
        return value.getRequirementCode() + " evidence is unavailable";
    }

    private static BigDecimal percent(int numerator, int denominator) {
        if (denominator == 0) return BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
