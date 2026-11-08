package com.primehr.evidence.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "evidence_readiness_snapshot", uniqueConstraints = {
        @UniqueConstraint(name = "uk_evidence_snapshot_revision", columnNames = {
                "agency_id", "assessment_case_id", "snapshot_revision"
        })
})
public class EvidenceReadinessSnapshot extends RspAuditedEntity {
    public static final String NON_OFFICIAL_DISCLAIMER =
            "Internal readiness/self-assessment support only. This is not an official CSC PRIME-ESG score, Governance Level, certification, accreditation, or determination.";

    @Column(name = "assessment_case_id", nullable = false, length = 36)
    private String assessmentCaseId;
    @Column(name = "cycle_id", nullable = false, length = 36)
    private String cycleId;
    @Column(name = "framework_version_id", nullable = false, length = 36)
    private String frameworkVersionId;
    @Column(name = "framework_definition_version", nullable = false)
    private int frameworkDefinitionVersion;
    @Column(name = "framework_content_revision", nullable = false)
    private int frameworkContentRevision;
    @Column(name = "framework_fingerprint", nullable = false, length = 64)
    private String frameworkFingerprint;
    @Column(name = "case_content_revision", nullable = false)
    private int caseContentRevision;
    @Column(name = "case_submission_revision", nullable = false)
    private int caseSubmissionRevision;
    @Column(name = "snapshot_revision", nullable = false)
    private int snapshotRevision;
    @Column(name = "supersedes_snapshot_id", length = 36)
    private String supersedesSnapshotId;
    @Column(name = "total_requirements", nullable = false)
    private int totalRequirements;
    @Column(name = "excluded_requirements", nullable = false)
    private int excludedRequirements;
    @Column(name = "applicable_requirements", nullable = false)
    private int applicableRequirements;
    @Column(name = "assessed_requirements", nullable = false)
    private int assessedRequirements;
    @Column(name = "covered_requirements", nullable = false)
    private int coveredRequirements;
    @Column(name = "completeness_percent", nullable = false, precision = 7, scale = 2)
    private BigDecimal completenessPercent;
    @Column(name = "coverage_percent", nullable = false, precision = 7, scale = 2)
    private BigDecimal coveragePercent;
    @Column(name = "missing_input_count", nullable = false)
    private int missingInputCount;
    @Column(name = "expiring_evidence_count", nullable = false)
    private int expiringEvidenceCount;
    @Column(name = "overdue_action_count", nullable = false)
    private int overdueActionCount;
    @Nationalized
    @Column(name = "facts_payload", nullable = false, length = 32000)
    private String factsPayload;
    @Column(name = "facts_fingerprint", nullable = false, length = 64)
    private String factsFingerprint;
    @Column(name = "disclaimer", nullable = false, length = 1000)
    private String disclaimer;
    @Column(name = "signed_by", nullable = false, length = 100)
    private String signedBy;
    @Column(name = "signed_at", nullable = false)
    private Instant signedAt;
    @Column(name = "sign_off_reason", nullable = false, length = 3000)
    private String signOffReason;

    protected EvidenceReadinessSnapshot() { }

