package com.primehr.positionprofile.domain;

import com.primehr.shared.audit.AgencyAuditableEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "prime_position_profile", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_profile_target_version",
                columnNames = {"agency_id", "target_key", "definition_version"}))
public class PositionProfile extends AgencyAuditableEntity {
    @Column(name = "target_type", length = 30, nullable = false, updatable = false)
    private String targetType;

    @Column(name = "target_key", length = 100, nullable = false, updatable = false)
    private String targetKey;

    @Column(name = "job_position_id", nullable = false, updatable = false)
    private Long jobPositionId;

    @Column(name = "plantilla_id", updatable = false)
    private Long plantillaId;

    @Column(name = "name", length = 200, nullable = false)
    @Nationalized
    private String name;

    @Column(name = "description", length = 2000)
    @Nationalized
    private String description;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "definition_version", nullable = false)
    private int definitionVersion;

    @Column(name = "supersedes_id", length = 36)
    private String supersedesId;

    @Column(name = "source_job_position_name", length = 200, nullable = false)
    @Nationalized
    private String sourceJobPositionName;

    @Column(name = "source_salary_grade")
    private Long sourceSalaryGrade;

    @Column(name = "source_salary_step")
    private Long sourceSalaryStep;

    @Column(name = "source_plantilla_name", length = 200)
    @Nationalized
    private String sourcePlantillaName;

    @Column(name = "source_fingerprint", length = 64, nullable = false)
    private String sourceFingerprint;

    @Column(name = "source_snapshot_at", nullable = false)
    private java.time.Instant sourceSnapshotAt;

    @Column(name = "content_revision", nullable = false)
    private long contentRevision;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    protected PositionProfile() {
    }

    private PositionProfile(String agencyId, PositionTargetSnapshot target, String name, String description,
                            int definitionVersion, String supersedesId,
                            LocalDate effectiveFrom, LocalDate effectiveTo) {
        super(agencyId, false, 0, effectiveFrom, effectiveTo);
        applyIdentity(target);
        applyCurrentSource(target);
        this.name = requireText(name, "name");
        this.description = normalize(description);
        this.status = PositionProfileStatus.DRAFT.name();
        this.definitionVersion = definitionVersion;
        this.supersedesId = supersedesId;
    }

    public static PositionProfile draft(String agencyId, PositionTargetSnapshot target, String name,
                                        String description, LocalDate effectiveFrom, LocalDate effectiveTo) {
        return new PositionProfile(agencyId, target, name, description, 1, null, effectiveFrom, effectiveTo);
    }

    public PositionProfile successorDraft(PositionTargetSnapshot currentTarget) {
        PositionProfileStatus current = getStatus();
        if (current != PositionProfileStatus.ACTIVE) {
            throw new IllegalLifecycleTransitionException(
                    "Only ACTIVE position profiles may start a successor draft");
        }
        if (!targetKey.equals(currentTarget.targetKey())) {
            throw new IllegalArgumentException("The successor target cannot change");
        }
        return new PositionProfile(getAgencyId(), currentTarget, name, description,
                definitionVersion + 1, getId(), getEffectiveFrom(), getEffectiveTo());
    }

    public void updateDraft(String name, String description, LocalDate effectiveFrom, LocalDate effectiveTo,
                            PositionTargetSnapshot currentTarget) {
        requireStatus(PositionProfileStatus.DRAFT);
        if (!targetKey.equals(currentTarget.targetKey())) {
            throw new IllegalArgumentException("The profile target cannot change");
        }
        this.name = requireText(name, "name");
        this.description = normalize(description);
        updateDefinitionFields(0, effectiveFrom, effectiveTo);
        applyCurrentSource(currentTarget);
        contentRevision++;
    }

    public void markRequirementsChanged() {
        requireStatus(PositionProfileStatus.DRAFT);
        contentRevision++;
    }

    public void archiveDraft() {
        requireStatus(PositionProfileStatus.DRAFT);
        status = PositionProfileStatus.ARCHIVED.name();
        setDefinitionActive(false);
        contentRevision++;
    }

    public void submit(String actor, Instant at, PositionTargetSnapshot currentTarget) {
        requireStatus(PositionProfileStatus.DRAFT);
        requireSameTarget(currentTarget);
        if (getEffectiveFrom() == null) {
            throw new IllegalArgumentException("effectiveFrom is required before submission");
        }
        applyCurrentSource(currentTarget);
        submittedBy = requireText(actor, "submitter");
        submittedAt = Objects.requireNonNull(at, "submittedAt");
        approvedBy = null;
        approvedAt = null;
        status = PositionProfileStatus.SUBMITTED.name();
        contentRevision++;
    }

    public void returnToDraft() {
        requireStatus(PositionProfileStatus.SUBMITTED);
        status = PositionProfileStatus.DRAFT.name();
        submittedBy = null;
        submittedAt = null;
        approvedBy = null;
        approvedAt = null;
        contentRevision++;
    }

    public void approve(String actor, Instant at, PositionTargetSnapshot currentTarget) {
        requireStatus(PositionProfileStatus.SUBMITTED);
        requireSameTarget(currentTarget);
        applyCurrentSource(currentTarget);
        approvedBy = requireText(actor, "approver");
        approvedAt = Objects.requireNonNull(at, "approvedAt");
        status = PositionProfileStatus.ACTIVE.name();
        setDefinitionActive(true);
        contentRevision++;
    }

    public void closeEffectivePeriodBefore(LocalDate successorFrom) {
        requireStatus(PositionProfileStatus.ACTIVE);
        LocalDate newEnd = Objects.requireNonNull(successorFrom, "successorFrom").minusDays(1);
        if (getEffectiveTo() == null || !getEffectiveTo().isBefore(successorFrom)) {
            updateDefinitionFields(getDisplayOrder(), getEffectiveFrom(), newEnd);
        }
    }

    public boolean isDraft() {
        return PositionProfileStatus.DRAFT.name().equals(status);
    }

    private void applyIdentity(PositionTargetSnapshot target) {
        this.targetType = target.type().name();
        this.targetKey = target.targetKey();
        this.jobPositionId = target.jobPositionId();
        this.plantillaId = target.plantillaId();
    }

    private void applyCurrentSource(PositionTargetSnapshot target) {
        this.sourceJobPositionName = target.jobPositionName();
        this.sourceSalaryGrade = target.salaryGrade();
        this.sourceSalaryStep = target.salaryStep();
        this.sourcePlantillaName = target.plantillaName();
        this.sourceFingerprint = target.fingerprint();
        this.sourceSnapshotAt = target.capturedAt();
    }

    private void requireSameTarget(PositionTargetSnapshot target) {
        if (!targetKey.equals(target.targetKey())) {
            throw new IllegalArgumentException("The profile target cannot change");
        }
    }

    private void requireStatus(PositionProfileStatus expected) {
        if (!expected.name().equals(status)) {
            throw new IllegalLifecycleTransitionException("Only " + expected + " position profiles may be changed");
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public PositionTargetType getTargetType() { return PositionTargetType.valueOf(targetType); }
    public String getTargetKey() { return targetKey; }
    public Long getJobPositionId() { return jobPositionId; }
    public Long getPlantillaId() { return plantillaId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public PositionProfileStatus getStatus() { return PositionProfileStatus.valueOf(status); }
    public int getDefinitionVersion() { return definitionVersion; }
    public String getSupersedesId() { return supersedesId; }
    public String getSourceJobPositionName() { return sourceJobPositionName; }
    public Long getSourceSalaryGrade() { return sourceSalaryGrade; }
    public Long getSourceSalaryStep() { return sourceSalaryStep; }
    public String getSourcePlantillaName() { return sourcePlantillaName; }
    public String getSourceFingerprint() { return sourceFingerprint; }
    public java.time.Instant getSourceSnapshotAt() { return sourceSnapshotAt; }
    public long getContentRevision() { return contentRevision; }
    public String getSubmittedBy() { return submittedBy; }
    public Instant getSubmittedAt() { return submittedAt; }
    public String getApprovedBy() { return approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
}
