package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "earning_type")
public class EarningType implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "earningTypeId")
    private Long earningTypeId;

    @Column(name = "accountingCode", length = 100, nullable = false)
    private String accountingCode;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "taxable")
    private Boolean taxable;

    @Column(name = "allowance")
    private Boolean allowance;

    @Column(name = "dailyBasis")
    private Boolean dailyBasis;

    @Column(name = "basic")
    private Boolean basic;

    @Column(name = "rata")
    private Boolean rata;

    @Column(name = "honorarium")
    private Boolean honorarium;

    @Column(name = "ecola")
    private Boolean ecola;

    @Column(name = "up")
    private Boolean up;

    @Column(name = "fixedHousing")
    private Boolean fixedHousing;

    @Column(name = "representation")
    private Boolean representation;

    @Column(name = "transportation")
    private Boolean transportation;

    @Column(name = "longevity")
    private Boolean longevity;

    @Column(name = "laundry")
    private Boolean laundry;

    @Column(name = "hazardPay")
    private Boolean hazardPay;

    @Column(name = "pera")
    private Boolean pera;

    @Column(name = "subsistence")
    private Boolean subsistence;

    @Column(name = "specialPayroll")
    private Boolean specialPayroll;

    public EarningType() {
    }

    public Long getEarningTypeId() { return earningTypeId; }
    public void setEarningTypeId(Long earningTypeId) { this.earningTypeId = earningTypeId; }

    public String getAccountingCode() { return accountingCode; }
    public void setAccountingCode(String accountingCode) { this.accountingCode = accountingCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getTaxable() { return taxable; }
    public void setTaxable(Boolean taxable) { this.taxable = taxable; }

    public Boolean getAllowance() { return allowance; }
    public void setAllowance(Boolean allowance) { this.allowance = allowance; }

    public Boolean getDailyBasis() { return dailyBasis; }
    public void setDailyBasis(Boolean dailyBasis) { this.dailyBasis = dailyBasis; }

    public Boolean getBasic() { return basic; }
    public void setBasic(Boolean basic) { this.basic = basic; }

    public Boolean getRata() { return rata; }
    public void setRata(Boolean rata) { this.rata = rata; }

    public Boolean getHonorarium() { return honorarium; }
    public void setHonorarium(Boolean honorarium) { this.honorarium = honorarium; }

    public Boolean getEcola() { return ecola; }
    public void setEcola(Boolean ecola) { this.ecola = ecola; }

    public Boolean getUp() { return up; }
    public void setUp(Boolean up) { this.up = up; }

    public Boolean getFixedHousing() { return fixedHousing; }
    public void setFixedHousing(Boolean fixedHousing) { this.fixedHousing = fixedHousing; }

    public Boolean getRepresentation() { return representation; }
    public void setRepresentation(Boolean representation) { this.representation = representation; }

    public Boolean getTransportation() { return transportation; }
    public void setTransportation(Boolean transportation) { this.transportation = transportation; }

    public Boolean getLongevity() { return longevity; }
    public void setLongevity(Boolean longevity) { this.longevity = longevity; }

    public Boolean getLaundry() { return laundry; }
    public void setLaundry(Boolean laundry) { this.laundry = laundry; }

    public Boolean getHazardPay() { return hazardPay; }
    public void setHazardPay(Boolean hazardPay) { this.hazardPay = hazardPay; }

    public Boolean getPera() { return pera; }
    public void setPera(Boolean pera) { this.pera = pera; }

    public Boolean getSubsistence() { return subsistence; }
    public void setSubsistence(Boolean subsistence) { this.subsistence = subsistence; }

    public Boolean getSpecialPayroll() { return specialPayroll; }
    public void setSpecialPayroll(Boolean specialPayroll) { this.specialPayroll = specialPayroll; }
}
