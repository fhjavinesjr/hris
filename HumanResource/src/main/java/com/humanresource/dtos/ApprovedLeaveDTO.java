package com.humanresource.dtos;

import java.time.LocalDate;

/**
 * DTO for approved leave records returned to Payroll service.
 * Each record represents a single date (leaves spanning multiple days are expanded).
 */
public class ApprovedLeaveDTO {

    private String employeeNo;
    private LocalDate leaveDate;
    private String leaveType;
    private Boolean withPay;
    private String workDayType;
    private Double noOfDaysApplied;

    public ApprovedLeaveDTO() {
    }

    public ApprovedLeaveDTO(String employeeNo, LocalDate leaveDate, String leaveType, 
                           Boolean withPay, String workDayType, Double noOfDaysApplied) {
        this.employeeNo = employeeNo;
        this.leaveDate = leaveDate;
        this.leaveType = leaveType;
        this.withPay = withPay;
        this.workDayType = workDayType;
        this.noOfDaysApplied = noOfDaysApplied;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public LocalDate getLeaveDate() {
        return leaveDate;
    }

    public void setLeaveDate(LocalDate leaveDate) {
        this.leaveDate = leaveDate;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public Boolean getWithPay() {
        return withPay;
    }

    public void setWithPay(Boolean withPay) {
        this.withPay = withPay;
    }

    public String getWorkDayType() {
        return workDayType;
    }

    public void setWorkDayType(String workDayType) {
        this.workDayType = workDayType;
    }

    public Double getNoOfDaysApplied() {
        return noOfDaysApplied;
    }

    public void setNoOfDaysApplied(Double noOfDaysApplied) {
        this.noOfDaysApplied = noOfDaysApplied;
    }
}
