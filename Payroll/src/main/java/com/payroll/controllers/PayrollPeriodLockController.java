package com.payroll.controllers;

import com.payroll.dtos.PayrollPeriodLockDTO;
import com.payroll.services.PayrollPeriodLockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payroll-lock")
public class PayrollPeriodLockController {

    private final PayrollPeriodLockService lockService;

    public PayrollPeriodLockController(PayrollPeriodLockService lockService) {
        this.lockService = lockService;
    }

    /**
     * GET /api/payroll-lock/{periodKey}
     * Returns the lock status for the given salary period.
     */
    @GetMapping("/{periodKey}")
    public ResponseEntity<PayrollPeriodLockDTO> getLockStatus(
            @PathVariable String periodKey) {
        return ResponseEntity.ok(lockService.getLockStatus(periodKey));
    }

    /**
     * POST /api/payroll-lock/{periodKey}?lockedBy=...
     * Permanently locks the salary period. Idempotent.
     */
    @PostMapping("/{periodKey}")
    public ResponseEntity<PayrollPeriodLockDTO> lockPeriod(
            @PathVariable String periodKey,
            @RequestParam(required = false) String lockedBy) {
        PayrollPeriodLockDTO result = lockService.lockPeriod(periodKey, lockedBy);
        return ResponseEntity.ok(result);
    }
}
