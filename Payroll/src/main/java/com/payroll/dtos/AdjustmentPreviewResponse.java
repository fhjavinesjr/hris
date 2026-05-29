package com.payroll.dtos;

import java.util.List;

/**
 * Response from the cascade-preview endpoint.
 *
 * Contains the manually entered line PLUS any auto-computed cascade lines.
 * The officer sees this before confirming and saving.
 */
public class AdjustmentPreviewResponse {

    /** The manual line echoed back. */
    private PayrollAdjustmentLineDTO manualLine;

    /** System-computed cascade lines (GSIS, PHIC, WTX deltas). Empty for non-taxable / deduction entries. */
    private List<PayrollAdjustmentLineDTO> cascadeLines;

    // ── Summary figures ────────────────────────────────────────────────────────
    /** Original net pay from the PayrollDetail record. */
    private Double originalNetPay;

    /** Sum of all adjustment lines (manual + cascade) that affect net pay. */
    private Double totalAdjustmentImpact;

    /** originalNetPay + totalAdjustmentImpact. */
    private Double projectedNetPay;

    // Getters / Setters
    public PayrollAdjustmentLineDTO getManualLine() { return manualLine; }
    public void setManualLine(PayrollAdjustmentLineDTO manualLine) { this.manualLine = manualLine; }
    public List<PayrollAdjustmentLineDTO> getCascadeLines() { return cascadeLines; }
    public void setCascadeLines(List<PayrollAdjustmentLineDTO> cascadeLines) { this.cascadeLines = cascadeLines; }
    public Double getOriginalNetPay() { return originalNetPay; }
    public void setOriginalNetPay(Double originalNetPay) { this.originalNetPay = originalNetPay; }
    public Double getTotalAdjustmentImpact() { return totalAdjustmentImpact; }
    public void setTotalAdjustmentImpact(Double totalAdjustmentImpact) { this.totalAdjustmentImpact = totalAdjustmentImpact; }
    public Double getProjectedNetPay() { return projectedNetPay; }
    public void setProjectedNetPay(Double projectedNetPay) { this.projectedNetPay = projectedNetPay; }
}
