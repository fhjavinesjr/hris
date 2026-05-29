package com.payroll.services;

import com.payroll.dtos.*;

import java.util.List;
import java.util.Optional;

public interface PayrollAdjustmentService {

    /**
     * Fetches the existing adjustment header (with lines) for an employee in a period.
     * Returns empty if no adjustment has been entered yet.
     */
    Optional<PayrollAdjustmentDTO> findAdjustment(String employeeNo, String salaryPeriodKey);

    /**
     * Computes the cascade impact of ONE new manual line without saving anything.
     * The officer uses this to preview what auto-computed lines will be added before confirming.
     *
     * @param req  The manual line the officer is about to add
     * @param salaryType e.g. "SEMI_MONTHLY" or "Monthly" — determines which WTX bracket table to use
     * @param authHeader  JWT header forwarded to Admin service for rate lookups
     */
    AdjustmentPreviewResponse previewCascade(AdjustmentPreviewRequest req,
                                              String salaryType,
                                              String authHeader);

    /**
     * Saves (or replaces) the full adjustment for an employee in a period.
     * Any previously auto-computed lines for the same header are removed and recomputed.
     * Returns the saved adjustment with computed totals.
     */
    PayrollAdjustmentDTO saveAdjustment(PayrollAdjustmentDTO dto, String authHeader);

    /**
     * Marks one active adjustment header as POSTED.
     * Only PENDING headers can be transitioned to POSTED.
     */
    PayrollAdjustmentDTO postAdjustment(Long headerId, String postedBy);

    /**
     * Deletes one manual line (and its associated auto-computed cascade lines) from an adjustment.
     * If the header has no lines left after deletion the header is also deleted.
     */
    void deleteLine(Long lineId);

    /**
     * Returns all saved versions (including SUPERSEDED) for an employee in a period,
     * newest version first. Used by the audit-trail history endpoint.
     */
    List<PayrollAdjustmentDTO> getHistoryForEmployee(String employeeNo, String salaryPeriodKey);

    /**
     * Returns a lightweight summary row per employee for a given salary period.
     * Includes originalNetPay, totalAdjustmentImpact, and adjustedNetPay.
     */
    List<AdjustmentSummaryDTO> getSummaryForPeriod(String salaryPeriodKey);

    /**
     * Returns how many active headers are still PENDING for a salary period.
     */
    long getPendingCountForPeriod(String salaryPeriodKey);
}
