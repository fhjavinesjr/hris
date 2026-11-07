package com.primehr.rewardsrecognition.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.Locale;

@Entity
@Table(name = "rr_award_criterion", uniqueConstraints = {
        @UniqueConstraint(name = "uk_rr_criterion_code", columnNames = {"agency_id", "category_version_id", "normalized_code"}),
        @UniqueConstraint(name = "uk_rr_criterion_order", columnNames = {"agency_id", "category_version_id", "display_order"})
})
public class RrAwardCriterion extends RspAuditedEntity {
    @Column(name = "category_version_id", nullable = false, length = 36)
    private String categoryVersionId;
    @Column(nullable = false, length = 80)
    private String code;
    @Column(name = "normalized_code", nullable = false, length = 80)
    private String normalizedCode;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(length = 2000)
    private String description;
    @Column(name = "weight_percent", nullable = false, precision = 7, scale = 2)
    private BigDecimal weightPercent;
    @Column(name = "maximum_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal maximumScore;
    @Column(name = "evidence_guidance", nullable = false, length = 3000)
    private String evidenceGuidance;
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected RrAwardCriterion() {
    }

    public RrAwardCriterion(String agencyId, String categoryVersionId, String code, String title,
                            String description, BigDecimal weightPercent, BigDecimal maximumScore,
                            String evidenceGuidance, int displayOrder) {
        super(agencyId);
        this.categoryVersionId = requiredText(categoryVersionId, "categoryVersionId");
        this.code = requiredText(code, "code");
        this.normalizedCode = this.code.toUpperCase(Locale.ROOT);
        this.title = requiredText(title, "title");
        this.description = optionalText(description);
        if (weightPercent == null || weightPercent.signum() <= 0) throw new IllegalArgumentException("weightPercent must be positive");
        if (maximumScore == null || maximumScore.signum() <= 0) throw new IllegalArgumentException("maximumScore must be positive");
        if (displayOrder < 1) throw new IllegalArgumentException("displayOrder must be positive");
        this.weightPercent = weightPercent;
        this.maximumScore = maximumScore;
        this.evidenceGuidance = requiredText(evidenceGuidance, "evidenceGuidance");
        this.displayOrder = displayOrder;
    }

    public String getCategoryVersionId() { return categoryVersionId; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getWeightPercent() { return weightPercent; }
    public BigDecimal getMaximumScore() { return maximumScore; }
    public String getEvidenceGuidance() { return evidenceGuidance; }
    public int getDisplayOrder() { return displayOrder; }
}
