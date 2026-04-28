package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveInformationDTO implements Serializable {

    private Long leaveInformationId;
    private Long employeeId;
    private Long salaryPeriodSettingId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate cutoffStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate cutoffEndDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime processDate;

    private Long processedById;

    private Double earnedSl;
    private Double earnedVl;
    private Double sickLeaveUsed;
    private Double vacationLeaveUsed;
    private Double leaveWithoutPaySl;
    private Double leaveWithoutPayVl;
    private Double previousSickLeaveBalance;
    private Double previousVacationLeaveBalance;
    private Double sickLeaveBalance;
    private Double vacationLeaveBalance;
    private Integer lateUndertimeMinutes;
    private Double lateUndertimeEquivalent;
    private Integer lateCount;
    private Integer undertimeCount;
    private Double absentCount;
    private String leaveParticulars;
    private Boolean isBegBalance;
    private Boolean isLocked;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime updatedAt;

    // Optional enriched fields (for UI display)
    private String employeeName;
    private String employeeNo;

    public LeaveInformationDTO() {
    }

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

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
}
