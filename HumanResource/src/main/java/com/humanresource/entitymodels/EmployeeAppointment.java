package com.humanresource.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "employeeappointment")
public class EmployeeAppointment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employeeAppointmentId")
    private Long employeeAppointmentId;

    @NotNull(message = "Employee ID is mandatory")
    @Column(name = "employeeId")
    private Long employeeId;

    @NotNull(message = "appointmentIssuedDate is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "appointmentIssuedDate", nullable = false)
    private LocalDateTime appointmentIssuedDate;

    @NotNull(message = "assumptionToDutyDate is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "assumptionToDutyDate", nullable = false)
    private LocalDateTime assumptionToDutyDate;

    @NotNull(message = "natureOfAppointmentId is mandatory")
    @Column(name = "natureOfAppointmentId")
    private Integer natureOfAppointmentId;

    @NotNull(message = "plantillaId is mandatory")
    @Column(name = "plantillaId")
    private Integer plantillaId;

    @NotNull(message = "jobPositionId is mandatory")
    @Column(name = "jobPositionId")
    private Integer jobPositionId;

    @NotNull(message = "salaryGrade is mandatory")
    @Column(name = "salaryGrade")
    private Integer salaryGrade;

    @NotNull(message = "salaryStep is mandatory")
    @Column(name = "salaryStep")
    private Integer salaryStep;

    @NotNull(message = "salaryPerAnnum is mandatory")
    @Column(name = "salaryPerAnnum")
    private BigDecimal salaryPerAnnum;

    @NotNull(message = "salaryPerMonth is mandatory")
    @Column(name = "salaryPerMonth")
    private BigDecimal salaryPerMonth;

    @Column(name = "salaryPerDay")
    private BigDecimal salaryPerDay;

    @NotNull(message = "details is mandatory")
    @Column(name = "details", length = 100, nullable = false)
    private String details;

    public EmployeeAppointment() {

    }

    public EmployeeAppointment(Long employeeId, LocalDateTime appointmentIssuedDate, LocalDateTime assumptionToDutyDate, Integer natureOfAppointmentId, Integer plantillaId, Integer jobPositionId, Integer salaryGrade, Integer salaryStep, BigDecimal salaryPerAnnum, BigDecimal salaryPerMonth, BigDecimal salaryPerDay, String details) {
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
    }

    public EmployeeAppointment(Long employeeAppointmentId, Long employeeId, LocalDateTime appointmentIssuedDate, LocalDateTime assumptionToDutyDate, Integer natureOfAppointmentId, Integer plantillaId, Integer jobPositionId, Integer salaryGrade, Integer salaryStep, BigDecimal salaryPerAnnum, BigDecimal salaryPerMonth, BigDecimal salaryPerDay, String details) {
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
}