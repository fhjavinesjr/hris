package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "annualization_snapshot",
        uniqueConstraints = @UniqueConstraint(name = "uq_annualization_snapshot_version",
                columnNames = {"employeeNo", "taxYear", "version"}),
        indexes = @Index(name = "idx_annualization_snapshot_lookup",
                columnList = "employeeNo,taxYear,status"))
public class AnnualizationSnapshot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50) private String employeeNo;
    @Column(nullable = false, length = 200) private String employeeName;
    @Column(length = 200) private String department;
    @Column(length = 200) private String employerName;
    @Column(length = 30) private String employerTin;
    @Column(length = 4) private String employerBirBranchCode;
    @Column(length = 10) private String employerRdoCode;
    @Column(length = 500) private String employerAddress;
    @Column(length = 10) private String employerZipCode;
    @Column(length = 30) private String employeeTin;
    @Column(length = 4) private String employeeBirBranchCode;
    @Column(length = 4) private String employeeBirRegionCode;
    @Column(length = 10) private String employeeRdoCode;
    @Column(length = 500) private String employeeAddress;
    @Column(length = 10) private String employeeZipCode;
    @Column(length = 500) private String employeeLocalAddress;
    @Column(length = 10) private String employeeLocalZipCode;
    private LocalDate employeeBirthDate;
    @Column(length = 50) private String employeeContactNo;
    @Column(length = 200) private String employeePosition;
    @Column(length = 100) private String employeeLastName;
    @Column(length = 100) private String employeeFirstName;
    @Column(length = 100) private String employeeMiddleName;
    @Column(length = 50) private String employeeExtensionName;
    @Column(length = 100) private String employeeNationality;
    @Column(length = 50) private String employeeEmploymentStatusCode;
    @Column(length = 200) private String employeeEmploymentStatusName;
    private LocalDate employeeEmploymentFrom;
    private LocalDate employeeEmploymentTo;
    @Column(length = 2) private String employeeBirSeparationReasonCode;
    @Column(nullable = false) private Integer taxYear;
    @Column(nullable = false) private Integer version;
    @Column(nullable = false, length = 20) private String annualizationType = "YEAR_END";
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal presentGrossCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal presentNonTaxableCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal presentTaxableCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal presentTaxWithheld;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal previousGrossCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal previousNonTaxableCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal previousTaxableCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal previousTaxWithheld;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal grossCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal nonTaxableCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal taxableCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal gsisContribution;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal philHealthContribution;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal pagIbigContribution;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal taxWithheld;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal annualTaxDue;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal peraTaxCredit;
    @Column(precision = 19, scale = 2) private BigDecimal peraContributionAmount;
    @Column(precision = 19, scale = 2) private BigDecimal peraCertifiedTaxCredit;
    @Column(length = 100) private String peraCertificateNo;
    @Column(length = 200) private String peraAdministratorName;
    private LocalDate peraCertificateIssuedDate;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal adjustmentAmount;
    @Column(nullable = false, length = 30) private String adjustmentType;
    @Column(nullable = false) private LocalDateTime finalizedAt;
    @Column(nullable = false, length = 100) private String finalizedBy;
    @Column(length = 500) private String finalizationRemarks;
    private Boolean substitutedFilingQualified;
    @Column(length = 500) private String substitutedFilingReason;
    private LocalDateTime substitutedFilingReviewedAt;
    @Column(length = 100) private String substitutedFilingReviewedBy;
    private LocalDateTime reopenedAt;
    @Column(length = 100) private String reopenedBy;
    @Column(length = 500) private String reopenReason;
    private Long adjustmentHeaderId;
    @Column(length = 50) private String adjustmentSalaryPeriodKey;
    private LocalDateTime adjustmentAppliedAt;
    @Column(length = 100) private String adjustmentAppliedBy;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getEmployeeNo() { return employeeNo; } public void setEmployeeNo(String v) { employeeNo = v; }
    public String getEmployeeName() { return employeeName; } public void setEmployeeName(String v) { employeeName = v; }
    public String getDepartment() { return department; } public void setDepartment(String v) { department = v; }
    public String getEmployerName() { return employerName; } public void setEmployerName(String v) { employerName = v; }
    public String getEmployerTin() { return employerTin; } public void setEmployerTin(String v) { employerTin = v; }
    public String getEmployerBirBranchCode() { return employerBirBranchCode; } public void setEmployerBirBranchCode(String v) { employerBirBranchCode = v; }
    public String getEmployerRdoCode() { return employerRdoCode; } public void setEmployerRdoCode(String v) { employerRdoCode = v; }
    public String getEmployerAddress() { return employerAddress; } public void setEmployerAddress(String v) { employerAddress = v; }
    public String getEmployerZipCode() { return employerZipCode; } public void setEmployerZipCode(String v) { employerZipCode = v; }
    public String getEmployeeTin() { return employeeTin; } public void setEmployeeTin(String v) { employeeTin = v; }
    public String getEmployeeBirBranchCode() { return employeeBirBranchCode; } public void setEmployeeBirBranchCode(String v) { employeeBirBranchCode = v; }
    public String getEmployeeBirRegionCode() { return employeeBirRegionCode; } public void setEmployeeBirRegionCode(String v) { employeeBirRegionCode = v; }
    public String getEmployeeRdoCode() { return employeeRdoCode; } public void setEmployeeRdoCode(String v) { employeeRdoCode = v; }
    public String getEmployeeAddress() { return employeeAddress; } public void setEmployeeAddress(String v) { employeeAddress = v; }
    public String getEmployeeZipCode() { return employeeZipCode; } public void setEmployeeZipCode(String v) { employeeZipCode = v; }
    public String getEmployeeLocalAddress() { return employeeLocalAddress; } public void setEmployeeLocalAddress(String v) { employeeLocalAddress = v; }
    public String getEmployeeLocalZipCode() { return employeeLocalZipCode; } public void setEmployeeLocalZipCode(String v) { employeeLocalZipCode = v; }
    public LocalDate getEmployeeBirthDate() { return employeeBirthDate; } public void setEmployeeBirthDate(LocalDate v) { employeeBirthDate = v; }
    public String getEmployeeContactNo() { return employeeContactNo; } public void setEmployeeContactNo(String v) { employeeContactNo = v; }
    public String getEmployeePosition() { return employeePosition; } public void setEmployeePosition(String v) { employeePosition = v; }
    public String getEmployeeLastName() { return employeeLastName; } public void setEmployeeLastName(String v) { employeeLastName = v; }
    public String getEmployeeFirstName() { return employeeFirstName; } public void setEmployeeFirstName(String v) { employeeFirstName = v; }
    public String getEmployeeMiddleName() { return employeeMiddleName; } public void setEmployeeMiddleName(String v) { employeeMiddleName = v; }
    public String getEmployeeExtensionName() { return employeeExtensionName; } public void setEmployeeExtensionName(String v) { employeeExtensionName = v; }
    public String getEmployeeNationality() { return employeeNationality; } public void setEmployeeNationality(String v) { employeeNationality = v; }
    public String getEmployeeEmploymentStatusCode() { return employeeEmploymentStatusCode; } public void setEmployeeEmploymentStatusCode(String v) { employeeEmploymentStatusCode = v; }
    public String getEmployeeEmploymentStatusName() { return employeeEmploymentStatusName; } public void setEmployeeEmploymentStatusName(String v) { employeeEmploymentStatusName = v; }
    public LocalDate getEmployeeEmploymentFrom() { return employeeEmploymentFrom; } public void setEmployeeEmploymentFrom(LocalDate v) { employeeEmploymentFrom = v; }
    public LocalDate getEmployeeEmploymentTo() { return employeeEmploymentTo; } public void setEmployeeEmploymentTo(LocalDate v) { employeeEmploymentTo = v; }
    public String getEmployeeBirSeparationReasonCode() { return employeeBirSeparationReasonCode; }
    public void setEmployeeBirSeparationReasonCode(String v) { employeeBirSeparationReasonCode = v; }
    public Integer getTaxYear() { return taxYear; } public void setTaxYear(Integer v) { taxYear = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { version = v; }
    public String getAnnualizationType() { return annualizationType; } public void setAnnualizationType(String v) { annualizationType = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public BigDecimal getPresentGrossCompensation() { return presentGrossCompensation; } public void setPresentGrossCompensation(BigDecimal v) { presentGrossCompensation = v; }
    public BigDecimal getPresentNonTaxableCompensation() { return presentNonTaxableCompensation; } public void setPresentNonTaxableCompensation(BigDecimal v) { presentNonTaxableCompensation = v; }
    public BigDecimal getPresentTaxableCompensation() { return presentTaxableCompensation; } public void setPresentTaxableCompensation(BigDecimal v) { presentTaxableCompensation = v; }
    public BigDecimal getPresentTaxWithheld() { return presentTaxWithheld; } public void setPresentTaxWithheld(BigDecimal v) { presentTaxWithheld = v; }
    public BigDecimal getPreviousGrossCompensation() { return previousGrossCompensation; } public void setPreviousGrossCompensation(BigDecimal v) { previousGrossCompensation = v; }
    public BigDecimal getPreviousNonTaxableCompensation() { return previousNonTaxableCompensation; } public void setPreviousNonTaxableCompensation(BigDecimal v) { previousNonTaxableCompensation = v; }
    public BigDecimal getPreviousTaxableCompensation() { return previousTaxableCompensation; } public void setPreviousTaxableCompensation(BigDecimal v) { previousTaxableCompensation = v; }
    public BigDecimal getPreviousTaxWithheld() { return previousTaxWithheld; } public void setPreviousTaxWithheld(BigDecimal v) { previousTaxWithheld = v; }
    public BigDecimal getGrossCompensation() { return grossCompensation; } public void setGrossCompensation(BigDecimal v) { grossCompensation = v; }
    public BigDecimal getNonTaxableCompensation() { return nonTaxableCompensation; } public void setNonTaxableCompensation(BigDecimal v) { nonTaxableCompensation = v; }
    public BigDecimal getTaxableCompensation() { return taxableCompensation; } public void setTaxableCompensation(BigDecimal v) { taxableCompensation = v; }
    public BigDecimal getGsisContribution() { return gsisContribution; } public void setGsisContribution(BigDecimal v) { gsisContribution = v; }
    public BigDecimal getPhilHealthContribution() { return philHealthContribution; } public void setPhilHealthContribution(BigDecimal v) { philHealthContribution = v; }
    public BigDecimal getPagIbigContribution() { return pagIbigContribution; } public void setPagIbigContribution(BigDecimal v) { pagIbigContribution = v; }
    public BigDecimal getTaxWithheld() { return taxWithheld; } public void setTaxWithheld(BigDecimal v) { taxWithheld = v; }
    public BigDecimal getAnnualTaxDue() { return annualTaxDue; } public void setAnnualTaxDue(BigDecimal v) { annualTaxDue = v; }
    public BigDecimal getPeraTaxCredit() { return peraTaxCredit; } public void setPeraTaxCredit(BigDecimal v) { peraTaxCredit = v; }
    public BigDecimal getPeraContributionAmount() { return peraContributionAmount; } public void setPeraContributionAmount(BigDecimal v) { peraContributionAmount = v; }
    public BigDecimal getPeraCertifiedTaxCredit() { return peraCertifiedTaxCredit; } public void setPeraCertifiedTaxCredit(BigDecimal v) { peraCertifiedTaxCredit = v; }
    public String getPeraCertificateNo() { return peraCertificateNo; } public void setPeraCertificateNo(String v) { peraCertificateNo = v; }
    public String getPeraAdministratorName() { return peraAdministratorName; } public void setPeraAdministratorName(String v) { peraAdministratorName = v; }
    public LocalDate getPeraCertificateIssuedDate() { return peraCertificateIssuedDate; } public void setPeraCertificateIssuedDate(LocalDate v) { peraCertificateIssuedDate = v; }
    public BigDecimal getAdjustmentAmount() { return adjustmentAmount; } public void setAdjustmentAmount(BigDecimal v) { adjustmentAmount = v; }
    public String getAdjustmentType() { return adjustmentType; } public void setAdjustmentType(String v) { adjustmentType = v; }
    public LocalDateTime getFinalizedAt() { return finalizedAt; } public void setFinalizedAt(LocalDateTime v) { finalizedAt = v; }
    public String getFinalizedBy() { return finalizedBy; } public void setFinalizedBy(String v) { finalizedBy = v; }
    public String getFinalizationRemarks() { return finalizationRemarks; } public void setFinalizationRemarks(String v) { finalizationRemarks = v; }
    public Boolean getSubstitutedFilingQualified() { return substitutedFilingQualified; } public void setSubstitutedFilingQualified(Boolean v) { substitutedFilingQualified = v; }
    public String getSubstitutedFilingReason() { return substitutedFilingReason; } public void setSubstitutedFilingReason(String v) { substitutedFilingReason = v; }
    public LocalDateTime getSubstitutedFilingReviewedAt() { return substitutedFilingReviewedAt; } public void setSubstitutedFilingReviewedAt(LocalDateTime v) { substitutedFilingReviewedAt = v; }
    public String getSubstitutedFilingReviewedBy() { return substitutedFilingReviewedBy; } public void setSubstitutedFilingReviewedBy(String v) { substitutedFilingReviewedBy = v; }
    public LocalDateTime getReopenedAt() { return reopenedAt; } public void setReopenedAt(LocalDateTime v) { reopenedAt = v; }
    public String getReopenedBy() { return reopenedBy; } public void setReopenedBy(String v) { reopenedBy = v; }
    public String getReopenReason() { return reopenReason; } public void setReopenReason(String v) { reopenReason = v; }
    public Long getAdjustmentHeaderId() { return adjustmentHeaderId; } public void setAdjustmentHeaderId(Long v) { adjustmentHeaderId = v; }
    public String getAdjustmentSalaryPeriodKey() { return adjustmentSalaryPeriodKey; } public void setAdjustmentSalaryPeriodKey(String v) { adjustmentSalaryPeriodKey = v; }
    public LocalDateTime getAdjustmentAppliedAt() { return adjustmentAppliedAt; } public void setAdjustmentAppliedAt(LocalDateTime v) { adjustmentAppliedAt = v; }
    public String getAdjustmentAppliedBy() { return adjustmentAppliedBy; } public void setAdjustmentAppliedBy(String v) { adjustmentAppliedBy = v; }
}
