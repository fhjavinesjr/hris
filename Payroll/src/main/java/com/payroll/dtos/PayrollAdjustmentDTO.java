package com.payroll.dtos;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Full view of one employee's adjustment header + all its lines for a salary period.
 * Returned by GET /api/payroll-adjustment/{employeeNo} and used when saving.
 */
public class PayrollAdjustmentDTO {

    private Long id;
    private String employeeNo;
    private String employeeName;
    private String salaryPeriodKey;
    private int version;
    private String authorityNo;
    private String reason;
    private String status;  // PENDING | POSTED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private LocalDateTime postedAt;
    private String postedBy;
    private List<PayrollAdjustmentLineDTO> lines;

    // ── Computed totals (derived, not persisted) ──────────────────────────────
    /** Net adjustment impact on take-home pay (positive = more pay, negative = less). */
    private Double netAdjustmentAmount;

    public PayrollAdjustmentDTO() {}

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getSalaryPeriodKey() { return salaryPeriodKey; }
    public void setSalaryPeriodKey(String salaryPeriodKey) { this.salaryPeriodKey = salaryPeriodKey; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getAuthorityNo() { return authorityNo; }
    public void setAuthorityNo(String authorityNo) { this.authorityNo = authorityNo; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
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
    public List<PayrollAdjustmentLineDTO> getLines() { return lines; }
    public void setLines(List<PayrollAdjustmentLineDTO> lines) { this.lines = lines; }
    public Double getNetAdjustmentAmount() { return netAdjustmentAmount; }
    public void setNetAdjustmentAmount(Double netAdjustmentAmount) { this.netAdjustmentAmount = netAdjustmentAmount; }
}
