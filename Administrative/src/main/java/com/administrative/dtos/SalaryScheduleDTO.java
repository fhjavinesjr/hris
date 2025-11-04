package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalaryScheduleDTO implements Serializable {

    private Long salaryScheduleId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime effectivityDate;

    private String nbcNo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime nbcDate;

    private String eoNo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime eoDate;

    private Long salaryGrade;
    private Long salaryStep;
    private BigDecimal monthlySalary;
    private Long createdOrModifiedByEployeeId;

    public SalaryScheduleDTO() {

    }

    public SalaryScheduleDTO(LocalDateTime effectivityDate, String nbcNo, LocalDateTime nbcDate, String eoNo, LocalDateTime eoDate, Long salaryGrade, Long salaryStep, BigDecimal monthlySalary, Long createdOrModifiedByEployeeId) {
        this.effectivityDate = effectivityDate;
        this.nbcNo = nbcNo;
        this.nbcDate = nbcDate;
        this.eoNo = eoNo;
        this.eoDate = eoDate;
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

    public LocalDateTime getEffectivityDate() {
        return effectivityDate;
    }

    public void setEffectivityDate(LocalDateTime effectivityDate) {
        this.effectivityDate = effectivityDate;
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

    public String getEoNo() {
        return eoNo;
    }

    public void setEoNo(String eoNo) {
        this.eoNo = eoNo;
    }

    public LocalDateTime getEoDate() {
        return eoDate;
    }

    public void setEoDate(LocalDateTime eoDate) {
        this.eoDate = eoDate;
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