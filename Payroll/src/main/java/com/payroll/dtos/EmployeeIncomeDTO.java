package com.payroll.dtos;

public class EmployeeIncomeDTO {

    private Long id;
    private String employeeNo;
    private String employeeName;
    private Integer month;
    private Integer year;
    private String earningType;
    private String earningTypeName;
    private Double amount;
    private Boolean isTaxable;
    private String remarks;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getEarningType() { return earningType; }
    public void setEarningType(String earningType) { this.earningType = earningType; }

    public String getEarningTypeName() { return earningTypeName; }
    public void setEarningTypeName(String earningTypeName) { this.earningTypeName = earningTypeName; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Boolean getIsTaxable() { return isTaxable; }
    public void setIsTaxable(Boolean isTaxable) { this.isTaxable = isTaxable; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
