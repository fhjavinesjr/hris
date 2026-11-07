package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "annualization_snapshot_period",
        indexes = @Index(name = "idx_annualization_period_snapshot", columnList = "snapshotId"))
public class AnnualizationSnapshotPeriod {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long snapshotId;
    @Column(nullable = false, length = 50) private String salaryPeriodKey;
    @Column(nullable = false) private LocalDate salaryDate;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal grossCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal nonTaxableCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal taxableCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal gsisContribution;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal philHealthContribution;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal pagIbigContribution;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal taxWithheld;
    private Boolean minimumWageEarner;
    @Column(length = 30) private String minimumWageRegionCode;
    @Column(precision = 19, scale = 2) private BigDecimal minimumWageDailyRate;
    @Column(precision = 19, scale = 2) private BigDecimal minimumWageMonthlyRate;
    @Column(length = 200) private String minimumWageLegalBasis;
    public Long getId(){return id;} public void setId(Long v){id=v;} public Long getSnapshotId(){return snapshotId;} public void setSnapshotId(Long v){snapshotId=v;}
    public String getSalaryPeriodKey(){return salaryPeriodKey;} public void setSalaryPeriodKey(String v){salaryPeriodKey=v;} public LocalDate getSalaryDate(){return salaryDate;} public void setSalaryDate(LocalDate v){salaryDate=v;}
    public BigDecimal getGrossCompensation(){return grossCompensation;} public void setGrossCompensation(BigDecimal v){grossCompensation=v;} public BigDecimal getNonTaxableCompensation(){return nonTaxableCompensation;} public void setNonTaxableCompensation(BigDecimal v){nonTaxableCompensation=v;}
    public BigDecimal getTaxableCompensation(){return taxableCompensation;} public void setTaxableCompensation(BigDecimal v){taxableCompensation=v;} public BigDecimal getGsisContribution(){return gsisContribution;} public void setGsisContribution(BigDecimal v){gsisContribution=v;}
    public BigDecimal getPhilHealthContribution(){return philHealthContribution;} public void setPhilHealthContribution(BigDecimal v){philHealthContribution=v;} public BigDecimal getPagIbigContribution(){return pagIbigContribution;} public void setPagIbigContribution(BigDecimal v){pagIbigContribution=v;}
    public BigDecimal getTaxWithheld(){return taxWithheld;} public void setTaxWithheld(BigDecimal v){taxWithheld=v;}
    public Boolean getMinimumWageEarner(){return minimumWageEarner;} public void setMinimumWageEarner(Boolean v){minimumWageEarner=v;}
    public String getMinimumWageRegionCode(){return minimumWageRegionCode;} public void setMinimumWageRegionCode(String v){minimumWageRegionCode=v;}
    public BigDecimal getMinimumWageDailyRate(){return minimumWageDailyRate;} public void setMinimumWageDailyRate(BigDecimal v){minimumWageDailyRate=v;}
    public BigDecimal getMinimumWageMonthlyRate(){return minimumWageMonthlyRate;} public void setMinimumWageMonthlyRate(BigDecimal v){minimumWageMonthlyRate=v;}
    public String getMinimumWageLegalBasis(){return minimumWageLegalBasis;} public void setMinimumWageLegalBasis(String v){minimumWageLegalBasis=v;}
}