    public EvidenceReadinessSnapshot(String agencyId, String assessmentCaseId, String cycleId,
            String frameworkVersionId, int frameworkDefinitionVersion, int frameworkContentRevision,
            String frameworkFingerprint, int caseContentRevision, int caseSubmissionRevision,
            int snapshotRevision, String supersedesSnapshotId, int totalRequirements,
            int excludedRequirements, int applicableRequirements, int assessedRequirements,
            int coveredRequirements, BigDecimal completenessPercent, BigDecimal coveragePercent,
            int missingInputCount, int expiringEvidenceCount, int overdueActionCount,
            String factsPayload, String factsFingerprint, String signedBy, Instant signedAt,
            String signOffReason) {
        super(agencyId);
        this.assessmentCaseId = requiredText(assessmentCaseId, "assessmentCaseId");
        this.cycleId = requiredText(cycleId, "cycleId");
        this.frameworkVersionId = requiredText(frameworkVersionId, "frameworkVersionId");
        if (frameworkDefinitionVersion < 1 || frameworkContentRevision < 1
                || caseContentRevision < 0 || caseSubmissionRevision < 1 || snapshotRevision < 1) {
            throw new IllegalArgumentException("Valid framework, case, and snapshot revisions are required");
        }
        if (totalRequirements < 1 || excludedRequirements < 0 || applicableRequirements < 0
                || assessedRequirements < 0 || coveredRequirements < 0 || missingInputCount < 0
                || expiringEvidenceCount < 0 || overdueActionCount < 0
                || excludedRequirements + applicableRequirements != totalRequirements
                || assessedRequirements > totalRequirements || coveredRequirements > applicableRequirements) {
            throw new IllegalArgumentException("Readiness facts are inconsistent");
        }
        this.frameworkDefinitionVersion = frameworkDefinitionVersion;
        this.frameworkContentRevision = frameworkContentRevision;
        this.frameworkFingerprint = requiredText(frameworkFingerprint, "frameworkFingerprint");
        this.caseContentRevision = caseContentRevision;
        this.caseSubmissionRevision = caseSubmissionRevision;
        this.snapshotRevision = snapshotRevision;
        this.supersedesSnapshotId = optionalText(supersedesSnapshotId);
        this.totalRequirements = totalRequirements;
        this.excludedRequirements = excludedRequirements;
        this.applicableRequirements = applicableRequirements;
        this.assessedRequirements = assessedRequirements;
        this.coveredRequirements = coveredRequirements;
        this.completenessPercent = percentage(completenessPercent, "completenessPercent");
        this.coveragePercent = percentage(coveragePercent, "coveragePercent");
        this.missingInputCount = missingInputCount;
        this.expiringEvidenceCount = expiringEvidenceCount;
        this.overdueActionCount = overdueActionCount;
        this.factsPayload = requiredText(factsPayload, "factsPayload");
        if (this.factsPayload.length() > 32000) throw new IllegalArgumentException("factsPayload is too large");
        this.factsFingerprint = requiredText(factsFingerprint, "factsFingerprint");
        this.disclaimer = NON_OFFICIAL_DISCLAIMER;
        this.signedBy = requiredText(signedBy, "signedBy");
        this.signedAt = java.util.Objects.requireNonNull(signedAt, "signedAt");
        this.signOffReason = requiredText(signOffReason, "signOffReason");
    }

    private static BigDecimal percentage(BigDecimal value, String name) {
        if (value == null || value.signum() < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException(name + " must be from 0 to 100");
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public String getAssessmentCaseId() { return assessmentCaseId; }
    public String getCycleId() { return cycleId; }
    public String getFrameworkVersionId() { return frameworkVersionId; }
    public int getFrameworkDefinitionVersion() { return frameworkDefinitionVersion; }
    public int getFrameworkContentRevision() { return frameworkContentRevision; }
    public String getFrameworkFingerprint() { return frameworkFingerprint; }
    public int getCaseContentRevision() { return caseContentRevision; }
    public int getCaseSubmissionRevision() { return caseSubmissionRevision; }
    public int getSnapshotRevision() { return snapshotRevision; }
    public String getSupersedesSnapshotId() { return supersedesSnapshotId; }
    public int getTotalRequirements() { return totalRequirements; }
    public int getExcludedRequirements() { return excludedRequirements; }
    public int getApplicableRequirements() { return applicableRequirements; }
    public int getAssessedRequirements() { return assessedRequirements; }
    public int getCoveredRequirements() { return coveredRequirements; }
    public BigDecimal getCompletenessPercent() { return completenessPercent; }
    public BigDecimal getCoveragePercent() { return coveragePercent; }
    public int getMissingInputCount() { return missingInputCount; }
    public int getExpiringEvidenceCount() { return expiringEvidenceCount; }
    public int getOverdueActionCount() { return overdueActionCount; }
    public String getFactsPayload() { return factsPayload; }
    public String getFactsFingerprint() { return factsFingerprint; }
    public String getDisclaimer() { return disclaimer; }
    public String getSignedBy() { return signedBy; }
    public Instant getSignedAt() { return signedAt; }
    public String getSignOffReason() { return signOffReason; }
}
