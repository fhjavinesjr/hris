package com.payroll.dtos;

import com.payroll.entitymodels.BatchJobStatus;
import java.time.LocalDateTime;

/** Progress snapshot returned when polling GET /api/payroll-computation/status/{jobId}. */
public class PayrollJobStatusDTO {

    private String jobId;
    private String salaryPeriodKey;
    private BatchJobStatus status;
    private Integer progressPct;
    private Integer totalEmployees;
    private Integer processedEmployees;
    private Integer failedEmployees;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String summary;
    private String errorDetail;

    // Getters / Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getSalaryPeriodKey() { return salaryPeriodKey; }
    public void setSalaryPeriodKey(String salaryPeriodKey) { this.salaryPeriodKey = salaryPeriodKey; }
    public BatchJobStatus getStatus() { return status; }
    public void setStatus(BatchJobStatus status) { this.status = status; }
    public Integer getProgressPct() { return progressPct; }
    public void setProgressPct(Integer progressPct) { this.progressPct = progressPct; }
    public Integer getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(Integer totalEmployees) { this.totalEmployees = totalEmployees; }
    public Integer getProcessedEmployees() { return processedEmployees; }
    public void setProcessedEmployees(Integer processedEmployees) { this.processedEmployees = processedEmployees; }
    public Integer getFailedEmployees() { return failedEmployees; }
    public void setFailedEmployees(Integer failedEmployees) { this.failedEmployees = failedEmployees; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getErrorDetail() { return errorDetail; }
    public void setErrorDetail(String errorDetail) { this.errorDetail = errorDetail; }
}
