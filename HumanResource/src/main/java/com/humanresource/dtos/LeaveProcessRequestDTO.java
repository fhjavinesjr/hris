package com.humanresource.dtos;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Request body for the Leave Process endpoint.
 * HR officer selects a period + scope and triggers computation for all matching employees.
 */
public class LeaveProcessRequestDTO implements Serializable {

    /** ID of the salary period setting to use for cut-off date resolution */
    private Long salaryPeriodSettingId;

    /** The resolved cut-off start date (from SalaryPeriodResolver) */
    private LocalDate cutoffStartDate;

    /** The resolved cut-off end date */
    private LocalDate cutoffEndDate;

    /** Scope: ALL | EMPLOYEE */
    private String scope;

    /**
     * When scope = EMPLOYEE, the specific employee to process.
     * When scope = ALL, this is ignored.
     */
    private Long employeeId;

    /**
     * The HR officer who is triggering the process (for audit trail).
     * Typically set from the JWT token on the server side.
     */
    private Long processedById;

    public LeaveProcessRequestDTO() {
    }

    public Long getSalaryPeriodSettingId() { return salaryPeriodSettingId; }
    public void setSalaryPeriodSettingId(Long salaryPeriodSettingId) { this.salaryPeriodSettingId = salaryPeriodSettingId; }

    public LocalDate getCutoffStartDate() { return cutoffStartDate; }
    public void setCutoffStartDate(LocalDate cutoffStartDate) { this.cutoffStartDate = cutoffStartDate; }

    public LocalDate getCutoffEndDate() { return cutoffEndDate; }
    public void setCutoffEndDate(LocalDate cutoffEndDate) { this.cutoffEndDate = cutoffEndDate; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getProcessedById() { return processedById; }
    public void setProcessedById(Long processedById) { this.processedById = processedById; }
}
