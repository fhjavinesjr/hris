package com.primehr.evidence.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehr.evidence.api.EvidenceAssessmentDtos.CaseResponse;
import com.primehr.evidence.api.EvidenceReadinessDtos.SignOffInput;
import com.primehr.evidence.application.EvidenceReadinessData.*;
import com.primehr.evidence.domain.*;
import com.primehr.evidence.infrastructure.*;
import com.primehr.evidence.report.EvidenceReadinessReportData;
import com.primehr.evidence.report.EvidenceReadinessReportRenderer;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class EvidenceReadinessServiceImpl implements EvidenceReadinessService {
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");
    private static final String PRIVACY_NOTICE = "Aggregate decision support only. Suppressed cohorts and source-domain restrictions are not bypassed, and no employee ranking is produced.";
    private final EvidenceAssessmentService assessments;
    private final EvidenceAssessmentCaseRepository cases;
    private final EvidenceAssessmentCycleRepository cycles;
    private final EvidenceRequirementRepository requirements;
    private final EvidenceArtifactRepository artifacts;
    private final EvidenceFindingRepository findings;
    private final EvidenceCorrectiveActionRepository actions;
    private final EvidenceReadinessSnapshotRepository snapshots;
    private final EvidenceReadinessCalculator calculator;
    private final EvidenceAggregateSourcePort aggregateSources;
    private final EvidenceReadinessReportRenderer renderer;
    private final PrimeHrAuditService audit;
    private final ObjectMapper json;
    private final int minimumCohort;

    public EvidenceReadinessServiceImpl(EvidenceAssessmentService assessments,
            EvidenceAssessmentCaseRepository cases, EvidenceAssessmentCycleRepository cycles,
            EvidenceRequirementRepository requirements, EvidenceArtifactRepository artifacts,
            EvidenceFindingRepository findings, EvidenceCorrectiveActionRepository actions,
            EvidenceReadinessSnapshotRepository snapshots,
            EvidenceReadinessCalculator calculator, EvidenceAggregateSourcePort aggregateSources,
            EvidenceReadinessReportRenderer renderer, PrimeHrAuditService audit, ObjectMapper json,
            @Value("${primehr.reporting.minimum-aggregate-cohort:5}") int minimumCohort) {
        if (minimumCohort < 2 || minimumCohort > 100) {
            throw new IllegalArgumentException("minimum aggregate cohort must be between 2 and 100");
        }
        this.assessments = assessments;
        this.cases = cases;
        this.cycles = cycles;
        this.requirements = requirements;
        this.artifacts = artifacts;
        this.findings = findings;
        this.actions = actions;
        this.snapshots = snapshots;
        this.calculator = calculator;
        this.aggregateSources = aggregateSources;
        this.renderer = renderer;
        this.audit = audit;
        this.json = json;
        this.minimumCohort = minimumCohort;
    }

    @Override
    @Transactional(readOnly = true)
    public Calculation calculate(String agencyId, String caseId, LocalDate asOf, LocalDate expiringThrough,
            String actor, PermissionDataScope scope) {
        assessments.get(agencyId, caseId, actor, scope);
        return calculation(agencyId, entity(agencyId, caseId), dates(asOf, expiringThrough));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Snapshot> snapshots(String agencyId, String caseId, String actor, PermissionDataScope scope) {
        assessments.get(agencyId, caseId, actor, scope);
        return snapshots.findByAgencyIdAndAssessmentCaseIdOrderBySnapshotRevisionAsc(agencyId, caseId)
                .stream().map(this::snapshot).toList();
    }

    @Override
    public Snapshot signOff(String agencyId, String caseId, SignOffInput input, String actor,
            PermissionDataScope scope, String correlationId) {
        assessments.get(agencyId, caseId, actor, scope);
        EvidenceAssessmentCase assessmentCase = entity(agencyId, caseId);
        check(assessmentCase.getVersion(), input.caseRecordVersion());
        if (assessmentCase.getStatus() != EvidenceAssessmentCase.Status.FINALIZED) {
            throw new IllegalStateException("Only a finalized internal assessment may be signed off");
        }
        if (assessmentCase.isValidatorRecused() || !assessmentCase.getValidatorEmployeeNo().equalsIgnoreCase(actor)
                || !actor.equalsIgnoreCase(assessmentCase.getFinalizedBy())) {
            throw new AccessDeniedException("Only the non-recused validator who finalized the case may sign its readiness snapshot");
        }
        Optional<EvidenceReadinessSnapshot> prior = snapshots
                .findFirstByAgencyIdAndAssessmentCaseIdOrderBySnapshotRevisionDesc(agencyId, caseId);
        validateSuccessor(prior, input);
        DateWindow window = dates(LocalDate.now(MANILA), input.expiringThrough());
        Calculation calculation = calculation(agencyId, assessmentCase, window);
        String payload = serialize(calculation);
        int revision = Math.toIntExact(snapshots.countByAgencyIdAndAssessmentCaseId(agencyId, caseId) + 1);
        EvidenceReadinessSnapshot value = new EvidenceReadinessSnapshot(agencyId, caseId,
                calculation.cycleId(), calculation.frameworkVersionId(),
                calculation.frameworkDefinitionVersion(), calculation.frameworkContentRevision(),
                calculation.frameworkFingerprint(), calculation.caseContentRevision(),
                calculation.caseSubmissionRevision(), revision, prior.map(EvidenceReadinessSnapshot::getId).orElse(null),
                calculation.totalRequirements(), calculation.excludedRequirements(),
                calculation.applicableRequirements(), calculation.assessedRequirements(),
                calculation.coveredRequirements(), calculation.completenessPercent(), calculation.coveragePercent(),
                calculation.missingInputCount(), calculation.expiringEvidenceCount(),
                calculation.overdueActionCount(), payload, sha(payload), actor, Instant.now(), input.reason());
        value = snapshots.saveAndFlush(value);
        Snapshot response = snapshot(value);
        audit.record(agencyId, prior.isPresent() ? "CREATE_INTERNAL_READINESS_SNAPSHOT_CORRECTION"
                        : "SIGN_INTERNAL_READINESS_SNAPSHOT", "EVIDENCE_READINESS_SNAPSHOT", value.getId(),
                revision, value.getVersion(), null, response, input.reason(), correlationId);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ExceptionView exceptions(String agencyId, LocalDate asOf, LocalDate expiringThrough,
            String actor, PermissionDataScope scope) {
        DateWindow window = dates(asOf, expiringThrough);
        List<CaseResponse> visible = assessments.cases(agencyId, actor, scope);
        List<EvidenceException> evidence = new ArrayList<>();
        List<ActionException> overdue = new ArrayList<>();
        for (CaseResponse item : visible) {
            EvidenceAssessmentCase assessmentCase = entity(agencyId, item.id());
            Calculation calculation = calculation(agencyId, assessmentCase, window);
            calculation.requirements().stream().filter(value -> !value.blockers().isEmpty()).forEach(value ->
                    evidence.add(new EvidenceException(item.id(), item.code(), item.organizationName(),
                            value.requirementCode(), null, "MISSING_OR_INVALID",
                            String.join("; ", value.blockers()), window.asOf())));
            artifacts.findByAgencyIdAndAssessmentCaseIdAndCurrentRecordTrueOrderByAddedAtAsc(agencyId, item.id())
                    .stream().filter(value -> value.getReviewDueDate() != null
                            && !value.getReviewDueDate().isBefore(window.asOf())
                            && !value.getReviewDueDate().isAfter(window.expiringThrough()))
                    .forEach(value -> evidence.add(new EvidenceException(item.id(), item.code(), item.organizationName(),
                            value.getRequirementCode(), value.getId(), "EXPIRING", value.getTitle(),
                            value.getReviewDueDate())));
            actions.findByAgencyIdAndAssessmentCaseIdOrderByTargetDateAsc(agencyId, item.id()).stream()
                    .filter(value -> value.getTargetDate().isBefore(window.asOf())
                            && value.getStatus() != EvidenceCorrectiveAction.Status.VERIFIED
                            && value.getStatus() != EvidenceCorrectiveAction.Status.CANCELLED)
                    .forEach(value -> overdue.add(new ActionException(item.id(), item.code(), item.organizationName(),
                            value.getId(), value.getResponsibleEmployeeNo(), value.getResponsibleName(),
                            value.getStatus().name(), value.getTargetDate(), calculator.overdueDays(value, window.asOf()))));
        }
        return new ExceptionView(window.asOf(), window.expiringThrough(), List.copyOf(evidence), List.copyOf(overdue));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] evidenceIndex(String agencyId, String caseId, String actor, PermissionDataScope scope) {
        CaseResponse value = assessments.get(agencyId, caseId, actor, scope);
        List<EvidenceReadinessReportData.Row> rows = new ArrayList<>();
        add(rows, "Assessment", "Case", value.code() + " - " + value.title(), value.status());
        add(rows, "Assessment", "Organization", value.organizationName(), value.organizationType());
        value.evidence().forEach(item -> add(rows, "Evidence index", item.requirementCode() + " - " + item.title(),
                source(item) + " | evidence " + item.evidenceDate() + " | review " + text(item.reviewDueDate())
                        + " | " + item.classification() + "/" + item.visibility(),
                item.currentRecord() ? item.verificationStatus() : "SUPERSEDED"));
        if (value.evidence().isEmpty()) add(rows, "Evidence index", "No evidence", "No evidence is recorded.", "MISSING");
        return renderer.render("Internal Evidence Index", EvidenceReadinessSnapshot.NON_OFFICIAL_DISCLAIMER,
                actor, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] internalAssessment(String agencyId, String snapshotId, String actor, PermissionDataScope scope) {
        EvidenceReadinessSnapshot value = snapshots.findByIdAndAgencyId(snapshotId, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Internal readiness snapshot not found"));
        CaseResponse assessment = assessments.get(agencyId, value.getAssessmentCaseId(), actor, scope);
        Snapshot response = snapshot(value);
        List<EvidenceReadinessReportData.Row> rows = new ArrayList<>();
        add(rows, "Signed snapshot", "Case", assessment.code() + " - " + assessment.title(), assessment.status());
        add(rows, "Signed snapshot", "Revision", response.snapshotRevision() + " / " + response.factsFingerprint(), "IMMUTABLE");
        add(rows, "Transparent facts", "Completeness", response.assessedRequirements() + " / "
                + response.totalRequirements() + " = " + response.completenessPercent() + "%", "DECISION SUPPORT");
        add(rows, "Transparent facts", "Coverage", response.coveredRequirements() + " / "
                + response.applicableRequirements() + " = " + response.coveragePercent() + "%", "DECISION SUPPORT");
        add(rows, "Exceptions", "Missing / expiring / overdue", response.missingInputCount() + " / "
                + response.expiringEvidenceCount() + " / " + response.overdueActionCount(), "OPEN FACTS");
        response.requirements().forEach(item -> add(rows, "Requirement", item.requirementCode() + " - " + item.title(),
                "finding=" + text(item.findingOutcome()) + "; evidence=" + item.validEvidenceCount() + "/"
                        + item.currentEvidenceCount() + (item.blockers().isEmpty() ? "" : "; " + String.join("; ", item.blockers())),
                item.excluded() ? "EXCLUDED - NOT APPLICABLE" : item.covered() ? "COVERED" : "NOT COVERED"));
        add(rows, "Human sign-off", response.signedBy() + " at " + response.signedAt(), response.signOffReason(), "SIGNED");
        return renderer.render("Internal Evidence Readiness Self-Assessment", response.disclaimer(), actor, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public Analytics analytics(String agencyId, LocalDate from, LocalDate to, String authorization,
            String actor, String correlationId) {
        if (from == null || to == null || to.isBefore(from)) throw new IllegalArgumentException("A valid analytics date range is required");
        Map<String, EvidenceReadinessSnapshot> latest = snapshots.findByAgencyIdOrderBySignedAtDesc(agencyId).stream()
                .filter(value -> !value.getSignedAt().atZone(MANILA).toLocalDate().isBefore(from)
                        && !value.getSignedAt().atZone(MANILA).toLocalDate().isAfter(to))
                .collect(Collectors.toMap(EvidenceReadinessSnapshot::getAssessmentCaseId, Function.identity(),
                        (left, right) -> left.getSnapshotRevision() > right.getSnapshotRevision() ? left : right));
        int cohort = latest.size();
        boolean suppressed = cohort < minimumCohort;
        List<ModuleMetric> metrics = suppressed ? List.of() : List.of(
                metric("SIGNED_CASES", "Signed internal assessment cases", cohort, "cases"),
                metric("AVERAGE_COMPLETENESS", "Average transparent completeness",
                        average(latest.values(), true), "percent"),
                metric("AVERAGE_COVERAGE", "Average transparent evidence coverage",
                        average(latest.values(), false), "percent"),
                metric("OPEN_MISSING_INPUTS", "Missing or blocked inputs",
                        latest.values().stream().mapToInt(EvidenceReadinessSnapshot::getMissingInputCount).sum(), "items"),
                metric("OVERDUE_ACTIONS", "Overdue corrective actions",
                        latest.values().stream().mapToInt(EvidenceReadinessSnapshot::getOverdueActionCount).sum(), "actions"));
        List<ModuleAggregate> modules = aggregateSources.load(agencyId, from, to, actor, authorization, correlationId);
        Analytics result = new Analytics(from, to, minimumCohort, cohort, suppressed,
                suppressed ? "Evidence assessment results are suppressed because the signed case cohort is below the configured minimum." : null,
                metrics, modules, PRIVACY_NOTICE);
        audit.record(agencyId, "VIEW_EVIDENCE_AGGREGATE_ANALYTICS", "EVIDENCE_ANALYTICS", agencyId,
                null, null, null, Map.of("from", from, "to", to, "cohort", cohort,
                        "suppressed", suppressed, "modules", modules.size()), null, correlationId);
        return result;
    }

    private Calculation calculation(String agencyId, EvidenceAssessmentCase assessmentCase, DateWindow window) {
        EvidenceAssessmentCycle cycle = cycles.findByIdAndAgencyId(assessmentCase.getCycleId(), agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence assessment cycle not found"));
        return calculator.calculate(assessmentCase, cycle,
                requirements.findByAgencyIdAndFrameworkVersionIdOrderByDisplayOrder(agencyId, assessmentCase.getFrameworkVersionId()),
                artifacts.findByAgencyIdAndAssessmentCaseIdOrderByAddedAtAsc(agencyId, assessmentCase.getId()),
                findings.findByAgencyIdAndAssessmentCaseIdOrderByDecidedAtAsc(agencyId, assessmentCase.getId()),
                actions.findByAgencyIdAndAssessmentCaseIdOrderByTargetDateAsc(agencyId, assessmentCase.getId()),
                window.asOf(), window.expiringThrough());
    }

    private EvidenceAssessmentCase entity(String agencyId, String caseId) {
        return cases.findByIdAndAgencyId(caseId, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence assessment case not found"));
    }

    private Snapshot snapshot(EvidenceReadinessSnapshot value) {
        List<RequirementFact> facts;
        try {
            Calculation calculation = json.readValue(value.getFactsPayload(), Calculation.class);
            facts = calculation.requirements();
        } catch (Exception exception) {
            throw new IllegalStateException("Stored readiness snapshot facts are unreadable", exception);
        }
        return new Snapshot(value.getId(), value.getVersion(), value.getAssessmentCaseId(), value.getCycleId(),
                value.getFrameworkVersionId(), value.getFrameworkDefinitionVersion(),
                value.getFrameworkContentRevision(), value.getFrameworkFingerprint(),
                value.getCaseContentRevision(), value.getCaseSubmissionRevision(), value.getSnapshotRevision(),
                value.getSupersedesSnapshotId(), value.getTotalRequirements(), value.getExcludedRequirements(),
                value.getApplicableRequirements(), value.getAssessedRequirements(), value.getCoveredRequirements(),
                value.getCompletenessPercent(), value.getCoveragePercent(), value.getMissingInputCount(),
                value.getExpiringEvidenceCount(), value.getOverdueActionCount(), value.getFactsFingerprint(),
                value.getDisclaimer(), value.getSignedBy(), value.getSignedAt(), value.getSignOffReason(), facts);
    }

    private void validateSuccessor(Optional<EvidenceReadinessSnapshot> prior, SignOffInput input) {
        if (prior.isEmpty()) {
            if (input.supersedesSnapshotId() != null || input.supersedesRecordVersion() != null) {
                throw new IllegalArgumentException("The first snapshot cannot supersede another snapshot");
            }
            return;
        }
        EvidenceReadinessSnapshot value = prior.get();
        if (!value.getId().equals(input.supersedesSnapshotId())) {
            throw new OptimisticConflictException("A correction must supersede the latest readiness snapshot");
        }
        check(value.getVersion(), input.supersedesRecordVersion());
    }

    private DateWindow dates(LocalDate asOf, LocalDate expiringThrough) {
        LocalDate actualAsOf = asOf == null ? LocalDate.now(MANILA) : asOf;
        LocalDate actualThrough = expiringThrough == null ? actualAsOf.plusDays(30) : expiringThrough;
        if (actualThrough.isBefore(actualAsOf)) throw new IllegalArgumentException("expiringThrough cannot precede asOf");
        return new DateWindow(actualAsOf, actualThrough);
    }

    private String serialize(Object value) {
        try { return json.writeValueAsString(value); }
        catch (Exception exception) { throw new IllegalStateException("Readiness facts could not be serialized", exception); }
    }

    private static String sha(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) { throw new IllegalStateException(exception); }
    }

    private static void check(long actual, Long expected) {
        if (expected == null || actual != expected) throw new OptimisticConflictException("Record changed; reload and retry");
    }

    private static ModuleMetric metric(String code, String label, Object value, String unit) {
        return new ModuleMetric(code, label, String.valueOf(value), unit);
    }

    private static BigDecimal average(Collection<EvidenceReadinessSnapshot> values, boolean completeness) {
        if (values.isEmpty()) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return values.stream().map(value -> completeness ? value.getCompletenessPercent() : value.getCoveragePercent())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private static String source(com.primehr.evidence.api.EvidenceAssessmentDtos.ArtifactResponse value) {
        return "LIVE_SOURCE".equals(value.kind())
                ? value.sourceType() + ":" + value.sourceId() + "@" + value.sourceVersion()
                : value.originalFilename() + " | SHA-256 " + value.checksum();
    }

    private static String text(Object value) { return value == null ? "-" : String.valueOf(value); }
    private static void add(List<EvidenceReadinessReportData.Row> rows, String section, String item,
            Object value, String status) {
        rows.add(new EvidenceReadinessReportData.Row(section, item, text(value), text(status)));
    }
    private record DateWindow(LocalDate asOf, LocalDate expiringThrough) { }
}
