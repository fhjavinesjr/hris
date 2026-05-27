package com.payroll.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 * One-time or special income entries for a specific pay period.
 * Examples: bonus, loyalty pay, longevity pay, cash gift, etc.
 *
 * Entries are attached to a specific month+year so the Payroll batch
 * can include them in the correct period's earnings computation.
 */
@Entity
@Table(name = "employee_income")
public class EmployeeIncome implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "employeeNo")
    private String employeeNo;

    @Column(name = "employeeName")
    private String employeeName;

    /** Month number (1 = January … 12 = December). */
    @Column(name = "month")
    private Integer month;

    /** 4-digit year. */
    @Column(name = "year")
    private Integer year;

    /** Earning type code (matches EarningType in Administrative module). */
    @Column(name = "earningType")
    private String earningType;

    /** Human-readable name for the earning type. */
    @Column(name = "earningTypeName")
    private String earningTypeName;

    @Column(name = "amount")
    private Double amount;

    /** Whether this income is subject to withholding tax. */
    @Column(name = "isTaxable")
    private Boolean isTaxable;

    @Column(name = "remarks")
    private String remarks;

    public EmployeeIncome() {}

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
