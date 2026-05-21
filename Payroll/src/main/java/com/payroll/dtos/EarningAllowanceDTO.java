package com.payroll.dtos;

import java.io.Serializable;

public class EarningAllowanceDTO implements Serializable {

    private Long id;
    private String employeeNo;
    private String employeeName;
    private String salaryPeriod;
    private String effectiveUntil;
    private String allowanceType;
    private Double amountPerSalary;
    private Double amountDaily;
    private Double percentage;
    private String reason;

    public EarningAllowanceDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getSalaryPeriod() { return salaryPeriod; }
    public void setSalaryPeriod(String salaryPeriod) { this.salaryPeriod = salaryPeriod; }

    public String getEffectiveUntil() { return effectiveUntil; }
    public void setEffectiveUntil(String effectiveUntil) { this.effectiveUntil = effectiveUntil; }

    public String getAllowanceType() { return allowanceType; }
    public void setAllowanceType(String allowanceType) { this.allowanceType = allowanceType; }

    public Double getAmountPerSalary() { return amountPerSalary; }
    public void setAmountPerSalary(Double amountPerSalary) { this.amountPerSalary = amountPerSalary; }

    public Double getAmountDaily() { return amountDaily; }
    public void setAmountDaily(Double amountDaily) { this.amountDaily = amountDaily; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
