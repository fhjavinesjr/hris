package com.payroll.entitymodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "previous_employer_tax_data", indexes = {
        @Index(name = "idx_petd_employee_year", columnList = "employeeNo,taxYear")
})
public class PreviousEmployerTaxData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String employeeNo;

    @Column(nullable = false)
    private Integer taxYear;

    @Column(nullable = false, length = 200)
    private String employerName;

    @Column(nullable = false, length = 30)
    private String employerTin;

    @Column(nullable = false, length = 500)
    private String employerAddress;

    @Column(nullable = false, length = 10)
    private String employerZipCode;

    @Column(nullable = false)
    private LocalDate employmentFrom;

    @Column(nullable = false)
    private LocalDate employmentTo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal grossCompensation;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal nonTaxableCompensation;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal taxableCompensation;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal taxWithheld;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, length = 100)
    private String createdBy;

    @Column(nullable = false, length = 100)
    private String updatedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public Integer getTaxYear() { return taxYear; }
    public void setTaxYear(Integer taxYear) { this.taxYear = taxYear; }
    public String getEmployerName() { return employerName; }
    public void setEmployerName(String employerName) { this.employerName = employerName; }
    public String getEmployerTin() { return employerTin; }
    public void setEmployerTin(String employerTin) { this.employerTin = employerTin; }
    public String getEmployerAddress() { return employerAddress; }
    public void setEmployerAddress(String employerAddress) { this.employerAddress = employerAddress; }
    public String getEmployerZipCode() { return employerZipCode; }
    public void setEmployerZipCode(String employerZipCode) { this.employerZipCode = employerZipCode; }
    public LocalDate getEmploymentFrom() { return employmentFrom; }
    public void setEmploymentFrom(LocalDate employmentFrom) { this.employmentFrom = employmentFrom; }
    public LocalDate getEmploymentTo() { return employmentTo; }
    public void setEmploymentTo(LocalDate employmentTo) { this.employmentTo = employmentTo; }
    public BigDecimal getGrossCompensation() { return grossCompensation; }
    public void setGrossCompensation(BigDecimal grossCompensation) { this.grossCompensation = grossCompensation; }
    public BigDecimal getNonTaxableCompensation() { return nonTaxableCompensation; }
    public void setNonTaxableCompensation(BigDecimal nonTaxableCompensation) { this.nonTaxableCompensation = nonTaxableCompensation; }
    public BigDecimal getTaxableCompensation() { return taxableCompensation; }
    public void setTaxableCompensation(BigDecimal taxableCompensation) { this.taxableCompensation = taxableCompensation; }
    public BigDecimal getTaxWithheld() { return taxWithheld; }
    public void setTaxWithheld(BigDecimal taxWithheld) { this.taxWithheld = taxWithheld; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
