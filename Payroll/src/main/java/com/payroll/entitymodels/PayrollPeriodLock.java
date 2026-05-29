package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Records a permanent, irreversible lock on a salary period.
 * Once a record exists for a given salaryPeriodKey, that period is LOCKED forever.
 * No computation, adjustment save, or adjustment post is allowed after locking.
 */
@Entity
@Table(name = "payroll_period_lock",
       indexes = @Index(name = "idx_ppl_period_key", columnList = "salaryPeriodKey"))
public class PayrollPeriodLock implements Serializable {

    /** The salary period key used as the natural + surrogate key (e.g. "2026-6-1"). */
    @Id
    @Column(nullable = false, length = 20)
    private String salaryPeriodKey;

    /** Full name (or username) of the officer who locked the period. */
    @Column(nullable = false, length = 200)
    private String lockedBy;

    /** Timestamp when the lock was applied. */
    @Column(nullable = false)
    private LocalDateTime lockedAt;

    public PayrollPeriodLock() {}

    public PayrollPeriodLock(String salaryPeriodKey, String lockedBy) {
        this.salaryPeriodKey = salaryPeriodKey;
        this.lockedBy = lockedBy;
        this.lockedAt = LocalDateTime.now();
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public String getSalaryPeriodKey() { return salaryPeriodKey; }
    public void setSalaryPeriodKey(String key) { this.salaryPeriodKey = key; }
    public String getLockedBy() { return lockedBy; }
    public void setLockedBy(String lockedBy) { this.lockedBy = lockedBy; }
    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }
}
