package com.primehr.learning.referral.domain;

import com.primehr.gap.domain.CompetencyGapAnalysis;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.*;
import java.util.UUID;

@Entity @EntityListeners(AuditingEntityListener.class) @Table(name = "prime_ld_referral")
public class LdReferral {
    @Id @Column(length = 36, nullable = false, updatable = false) private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false) private String agencyId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "analysis_id", nullable = false, updatable = false) private CompetencyGapAnalysis analysis;
    @Column(name = "subject_employee_id", nullable = false, updatable = false) private Long subjectEmployeeId;
    @Column(name = "subject_employee_no", length = 100, nullable = false, updatable = false) private String subjectEmployeeNo;
    @Nationalized @Column(name = "subject_display_name", length = 300, nullable = false, updatable = false) private String subjectDisplayName;
    @Column(name = "analysis_date", nullable = false, updatable = false) private LocalDate analysisDate;
    @Nationalized @Column(name = "position_name", length = 200, nullable = false, updatable = false) private String positionName;
    @Column(name = "status", length = 30, nullable = false) private String status;
    @Nationalized @Column(name = "development_need", length = 4000, nullable = false) private String developmentNeed;
    @Nationalized @Column(name = "recommended_intervention", length = 4000, nullable = false) private String recommendedIntervention;
    @Column(name = "target_completion_date") private LocalDate targetCompletionDate;
    @Nationalized @Column(name = "referral_reason", length = 1000) private String referralReason;
    @Nationalized @Column(name = "remarks", length = 2000) private String remarks;
    @Column(name = "referred_by", length = 100) private String referredBy;
    @Column(name = "referred_at") private Instant referredAt;
    @Version @Column(name = "record_version", nullable = false) private long version;
    @CreatedBy @Column(name = "created_by", length = 100, nullable = false, updatable = false) private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @LastModifiedBy @Column(name = "updated_by", length = 100, nullable = false) private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected LdReferral() { }
    public LdReferral(String agencyId, CompetencyGapAnalysis analysis, String need, String intervention,
                      LocalDate targetDate, String reason, String remarks) {
        this.agencyId = requireText(agencyId, "agencyId");
        this.analysis = java.util.Objects.requireNonNull(analysis, "analysis");
        if (!agencyId.equals(analysis.getAgencyId())) throw new IllegalArgumentException("Referral and analysis agency must match");
        subjectEmployeeId = analysis.getSubjectEmployeeId(); subjectEmployeeNo = analysis.getSubjectEmployeeNo();
        subjectDisplayName = analysis.getSubjectDisplayName(); analysisDate = analysis.getAnalysisDate();
        positionName = analysis.getSourcePlantillaName() == null ? analysis.getSourceJobPositionName() : analysis.getSourcePlantillaName();
        status = LdReferralStatus.DRAFT.name();
        updateFields(need, intervention, targetDate, reason, remarks);
    }
    public void update(String need, String intervention, LocalDate targetDate, String reason, String remarks) {
        requireDraft(); updateFields(need, intervention, targetDate, reason, remarks);
    }
    public void submit(String actor, Instant at) {
        requireDraft(); status = LdReferralStatus.REFERRED.name();
        referredBy = requireText(actor, "actor"); referredAt = java.util.Objects.requireNonNull(at, "referredAt");
    }
    public void archiveDraft() { requireDraft(); status = LdReferralStatus.ARCHIVED.name(); }
    public void requireDraft() {
        if (getStatus() != LdReferralStatus.DRAFT) throw new IllegalLifecycleTransitionException("Only DRAFT L&D referrals may be changed");
    }
    private void updateFields(String need, String intervention, LocalDate targetDate, String reason, String notes) {
        developmentNeed = requireText(need, "developmentNeed"); recommendedIntervention = requireText(intervention, "recommendedIntervention");
        targetCompletionDate = targetDate; referralReason = normalize(reason); remarks = normalize(notes);
    }
    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID().toString(); }
    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required"); return value.trim();
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    public String getId() { return id; } public String getAgencyId() { return agencyId; }
    public CompetencyGapAnalysis getAnalysis() { return analysis; } public Long getSubjectEmployeeId() { return subjectEmployeeId; }
    public String getSubjectEmployeeNo() { return subjectEmployeeNo; } public String getSubjectDisplayName() { return subjectDisplayName; }
    public LocalDate getAnalysisDate() { return analysisDate; } public String getPositionName() { return positionName; }
    public LdReferralStatus getStatus() { return LdReferralStatus.valueOf(status); } public String getDevelopmentNeed() { return developmentNeed; }
    public String getRecommendedIntervention() { return recommendedIntervention; } public LocalDate getTargetCompletionDate() { return targetCompletionDate; }
    public String getReferralReason() { return referralReason; } public String getRemarks() { return remarks; }
    public String getReferredBy() { return referredBy; } public Instant getReferredAt() { return referredAt; }
    public long getVersion() { return version; } public String getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; } public String getUpdatedBy() { return updatedBy; } public Instant getUpdatedAt() { return updatedAt; }
}
