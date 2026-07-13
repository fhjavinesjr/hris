package com.humanresource.dtos;

/**
 * Payroll-relevant snapshot of an employee, returned by
 * GET /api/employee/payroll-info/bulk.
 *
 * Field names intentionally mirror PayrollBatchServiceImpl's EmployeePayrollInfoDTO
 * so Jackson deserialises them without custom mapping on the Payroll side.
 */
public class EmployeePayrollInfoResponse {

    private String employeeNo;
    private String fullName;
    private String department;
    private Integer salaryGrade;
    private Integer salaryStep;
    private Double basicMonthlySalary;

    /** Daily rate from the active employee appointment. Used for Contractual / COS / Job Order payroll. */
    private Double salaryPerDay;

    /** Derived from natureofappointment.isContractual of the employee's active appointment. */
    private Boolean isExcludedFromPayroll = false;

    private Boolean isPartTime   = false;
    private Boolean isDoh        = false;
    private Boolean noHazardPay  = false;
    private Boolean noHoliday    = false;
    private Double  pagibigPreferred = 0.0;

    public EmployeePayrollInfoResponse() {}

    public String getEmployeeNo()             { return employeeNo; }
    public void   setEmployeeNo(String v)     { this.employeeNo = v; }

    public String getFullName()               { return fullName; }
    public void   setFullName(String v)       { this.fullName = v; }

    public String getDepartment()             { return department; }
    public void   setDepartment(String v)     { this.department = v; }

    public Integer getSalaryGrade()           { return salaryGrade; }
    public void    setSalaryGrade(Integer v)  { this.salaryGrade = v; }

    public Integer getSalaryStep()            { return salaryStep; }
    public void    setSalaryStep(Integer v)   { this.salaryStep = v; }

    public Double getBasicMonthlySalary()         { return basicMonthlySalary; }
    public void   setBasicMonthlySalary(Double v) { this.basicMonthlySalary = v; }

    public Double getSalaryPerDay()               { return salaryPerDay; }
    public void   setSalaryPerDay(Double v)       { this.salaryPerDay = v; }

    public Boolean getIsExcludedFromPayroll()         { return isExcludedFromPayroll; }
    public void    setIsExcludedFromPayroll(Boolean v){ this.isExcludedFromPayroll = v; }

    public Boolean getIsPartTime()            { return isPartTime; }
    public void    setIsPartTime(Boolean v)   { this.isPartTime = v; }

    public Boolean getIsDoh()                 { return isDoh; }
    public void    setIsDoh(Boolean v)        { this.isDoh = v; }

    public Boolean getNoHazardPay()           { return noHazardPay; }
    public void    setNoHazardPay(Boolean v)  { this.noHazardPay = v; }

    public Boolean getNoHoliday()             { return noHoliday; }
    public void    setNoHoliday(Boolean v)    { this.noHoliday = v; }

    public Double getPagibigPreferred()           { return pagibigPreferred; }
    public void   setPagibigPreferred(Double v)   { this.pagibigPreferred = v; }
}
