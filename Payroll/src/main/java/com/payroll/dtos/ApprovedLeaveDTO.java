package com.payroll.dtos;

import java.time.LocalDate;

/**
 * An approved leave record for an employee within the cutoff window.
 * Fetched in bulk from the HumanResource service.
 *
 * The HR service must expose:
 *   GET /api/leave/bulk-approved?from={cutoffStart}&to={cutoffEnd}
 * returning List&lt;ApprovedLeaveDTO&gt;.
 */
public class ApprovedLeaveDTO {

    private String employeeNo;
    private LocalDate leaveDate;

    /**
     * Leave type code: VL = Vacation Leave, SL = Sick Leave,
     * CL = Compensatory/Force Leave, RL = Special Leave (e.g. Rehabilitation Leave).
     */
    private String leaveType;

    /** true = with pay; false = without pay (AWOL equivalent for counting absences). */
    private Boolean withPay = true;

    /**
     * WHOLEDAY | HALFDAYAM | HALFDAYPM
     * Half-day with-pay = workDaysPresent + 0.5 (not counted as absent).
     * Half-day without-pay = countHalfDayWOPay++, workDaysPresent + 0.5.
     */
    private String workDayType = "WHOLEDAY";

    /** Number of days this leave record spans (used for leave balance deduction). */
    private Double noOfDaysApplied = 1.0;

    // Getters / Setters
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public LocalDate getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public Boolean getWithPay() { return withPay; }
    public void setWithPay(Boolean withPay) { this.withPay = withPay; }
    public String getWorkDayType() { return workDayType; }
    public void setWorkDayType(String workDayType) { this.workDayType = workDayType; }
    public Double getNoOfDaysApplied() { return noOfDaysApplied; }
    public void setNoOfDaysApplied(Double noOfDaysApplied) { this.noOfDaysApplied = noOfDaysApplied; }
}
