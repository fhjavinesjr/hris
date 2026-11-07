package com.primehr.evidence;

import com.primehr.evidence.application.EvidenceReadinessCalculator;
import com.primehr.evidence.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvidenceReadinessCalculatorTest {
    private final EvidenceReadinessCalculator calculator = new EvidenceReadinessCalculator();

    @Test
    void calculatesTransparentDenominatorsExclusionsBlockersExpiryAndOverdueFacts() {
        LocalDate today = LocalDate.of(2026, 11, 5);
        EvidenceAssessmentCase assessmentCase = mock(EvidenceAssessmentCase.class);
        when(assessmentCase.getId()).thenReturn("case");
        when(assessmentCase.getCycleId()).thenReturn("cycle");
        when(assessmentCase.getFrameworkVersionId()).thenReturn("framework");
        when(assessmentCase.getContentRevision()).thenReturn(4);
        when(assessmentCase.getSubmissionRevision()).thenReturn(2);
        EvidenceAssessmentCycle cycle = mock(EvidenceAssessmentCycle.class);
        when(cycle.getFrameworkDefinitionVersion()).thenReturn(3);
        when(cycle.getFrameworkContentRevision()).thenReturn(7);
        when(cycle.getFrameworkFingerprint()).thenReturn("fingerprint");

        EvidenceRequirement met = requirement("r1", "REQ-1", "Evidence present");
        EvidenceRequirement notApplicable = requirement("r2", "REQ-2", "Excluded by authority");
        EvidenceRequirement needsReview = requirement("r3", "REQ-3", "Still incomplete");
        EvidenceFinding metFinding = finding("r1", EvidenceFinding.Outcome.MET);
        EvidenceFinding excludedFinding = finding("r2", EvidenceFinding.Outcome.NOT_APPLICABLE);
        EvidenceFinding reviewFinding = finding("r3", EvidenceFinding.Outcome.NEEDS_REVIEW);
        EvidenceArtifact valid = mock(EvidenceArtifact.class);
        when(valid.isCurrentRecord()).thenReturn(true);
        when(valid.getRequirementId()).thenReturn("r1");
        when(valid.getRequirementCode()).thenReturn("REQ-1");
        when(valid.getVerificationStatus()).thenReturn(EvidenceArtifact.VerificationStatus.VERIFIED);
        when(valid.getReviewDueDate()).thenReturn(today.plusDays(5));
        EvidenceCorrectiveAction overdue = mock(EvidenceCorrectiveAction.class);
        when(overdue.getTargetDate()).thenReturn(today.minusDays(3));
        when(overdue.getStatus()).thenReturn(EvidenceCorrectiveAction.Status.IN_PROGRESS);

        var result = calculator.calculate(assessmentCase, cycle,
                List.of(met, notApplicable, needsReview), List.of(valid),
                List.of(metFinding, excludedFinding, reviewFinding), List.of(overdue),
                today, today.plusDays(30));

        assertThat(result.totalRequirements()).isEqualTo(3);
        assertThat(result.excludedRequirements()).isOne();
        assertThat(result.applicableRequirements()).isEqualTo(2);
        assertThat(result.assessedRequirements()).isEqualTo(2);
        assertThat(result.coveredRequirements()).isOne();
        assertThat(result.completenessPercent()).isEqualByComparingTo("66.67");
        assertThat(result.coveragePercent()).isEqualByComparingTo("50.00");
        assertThat(result.expiringEvidenceCount()).isOne();
        assertThat(result.overdueActionCount()).isOne();
        assertThat(result.missingInputs()).singleElement().asString().contains("REQ-3", "needs review");
        assertThat(result.disclaimer()).contains("not an official CSC PRIME-ESG score");
    }

    private static EvidenceRequirement requirement(String id, String code, String title) {
        EvidenceRequirement value = mock(EvidenceRequirement.class);
        when(value.getId()).thenReturn(id);
        when(value.getCode()).thenReturn(code);
        when(value.getTitle()).thenReturn(title);
        when(value.isRequired()).thenReturn(true);
        return value;
    }

    private static EvidenceFinding finding(String requirementId, EvidenceFinding.Outcome outcome) {
        EvidenceFinding value = mock(EvidenceFinding.class);
        when(value.isCurrentRecord()).thenReturn(true);
        when(value.getRequirementId()).thenReturn(requirementId);
        when(value.getOutcome()).thenReturn(outcome);
        return value;
    }
}
