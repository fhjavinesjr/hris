package com.payroll.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "earning_allowance")
public class EarningAllowance implements Serializable {

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

    @Column(name = "effectiveUntil", length = 100)
    private String effectiveUntil;

    @Column(name = "allowanceType", length = 200, nullable = false)
    private String allowanceType;

    @Column(name = "amountPerSalary")
    private Double amountPerSalary;

    @Column(name = "amountDaily")
    private Double amountDaily;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "reason", length = 500)
    private String reason;

    public EarningAllowance() {}

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
