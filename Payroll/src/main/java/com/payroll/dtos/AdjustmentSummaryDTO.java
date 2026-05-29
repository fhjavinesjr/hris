package com.payroll.dtos;

/**
 * Lightweight summary row for the Payroll Register view.
 * One per employee that has adjustments in the given salary period.
 */
public class AdjustmentSummaryDTO {

    private String employeeNo;
    private String salaryPeriodKey;
    private Long adjustmentHeaderId;
    private String status;                // PENDING | POSTED
    private Double originalNetPay;        // from PayrollDetail.netAmount
    private Double totalAdjustmentImpact; // net delta from all adjustment lines
    private Double adjustedNetPay;        // originalNetPay + totalAdjustmentImpact

    public AdjustmentSummaryDTO() {}

    // Getters / Setters
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getSalaryPeriodKey() { return salaryPeriodKey; }
    public void setSalaryPeriodKey(String salaryPeriodKey) { this.salaryPeriodKey = salaryPeriodKey; }
    public Long getAdjustmentHeaderId() { return adjustmentHeaderId; }
    public void setAdjustmentHeaderId(Long adjustmentHeaderId) { this.adjustmentHeaderId = adjustmentHeaderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getOriginalNetPay() { return originalNetPay; }
    public void setOriginalNetPay(Double originalNetPay) { this.originalNetPay = originalNetPay; }
    public Double getTotalAdjustmentImpact() { return totalAdjustmentImpact; }
    public void setTotalAdjustmentImpact(Double totalAdjustmentImpact) { this.totalAdjustmentImpact = totalAdjustmentImpact; }
    public Double getAdjustedNetPay() { return adjustedNetPay; }
    public void setAdjustedNetPay(Double adjustedNetPay) { this.adjustedNetPay = adjustedNetPay; }
}
