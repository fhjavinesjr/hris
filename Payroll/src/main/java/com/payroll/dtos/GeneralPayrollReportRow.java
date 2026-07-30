package com.payroll.dtos;

import java.sql.Date;

/**
 * Provider-neutral row consumed by the General Payroll Jasper template.
 */
public class GeneralPayrollReportRow {

    private Integer rowNo;
    private String employeeNo;
    private String employeeName;
    private String department;
    private Integer salaryGrade;
    private Integer salaryStep;
    private String salaryPeriodKey;
    private Date cutoffStartDate;
    private Date cutoffEndDate;
    private Date salaryDate;
    private Double actualBasic;
    private String earningBreakdown;
    private Double grossAmount;
    private String deductionBreakdown;
    private Double totalDeduction;
    private Double netAmount;
    private Double grandActualBasic;
    private Double grandGrossAmount;
    private Double grandTotalDeduction;
    private Double grandNetAmount;
    private String grandEarningsBreakdown;
    private String grandDeductionsBreakdown;
    private String reportMode;

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getSalaryGrade() {
        return salaryGrade;
    }

    public void setSalaryGrade(Integer salaryGrade) {
        this.salaryGrade = salaryGrade;
    }

    public Integer getSalaryStep() {
        return salaryStep;
    }

    public void setSalaryStep(Integer salaryStep) {
        this.salaryStep = salaryStep;
    }

    public String getSalaryPeriodKey() {
        return salaryPeriodKey;
    }

    public void setSalaryPeriodKey(String salaryPeriodKey) {
        this.salaryPeriodKey = salaryPeriodKey;
    }

    public Date getCutoffStartDate() {
        return cutoffStartDate;
    }

    public void setCutoffStartDate(Date cutoffStartDate) {
        this.cutoffStartDate = cutoffStartDate;
    }

    public Date getCutoffEndDate() {
        return cutoffEndDate;
    }

    public void setCutoffEndDate(Date cutoffEndDate) {
        this.cutoffEndDate = cutoffEndDate;
    }

    public Date getSalaryDate() {
        return salaryDate;
    }

    public void setSalaryDate(Date salaryDate) {
        this.salaryDate = salaryDate;
    }

    public Double getActualBasic() {
        return actualBasic;
    }

    public void setActualBasic(Double actualBasic) {
        this.actualBasic = actualBasic;
    }

    public String getEarningBreakdown() {
        return earningBreakdown;
    }

    public void setEarningBreakdown(String earningBreakdown) {
        this.earningBreakdown = earningBreakdown;
    }

    public Double getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(Double grossAmount) {
        this.grossAmount = grossAmount;
    }

    public String getDeductionBreakdown() {
        return deductionBreakdown;
    }

    public void setDeductionBreakdown(String deductionBreakdown) {
        this.deductionBreakdown = deductionBreakdown;
    }

    public Double getTotalDeduction() {
        return totalDeduction;
    }

    public void setTotalDeduction(Double totalDeduction) {
        this.totalDeduction = totalDeduction;
    }

    public Double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(Double netAmount) {
        this.netAmount = netAmount;
    }

    public Double getGrandActualBasic() {
        return grandActualBasic;
    }

    public void setGrandActualBasic(Double grandActualBasic) {
        this.grandActualBasic = grandActualBasic;
    }

    public Double getGrandGrossAmount() {
        return grandGrossAmount;
    }

    public void setGrandGrossAmount(Double grandGrossAmount) {
        this.grandGrossAmount = grandGrossAmount;
    }

    public Double getGrandTotalDeduction() {
        return grandTotalDeduction;
    }

    public void setGrandTotalDeduction(Double grandTotalDeduction) {
        this.grandTotalDeduction = grandTotalDeduction;
    }

    public Double getGrandNetAmount() {
        return grandNetAmount;
    }

    public void setGrandNetAmount(Double grandNetAmount) {
        this.grandNetAmount = grandNetAmount;
    }

    public String getGrandEarningsBreakdown() {
        return grandEarningsBreakdown;
    }

    public void setGrandEarningsBreakdown(String grandEarningsBreakdown) {
        this.grandEarningsBreakdown = grandEarningsBreakdown;
    }

    public String getGrandDeductionsBreakdown() {
        return grandDeductionsBreakdown;
    }

    public void setGrandDeductionsBreakdown(String grandDeductionsBreakdown) {
        this.grandDeductionsBreakdown = grandDeductionsBreakdown;
    }

    public String getReportMode() {
        return reportMode;
    }

    public void setReportMode(String reportMode) {
        this.reportMode = reportMode;
    }
}
