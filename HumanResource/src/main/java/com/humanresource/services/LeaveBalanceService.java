package com.humanresource.services;

import com.humanresource.dtos.LeaveBalanceDTO;

import java.time.LocalDate;
import java.util.Map;

public interface LeaveBalanceService {

    /**
     * Returns the estimated current leave balance for a single employee.
     * This is a read-only computation — it does NOT modify any records.
     */
    LeaveBalanceDTO getCurrentBalance(Long employeeId) throws Exception;

    /**
     * Returns each employee's posted VL or SL balance as of a historical date.
     * Payroll uses the day before its cutoff as the opening balance, then applies
     * only the attendance and leave activity inside that cutoff.
     */
    Map<String, Double> getPostedBalancesAsOf(String leaveType, LocalDate asOfDate);

    /**
     * Computes the same running balance while excluding one monetization record.
     * Used when editing/finalizing that record so its existing reservation is not
     * counted twice during validation.
     */
    LeaveBalanceDTO getCurrentBalanceExcludingMonetization(
            Long employeeId,
            Long leaveMonetizationId) throws Exception;

    /**
     * Computes the running balance without reserving the specified leave
     * application. Report generation uses this as the credit available before
     * the application shown in Section 7.A of CS Form No. 6.
     */
    LeaveBalanceDTO getCurrentBalanceExcludingLeaveApplication(
            Long employeeId,
            Long leaveApplicationId) throws Exception;
}
