package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Locale;

@Entity
@Table(name = "spms_rating_band", uniqueConstraints = {
        @UniqueConstraint(name = "uk_spms_rating_band_code", columnNames = {"agency_id", "rating_scale_version_id", "normalized_code"}),
        @UniqueConstraint(name = "uk_spms_rating_band_order", columnNames = {"agency_id", "rating_scale_version_id", "display_order"})})
public class PerformanceRatingBand extends RspAuditedEntity {
    @Column(name = "rating_scale_version_id", nullable = false, length = 36) private String ratingScaleVersionId;
    @Column(nullable = false, length = 80) private String code;
    @Column(name = "normalized_code", nullable = false, length = 80) private String normalizedCode;
    @Column(name = "numeric_score", nullable = false, precision = 19, scale = 6) private BigDecimal numericScore;
    @Column(nullable = false, length = 200) private String label;
    @Column(name = "lower_bound", nullable = false, precision = 19, scale = 6) private BigDecimal lowerBound;
    @Column(name = "upper_bound", nullable = false, precision = 19, scale = 6) private BigDecimal upperBound;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Column(length = 2000) private String guidance;

    protected PerformanceRatingBand() {}
    public PerformanceRatingBand(String agencyId, String versionId, String code, BigDecimal numericScore,
            String label, BigDecimal lowerBound, BigDecimal upperBound, int displayOrder, String guidance) {
        super(agencyId); this.ratingScaleVersionId = requiredText(versionId, "ratingScaleVersionId");
        this.code = requiredText(code, "code"); this.normalizedCode = this.code.toUpperCase(Locale.ROOT);
        this.numericScore = numericScore; this.label = requiredText(label, "label");
        this.lowerBound = lowerBound; this.upperBound = upperBound;
        if (numericScore == null || lowerBound == null || upperBound == null) throw new IllegalArgumentException("Band scores and bounds are required");
        if (upperBound.compareTo(lowerBound) < 0) throw new IllegalArgumentException("Band upperBound cannot be less than lowerBound");
        if (displayOrder < 1) throw new IllegalArgumentException("displayOrder must be positive");
        this.displayOrder = displayOrder; this.guidance = optionalText(guidance);
    }
    public String getRatingScaleVersionId(){return ratingScaleVersionId;} public String getCode(){return code;}
    public BigDecimal getNumericScore(){return numericScore;} public String getLabel(){return label;}
    public BigDecimal getLowerBound(){return lowerBound;} public BigDecimal getUpperBound(){return upperBound;}
    public int getDisplayOrder(){return displayOrder;} public String getGuidance(){return guidance;}
}
