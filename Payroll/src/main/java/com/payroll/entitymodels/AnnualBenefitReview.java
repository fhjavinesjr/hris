package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "annual_benefit_review", uniqueConstraints =
        @UniqueConstraint(name = "uq_annual_benefit_review_source", columnNames = {"sourceType", "sourceId"}),
        indexes = @Index(name = "idx_annual_benefit_review_employee", columnList = "employeeNo,taxYear"))
public class AnnualBenefitReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 30) private String sourceType;
    @Column(nullable = false) private Long sourceId;
    @Column(nullable = false, length = 50) private String employeeNo;
    @Column(nullable = false) private Integer taxYear;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal sourceAmount;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal exemptAmount;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal otherExemptAmount;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal taxableAmount;
    @Column(length = 4) private String exemptBirItem;
    @Column(length = 4) private String otherExemptBirItem;
    @Column(length = 4) private String taxableBirItem;
    @Column(nullable = false, length = 50) private String profileCode;
    @Column(nullable = false, length = 200) private String profileLegalBasis;
    @Column(precision = 19, scale = 2) private BigDecimal profileGuidanceCeiling;
    @Column(length = 30) private String profileCeilingPeriod;
    @Column(nullable = false, length = 500) private String reason;
    @Column(nullable = false, length = 100) private String reviewedBy;
    @Column(nullable = false) private LocalDateTime reviewedAt;

    public Long getId() { return id; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String v) { sourceType = v; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long v) { sourceId = v; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String v) { employeeNo = v; }
    public Integer getTaxYear() { return taxYear; }
    public void setTaxYear(Integer v) { taxYear = v; }
    public BigDecimal getSourceAmount() { return sourceAmount; }
    public void setSourceAmount(BigDecimal v) { sourceAmount = v; }
    public BigDecimal getExemptAmount() { return exemptAmount; }
    public void setExemptAmount(BigDecimal v) { exemptAmount = v; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal v) { taxableAmount = v; }
    public BigDecimal getOtherExemptAmount() { return otherExemptAmount; }
    public void setOtherExemptAmount(BigDecimal v) { otherExemptAmount = v; }
    public String getExemptBirItem() { return exemptBirItem; }
    public void setExemptBirItem(String v) { exemptBirItem = v; }
    public String getOtherExemptBirItem() { return otherExemptBirItem; }
    public void setOtherExemptBirItem(String v) { otherExemptBirItem = v; }
    public String getTaxableBirItem() { return taxableBirItem; }
    public void setTaxableBirItem(String v) { taxableBirItem = v; }
    public String getProfileCode() { return profileCode; }
    public void setProfileCode(String v) { profileCode = v; }
    public String getProfileLegalBasis() { return profileLegalBasis; }
    public void setProfileLegalBasis(String v) { profileLegalBasis = v; }
    public BigDecimal getProfileGuidanceCeiling() { return profileGuidanceCeiling; }
    public void setProfileGuidanceCeiling(BigDecimal v) { profileGuidanceCeiling = v; }
    public String getProfileCeilingPeriod() { return profileCeilingPeriod; }
    public void setProfileCeilingPeriod(String v) { profileCeilingPeriod = v; }
    public String getReason() { return reason; }
    public void setReason(String v) { reason = v; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String v) { reviewedBy = v; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime v) { reviewedAt = v; }
}
