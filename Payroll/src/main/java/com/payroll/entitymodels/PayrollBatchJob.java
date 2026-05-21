package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Tracks the lifecycle of a batch payroll computation job.
 * Clients poll {@link #getProgressPct()} until {@link #getStatus()} reaches DONE or FAILED.
 */
@Entity
@Table(name = "payroll_batch_job",
       indexes = @Index(name = "idx_pbj_job_id", columnList = "jobId"))
public class PayrollBatchJob implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** UUID given to the client so they can poll for progress. */
    @Column(nullable = false, unique = true, length = 36)
    private String jobId;

    @Column(nullable = false, length = 20)
    private String salaryPeriodKey;   // e.g. "2026-6-1"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchJobStatus status = BatchJobStatus.PENDING;

    /** 0-100 progress percentage. */
    private Integer progressPct = 0;

    private Integer totalEmployees = 0;
    private Integer processedEmployees = 0;
    private Integer failedEmployees = 0;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    /** Human-readable summary set when status = DONE or FAILED. */
    @Column(length = 1000)
    private String summary;

    /** Stacktrace or error message if status = FAILED. */
    @Column(length = 2000)
    private String errorDetail;

    /** Who triggered the job (employeeNo of the admin). */
    @Column(length = 50)
    private String triggeredBy;

    public PayrollBatchJob() {}

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
}
