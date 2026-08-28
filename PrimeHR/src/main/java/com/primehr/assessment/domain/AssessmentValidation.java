package com.primehr.assessment.domain;

import jakarta.persistence.*;
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
@Table(name = "prime_assessment_validation", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_assessment_validation_case", columnNames = "assessment_case_id"))
public class AssessmentValidation {
    @Id @Column(length = 36, nullable = false, updatable = false) private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false) private String agencyId;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "assessment_case_id", nullable = false, updatable = false)
    private AssessmentCase assessmentCase;
    @Column(name = "validator_employee_no", length = 100, nullable = false, updatable = false) private String validatorEmployeeNo;
    @Column(name = "administrator_override", nullable = false, updatable = false) private boolean administratorOverride;
    @Nationalized @Column(name = "validation_remarks", length = 4000, updatable = false) private String validationRemarks;
    @Nationalized @Column(name = "override_reason", length = 1000, updatable = false) private String overrideReason;
    @Column(name = "validated_at", nullable = false, updatable = false) private Instant validatedAt;
    @Column(nullable = false, updatable = false) private boolean active;
    @Version @Column(name = "record_version", nullable = false) private long version;
    @CreatedBy @Column(name = "created_by", length = 100, nullable = false, updatable = false) private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @LastModifiedBy @Column(name = "updated_by", length = 100, nullable = false) private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected AssessmentValidation() { }
    public AssessmentValidation(String agencyId, AssessmentCase assessmentCase, String validatorEmployeeNo,
                                boolean administratorOverride, String validationRemarks,
                                String overrideReason, Instant validatedAt) {
        if (!agencyId.equals(assessmentCase.getAgencyId())) throw new IllegalArgumentException("Case agency does not match");
        if (assessmentCase.getStatus() != AssessmentCaseStatus.FOR_VALIDATION) throw new IllegalArgumentException("Case is not ready for validation");
        if (validatorEmployeeNo == null || validatorEmployeeNo.isBlank()) throw new IllegalArgumentException("validator is required");
        if (administratorOverride && (overrideReason == null || overrideReason.isBlank())) throw new IllegalArgumentException("Administrator override reason is required");
        this.agencyId = agencyId; this.assessmentCase = assessmentCase;
        this.validatorEmployeeNo = validatorEmployeeNo.trim(); this.administratorOverride = administratorOverride;
        this.validationRemarks = normalize(validationRemarks); this.overrideReason = normalize(overrideReason);
        this.validatedAt = java.util.Objects.requireNonNull(validatedAt); this.active = true;
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID().toString(); }
    public String getId() { return id; } public String getAgencyId() { return agencyId; }
    public AssessmentCase getAssessmentCase() { return assessmentCase; }
    public String getValidatorEmployeeNo() { return validatorEmployeeNo; }
    public boolean isAdministratorOverride() { return administratorOverride; }
    public String getValidationRemarks() { return validationRemarks; } public String getOverrideReason() { return overrideReason; }
    public Instant getValidatedAt() { return validatedAt; } public long getVersion() { return version; }
}
