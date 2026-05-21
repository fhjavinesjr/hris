package com.payroll.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "employee_deduction")
public class EmployeeDeduction implements Serializable {

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

    @Column(name = "deductionType", length = 200, nullable = false)
    private String deductionType;

    @Column(name = "referenceNo", length = 100)
    private String referenceNo;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "isFixed")
    private Boolean isFixed;

    public EmployeeDeduction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getSalaryPeriod() { return salaryPeriod; }
    public void setSalaryPeriod(String salaryPeriod) { this.salaryPeriod = salaryPeriod; }

    public String getDeductionType() { return deductionType; }
    public void setDeductionType(String deductionType) { this.deductionType = deductionType; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Boolean getIsFixed() { return isFixed; }
    public void setIsFixed(Boolean isFixed) { this.isFixed = isFixed; }
}
