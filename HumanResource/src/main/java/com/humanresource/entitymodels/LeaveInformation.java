package com.humanresource.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One row per employee per salary period.
 * This is the Leave Ledger (CSC Form 48 equivalent).
 * Computed by LeaveProcessService from DTR, LeaveApplication, EarningLeave, and DayEquivalent tables.
 */
@Entity
@Table(name = "leave_information",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employeeId", "cutoffStartDate", "cutoffEndDate"}))
public class LeaveInformation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leaveInformationId")
    private Long leaveInformationId;

    @Column(name = "employeeId", nullable = false)
    private Long employeeId;

    @Column(name = "salaryPeriodSettingId")
    private Long salaryPeriodSettingId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "cutoffStartDate", nullable = false)
    private LocalDate cutoffStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "cutoffEndDate", nullable = false)
    private LocalDate cutoffEndDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "processDate")
    private LocalDateTime processDate;

    @Column(name = "processedById")
    private Long processedById;

    // ── Leave Earned ───────────────────────────────────
    @Column(name = "earnedSl")
    private Double earnedSl;

    @Column(name = "earnedVl")
    private Double earnedVl;

    // ── Leave Used (approved leaves within the period) ─
    @Column(name = "sickLeaveUsed")
    private Double sickLeaveUsed;

    @Column(name = "vacationLeaveUsed")
    private Double vacationLeaveUsed;

    // ── Leave Without Pay ──────────────────────────────
    @Column(name = "leaveWithoutPaySl")
    private Double leaveWithoutPaySl;

    @Column(name = "leaveWithoutPayVl")
    private Double leaveWithoutPayVl;

    // ── Previous Balances (carried forward from last period) ─
    @Column(name = "previousSickLeaveBalance")
    private Double previousSickLeaveBalance;

    @Column(name = "previousVacationLeaveBalance")
    private Double previousVacationLeaveBalance;

    // ── Computed New Balances ──────────────────────────
    @Column(name = "sickLeaveBalance")
    private Double sickLeaveBalance;

    @Column(name = "vacationLeaveBalance")
    private Double vacationLeaveBalance;

    // ── Late & Undertime (Day Equivalent deduction) ────
    @Column(name = "lateUndertimeMinutes")
    private Integer lateUndertimeMinutes;

    @Column(name = "lateUndertimeEquivalent")
    private Double lateUndertimeEquivalent;

    @Column(name = "lateCount")
    private Integer lateCount;

    @Column(name = "undertimeCount")
    private Integer undertimeCount;

    // ── Absences ───────────────────────────────────────
    @Column(name = "absentCount")
    private Double absentCount;

    // ── Particulars (human-readable summary) ──────────
    @Column(name = "leaveParticulars", length = 2000)
    private String leaveParticulars;

    // ── Control Flags ──────────────────────────────────
    @Column(name = "isBegBalance", nullable = false)
    private Boolean isBegBalance = false;

    @Column(name = "isLocked", nullable = false)
    private Boolean isLocked = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public LeaveInformation() {
    }

    // ── Getters & Setters ──────────────────────────────

    public Long getLeaveInformationId() { return leaveInformationId; }
    public void setLeaveInformationId(Long leaveInformationId) { this.leaveInformationId = leaveInformationId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getSalaryPeriodSettingId() { return salaryPeriodSettingId; }
    public void setSalaryPeriodSettingId(Long salaryPeriodSettingId) { this.salaryPeriodSettingId = salaryPeriodSettingId; }

    public LocalDate getCutoffStartDate() { return cutoffStartDate; }
    public void setCutoffStartDate(LocalDate cutoffStartDate) { this.cutoffStartDate = cutoffStartDate; }

    public LocalDate getCutoffEndDate() { return cutoffEndDate; }
    public void setCutoffEndDate(LocalDate cutoffEndDate) { this.cutoffEndDate = cutoffEndDate; }

    public LocalDateTime getProcessDate() { return processDate; }
    public void setProcessDate(LocalDateTime processDate) { this.processDate = processDate; }

    public Long getProcessedById() { return processedById; }
    public void setProcessedById(Long processedById) { this.processedById = processedById; }

    public Double getEarnedSl() { return earnedSl; }
    public void setEarnedSl(Double earnedSl) { this.earnedSl = earnedSl; }

    public Double getEarnedVl() { return earnedVl; }
    public void setEarnedVl(Double earnedVl) { this.earnedVl = earnedVl; }

    public Double getSickLeaveUsed() { return sickLeaveUsed; }
    public void setSickLeaveUsed(Double sickLeaveUsed) { this.sickLeaveUsed = sickLeaveUsed; }

    public Double getVacationLeaveUsed() { return vacationLeaveUsed; }
    public void setVacationLeaveUsed(Double vacationLeaveUsed) { this.vacationLeaveUsed = vacationLeaveUsed; }

    public Double getLeaveWithoutPaySl() { return leaveWithoutPaySl; }
    public void setLeaveWithoutPaySl(Double leaveWithoutPaySl) { this.leaveWithoutPaySl = leaveWithoutPaySl; }

    public Double getLeaveWithoutPayVl() { return leaveWithoutPayVl; }
    public void setLeaveWithoutPayVl(Double leaveWithoutPayVl) { this.leaveWithoutPayVl = leaveWithoutPayVl; }

    public Double getPreviousSickLeaveBalance() { return previousSickLeaveBalance; }
    public void setPreviousSickLeaveBalance(Double previousSickLeaveBalance) { this.previousSickLeaveBalance = previousSickLeaveBalance; }

    public Double getPreviousVacationLeaveBalance() { return previousVacationLeaveBalance; }
    public void setPreviousVacationLeaveBalance(Double previousVacationLeaveBalance) { this.previousVacationLeaveBalance = previousVacationLeaveBalance; }

    public Double getSickLeaveBalance() { return sickLeaveBalance; }
    public void setSickLeaveBalance(Double sickLeaveBalance) { this.sickLeaveBalance = sickLeaveBalance; }

    public Double getVacationLeaveBalance() { return vacationLeaveBalance; }
    public void setVacationLeaveBalance(Double vacationLeaveBalance) { this.vacationLeaveBalance = vacationLeaveBalance; }

    public Integer getLateUndertimeMinutes() { return lateUndertimeMinutes; }
    public void setLateUndertimeMinutes(Integer lateUndertimeMinutes) { this.lateUndertimeMinutes = lateUndertimeMinutes; }

    public Double getLateUndertimeEquivalent() { return lateUndertimeEquivalent; }
    public void setLateUndertimeEquivalent(Double lateUndertimeEquivalent) { this.lateUndertimeEquivalent = lateUndertimeEquivalent; }

    public Integer getLateCount() { return lateCount; }
    public void setLateCount(Integer lateCount) { this.lateCount = lateCount; }

    public Integer getUndertimeCount() { return undertimeCount; }
    public void setUndertimeCount(Integer undertimeCount) { this.undertimeCount = undertimeCount; }

    public Double getAbsentCount() { return absentCount; }
    public void setAbsentCount(Double absentCount) { this.absentCount = absentCount; }

    public String getLeaveParticulars() { return leaveParticulars; }
    public void setLeaveParticulars(String leaveParticulars) { this.leaveParticulars = leaveParticulars; }

    public Boolean getIsBegBalance() { return isBegBalance; }
    public void setIsBegBalance(Boolean isBegBalance) { this.isBegBalance = isBegBalance; }

    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
