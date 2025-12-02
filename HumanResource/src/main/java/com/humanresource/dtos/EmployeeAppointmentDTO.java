package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EmployeeAppointmentDTO implements Serializable {

    private Long employeeAppointmentId;

    @NotNull(message = "Employee ID is mandatory")
    private Long employeeId;

    @NotNull(message = "appointmentIssuedDate is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime appointmentIssuedDate;

    @NotNull(message = "assumptionToDutyDate is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime assumptionToDutyDate;

    @NotNull(message = "natureOfAppointmentId is mandatory")
    private Integer natureOfAppointmentId;

    @NotNull(message = "plantillaId is mandatory")
    private Integer plantillaId;

    @NotNull(message = "jobPositionId is mandatory")
    private Integer jobPositionId;

    @NotNull(message = "salaryGrade is mandatory")
    private Integer salaryGrade;

    @NotNull(message = "salaryStep is mandatory")
    private Integer salaryStep;

    @NotNull(message = "salaryPerAnnum is mandatory")
    private BigDecimal salaryPerAnnum;

    @NotNull(message = "salaryPerMonth is mandatory")
    private BigDecimal salaryPerMonth;

    private BigDecimal salaryPerDay;

    @NotNull(message = "details is mandatory")
    private String details;

    @NotNull(message = "isActive is mandatory")
    private Boolean isActive;

    public EmployeeAppointmentDTO() {

    }

    public EmployeeAppointmentDTO(Long employeeId, LocalDateTime appointmentIssuedDate, LocalDateTime assumptionToDutyDate, Integer natureOfAppointmentId, Integer plantillaId, Integer jobPositionId, Integer salaryGrade, Integer salaryStep, BigDecimal salaryPerAnnum, BigDecimal salaryPerMonth, BigDecimal salaryPerDay, String details, Boolean isActive) {
        this.employeeId = employeeId;
        this.appointmentIssuedDate = appointmentIssuedDate;
        this.assumptionToDutyDate = assumptionToDutyDate;
        this.natureOfAppointmentId = natureOfAppointmentId;
        this.plantillaId = plantillaId;
        this.jobPositionId = jobPositionId;
        this.salaryGrade = salaryGrade;
        this.salaryStep = salaryStep;
        this.salaryPerAnnum = salaryPerAnnum;
        this.salaryPerMonth = salaryPerMonth;
        this.salaryPerDay = salaryPerDay;
        this.details = details;
        this.isActive = isActive;
    }

    public EmployeeAppointmentDTO(Long employeeAppointmentId, Long employeeId, LocalDateTime appointmentIssuedDate, LocalDateTime assumptionToDutyDate, Integer natureOfAppointmentId, Integer plantillaId, Integer jobPositionId, Integer salaryGrade, Integer salaryStep, BigDecimal salaryPerAnnum, BigDecimal salaryPerMonth, BigDecimal salaryPerDay, String details, Boolean isActive) {
        this.employeeAppointmentId = employeeAppointmentId;
        this.employeeId = employeeId;
        this.appointmentIssuedDate = appointmentIssuedDate;
        this.assumptionToDutyDate = assumptionToDutyDate;
        this.natureOfAppointmentId = natureOfAppointmentId;
        this.plantillaId = plantillaId;
        this.jobPositionId = jobPositionId;
        this.salaryGrade = salaryGrade;
        this.salaryStep = salaryStep;
        this.salaryPerAnnum = salaryPerAnnum;
        this.salaryPerMonth = salaryPerMonth;
        this.salaryPerDay = salaryPerDay;
        this.details = details;
        this.isActive = isActive;
    }

    public Long getEmployeeAppointmentId() {
        return employeeAppointmentId;
    }

    public void setEmployeeAppointmentId(Long employeeAppointmentId) {
        this.employeeAppointmentId = employeeAppointmentId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getAppointmentIssuedDate() {
        return appointmentIssuedDate;
    }

    public void setAppointmentIssuedDate(LocalDateTime appointmentIssuedDate) {
        this.appointmentIssuedDate = appointmentIssuedDate;
    }

    public LocalDateTime getAssumptionToDutyDate() {
        return assumptionToDutyDate;
    }

    public void setAssumptionToDutyDate(LocalDateTime assumptionToDutyDate) {
        this.assumptionToDutyDate = assumptionToDutyDate;
    }

    public Integer getNatureOfAppointmentId() {
        return natureOfAppointmentId;
    }

    public void setNatureOfAppointmentId(Integer natureOfAppointmentId) {
        this.natureOfAppointmentId = natureOfAppointmentId;
    }

    public Integer getPlantillaId() {
        return plantillaId;
    }

    public void setPlantillaId(Integer plantillaId) {
        this.plantillaId = plantillaId;
    }

    public Integer getJobPositionId() {
        return jobPositionId;
    }

    public void setJobPositionId(Integer jobPositionId) {
        this.jobPositionId = jobPositionId;
    }

    public Integer getSalaryGrade() {
        return salaryGrade;
    }

    public void setSalaryGrade(Integer salaryGrade) {
        this.salaryGrade = salaryGrade;
    }

    public Integer getSalaryStep() {
        return salaryStep;
    }

    public void setSalaryStep(Integer salaryStep) {
        this.salaryStep = salaryStep;
    }

    public BigDecimal getSalaryPerAnnum() {
        return salaryPerAnnum;
    }

    public void setSalaryPerAnnum(BigDecimal salaryPerAnnum) {
        this.salaryPerAnnum = salaryPerAnnum;
    }

    public BigDecimal getSalaryPerMonth() {
        return salaryPerMonth;
    }

    public void setSalaryPerMonth(BigDecimal salaryPerMonth) {
        this.salaryPerMonth = salaryPerMonth;
    }

    public BigDecimal getSalaryPerDay() {
        return salaryPerDay;
    }

    public void setSalaryPerDay(BigDecimal salaryPerDay) {
        this.salaryPerDay = salaryPerDay;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}