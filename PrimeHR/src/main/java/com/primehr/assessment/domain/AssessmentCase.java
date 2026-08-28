package com.primehr.assessment.domain;

import com.primehr.positionprofile.domain.PositionTargetType;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prime_assessment_case", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_assessment_case_subject", columnNames = {"tool_id", "subject_employee_id"}))
public class AssessmentCase {
    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false)
    private String agencyId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tool_id", nullable = false, updatable = false)
    private AssessmentTool tool;
    @Column(name = "subject_employee_id", nullable = false, updatable = false)
    private Long subjectEmployeeId;
    @Column(name = "subject_employee_no", length = 100, nullable = false, updatable = false)
    private String subjectEmployeeNo;
    @Nationalized @Column(name = "subject_display_name", length = 300, nullable = false, updatable = false)
    private String subjectDisplayName;
    @Column(name = "appointment_id", nullable = false, updatable = false)
    private Long appointmentId;
    @Column(name = "assumption_to_duty_date", nullable = false, updatable = false)
    private LocalDateTime assumptionToDutyDate;
    @Column(name = "job_position_id", nullable = false, updatable = false)
    private Long jobPositionId;
    @Column(name = "plantilla_id", updatable = false)
    private Long plantillaId;
    @Column(name = "subject_source_fingerprint", length = 64, nullable = false, updatable = false)
    private String subjectSourceFingerprint;
    @Column(name = "subject_source_updated_at", updatable = false)
    private LocalDateTime subjectSourceUpdatedAt;
    @Column(name = "subject_snapshot_at", nullable = false, updatable = false)
    private Instant subjectSnapshotAt;
    @Column(name = "status", length = 30, nullable = false)
    private String status;
    @Column(name = "for_validation_at")
    private Instant forValidationAt;
    @Column(name = "active", nullable = false)
    private boolean active;
    @Version @Column(name = "record_version", nullable = false)
    private long version;
    @CreatedBy @Column(name = "created_by", length = 100, nullable = false, updatable = false)
    private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @LastModifiedBy @Column(name = "updated_by", length = 100, nullable = false)
    private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AssessmentCase() { }

    public AssessmentCase(String agencyId, AssessmentTool tool, AssessmentSubjectSnapshot subject) {
        if (agencyId == null || agencyId.isBlank()) throw new IllegalArgumentException("agencyId is required");
        if (!agencyId.equals(tool.getAgencyId())) throw new IllegalArgumentException("Tool agency does not match");
        tool.requireDraft();
        requireTargetMatch(tool, subject);
        this.agencyId = agencyId;
        this.tool = tool;
        this.subjectEmployeeId = subject.employeeId();
        this.subjectEmployeeNo = subject.employeeNo();
        this.subjectDisplayName = subject.displayName();
        this.appointmentId = subject.appointmentId();
        this.assumptionToDutyDate = subject.assumptionToDutyDate();
        this.jobPositionId = subject.jobPositionId();
        this.plantillaId = subject.plantillaId();
        this.subjectSourceFingerprint = subject.sourceFingerprint();
        this.subjectSourceUpdatedAt = subject.sourceUpdatedAt();
        this.subjectSnapshotAt = subject.capturedAt();
        this.status = AssessmentCaseStatus.DRAFT.name();
        this.active = true;
    }

    public void archiveDraft() {
        requireDraft();
        status = AssessmentCaseStatus.ARCHIVED.name();
        active = false;
    }

    public void assignForOpenCycle() {
        if (getStatus() != AssessmentCaseStatus.DRAFT) {
            throw new IllegalLifecycleTransitionException("Only DRAFT assessment cases may be assigned");
        }
        status = AssessmentCaseStatus.ASSIGNED.name();
    }

    public void markInProgress() {
        tool.requirePublishedForExecution();
        if (getStatus() == AssessmentCaseStatus.ASSIGNED || getStatus() == AssessmentCaseStatus.RETURNED) {
            status = AssessmentCaseStatus.IN_PROGRESS.name();
        } else if (getStatus() != AssessmentCaseStatus.IN_PROGRESS) {
            throw new IllegalLifecycleTransitionException("Assessment case cannot be changed in its current status");
        }
    }

    public void markForValidation(Instant at) {
        tool.requirePublishedForExecution();
        if (getStatus() != AssessmentCaseStatus.IN_PROGRESS) {
            throw new IllegalLifecycleTransitionException("Only an IN_PROGRESS case may be submitted for validation");
        }
        status = AssessmentCaseStatus.FOR_VALIDATION.name();
        forValidationAt = java.util.Objects.requireNonNull(at, "forValidationAt");
    }

    public void returnForCorrection() {
        if (getStatus() != AssessmentCaseStatus.FOR_VALIDATION) {
            throw new IllegalLifecycleTransitionException("Only a FOR_VALIDATION case may be returned");
        }
        status = AssessmentCaseStatus.RETURNED.name();
        forValidationAt = null;
    }

    public void validate() {
        if (getStatus() != AssessmentCaseStatus.FOR_VALIDATION) {
            throw new IllegalLifecycleTransitionException("Only a FOR_VALIDATION case may be validated");
        }
        status = AssessmentCaseStatus.VALIDATED.name();
    }

    public void requireDraft() {
        tool.requireDraft();
        if (getStatus() != AssessmentCaseStatus.DRAFT) throw new IllegalLifecycleTransitionException(
                "Only DRAFT assessment cases may be changed in Phase 3.1");
    }

    private static void requireTargetMatch(AssessmentTool tool, AssessmentSubjectSnapshot subject) {
        var profile = tool.getPositionProfile();
        if (!profile.getJobPositionId().equals(subject.jobPositionId())) {
            throw new IllegalArgumentException("The employee's active Job Position does not match the assessment tool");
        }
        if (profile.getTargetType() == PositionTargetType.PLANTILLA
                && !java.util.Objects.equals(profile.getPlantillaId(), subject.plantillaId())) {
            throw new IllegalArgumentException("The employee's active Plantilla does not match the assessment tool");
        }
    }

    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID().toString(); }
    public String getId() { return id; }
    public String getAgencyId() { return agencyId; }
    public AssessmentTool getTool() { return tool; }
    public Long getSubjectEmployeeId() { return subjectEmployeeId; }
    public String getSubjectEmployeeNo() { return subjectEmployeeNo; }
    public String getSubjectDisplayName() { return subjectDisplayName; }
    public Long getAppointmentId() { return appointmentId; }
    public LocalDateTime getAssumptionToDutyDate() { return assumptionToDutyDate; }
    public Long getJobPositionId() { return jobPositionId; }
    public Long getPlantillaId() { return plantillaId; }
    public String getSubjectSourceFingerprint() { return subjectSourceFingerprint; }
    public LocalDateTime getSubjectSourceUpdatedAt() { return subjectSourceUpdatedAt; }
    public Instant getSubjectSnapshotAt() { return subjectSnapshotAt; }
    public AssessmentCaseStatus getStatus() { return AssessmentCaseStatus.valueOf(status); }
    public boolean isActive() { return active; }
    public long getVersion() { return version; }
    public Instant getForValidationAt() { return forValidationAt; }
}
