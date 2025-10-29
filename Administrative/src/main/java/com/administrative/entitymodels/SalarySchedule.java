package com.administrative.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_schedule")
public class SalarySchedule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salaryScheduleId")
    private Long salaryScheduleId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "effectDate")
    private LocalDateTime effectDate;

    @Column(name = "nbcNo", length = 100)
    private String nbcNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "nbcDate")
    private LocalDateTime nbcDate;

    @Column(name = "executiveOrderNo", length = 100)
    private String executiveOrderNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "executiveOrderDate")
    private LocalDateTime executiveOrderDate;

    @Column(name = "salaryGrade")
    private Long salaryGrade;

    @Column(name = "salaryStep")
    private Long salaryStep;

    @Column(name = "monthlySalary")
    private BigDecimal monthlySalary;

    @Column(name = "createdOrModifiedByEployeeId")
    private Long createdOrModifiedByEployeeId;

    public SalarySchedule() {

    }

    public SalarySchedule(LocalDateTime effectDate, String nbcNo, LocalDateTime nbcDate, String executiveOrderNo, LocalDateTime executiveOrderDate, Long salaryGrade, Long salaryStep, BigDecimal monthlySalary, Long createdOrModifiedByEployeeId) {
        this.effectDate = effectDate;
        this.nbcNo = nbcNo;
        this.nbcDate = nbcDate;
        this.executiveOrderNo = executiveOrderNo;
        this.executiveOrderDate = executiveOrderDate;
        this.salaryGrade = salaryGrade;
        this.salaryStep = salaryStep;
        this.monthlySalary = monthlySalary;
        this.createdOrModifiedByEployeeId = createdOrModifiedByEployeeId;
    }

    public Long getSalaryScheduleId() {
        return salaryScheduleId;
    }

    public void setSalaryScheduleId(Long salaryScheduleId) {
        this.salaryScheduleId = salaryScheduleId;
    }

    public LocalDateTime getEffectDate() {
        return effectDate;
    }

    public void setEffectDate(LocalDateTime effectDate) {
        this.effectDate = effectDate;
    }

    public String getNbcNo() {
        return nbcNo;
    }

    public void setNbcNo(String nbcNo) {
        this.nbcNo = nbcNo;
    }

    public LocalDateTime getNbcDate() {
        return nbcDate;
    }

    public void setNbcDate(LocalDateTime nbcDate) {
        this.nbcDate = nbcDate;
    }

    public String getExecutiveOrderNo() {
        return executiveOrderNo;
    }

    public void setExecutiveOrderNo(String executiveOrderNo) {
        this.executiveOrderNo = executiveOrderNo;
    }

    public LocalDateTime getExecutiveOrderDate() {
        return executiveOrderDate;
    }

    public void setExecutiveOrderDate(LocalDateTime executiveOrderDate) {
        this.executiveOrderDate = executiveOrderDate;
    }

    public Long getSalaryGrade() {
        return salaryGrade;
    }

    public void setSalaryGrade(Long salaryGrade) {
        this.salaryGrade = salaryGrade;
    }

    public Long getSalaryStep() {
        return salaryStep;
    }

    public void setSalaryStep(Long salaryStep) {
        this.salaryStep = salaryStep;
    }

    public BigDecimal getMonthlySalary() {
        return monthlySalary;
    }

    public void setMonthlySalary(BigDecimal monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    public Long getCreatedOrModifiedByEployeeId() {
        return createdOrModifiedByEployeeId;
    }

    public void setCreatedOrModifiedByEployeeId(Long createdOrModifiedByEployeeId) {
        this.createdOrModifiedByEployeeId = createdOrModifiedByEployeeId;
    }
}