package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "deduction_type")
public class DeductionType implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deductionTypeId")
    private Long deductionTypeId;

    @Column(name = "accountingCode", length = 100, nullable = false)
    private String accountingCode;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "mandatoryDeduction")
    private Boolean mandatoryDeduction;

    @Column(name = "agency_mandatory")
    private Boolean agencyMandatory;

    @Column(name = "voluntary_contribution")
    private Boolean voluntaryContribution;

    @Column(name = "gsis")
    private Boolean gsis;

    @Column(name = "philHealth")
    private Boolean philHealth;

    @Column(name = "pagIbig")
    private Boolean pagIbig;

    @Column(name = "withholdingTax")
    private Boolean withholdingTax;

    @Column(name = "is_union")
    private Boolean union;

    @Column(name = "others")
    private Boolean others;

    public DeductionType() {
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
