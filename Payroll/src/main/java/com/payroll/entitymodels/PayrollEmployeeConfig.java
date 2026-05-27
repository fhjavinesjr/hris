package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Per-employee, per-salary-period pre-computation flags.
 * Set by the payroll officer before triggering batch computation.
 *
 * Hybrid persistence: defaults are carried over from the previous period;
 * the officer can override any flag for the current run.
 */
@Entity
@Table(name = "payroll_employee_config",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_pec_employee_period",
               columnNames = {"employeeNo", "salaryPeriodKey"}),
       indexes = {
           @Index(name = "idx_pec_salary_period_key", columnList = "salaryPeriodKey"),
           @Index(name = "idx_pec_employee_no",       columnList = "employeeNo")
       })
public class PayrollEmployeeConfig implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String employeeNo;

    /** Denormalized for display / sorting in the pre-setup table. */
    @Column(length = 200)
    private String employeeName;

    @Column(length = 200)
    private String department;

    /** Composite period key, e.g. "2026-6-1". */
    @Column(nullable = false, length = 20)
    private String salaryPeriodKey;

    /**
     * Officer-level override: skip this specific employee from this payroll run.
     * Overrides the isContractual group routing set by Nature of Appointment.
     */
    @Column(nullable = false)
    private Boolean isExcludedFromPayroll = false;

    /**
     * Skip hazard pay computation for this employee this period.
     */
    @Column(nullable = false)
    private Boolean noHazardPay = false;

    /**
     * Flag this employee for special-attention display on the last page of
     * the payroll report (e.g. they have a payroll adjustment, back-pay, etc.).
     */
    @Column(nullable = false)
    private Boolean displayToLastPage = false;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ── Constructors ──────────────────────────────────────────────────────────
    public PayrollEmployeeConfig() {}

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getSalaryPeriodKey() { return salaryPeriodKey; }
    public void setSalaryPeriodKey(String salaryPeriodKey) { this.salaryPeriodKey = salaryPeriodKey; }
    public Boolean getIsExcludedFromPayroll() { return isExcludedFromPayroll; }
    public void setIsExcludedFromPayroll(Boolean isExcludedFromPayroll) { this.isExcludedFromPayroll = isExcludedFromPayroll; }
    public Boolean getNoHazardPay() { return noHazardPay; }
    public void setNoHazardPay(Boolean noHazardPay) { this.noHazardPay = noHazardPay; }
    public Boolean getDisplayToLastPage() { return displayToLastPage; }
    public void setDisplayToLastPage(Boolean displayToLastPage) { this.displayToLastPage = displayToLastPage; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
