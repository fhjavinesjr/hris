package com.payroll.dtos;

/**
 * Employee information needed by the payroll computation engine.
 * Fetched in bulk from the Administrative / HR service before computation starts.
 */
public class EmployeePayrollInfoDTO {

    private String employeeNo;
    private String fullName;
    private String department;
    private Integer salaryGrade;
    private Integer salaryStep;

    /** Monthly basic salary in PHP. */
    private Double basicMonthlySalary;

    /**
     * For semi-monthly computation this equals basicMonthlySalary / 2.
     * For monthly this equals basicMonthlySalary.
     * Set by the orchestrator before passing to the engine.
     */
    private Double basicPerSalary;

    /** true = PART_TIME employee (salary and allowances halved). */
    private Boolean isPartTime = false;

    /** true = excluded from payroll in PayrollSetup. */
    private Boolean isExcludedFromPayroll = false;

    /** true = DOH employee entitled to hazard pay from grade table. */
    private Boolean isDoh = false;

    /** true = this employee should not receive hazard pay this period. */
    private Boolean noHazardPay = false;

    /** true = public holidays do not apply to this employee's schedule. */
    private Boolean noHoliday = false;

    /** Preferred PagIbig additional contribution on top of mandatory (may be 0). */
    private Double pagibigPreferred = 0.0;

    // Getters / Setters
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getSalaryGrade() { return salaryGrade; }
    public void setSalaryGrade(Integer salaryGrade) { this.salaryGrade = salaryGrade; }
    public Integer getSalaryStep() { return salaryStep; }
    public void setSalaryStep(Integer salaryStep) { this.salaryStep = salaryStep; }
    public Double getBasicMonthlySalary() { return basicMonthlySalary; }
    public void setBasicMonthlySalary(Double basicMonthlySalary) { this.basicMonthlySalary = basicMonthlySalary; }
    public Double getBasicPerSalary() { return basicPerSalary; }
    public void setBasicPerSalary(Double basicPerSalary) { this.basicPerSalary = basicPerSalary; }
    public Boolean getIsPartTime() { return isPartTime; }
    public void setIsPartTime(Boolean isPartTime) { this.isPartTime = isPartTime; }
    public Boolean getIsExcludedFromPayroll() { return isExcludedFromPayroll; }
    public void setIsExcludedFromPayroll(Boolean isExcludedFromPayroll) { this.isExcludedFromPayroll = isExcludedFromPayroll; }
    public Boolean getIsDoh() { return isDoh; }
    public void setIsDoh(Boolean isDoh) { this.isDoh = isDoh; }
    public Boolean getNoHazardPay() { return noHazardPay; }
    public void setNoHazardPay(Boolean noHazardPay) { this.noHazardPay = noHazardPay; }
    public Boolean getNoHoliday() { return noHoliday; }
    public void setNoHoliday(Boolean noHoliday) { this.noHoliday = noHoliday; }
    public Double getPagibigPreferred() { return pagibigPreferred; }
    public void setPagibigPreferred(Double pagibigPreferred) { this.pagibigPreferred = pagibigPreferred; }
}
