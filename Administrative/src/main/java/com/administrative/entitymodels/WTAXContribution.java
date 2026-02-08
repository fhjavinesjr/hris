package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "wtaxcontribution")
public class WTAXContribution implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wTaxContributionId")
    private Long wTaxContributionId;

    @Column(name = "salaryType")
    private String salaryType;

    @Column(name = "fixedAmount")
    private String fixedAmount;

    @Column(name = "percentageOverBase")
    private String percentageOverBase;

    @Column(name = "taxAmount")
    private String taxAmount;

    public WTAXContribution() {

    }

    public WTAXContribution(Long wTaxContributionId, String salaryType, String fixedAmount, String percentageOverBase, String taxAmount) {
        this.wTaxContributionId = wTaxContributionId;
        this.salaryType = salaryType;
        this.fixedAmount = fixedAmount;
        this.percentageOverBase = percentageOverBase;
        this.taxAmount = taxAmount;
    }

    public Long getwTaxContributionId() {
        return wTaxContributionId;
    }

    public void setwTaxContributionId(Long wTaxContributionId) {
        this.wTaxContributionId = wTaxContributionId;
    }

    public String getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(String salaryType) {
        this.salaryType = salaryType;
    }

    public String getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(String fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public String getPercentageOverBase() {
        return percentageOverBase;
    }

    public void setPercentageOverBase(String percentageOverBase) {
        this.percentageOverBase = percentageOverBase;
    }

    public String getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(String taxAmount) {
        this.taxAmount = taxAmount;
    }
}