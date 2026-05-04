package com.humanresource.controllers;

import com.humanresource.dtos.LeaveBalanceDTO;
import com.humanresource.services.LeaveBalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-balance")
@CrossOrigin("*")
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    public LeaveBalanceController(LeaveBalanceService leaveBalanceService) {
        this.leaveBalanceService = leaveBalanceService;
    }

    /**
     * GET /api/leave-balance/current/{employeeId}
     *
     * Returns the estimated current leave balance for an employee.
     * This is a read-only computation — it never modifies records.
     *
     * Response:
     *   vacationLeaveBalance  — VL available (including unposted VL and Forced Leave)
     *   sickLeaveBalance      — SL available (including unposted SL)
     *   splBalance            — Special Privilege Leave remaining this calendar year
     *   forcedLeaveBalance    — Forced Leave remaining this calendar year
     *   lastProcessedPeriodEnd — ISO date of last Leave Process run, or null
     */
    @GetMapping("/current/{employeeId}")
    public ResponseEntity<LeaveBalanceDTO> getCurrentBalance(@PathVariable Long employeeId) {
        try {
            LeaveBalanceDTO balance = leaveBalanceService.getCurrentBalance(employeeId);
            return ResponseEntity.ok(balance);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
