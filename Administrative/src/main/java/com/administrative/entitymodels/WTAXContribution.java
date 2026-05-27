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

    /** Minimum taxable income for this bracket (inclusive). e.g. "10417.00" */
    @Column(name = "incomeFrom")
    private String incomeFrom;

    /** Maximum taxable income for this bracket (inclusive). Null = no upper bound (and above). */
    @Column(name = "incomeTo")
    private String incomeTo;

    /** Fixed tax amount at the lower bound of this bracket. e.g. "937.50" */
    @Column(name = "fixedAmount")
    private String fixedAmount;

    /** Rate applied to income exceeding incomeFrom. Store as percentage: "15" = 15%, or decimal "0.15". */
    @Column(name = "percentageOverBase")
    private String percentageOverBase;

    @Column(name = "taxAmount")
    private String taxAmount;

    public WTAXContribution() {

    }

    public WTAXContribution(Long wTaxContributionId, String salaryType, String incomeFrom, String incomeTo,
                             String fixedAmount, String percentageOverBase, String taxAmount) {
        this.wTaxContributionId = wTaxContributionId;
        this.salaryType = salaryType;
        this.incomeFrom = incomeFrom;
        this.incomeTo = incomeTo;
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

    public String getIncomeFrom() { return incomeFrom; }
    public void setIncomeFrom(String incomeFrom) { this.incomeFrom = incomeFrom; }

    public String getIncomeTo() { return incomeTo; }
    public void setIncomeTo(String incomeTo) { this.incomeTo = incomeTo; }

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