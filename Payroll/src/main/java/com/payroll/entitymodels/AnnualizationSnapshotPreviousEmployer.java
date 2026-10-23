package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "annualization_snapshot_previous",
        indexes = @Index(name = "idx_annualization_previous_snapshot", columnList = "snapshotId"))
public class AnnualizationSnapshotPreviousEmployer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long snapshotId;
    @Column(nullable = false, length = 200) private String employerName;
    @Column(nullable = false, length = 30) private String employerTin;
    @Column(nullable = false, length = 500) private String employerAddress;
    @Column(nullable = false, length = 10) private String employerZipCode;
    @Column(nullable = false) private LocalDate employmentFrom;
    @Column(nullable = false) private LocalDate employmentTo;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal grossCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal nonTaxableCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal taxableCompensation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal taxWithheld;
    public Long getId(){return id;} public void setId(Long v){id=v;} public Long getSnapshotId(){return snapshotId;} public void setSnapshotId(Long v){snapshotId=v;}
    public String getEmployerName(){return employerName;} public void setEmployerName(String v){employerName=v;} public String getEmployerTin(){return employerTin;} public void setEmployerTin(String v){employerTin=v;}
    public String getEmployerAddress(){return employerAddress;} public void setEmployerAddress(String v){employerAddress=v;} public String getEmployerZipCode(){return employerZipCode;} public void setEmployerZipCode(String v){employerZipCode=v;}
    public LocalDate getEmploymentFrom(){return employmentFrom;} public void setEmploymentFrom(LocalDate v){employmentFrom=v;} public LocalDate getEmploymentTo(){return employmentTo;} public void setEmploymentTo(LocalDate v){employmentTo=v;}
    public BigDecimal getGrossCompensation(){return grossCompensation;} public void setGrossCompensation(BigDecimal v){grossCompensation=v;} public BigDecimal getNonTaxableCompensation(){return nonTaxableCompensation;} public void setNonTaxableCompensation(BigDecimal v){nonTaxableCompensation=v;}
    public BigDecimal getTaxableCompensation(){return taxableCompensation;} public void setTaxableCompensation(BigDecimal v){taxableCompensation=v;} public BigDecimal getTaxWithheld(){return taxWithheld;} public void setTaxWithheld(BigDecimal v){taxWithheld=v;}
}
