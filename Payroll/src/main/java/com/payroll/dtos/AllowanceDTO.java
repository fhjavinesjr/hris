package com.payroll.dtos;

/**
 * An allowance / earning entry for an employee.
 * Fetched in bulk from the Payroll DB (earning_allowance table) or HR service.
 *
 * The engine uses allowanceType codes to apply ZCMC-specific computation rules:
 *   PERA       – deducted proportionally per absent day
 *   SUBSIST    – reduced by ₱50/absent+leave day
 *   LAUNDRY    – reduced proportionally per absent+leave day
 *   HAZARD     – zeroed if absents ≥ 11 or leaves ≥ 11; computed from grade % or fixed
 *   OTHERS     – fixed amount, pass through as-is
 */
public class AllowanceDTO {

    private String employeeNo;
    private String allowanceCode;    // e.g. "PERA", "SUBSIST", "LAUNDRY", "HAZARD", "RICE"
    private String allowanceName;    // display name, e.g. "PERA Allowance"
    private Double amountPerSalary = 0.0;
    private Double amountPerDay = 0.0;    // daily rate (used for hazard pay when set)
    private Double ratePerBasic = 0.0;    // percentage of basic (hazard pay alternative)

    /** true = include in taxable income computation. */
    private Boolean isTaxable = false;

    /** true = this is specifically hazard pay (affects absent-day threshold check). */
    private Boolean isHazardPay = false;

    /** true = PERA allowance (needs proportional absent-day deduction). */
    private Boolean isPera = false;

    /** true = Subsistence allowance (needs ₱50/day deduction per absent+leave). */
    private Boolean isSubsistence = false;

    /** true = Laundry allowance (needs proportional absent+leave day deduction). */
    private Boolean isLaundry = false;

    // Getters / Setters
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getAllowanceCode() { return allowanceCode; }
    public void setAllowanceCode(String allowanceCode) { this.allowanceCode = allowanceCode; }
    public String getAllowanceName() { return allowanceName; }
    public void setAllowanceName(String allowanceName) { this.allowanceName = allowanceName; }
    public Double getAmountPerSalary() { return amountPerSalary; }
    public void setAmountPerSalary(Double amountPerSalary) { this.amountPerSalary = amountPerSalary; }
    public Double getAmountPerDay() { return amountPerDay; }
    public void setAmountPerDay(Double amountPerDay) { this.amountPerDay = amountPerDay; }
    public Double getRatePerBasic() { return ratePerBasic; }
    public void setRatePerBasic(Double ratePerBasic) { this.ratePerBasic = ratePerBasic; }
    public Boolean getIsTaxable() { return isTaxable; }
    public void setIsTaxable(Boolean isTaxable) { this.isTaxable = isTaxable; }
    public Boolean getIsHazardPay() { return isHazardPay; }
    public void setIsHazardPay(Boolean isHazardPay) { this.isHazardPay = isHazardPay; }
    public Boolean getIsPera() { return isPera; }
    public void setIsPera(Boolean isPera) { this.isPera = isPera; }
    public Boolean getIsSubsistence() { return isSubsistence; }
    public void setIsSubsistence(Boolean isSubsistence) { this.isSubsistence = isSubsistence; }
    public Boolean getIsLaundry() { return isLaundry; }
    public void setIsLaundry(Boolean isLaundry) { this.isLaundry = isLaundry; }
}
