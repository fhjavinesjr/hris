package com.administrative.dtos;

import java.io.Serializable;

public class WTAXContributionDTO implements Serializable {

    private Long wTaxContributionId;
    private String salaryType;
    private String incomeFrom;
    private String incomeTo;
    private String fixedAmount;
    private String percentageOverBase;
    private String taxAmount;

    public WTAXContributionDTO() {

    }

    public WTAXContributionDTO(Long wTaxContributionId, String salaryType, String incomeFrom, String incomeTo,
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