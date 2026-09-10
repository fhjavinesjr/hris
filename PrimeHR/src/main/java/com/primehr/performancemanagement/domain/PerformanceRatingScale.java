package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Locale;

@Entity
@Table(name = "spms_rating_scale", uniqueConstraints = @UniqueConstraint(
        name = "uk_spms_rating_scale_code", columnNames = {"agency_id", "normalized_code"}))
public class PerformanceRatingScale extends RspAuditedEntity {
    @Column(nullable = false, length = 80)
    private String code;
    @Column(name = "normalized_code", nullable = false, length = 80)
    private String normalizedCode;

    protected PerformanceRatingScale() {}

    public PerformanceRatingScale(String agencyId, String code) {
        super(agencyId);
        this.code = requiredText(code, "code");
        this.normalizedCode = this.code.toUpperCase(Locale.ROOT);
    }

    public String getCode() { return code; }
    public String getNormalizedCode() { return normalizedCode; }
}
