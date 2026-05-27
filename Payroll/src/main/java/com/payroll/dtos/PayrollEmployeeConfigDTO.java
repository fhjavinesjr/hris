package com.payroll.dtos;

/**
 * Per-employee pre-setup config for a salary period.
 * Returned by GET /api/payroll-computation/employee-config
 * and accepted by POST /api/payroll-computation/employee-config/bulk-save.
 */
public class PayrollEmployeeConfigDTO {

    private String employeeNo;
    private String employeeName;
    private String department;
    private Integer salaryGrade;
    private Integer salaryStep;
    private String salaryPeriodKey;
    private Boolean isExcludedFromPayroll = false;
    private Boolean noHazardPay = false;
    private Boolean displayToLastPage = false;

    // ── Getters / Setters ──────────────────────────────────────────────────────
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getSalaryGrade() { return salaryGrade; }
    public void setSalaryGrade(Integer salaryGrade) { this.salaryGrade = salaryGrade; }
    public Integer getSalaryStep() { return salaryStep; }
    public void setSalaryStep(Integer salaryStep) { this.salaryStep = salaryStep; }
    public String getSalaryPeriodKey() { return salaryPeriodKey; }
    public void setSalaryPeriodKey(String salaryPeriodKey) { this.salaryPeriodKey = salaryPeriodKey; }
    public Boolean getIsExcludedFromPayroll() { return isExcludedFromPayroll; }
    public void setIsExcludedFromPayroll(Boolean isExcludedFromPayroll) { this.isExcludedFromPayroll = isExcludedFromPayroll; }
    public Boolean getNoHazardPay() { return noHazardPay; }
    public void setNoHazardPay(Boolean noHazardPay) { this.noHazardPay = noHazardPay; }
    public Boolean getDisplayToLastPage() { return displayToLastPage; }
    public void setDisplayToLastPage(Boolean displayToLastPage) { this.displayToLastPage = displayToLastPage; }
}
