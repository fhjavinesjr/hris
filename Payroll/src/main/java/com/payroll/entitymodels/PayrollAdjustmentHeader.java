package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * One adjustment session per employee per salary period.
 * Multiple lines (earning additions, deduction fixes, cascade entries) belong to this header.
 *
 * Design: original PayrollDetail records are NEVER modified by adjustments.
 * All deltas are stored here and surfaced separately in the Payroll Register.
 */
@Entity
@Table(name = "payroll_adjustment_header",
       uniqueConstraints = @UniqueConstraint(
               name = "UQ_pah_emp_period_version",
               columnNames = {"employeeNo", "salaryPeriodKey", "version"}),
       indexes = {
           @Index(name = "idx_pah_employee_no",       columnList = "employeeNo"),
           @Index(name = "idx_pah_salary_period_key", columnList = "salaryPeriodKey")
       })
public class PayrollAdjustmentHeader implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String employeeNo;

    @Column(nullable = false, length = 200)
    private String employeeName;

    @Column(nullable = false, length = 20)
    private String salaryPeriodKey;   // e.g. "2026-6-1"

    /** Authority document number (e.g. "CSC Resolution No. 2026-001", "Office Order No. 12"). */
    @Column(length = 200)
    private String authorityNo;

    /** Human-readable reason / remarks for this adjustment. */
    @Column(length = 500)
    private String reason;

    /**
     * Adjustment revision sequence (1 = first save, 2 = second save, …).
     * Each new save on the same employee+period increments this and marks the old header SUPERSEDED.
     */
    @Column(nullable = false)
    private int version = 1;

    /** PENDING = entered, not yet finalised; POSTED = finalised; SUPERSEDED = replaced by a newer version. */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String createdBy;

    private LocalDateTime postedAt;

    @Column(length = 100)
    private String postedBy;

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PayrollAdjustmentLine> lines = new ArrayList<>();

    public PayrollAdjustmentHeader() {}

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getSalaryPeriodKey() { return salaryPeriodKey; }
    public void setSalaryPeriodKey(String salaryPeriodKey) { this.salaryPeriodKey = salaryPeriodKey; }
    public String getAuthorityNo() { return authorityNo; }
    public void setAuthorityNo(String authorityNo) { this.authorityNo = authorityNo; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }
    public List<PayrollAdjustmentLine> getLines() { return lines; }
    public void setLines(List<PayrollAdjustmentLine> lines) { this.lines = lines; }
}
