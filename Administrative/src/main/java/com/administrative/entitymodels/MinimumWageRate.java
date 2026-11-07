package com.administrative.entitymodels;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "minimum_wage_rate",
        uniqueConstraints = @UniqueConstraint(name = "uq_minimum_wage_rate_region_from",
                columnNames = {"regionCode", "effectiveFrom"}),
        indexes = @Index(name = "idx_minimum_wage_rate_effective",
                columnList = "regionCode,effectiveFrom,effectiveTo"))
public class MinimumWageRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30) private String regionCode;
    @Column(nullable = false, length = 150) private String regionName;
    @Column(nullable = false) private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal dailyRate;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal monthlyRate;
    @Column(nullable = false, length = 200) private String legalBasis;
    @Column(length = 500) private String remarks;

    public Long getId() { return id; } public void setId(Long value) { id = value; }
    public String getRegionCode() { return regionCode; } public void setRegionCode(String value) { regionCode = value; }
    public String getRegionName() { return regionName; } public void setRegionName(String value) { regionName = value; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; } public void setEffectiveFrom(LocalDate value) { effectiveFrom = value; }
    public LocalDate getEffectiveTo() { return effectiveTo; } public void setEffectiveTo(LocalDate value) { effectiveTo = value; }
    public BigDecimal getDailyRate() { return dailyRate; } public void setDailyRate(BigDecimal value) { dailyRate = value; }
    public BigDecimal getMonthlyRate() { return monthlyRate; } public void setMonthlyRate(BigDecimal value) { monthlyRate = value; }
    public String getLegalBasis() { return legalBasis; } public void setLegalBasis(String value) { legalBasis = value; }
    public String getRemarks() { return remarks; } public void setRemarks(String value) { remarks = value; }
}
