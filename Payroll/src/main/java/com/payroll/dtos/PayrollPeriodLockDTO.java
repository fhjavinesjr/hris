package com.payroll.dtos;

import java.time.LocalDateTime;

/** Response returned by GET and POST /api/payroll-lock/{periodKey}. */
public class PayrollPeriodLockDTO {
    private String salaryPeriodKey;
    private boolean locked;
    private String lockedBy;
    private LocalDateTime lockedAt;

    public PayrollPeriodLockDTO() {}

    public PayrollPeriodLockDTO(String salaryPeriodKey, boolean locked, String lockedBy, LocalDateTime lockedAt) {
        this.salaryPeriodKey = salaryPeriodKey;
        this.locked = locked;
        this.lockedBy = lockedBy;
        this.lockedAt = lockedAt;
    }

    public String getSalaryPeriodKey() { return salaryPeriodKey; }
    public void setSalaryPeriodKey(String k) { this.salaryPeriodKey = k; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public String getLockedBy() { return lockedBy; }
    public void setLockedBy(String lockedBy) { this.lockedBy = lockedBy; }
    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }
}
