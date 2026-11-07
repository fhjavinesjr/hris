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
@Table(name = "tax_remittance_1601c",
        uniqueConstraints = @UniqueConstraint(name = "uk_1601c_year_month", columnNames = {"taxYear", "taxMonth"}))
public class TaxRemittance1601C {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Integer taxYear;
    @Column(nullable = false) private Integer taxMonth;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal taxesWithheld;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal adjustment;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal penalties;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amountRemitted;
    private LocalDate filingDate;
    private LocalDate remittanceDate;
    @Column(length = 100) private String remittanceChannel;
    @Column(length = 100) private String referenceNo;
    @Column(length = 500) private String remarks;
    @Column(length = 255) private String evidenceReference;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false, length = 100) private String recordedBy;
    @Column(nullable = false) private LocalDateTime recordedAt;
    @Column(nullable = false, length = 100) private String updatedBy;
    @Column(nullable = false) private LocalDateTime updatedAt;

    public Long getId() { return id; } public void setId(Long value) { id = value; }
    public Integer getTaxYear() { return taxYear; } public void setTaxYear(Integer value) { taxYear = value; }
    public Integer getTaxMonth() { return taxMonth; } public void setTaxMonth(Integer value) { taxMonth = value; }
    public BigDecimal getTaxesWithheld() { return taxesWithheld; } public void setTaxesWithheld(BigDecimal value) { taxesWithheld = value; }
    public BigDecimal getAdjustment() { return adjustment; } public void setAdjustment(BigDecimal value) { adjustment = value; }
    public BigDecimal getPenalties() { return penalties; } public void setPenalties(BigDecimal value) { penalties = value; }
    public BigDecimal getAmountRemitted() { return amountRemitted; } public void setAmountRemitted(BigDecimal value) { amountRemitted = value; }
    public LocalDate getFilingDate() { return filingDate; } public void setFilingDate(LocalDate value) { filingDate = value; }
    public LocalDate getRemittanceDate() { return remittanceDate; } public void setRemittanceDate(LocalDate value) { remittanceDate = value; }
    public String getRemittanceChannel() { return remittanceChannel; } public void setRemittanceChannel(String value) { remittanceChannel = value; }
    public String getReferenceNo() { return referenceNo; } public void setReferenceNo(String value) { referenceNo = value; }
    public String getRemarks() { return remarks; } public void setRemarks(String value) { remarks = value; }
    public String getEvidenceReference() { return evidenceReference; } public void setEvidenceReference(String value) { evidenceReference = value; }
    public String getStatus() { return status; } public void setStatus(String value) { status = value; }
    public String getRecordedBy() { return recordedBy; } public void setRecordedBy(String value) { recordedBy = value; }
    public LocalDateTime getRecordedAt() { return recordedAt; } public void setRecordedAt(LocalDateTime value) { recordedAt = value; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String value) { updatedBy = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}
