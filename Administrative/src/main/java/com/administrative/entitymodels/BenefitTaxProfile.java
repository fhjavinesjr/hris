package com.administrative.entitymodels;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "benefit_tax_profile", uniqueConstraints =
        @UniqueConstraint(name = "uq_benefit_tax_profile_year", columnNames = {"profileCode", "taxYear"}))
public class BenefitTaxProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50) private String profileCode;
    @Column(nullable = false) private Integer taxYear;
    @Column(nullable = false, length = 120) private String label;
    @Column(precision = 19, scale = 2) private BigDecimal guidanceCeiling;
    @Column(length = 30) private String ceilingPeriod;
    @Column(length = 50) private String sharedPoolCode;
    @Column(length = 4) private String exemptBirItem;
    @Column(length = 4) private String taxableBirItem;
    @Column(nullable = false, length = 200) private String legalBasis;
    @Column(length = 500) private String sourceUrl;

    public Long getId() { return id; }
    public String getProfileCode() { return profileCode; }
    public void setProfileCode(String v) { profileCode = v; }
    public Integer getTaxYear() { return taxYear; }
    public void setTaxYear(Integer v) { taxYear = v; }
    public String getLabel() { return label; }
    public void setLabel(String v) { label = v; }
    public BigDecimal getGuidanceCeiling() { return guidanceCeiling; }
    public void setGuidanceCeiling(BigDecimal v) { guidanceCeiling = v; }
    public String getCeilingPeriod() { return ceilingPeriod; }
    public void setCeilingPeriod(String v) { ceilingPeriod = v; }
    public String getSharedPoolCode() { return sharedPoolCode; }
    public void setSharedPoolCode(String v) { sharedPoolCode = v; }
    public String getExemptBirItem() { return exemptBirItem; }
    public void setExemptBirItem(String v) { exemptBirItem = v; }
    public String getTaxableBirItem() { return taxableBirItem; }
    public void setTaxableBirItem(String v) { taxableBirItem = v; }
    public String getLegalBasis() { return legalBasis; }
    public void setLegalBasis(String v) { legalBasis = v; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String v) { sourceUrl = v; }
}
