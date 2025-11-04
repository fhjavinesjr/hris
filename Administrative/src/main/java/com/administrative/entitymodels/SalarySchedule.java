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
    @Column(name = "effectivityDate")
    private LocalDateTime effectivityDate;

    @Column(name = "nbcNo", length = 100)
    private String nbcNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "nbcDate")
    private LocalDateTime nbcDate;

    @Column(name = "eoNo", length = 100)
    private String eoNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "eoDate")
    private LocalDateTime eoDate;

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

    public SalarySchedule(LocalDateTime effectivityDate, String nbcNo, LocalDateTime nbcDate, String eoNo, LocalDateTime eoDate, Long salaryGrade, Long salaryStep, BigDecimal monthlySalary, Long createdOrModifiedByEployeeId) {
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