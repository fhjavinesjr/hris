package com.humanresource.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LeaveProcessJobStatusDTO implements Serializable {

    private String jobId;
    private String status;
    private Integer progressPct;
    private Integer totalEmployees;
    private Integer processedEmployees;
    private Integer skippedEmployees;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String summary;
    private String errorDetail;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProgressPct() {
        return progressPct;
    }

    public void setProgressPct(Integer progressPct) {
        this.progressPct = progressPct;
    }

    public Integer getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(Integer totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public Integer getProcessedEmployees() {
        return processedEmployees;
    }

    public void setProcessedEmployees(Integer processedEmployees) {
        this.processedEmployees = processedEmployees;
    }

    public Integer getSkippedEmployees() {
        return skippedEmployees;
    }

    public void setSkippedEmployees(Integer skippedEmployees) {
        this.skippedEmployees = skippedEmployees;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }
}
