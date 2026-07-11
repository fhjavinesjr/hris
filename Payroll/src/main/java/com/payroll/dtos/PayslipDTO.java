package com.payroll.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PayslipDTO {
    private Long payrollDetailId;

    private String employeeNo;
    private String employeeName;
    private String department;
    private Integer salaryGrade;
    private Integer salaryStep;

    private String salaryPeriodKey;
    private LocalDate cutoffStartDate;
    private LocalDate cutoffEndDate;
    private LocalDate salaryDate;

    private Double basicPerSalary;
    private Double salaryPerDay;
    private Double salaryPerMinute;
    private Integer cutoffDays;

    private Integer workDays;
    private Double workDaysPresent;
    private Double absentDays;
    private String absentParticulars;

    private Integer lateMinutes;
    private Double lateValue;
    private Integer undertimeMinutes;
    private Double undertimeValue;

    private Double earnedLeave;
    private Double vacationLeaveUsed;
    private Double sickLeaveUsed;
    private Double forceLeaveUsed;
    private Double vlDeductedDays;
    private Double vlBalance;
    private Double slBalance;

    private Double actualBasic;
    private Double grossAmount;
    private Double totalDeduction;
    private Double netAmount;

    private Double taxableIncome;
    private Double taxAmount;
    private Double taxableIncomeToDate;
    private Double taxToDate;

    private String status;
    private Boolean locked;
    private LocalDateTime computedAt;
    private LocalDateTime lockedAt;

    private List<PayslipLineDTO> earnings = new ArrayList<>();
    private List<PayslipLineDTO> deductions = new ArrayList<>();
    private List<PayslipLineDTO> adjustments = new ArrayList<>();

    private Double adjustmentEarnings = 0.0;
    private Double adjustmentDeductions = 0.0;
    private Double adjustmentNet = 0.0;
    private Double adjustedGrossAmount = 0.0;
    private Double adjustedTotalDeduction = 0.0;
    private Double adjustedNetAmount = 0.0;

    public Long getPayrollDetailId() { return payrollDetailId; }
    public void setPayrollDetailId(Long payrollDetailId) { this.payrollDetailId = payrollDetailId; }

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

    public String getAbsentParticulars() { return absentParticulars; }
    public void setAbsentParticulars(String absentParticulars) { this.absentParticulars = absentParticulars; }

    public Integer getLateMinutes() { return lateMinutes; }
    public void setLateMinutes(Integer lateMinutes) { this.lateMinutes = lateMinutes; }

    public Double getLateValue() { return lateValue; }
    public void setLateValue(Double lateValue) { this.lateValue = lateValue; }

    public Integer getUndertimeMinutes() { return undertimeMinutes; }
    public void setUndertimeMinutes(Integer undertimeMinutes) { this.undertimeMinutes = undertimeMinutes; }

    public Double getUndertimeValue() { return undertimeValue; }
    public void setUndertimeValue(Double undertimeValue) { this.undertimeValue = undertimeValue; }

    public Double getEarnedLeave() { return earnedLeave; }
    public void setEarnedLeave(Double earnedLeave) { this.earnedLeave = earnedLeave; }

    public Double getVacationLeaveUsed() { return vacationLeaveUsed; }
    public void setVacationLeaveUsed(Double vacationLeaveUsed) { this.vacationLeaveUsed = vacationLeaveUsed; }

    public Double getSickLeaveUsed() { return sickLeaveUsed; }
    public void setSickLeaveUsed(Double sickLeaveUsed) { this.sickLeaveUsed = sickLeaveUsed; }

    public Double getForceLeaveUsed() { return forceLeaveUsed; }
    public void setForceLeaveUsed(Double forceLeaveUsed) { this.forceLeaveUsed = forceLeaveUsed; }

    public Double getVlDeductedDays() { return vlDeductedDays; }
    public void setVlDeductedDays(Double vlDeductedDays) { this.vlDeductedDays = vlDeductedDays; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getLocked() { return locked; }
    public void setLocked(Boolean locked) { this.locked = locked; }

    public LocalDateTime getComputedAt() { return computedAt; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }

    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }

    public List<PayslipLineDTO> getEarnings() { return earnings; }
    public void setEarnings(List<PayslipLineDTO> earnings) { this.earnings = earnings; }

    public List<PayslipLineDTO> getDeductions() { return deductions; }
    public void setDeductions(List<PayslipLineDTO> deductions) { this.deductions = deductions; }

    public List<PayslipLineDTO> getAdjustments() { return adjustments; }
    public void setAdjustments(List<PayslipLineDTO> adjustments) { this.adjustments = adjustments; }

    public Double getAdjustmentEarnings() { return adjustmentEarnings; }
    public void setAdjustmentEarnings(Double adjustmentEarnings) { this.adjustmentEarnings = adjustmentEarnings; }

    public Double getAdjustmentDeductions() { return adjustmentDeductions; }
    public void setAdjustmentDeductions(Double adjustmentDeductions) { this.adjustmentDeductions = adjustmentDeductions; }

    public Double getAdjustmentNet() { return adjustmentNet; }
    public void setAdjustmentNet(Double adjustmentNet) { this.adjustmentNet = adjustmentNet; }

    public Double getAdjustedGrossAmount() { return adjustedGrossAmount; }
    public void setAdjustedGrossAmount(Double adjustedGrossAmount) { this.adjustedGrossAmount = adjustedGrossAmount; }

    public Double getAdjustedTotalDeduction() { return adjustedTotalDeduction; }
    public void setAdjustedTotalDeduction(Double adjustedTotalDeduction) { this.adjustedTotalDeduction = adjustedTotalDeduction; }

    public Double getAdjustedNetAmount() { return adjustedNetAmount; }
    public void setAdjustedNetAmount(Double adjustedNetAmount) { this.adjustedNetAmount = adjustedNetAmount; }
}
