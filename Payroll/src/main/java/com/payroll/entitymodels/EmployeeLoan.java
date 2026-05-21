package com.payroll.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "employee_loan")
public class EmployeeLoan implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "employeeNo", length = 50, nullable = false)
    private String employeeNo;

    @Column(name = "employeeName", length = 200, nullable = false)
    private String employeeName;

    @Column(name = "salaryPeriod", length = 100, nullable = false)
    private String salaryPeriod;

    @Column(name = "loanType", length = 200, nullable = false)
    private String loanType;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "toPay")
    private Integer toPay;

    @Column(name = "paid")
    private Integer paid;

    @Column(name = "isStopDeduction")
    private Boolean isStopDeduction;

    @Column(name = "loanStopDate", length = 20)
    private String loanStopDate;

    public EmployeeLoan() {}

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
