package com.humanresource.services;

import com.humanresource.dtos.LeaveBalanceDTO;

public interface LeaveBalanceService {

    /**
     * Returns the estimated current leave balance for a single employee.
     * This is a read-only computation — it does NOT modify any records.
     */
    LeaveBalanceDTO getCurrentBalance(Long employeeId) throws Exception;
}
