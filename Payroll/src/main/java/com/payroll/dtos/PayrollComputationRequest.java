package com.payroll.dtos;

import java.time.LocalDate;
import java.util.List;

/**
 * Request to trigger a batch payroll computation.
 * Sent from the admin UI to POST /api/payroll-computation/batch.
 */
public class PayrollComputationRequest {

    /** Salary period identifier, e.g. "2026-6-1" (year-month-nthPeriod). */
    private String salaryPeriodKey;

    /** Salary type from SalaryPeriodSetting, e.g. "SEMI_MONTHLY", "MONTHLY". */
    private String salaryType;

    /** First day of the cutoff window. */
    private LocalDate cutoffStartDate;

    /** Last day of the cutoff window (used to pull DTR and leave data). */
    private LocalDate cutoffEndDate;

    /**
     * Official salary release date.
     * Used to determine applicable deduction rules and prior payroll lookup.
     */
    private LocalDate salaryDate;

    /**
     * Fixed number of work days used for rate calculations.
     * Null means: read from Administrative PayrollSettings. Falls back to 22 if unavailable.
     */
    private Integer cutoffDays = null;

    /**
     * Optional: restrict computation to a specific department code.
     * If null, all active departments are processed.
     */
    private String departmentCode;

    /**
     * Optional: compute for a single employee only (for re-computation).
     * If null, all eligible employees are processed.
     */
    private String employeeNo;

    /**
     * If true, lock the payroll records immediately after computation.
     * Locked records cannot be recomputed without explicit unlock.
     */
    private Boolean lockAfterCompute = false;

    /**
     * If true, also compute employees flagged as "excluded from payroll"
     * (e.g. for special without-pay reports).
     */
    private Boolean includeExcluded = false;

    /**
     * If true, compute ONLY employees flagged as "excluded from payroll"
     * (i.e. Contractual / COS / Job Order only). Takes effect only when
     * {@link #includeExcluded} is also true.
     */
    private Boolean excludedOnly = false;

    /**
     * Optional allowlist of employee numbers to include in this computation run.
     * When non-null and non-empty, only employees in this list are processed.
     * Null or empty means process all eligible employees (default).
     */
    private List<String> selectedEmployeeNos;

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public String getSalaryPeriodKey() { return salaryPeriodKey; }
    public void setSalaryPeriodKey(String salaryPeriodKey) { this.salaryPeriodKey = salaryPeriodKey; }
    public String getSalaryType() { return salaryType; }
    public void setSalaryType(String salaryType) { this.salaryType = salaryType; }
    public LocalDate getCutoffStartDate() { return cutoffStartDate; }
    public void setCutoffStartDate(LocalDate cutoffStartDate) { this.cutoffStartDate = cutoffStartDate; }
    public LocalDate getCutoffEndDate() { return cutoffEndDate; }
    public void setCutoffEndDate(LocalDate cutoffEndDate) { this.cutoffEndDate = cutoffEndDate; }
    public LocalDate getSalaryDate() { return salaryDate; }
    public void setSalaryDate(LocalDate salaryDate) { this.salaryDate = salaryDate; }
    public Integer getCutoffDays() { return cutoffDays; }
    public void setCutoffDays(Integer cutoffDays) { this.cutoffDays = cutoffDays; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public Boolean getLockAfterCompute() { return lockAfterCompute; }
    public void setLockAfterCompute(Boolean lockAfterCompute) { this.lockAfterCompute = lockAfterCompute; }
    public Boolean getIncludeExcluded() { return includeExcluded; }
    public void setIncludeExcluded(Boolean includeExcluded) { this.includeExcluded = includeExcluded; }
    public Boolean getExcludedOnly() { return excludedOnly; }
    public void setExcludedOnly(Boolean excludedOnly) { this.excludedOnly = excludedOnly; }
    public List<String> getSelectedEmployeeNos() { return selectedEmployeeNos; }
    public void setSelectedEmployeeNos(List<String> selectedEmployeeNos) { this.selectedEmployeeNos = selectedEmployeeNos; }
}
