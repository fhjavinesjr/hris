package com.primehr.evidence.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class EvidenceReadinessData {
    private EvidenceReadinessData() { }

    public record RequirementFact(String requirementId, String requirementCode, String title,
            boolean required, String findingOutcome, boolean excluded, int currentEvidenceCount,
            int validEvidenceCount, boolean covered, List<String> blockers) { }

    public record Calculation(String assessmentCaseId, String cycleId, String frameworkVersionId,
            int frameworkDefinitionVersion, int frameworkContentRevision, String frameworkFingerprint,
            int caseContentRevision, int caseSubmissionRevision, LocalDate calculatedOn,
            LocalDate expiringThrough, int totalRequirements, int excludedRequirements,
            int applicableRequirements, int assessedRequirements, int coveredRequirements,
            BigDecimal completenessPercent, BigDecimal coveragePercent, int missingInputCount,
            int expiringEvidenceCount, int overdueActionCount, List<RequirementFact> requirements,
            List<String> missingInputs, String disclaimer) { }

    public record Snapshot(String id, long recordVersion, String assessmentCaseId, String cycleId,
            String frameworkVersionId, int frameworkDefinitionVersion, int frameworkContentRevision,
            String frameworkFingerprint, int caseContentRevision, int caseSubmissionRevision,
            int snapshotRevision, String supersedesSnapshotId, int totalRequirements,
            int excludedRequirements, int applicableRequirements, int assessedRequirements,
            int coveredRequirements, BigDecimal completenessPercent, BigDecimal coveragePercent,
            int missingInputCount, int expiringEvidenceCount, int overdueActionCount,
            String factsFingerprint, String disclaimer, String signedBy, Instant signedAt,
            String signOffReason, List<RequirementFact> requirements) { }

    public record EvidenceException(String assessmentCaseId, String caseCode, String organizationName,
            String requirementCode, String evidenceId, String exceptionType, String details,
            LocalDate relevantDate) { }

    public record ActionException(String assessmentCaseId, String caseCode, String organizationName,
            String actionId, String responsibleEmployeeNo, String responsibleName, String status,
            LocalDate targetDate, long overdueDays) { }

    public record ExceptionView(LocalDate asOf, LocalDate expiringThrough,
            List<EvidenceException> evidence, List<ActionException> overdueActions) { }

    public record ModuleMetric(String code, String label, String value, String unit) { }
    public record ModuleAggregate(String module, int minimumCohort, int cohortSize, boolean suppressed,
            String suppressionReason, List<ModuleMetric> metrics) { }
    public record Analytics(LocalDate from, LocalDate to, int minimumCohort, int assessmentCohort,
            boolean assessmentSuppressed, String assessmentSuppressionReason,
            List<ModuleMetric> assessmentMetrics, List<ModuleAggregate> modules,
            String privacyNotice) { }
}
