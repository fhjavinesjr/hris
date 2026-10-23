package com.payroll.entitymodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bir_1604c_officer_review",
        uniqueConstraints = @UniqueConstraint(name = "uk_1604c_review_year", columnNames = "taxYear"))
public class Bir1604COfficerReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Integer taxYear;
    @Column(length = 30) private String contactNumber;
    @Column(length = 100) private String emailAddress;
    private Integer sheetsAttached;
    @Column(length = 3) private String refundsReleased;
    private LocalDate refundDate;
    @Column(precision = 19, scale = 2) private BigDecimal overremittanceAmount;
    private Integer firstCreditingMonth;
    @Column(length = 255) private String evidenceReference;
    @Column(length = 100) private String signatoryName;
    @Column(length = 100) private String signatoryTitle;
    @Column(length = 30) private String signatoryTin;
    @Column(length = 500) private String remarks;
    @Column(precision = 19, scale = 2) private BigDecimal basis1601CWithheld;
    @Column(precision = 19, scale = 2) private BigDecimal basisAlphalistWithheld;
    @Column(precision = 19, scale = 2) private BigDecimal basisPaidRefunds;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false, length = 100) private String updatedBy;
    @Column(nullable = false) private LocalDateTime updatedAt;

    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public Integer getTaxYear() { return taxYear; } public void setTaxYear(Integer v) { taxYear = v; }
    public String getContactNumber() { return contactNumber; } public void setContactNumber(String v) { contactNumber = v; }
    public String getEmailAddress() { return emailAddress; } public void setEmailAddress(String v) { emailAddress = v; }
    public Integer getSheetsAttached() { return sheetsAttached; } public void setSheetsAttached(Integer v) { sheetsAttached = v; }
    public String getRefundsReleased() { return refundsReleased; } public void setRefundsReleased(String v) { refundsReleased = v; }
    public LocalDate getRefundDate() { return refundDate; } public void setRefundDate(LocalDate v) { refundDate = v; }
    public BigDecimal getOverremittanceAmount() { return overremittanceAmount; } public void setOverremittanceAmount(BigDecimal v) { overremittanceAmount = v; }
    public Integer getFirstCreditingMonth() { return firstCreditingMonth; } public void setFirstCreditingMonth(Integer v) { firstCreditingMonth = v; }
    public String getEvidenceReference() { return evidenceReference; } public void setEvidenceReference(String v) { evidenceReference = v; }
    public String getSignatoryName() { return signatoryName; } public void setSignatoryName(String v) { signatoryName = v; }
    public String getSignatoryTitle() { return signatoryTitle; } public void setSignatoryTitle(String v) { signatoryTitle = v; }
    public String getSignatoryTin() { return signatoryTin; } public void setSignatoryTin(String v) { signatoryTin = v; }
    public String getRemarks() { return remarks; } public void setRemarks(String v) { remarks = v; }
    public BigDecimal getBasis1601CWithheld() { return basis1601CWithheld; } public void setBasis1601CWithheld(BigDecimal v) { basis1601CWithheld = v; }
    public BigDecimal getBasisAlphalistWithheld() { return basisAlphalistWithheld; } public void setBasisAlphalistWithheld(BigDecimal v) { basisAlphalistWithheld = v; }
    public BigDecimal getBasisPaidRefunds() { return basisPaidRefunds; } public void setBasisPaidRefunds(BigDecimal v) { basisPaidRefunds = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { updatedBy = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { updatedAt = v; }
}
