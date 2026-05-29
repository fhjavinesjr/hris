package com.payroll.dtos;

import java.util.List;

/**
 * Request body for the cascade-preview endpoint.
 *
 * The officer fills in one manual line (a taxable earning or standalone deduction),
 * and the server returns the preview of what cascade lines will be auto-computed
 * along with the projected adjusted net pay.
 */
public class AdjustmentPreviewRequest {

    /** Employee whose payroll data will be used as the base for cascade computation. */
    private String employeeNo;
    private String salaryPeriodKey;

    /** The single manual line the officer is about to add. */
    private String type;       // EARNING | DEDUCTION
    private String code;       // user-chosen code
    private String name;       // user-chosen display name
    private Double amount;     // positive or negative delta
    private Boolean isTaxable; // only for EARNING type

    // Getters / Setters
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getSalaryPeriodKey() { return salaryPeriodKey; }
    public void setSalaryPeriodKey(String salaryPeriodKey) { this.salaryPeriodKey = salaryPeriodKey; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Boolean getIsTaxable() { return isTaxable; }
    public void setIsTaxable(Boolean isTaxable) { this.isTaxable = isTaxable; }
}
