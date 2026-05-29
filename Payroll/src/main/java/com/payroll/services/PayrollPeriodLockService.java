package com.payroll.services;

import com.payroll.dtos.PayrollPeriodLockDTO;

public interface PayrollPeriodLockService {
    /** Returns the lock status for a given salary period key. */
    PayrollPeriodLockDTO getLockStatus(String salaryPeriodKey);

    /**
     * Permanently locks the salary period. Idempotent: calling again on an already-locked
     * period returns the existing lock record without error.
     *
     * @throws IllegalStateException if the period is already locked (optional — idempotent is fine)
     */
    PayrollPeriodLockDTO lockPeriod(String salaryPeriodKey, String lockedBy);

    /** Convenience check used by other services to guard writes. */
    boolean isPeriodLocked(String salaryPeriodKey);
}
