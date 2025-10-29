package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalaryScheduleDTO implements Serializable {

    private Long salaryScheduleId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime effectDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime nbcDate;

    private String executiveOrderNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime executiveOrderDate;

    private Long salaryGrade;
    private Long salaryStep;
    private BigDecimal monthlySalary;
    private Long createdOrModifiedByEployeeId;

    public SalaryScheduleDTO() {

    }

    public SalaryScheduleDTO(Long salaryScheduleId, LocalDateTime effectDate, LocalDateTime nbcDate, String executiveOrderNo, LocalDateTime executiveOrderDate, Long salaryGrade, Long salaryStep, BigDecimal monthlySalary, Long createdOrModifiedByEployeeId) {
        this.salaryScheduleId = salaryScheduleId;
        this.effectDate = effectDate;
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
