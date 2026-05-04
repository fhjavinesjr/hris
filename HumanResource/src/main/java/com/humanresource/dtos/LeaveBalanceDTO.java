package com.humanresource.dtos;

import java.io.Serializable;

/**
 * Estimated current leave balance for an employee.
 *
 * Formula (Option A — computed from beginning balance / latest LeaveInformation,
 * minus any Pending or Approved applications filed after the last processed period):
 *
 *   vacationLeaveBalance  = lastVL  - pendingOrApprovedVLAndForcedLeave (after lastPeriodEnd)
 *   sickLeaveBalance      = lastSL  - pendingOrApprovedSL               (after lastPeriodEnd)
 *   splBalance            = splBeg  - approvedSPLInCurrentCalendarYear
 *   forcedLeaveBalance    = flBeg   - approvedForcedLeaveInCurrentCalendarYear
 *
 * "Conservative" — Pending leaves are included in the deduction so the employee
 * sees a safe "still available" number.
 */
public class LeaveBalanceDTO implements Serializable {

    private Long employeeId;

    /** Latest processed VL balance, minus any VL/Forced Leave applications not yet in the ledger. */
    private Double vacationLeaveBalance;

    /** Latest processed SL balance, minus any SL applications not yet in the ledger. */
    private Double sickLeaveBalance;

    /** SPL balance: beginning balance minus approved SPL applications in the current calendar year. */
    private Double splBalance;

    /** Forced Leave balance: beginning balance minus approved Forced Leave in the current calendar year. */
    private Double forcedLeaveBalance;

    /** The period end date of the last processed LeaveInformation (null = no period run yet). */
    private String lastProcessedPeriodEnd;

    public LeaveBalanceDTO() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Double getVacationLeaveBalance() { return vacationLeaveBalance; }
    public void setVacationLeaveBalance(Double vacationLeaveBalance) { this.vacationLeaveBalance = vacationLeaveBalance; }

    public Double getSickLeaveBalance() { return sickLeaveBalance; }
    public void setSickLeaveBalance(Double sickLeaveBalance) { this.sickLeaveBalance = sickLeaveBalance; }

    public Double getSplBalance() { return splBalance; }
    public void setSplBalance(Double splBalance) { this.splBalance = splBalance; }

    public Double getForcedLeaveBalance() { return forcedLeaveBalance; }
    public void setForcedLeaveBalance(Double forcedLeaveBalance) { this.forcedLeaveBalance = forcedLeaveBalance; }

    public String getLastProcessedPeriodEnd() { return lastProcessedPeriodEnd; }
    public void setLastProcessedPeriodEnd(String lastProcessedPeriodEnd) { this.lastProcessedPeriodEnd = lastProcessedPeriodEnd; }
}
