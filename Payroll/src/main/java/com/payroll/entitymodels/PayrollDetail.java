package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores computed payroll result per employee per salary period.
 * One record per (employeeNo + salaryPeriodKey).
 */
@Entity
@Table(name = "payroll_detail",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employeeNo", "salaryPeriodKey"}),
       indexes = {
           @Index(name = "idx_pd_employee_no", columnList = "employeeNo"),
           @Index(name = "idx_pd_salary_period_key", columnList = "salaryPeriodKey")
       })
public class PayrollDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Identity ──────────────────────────────────────────────────────────────
    @Column(nullable = false, length = 50)
    private String employeeNo;

    @Column(nullable = false, length = 200)
    private String employeeName;

    @Column(length = 200)
    private String department;

    @Column(nullable = false)
    private Integer salaryGrade;

    @Column(nullable = false)
    private Integer salaryStep;

    // ── Period ────────────────────────────────────────────────────────────────
    /**
     * Composite key: "YYYY-M-N" where N = nth period order (1, 2, …)
     * e.g. "2026-6-1" = June 2026, 1st period
     */
    @Column(nullable = false, length = 20)
    private String salaryPeriodKey;

    private LocalDate cutoffStartDate;
    private LocalDate cutoffEndDate;
    private LocalDate salaryDate;

    // ── Rates ─────────────────────────────────────────────────────────────────
    @Column(nullable = false)
    private Double basicPerSalary = 0.0;

    @Column(nullable = false)
    private Double salaryPerDay = 0.0;

    @Column(nullable = false)
    private Double salaryPerMinute = 0.0;

    @Column(nullable = false)
    private Integer cutoffDays = 22;   // ZCMC standard

    // ── Attendance ────────────────────────────────────────────────────────────
    private Integer workDays = 0;           // expected work days in cutoff
    private Double workDaysPresent = 0.0;
    private Double absentDays = 0.0;

    // ── Late / Undertime ──────────────────────────────────────────────────────
    private Integer lateMinutes = 0;
    private Double lateValue = 0.0;
    private Integer undertimeMinutes = 0;
    private Double undertimeValue = 0.0;

    // ── Leave ─────────────────────────────────────────────────────────────────
    /**
     * Days charged to VL credits instead of salary deduction (CSC rule).
     * Populated only when the employee has VL balance.
     */
    private Double vlDeductedDays = 0.0;
    private Double earnedLeave = 0.0;         // leave earned this period (typically 1.25)
    private Double vacationLeaveUsed = 0.0;   // approved VL taken
    private Double sickLeaveUsed = 0.0;       // approved SL taken
    private Double forceLeaveUsed = 0.0;      // CL taken
    private Double vlBalance = 0.0;           // running VL balance after this period
    private Double slBalance = 0.0;           // running SL balance after this period

    // ── Earnings / Deductions ─────────────────────────────────────────────────
    @Column(nullable = false)
    private Double actualBasic = 0.0;         // basicPerSalary – absent – late – undertime

    @Column(nullable = false)
    private Double grossAmount = 0.0;         // sum of all earnings

    @Column(nullable = false)
    private Double totalDeduction = 0.0;      // sum of all deductions

    @Column(nullable = false)
    private Double netAmount = 0.0;           // grossAmount – totalDeduction

    // ── Tax ───────────────────────────────────────────────────────────────────
    private Double taxableIncome = 0.0;
    private Double taxAmount = 0.0;
    private Double taxableIncomeToDate = 0.0;
    private Double taxToDate = 0.0;

    // ── Metadata ──────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayrollStatus status = PayrollStatus.COMPUTED;

    @Column(nullable = false)
    private LocalDateTime computedAt;

    private LocalDateTime lockedAt;

    @Column(nullable = false)
    private Boolean isLocked = false;

    /** Free-text AWOL dates string for the payslip, e.g. "Jun 3,4,5" */
    @Column(length = 500)
    private String absentParticulars;

    // ── Children ──────────────────────────────────────────────────────────────
    @OneToMany(mappedBy = "payrollDetail", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PayrollDetailEarning> earnings = new ArrayList<>();

    @OneToMany(mappedBy = "payrollDetail", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PayrollDetailDeduction> deductions = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────────────────────
    public PayrollDetail() {}

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDate getCutoffStartDate() { return cutoffStartDate; }
    public void setCutoffStartDate(LocalDate cutoffStartDate) { this.cutoffStartDate = cutoffStartDate; }
    public LocalDate getCutoffEndDate() { return cutoffEndDate; }
    public void setCutoffEndDate(LocalDate cutoffEndDate) { this.cutoffEndDate = cutoffEndDate; }
    public LocalDate getSalaryDate() { return salaryDate; }
    public void setSalaryDate(LocalDate salaryDate) { this.salaryDate = salaryDate; }
    public Double getBasicPerSalary() { return basicPerSalary; }
    public void setBasicPerSalary(Double basicPerSalary) { this.basicPerSalary = basicPerSalary; }
    public Double getSalaryPerDay() { return salaryPerDay; }
    public void setSalaryPerDay(Double salaryPerDay) { this.salaryPerDay = salaryPerDay; }
    public Double getSalaryPerMinute() { return salaryPerMinute; }
    public void setSalaryPerMinute(Double salaryPerMinute) { this.salaryPerMinute = salaryPerMinute; }
    public Integer getCutoffDays() { return cutoffDays; }
    public void setCutoffDays(Integer cutoffDays) { this.cutoffDays = cutoffDays; }
    public Integer getWorkDays() { return workDays; }
    public void setWorkDays(Integer workDays) { this.workDays = workDays; }
    public Double getWorkDaysPresent() { return workDaysPresent; }
    public void setWorkDaysPresent(Double workDaysPresent) { this.workDaysPresent = workDaysPresent; }
    public Double getAbsentDays() { return absentDays; }
    public void setAbsentDays(Double absentDays) { this.absentDays = absentDays; }
    public Integer getLateMinutes() { return lateMinutes; }
    public void setLateMinutes(Integer lateMinutes) { this.lateMinutes = lateMinutes; }
    public Double getLateValue() { return lateValue; }
    public void setLateValue(Double lateValue) { this.lateValue = lateValue; }
    public Integer getUndertimeMinutes() { return undertimeMinutes; }
    public void setUndertimeMinutes(Integer undertimeMinutes) { this.undertimeMinutes = undertimeMinutes; }
    public Double getUndertimeValue() { return undertimeValue; }
    public void setUndertimeValue(Double undertimeValue) { this.undertimeValue = undertimeValue; }
    public Double getVlDeductedDays() { return vlDeductedDays; }
    public void setVlDeductedDays(Double vlDeductedDays) { this.vlDeductedDays = vlDeductedDays; }
    public Double getEarnedLeave() { return earnedLeave; }
    public void setEarnedLeave(Double earnedLeave) { this.earnedLeave = earnedLeave; }
    public Double getVacationLeaveUsed() { return vacationLeaveUsed; }
    public void setVacationLeaveUsed(Double vacationLeaveUsed) { this.vacationLeaveUsed = vacationLeaveUsed; }
    public Double getSickLeaveUsed() { return sickLeaveUsed; }
    public void setSickLeaveUsed(Double sickLeaveUsed) { this.sickLeaveUsed = sickLeaveUsed; }
    public Double getForceLeaveUsed() { return forceLeaveUsed; }
    public void setForceLeaveUsed(Double forceLeaveUsed) { this.forceLeaveUsed = forceLeaveUsed; }
    public Double getVlBalance() { return vlBalance; }
    public void setVlBalance(Double vlBalance) { this.vlBalance = vlBalance; }
    public Double getSlBalance() { return slBalance; }
    public void setSlBalance(Double slBalance) { this.slBalance = slBalance; }
    public Double getActualBasic() { return actualBasic; }
    public void setActualBasic(Double actualBasic) { this.actualBasic = actualBasic; }
    public Double getGrossAmount() { return grossAmount; }
    public void setGrossAmount(Double grossAmount) { this.grossAmount = grossAmount; }
    public Double getTotalDeduction() { return totalDeduction; }
    public void setTotalDeduction(Double totalDeduction) { this.totalDeduction = totalDeduction; }
    public Double getNetAmount() { return netAmount; }
    public void setNetAmount(Double netAmount) { this.netAmount = netAmount; }
    public Double getTaxableIncome() { return taxableIncome; }
    public void setTaxableIncome(Double taxableIncome) { this.taxableIncome = taxableIncome; }
    public Double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Double taxAmount) { this.taxAmount = taxAmount; }
    public Double getTaxableIncomeToDate() { return taxableIncomeToDate; }
    public void setTaxableIncomeToDate(Double taxableIncomeToDate) { this.taxableIncomeToDate = taxableIncomeToDate; }
    public Double getTaxToDate() { return taxToDate; }
    public void setTaxToDate(Double taxToDate) { this.taxToDate = taxToDate; }
    public PayrollStatus getStatus() { return status; }
    public void setStatus(PayrollStatus status) { this.status = status; }
    public LocalDateTime getComputedAt() { return computedAt; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }
    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }
    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }
    public String getAbsentParticulars() { return absentParticulars; }
    public void setAbsentParticulars(String absentParticulars) { this.absentParticulars = absentParticulars; }
    public List<PayrollDetailEarning> getEarnings() { return earnings; }
    public void setEarnings(List<PayrollDetailEarning> earnings) { this.earnings = earnings; }
    public List<PayrollDetailDeduction> getDeductions() { return deductions; }
    public void setDeductions(List<PayrollDetailDeduction> deductions) { this.deductions = deductions; }
}
