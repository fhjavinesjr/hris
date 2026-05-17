package com.administrative.dtos;

import java.io.Serializable;

public class EarningTypeDTO implements Serializable {

    private Long earningTypeId;
    private String accountingCode;
    private String name;
    private Boolean taxable;
    private Boolean allowance;
    private Boolean dailyBasis;
    private Boolean basic;
    private Boolean rata;
    private Boolean honorarium;
    private Boolean ecola;
    private Boolean up;
    private Boolean fixedHousing;
    private Boolean representation;
    private Boolean transportation;
    private Boolean longevity;
    private Boolean laundry;
    private Boolean hazardPay;
    private Boolean pera;
    private Boolean subsistence;
    private Boolean specialPayroll;

    public EarningTypeDTO() {
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
