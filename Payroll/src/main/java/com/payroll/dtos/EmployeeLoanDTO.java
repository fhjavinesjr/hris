package com.payroll.dtos;

public class EmployeeLoanDTO {

    private Long id;
    private String employeeNo;
    private String employeeName;
    private String salaryPeriod;
    private String loanType;
    private String reference;
    private Double amount;
    private Integer toPay;
    private Integer paid;
    private Boolean isStopDeduction;
    private String loanStopDate;

    public EmployeeLoanDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getSalaryPeriod() { return salaryPeriod; }
    public void setSalaryPeriod(String salaryPeriod) { this.salaryPeriod = salaryPeriod; }

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Integer getToPay() { return toPay; }
    public void setToPay(Integer toPay) { this.toPay = toPay; }

    public Integer getPaid() { return paid; }
    public void setPaid(Integer paid) { this.paid = paid; }

    public Boolean getIsStopDeduction() { return isStopDeduction; }
    public void setIsStopDeduction(Boolean isStopDeduction) { this.isStopDeduction = isStopDeduction; }

    public String getLoanStopDate() { return loanStopDate; }
    public void setLoanStopDate(String loanStopDate) { this.loanStopDate = loanStopDate; }
}
