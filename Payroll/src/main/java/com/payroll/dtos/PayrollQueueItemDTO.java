package com.payroll.dtos;

import java.time.LocalDateTime;

/**
 * Represents a single employee's computation result inside the live processing queue.
 * Returned by GET /api/payroll-computation/queue/{jobId}?from={seqNo}
 */
public class PayrollQueueItemDTO {

    /** 0-based sequence number within the job; used for incremental polling (from=N). */
    private int seqNo;

    private String employeeNo;
    private String employeeName;

    /** "OK" or "FAILED" */
    private String status;

    private Double grossAmount;
    private Double totalDeduction;
    private Double netAmount;

    /** Present only when status = "FAILED". */
    private String errorMessage;

    private LocalDateTime processedAt;

    public PayrollQueueItemDTO() {}

    public PayrollQueueItemDTO(int seqNo, String employeeNo, String employeeName,
                                String status, Double grossAmount,
                                Double totalDeduction, Double netAmount,
                                String errorMessage) {
        this.seqNo          = seqNo;
        this.employeeNo     = employeeNo;
        this.employeeName   = employeeName;
        this.status         = status;
        this.grossAmount    = grossAmount;
        this.totalDeduction = totalDeduction;
        this.netAmount      = netAmount;
        this.errorMessage   = errorMessage;
        this.processedAt    = LocalDateTime.now();
    }

    // Getters / Setters
    public int getSeqNo() { return seqNo; }
    public void setSeqNo(int seqNo) { this.seqNo = seqNo; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getGrossAmount() { return grossAmount; }
    public void setGrossAmount(Double grossAmount) { this.grossAmount = grossAmount; }
    public Double getTotalDeduction() { return totalDeduction; }
    public void setTotalDeduction(Double totalDeduction) { this.totalDeduction = totalDeduction; }
    public Double getNetAmount() { return netAmount; }
    public void setNetAmount(Double netAmount) { this.netAmount = netAmount; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
