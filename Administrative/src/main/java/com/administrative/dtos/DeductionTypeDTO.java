package com.administrative.dtos;

import java.io.Serializable;

public class DeductionTypeDTO implements Serializable {

    private Long deductionTypeId;
    private String accountingCode;
    private String name;
    private Boolean mandatoryDeduction;
    private Boolean agencyMandatory;
    private Boolean voluntaryContribution;
    private Boolean gsis;
    private Boolean philHealth;
    private Boolean pagIbig;
    private Boolean withholdingTax;
    private Boolean union;
    private Boolean others;

    public DeductionTypeDTO() {
    }

    public Long getDeductionTypeId() { return deductionTypeId; }
    public void setDeductionTypeId(Long deductionTypeId) { this.deductionTypeId = deductionTypeId; }

    public String getAccountingCode() { return accountingCode; }
    public void setAccountingCode(String accountingCode) { this.accountingCode = accountingCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getMandatoryDeduction() { return mandatoryDeduction; }
    public void setMandatoryDeduction(Boolean mandatoryDeduction) { this.mandatoryDeduction = mandatoryDeduction; }

    public Boolean getAgencyMandatory() { return agencyMandatory; }
    public void setAgencyMandatory(Boolean agencyMandatory) { this.agencyMandatory = agencyMandatory; }

    public Boolean getVoluntaryContribution() { return voluntaryContribution; }
    public void setVoluntaryContribution(Boolean voluntaryContribution) { this.voluntaryContribution = voluntaryContribution; }

    public Boolean getGsis() { return gsis; }
    public void setGsis(Boolean gsis) { this.gsis = gsis; }

    public Boolean getPhilHealth() { return philHealth; }
    public void setPhilHealth(Boolean philHealth) { this.philHealth = philHealth; }

    public Boolean getPagIbig() { return pagIbig; }
    public void setPagIbig(Boolean pagIbig) { this.pagIbig = pagIbig; }

    public Boolean getWithholdingTax() { return withholdingTax; }
    public void setWithholdingTax(Boolean withholdingTax) { this.withholdingTax = withholdingTax; }

    public Boolean getUnion() { return union; }
    public void setUnion(Boolean union) { this.union = union; }

    public Boolean getOthers() { return others; }
    public void setOthers(Boolean others) { this.others = others; }
}
