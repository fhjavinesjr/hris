package com.payroll.dtos;

/**
 * Immediate response after triggering a batch payroll computation.
 * The client uses {@link #getJobId()} to poll for progress via
 * GET /api/payroll-computation/status/{jobId}.
 */
public class PayrollComputationResponse {

    private String jobId;
    private String message;
    private String salaryPeriodKey;

    public PayrollComputationResponse() {}

    public PayrollComputationResponse(String jobId, String salaryPeriodKey) {
        this.jobId = jobId;
        this.salaryPeriodKey = salaryPeriodKey;
        this.message = "Payroll computation job started. Poll /api/payroll-computation/status/" + jobId + " for progress.";
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSalaryPeriodKey() { return salaryPeriodKey; }
    public void setSalaryPeriodKey(String salaryPeriodKey) { this.salaryPeriodKey = salaryPeriodKey; }
}
