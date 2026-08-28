package com.primehr.assessment.domain;

import jakarta.persistence.*;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prime_assessor_assignment", uniqueConstraints = @UniqueConstraint(
        name = "uk_prime_assessor_assignment",
        columnNames = {"assessment_case_id", "method_code", "assessor_employee_id"}))
public class AssessorAssignment {
    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false)
    private String agencyId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_case_id", nullable = false, updatable = false)
    private AssessmentCase assessmentCase;
    @Column(name = "method_code", length = 50, nullable = false, updatable = false)
    private String methodCode;
    @Column(name = "assessor_employee_id", nullable = false, updatable = false)
    private Long assessorEmployeeId;
    @Column(name = "assessor_employee_no", length = 100, nullable = false, updatable = false)
    private String assessorEmployeeNo;
    @Nationalized @Column(name = "assessor_display_name", length = 300, nullable = false, updatable = false)
    private String assessorDisplayName;
    @Nationalized @Column(name = "assignment_reason", length = 1000)
    private String assignmentReason;
    @Column(name = "assessor_source_fingerprint", length = 64, nullable = false, updatable = false)
    private String assessorSourceFingerprint;
    @Column(name = "assessor_snapshot_at", nullable = false, updatable = false)
    private Instant assessorSnapshotAt;
    @Column(name = "status", length = 30, nullable = false)
    private String status;
    @Column(name = "submitted_by", length = 100)
    private String submittedBy;
    @Column(name = "submitted_at")
    private Instant submittedAt;
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

    protected AssessorAssignment() { }

    public AssessorAssignment(String agencyId, AssessmentCase assessmentCase, AssessmentMethod method,
                              AssessmentSubjectSnapshot assessor, String assignmentReason) {
        if (agencyId == null || agencyId.isBlank()) throw new IllegalArgumentException("agencyId is required");
        if (!agencyId.equals(assessmentCase.getAgencyId())) {
            throw new IllegalArgumentException("Assessment case agency does not match");
        }
        assessmentCase.requireDraft();
        requireIdentityRule(assessmentCase, method, assessor);
        this.agencyId = agencyId;
        this.assessmentCase = assessmentCase;
        this.methodCode = java.util.Objects.requireNonNull(method, "method").name();
        this.assessorEmployeeId = assessor.employeeId();
        this.assessorEmployeeNo = assessor.employeeNo();
        this.assessorDisplayName = assessor.displayName();
        this.assignmentReason = normalize(assignmentReason);
        this.assessorSourceFingerprint = assessor.sourceFingerprint();
        this.assessorSnapshotAt = assessor.capturedAt();
        this.status = AssessorAssignmentStatus.DRAFT.name();
        this.active = true;
    }

    public void updateDraft(String reason) {
        requireDraft();
        assignmentReason = normalize(reason);
    }

    public void archiveDraft() {
        requireDraft();
        status = AssessorAssignmentStatus.ARCHIVED.name();
        active = false;
    }

    public void assignForOpenCycle() {
        if (getStatus() != AssessorAssignmentStatus.DRAFT || !active) {
            throw new IllegalLifecycleTransitionException("Only active DRAFT assignments may be activated");
        }
        status = AssessorAssignmentStatus.ASSIGNED.name();
    }

    public void startWork(String actor) {
        requireActor(actor);
        assessmentCase.markInProgress();
        if (getStatus() == AssessorAssignmentStatus.ASSIGNED
                || getStatus() == AssessorAssignmentStatus.RETURNED) {
            status = AssessorAssignmentStatus.IN_PROGRESS.name();
            submittedBy = null;
            submittedAt = null;
        } else if (getStatus() != AssessorAssignmentStatus.IN_PROGRESS) {
            throw new IllegalLifecycleTransitionException("Submitted or archived assignments cannot be changed");
        }
    }

    public void submit(String actor, Instant at) {
        requireActor(actor);
        assessmentCase.getTool().requirePublishedForExecution();
        if (getStatus() != AssessorAssignmentStatus.IN_PROGRESS) {
            throw new IllegalLifecycleTransitionException("Only an IN_PROGRESS assignment may be submitted");
        }
        status = AssessorAssignmentStatus.SUBMITTED.name();
        submittedBy = actor.trim();
        submittedAt = java.util.Objects.requireNonNull(at, "submittedAt");
    }

    public void returnForCorrection() {
        if (getStatus() != AssessorAssignmentStatus.SUBMITTED) {
            throw new IllegalLifecycleTransitionException("Only submitted assignments may be returned");
        }
        status = AssessorAssignmentStatus.RETURNED.name();
        submittedBy = null;
        submittedAt = null;
    }

    public void markValidated() {
        if (getStatus() != AssessorAssignmentStatus.SUBMITTED) {
            throw new IllegalLifecycleTransitionException("Only submitted assignments may be validated");
        }
        status = AssessorAssignmentStatus.VALIDATED.name();
    }

    public void requireActor(String actor) {
        if (actor == null || !assessorEmployeeNo.equalsIgnoreCase(actor.trim())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only the explicitly assigned assessor may access this contribution");
        }
    }

    private void requireDraft() {
        assessmentCase.requireDraft();
        if (getStatus() != AssessorAssignmentStatus.DRAFT || !active) {
            throw new IllegalLifecycleTransitionException("Only active DRAFT assessor assignments may be changed");
        }
    }

    private static void requireIdentityRule(AssessmentCase assessmentCase, AssessmentMethod method,
                                            AssessmentSubjectSnapshot assessor) {
        boolean same = assessmentCase.getSubjectEmployeeId().equals(assessor.employeeId());
        if (method == AssessmentMethod.SELF_ASSESSMENT && !same) {
            throw new IllegalArgumentException("SELF_ASSESSMENT must be assigned to the subject");
        }
        if (method != AssessmentMethod.SELF_ASSESSMENT && same) {
            throw new IllegalArgumentException("Only SELF_ASSESSMENT may use the subject as assessor");
        }
    }

    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID().toString(); }
    public String getId() { return id; }
    public String getAgencyId() { return agencyId; }
    public AssessmentCase getAssessmentCase() { return assessmentCase; }
    public AssessmentMethod getMethod() { return AssessmentMethod.valueOf(methodCode); }
    public Long getAssessorEmployeeId() { return assessorEmployeeId; }
    public String getAssessorEmployeeNo() { return assessorEmployeeNo; }
    public String getAssessorDisplayName() { return assessorDisplayName; }
    public String getAssignmentReason() { return assignmentReason; }
    public String getAssessorSourceFingerprint() { return assessorSourceFingerprint; }
    public Instant getAssessorSnapshotAt() { return assessorSnapshotAt; }
    public AssessorAssignmentStatus getStatus() { return AssessorAssignmentStatus.valueOf(status); }
    public boolean isActive() { return active; }
    public long getVersion() { return version; }
    public String getSubmittedBy() { return submittedBy; }
    public Instant getSubmittedAt() { return submittedAt; }
}
