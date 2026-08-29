package com.primehr.learning.referral.domain;

import com.primehr.gap.domain.*;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.UUID;

@Entity @EntityListeners(AuditingEntityListener.class)
@Table(name = "prime_ld_referral_item", uniqueConstraints = @UniqueConstraint(name = "uk_prime_ld_referral_item", columnNames = {"referral_id", "gap_item_id"}))
public class LdReferralItem {
    @Id @Column(length = 36, nullable = false, updatable = false) private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false) private String agencyId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "referral_id", nullable = false, updatable = false) private LdReferral referral;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "analysis_id", nullable = false, updatable = false) private CompetencyGapAnalysis analysis;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "gap_item_id", nullable = false, updatable = false) private CompetencyGapItem gapItem;
    @Column(name = "active", nullable = false) private boolean active;
    @Column(name = "competency_code", length = 50, nullable = false, updatable = false) private String competencyCode;
    @Nationalized @Column(name = "competency_name", length = 200, nullable = false, updatable = false) private String competencyName;
    @Column(name = "gap_classification", length = 30, nullable = false, updatable = false) private String gapClassification;
    @Column(name = "not_assessed_reason", length = 40, updatable = false) private String notAssessedReason;
    @Column(name = "gap_value", updatable = false) private Integer gapValue;
    @Column(name = "priority_code", length = 50, updatable = false) private String priorityCode;
    @Nationalized @Column(name = "priority_label", length = 150, updatable = false) private String priorityLabel;
    @Column(name = "priority_rank", updatable = false) private Integer priorityRank;
    @Column(name = "display_order", nullable = false, updatable = false) private int displayOrder;
    @Version @Column(name = "record_version", nullable = false) private long version;
    @CreatedBy @Column(name = "created_by", length = 100, nullable = false, updatable = false) private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @LastModifiedBy @Column(name = "updated_by", length = 100, nullable = false) private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    protected LdReferralItem() { }
    public LdReferralItem(String agencyId, LdReferral referral, CompetencyGapItem gapItem) {
        this.agencyId = requireText(agencyId); this.referral = java.util.Objects.requireNonNull(referral, "referral");
        this.gapItem = java.util.Objects.requireNonNull(gapItem, "gapItem"); this.analysis = gapItem.getAnalysis();
        if (!agencyId.equals(referral.getAgencyId()) || !referral.getAnalysis().getId().equals(analysis.getId()))
            throw new IllegalArgumentException("Referral item must belong to the referral analysis and agency");
        if (gapItem.getGapClassification() != GapClassification.BELOW && gapItem.getGapClassification() != GapClassification.NOT_ASSESSED)
            throw new IllegalArgumentException("Only BELOW or NOT_ASSESSED gap items may be referred");
        active = true; competencyCode = gapItem.getCompetencyCode(); competencyName = gapItem.getCompetencyName();
        gapClassification = gapItem.getGapClassification().name();
        notAssessedReason = gapItem.getNotAssessedReason() == null ? null : gapItem.getNotAssessedReason().name();
        gapValue = gapItem.getGapValue(); priorityCode = gapItem.getPriorityCode(); priorityLabel = gapItem.getPriorityLabel();
        priorityRank = gapItem.getPriorityRank(); displayOrder = gapItem.getDisplayOrder();
    }
    public void archive() { referral.requireDraft(); active = false; }
    public void restore() { referral.requireDraft(); active = true; }
    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID().toString(); }
    private static String requireText(String value) { if (value == null || value.isBlank()) throw new IllegalArgumentException("agencyId is required"); return value.trim(); }
    public String getId() { return id; } public LdReferral getReferral() { return referral; }
    public CompetencyGapAnalysis getAnalysis() { return analysis; } public CompetencyGapItem getGapItem() { return gapItem; }
    public boolean isActive() { return active; } public String getCompetencyCode() { return competencyCode; }
    public String getCompetencyName() { return competencyName; } public GapClassification getGapClassification() { return GapClassification.valueOf(gapClassification); }
    public NotAssessedReason getNotAssessedReason() { return notAssessedReason == null ? null : NotAssessedReason.valueOf(notAssessedReason); }
    public Integer getGapValue() { return gapValue; } public String getPriorityCode() { return priorityCode; }
    public String getPriorityLabel() { return priorityLabel; } public Integer getPriorityRank() { return priorityRank; }
    public int getDisplayOrder() { return displayOrder; } public long getVersion() { return version; }
}
