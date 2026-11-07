package com.payroll.entitymodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "annual_pera_tax_credit",
        uniqueConstraints = @UniqueConstraint(name = "uq_annual_pera_employee_year",
                columnNames = {"employeeNo", "taxYear"}),
        indexes = @Index(name = "idx_annual_pera_lookup", columnList = "employeeNo,taxYear"))
public class AnnualPeraTaxCredit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String employeeNo;
    @Column(nullable = false)
    private Integer taxYear;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal contributionAmount;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal certifiedTaxCredit;
    @Column(nullable = false, length = 100)
    private String certificateNo;
    @Column(nullable = false, length = 200)
    private String administratorName;
    @Column(nullable = false)
    private LocalDate certificateIssuedDate;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    @Column(nullable = false, length = 100)
    private String createdBy;
    @Column(nullable = false, length = 100)
    private String updatedBy;

    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String value) { employeeNo = value; }
    public Integer getTaxYear() { return taxYear; }
    public void setTaxYear(Integer value) { taxYear = value; }
    public BigDecimal getContributionAmount() { return contributionAmount; }
    public void setContributionAmount(BigDecimal value) { contributionAmount = value; }
    public BigDecimal getCertifiedTaxCredit() { return certifiedTaxCredit; }
    public void setCertifiedTaxCredit(BigDecimal value) { certifiedTaxCredit = value; }
    public String getCertificateNo() { return certificateNo; }
    public void setCertificateNo(String value) { certificateNo = value; }
    public String getAdministratorName() { return administratorName; }
    public void setAdministratorName(String value) { administratorName = value; }
    public LocalDate getCertificateIssuedDate() { return certificateIssuedDate; }
    public void setCertificateIssuedDate(LocalDate value) { certificateIssuedDate = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String value) { createdBy = value; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String value) { updatedBy = value; }
}
