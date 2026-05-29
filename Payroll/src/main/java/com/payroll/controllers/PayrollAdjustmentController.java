package com.payroll.controllers;

import com.payroll.dtos.*;
import com.payroll.services.PayrollAdjustmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for payroll adjustments.
 *
 * All endpoints operate on the "adjustment layer" — original computed PayrollDetail
 * records are NEVER modified by these endpoints.
 *
 * Endpoints:
 *   GET  /api/payroll-adjustment/{employeeNo}?period=
 *       → Returns the existing adjustment for an employee in a period (or 404).
 *
 *   POST /api/payroll-adjustment/preview?salaryType=
 *       → Returns cascade lines for a single manual entry without saving.
 *
 *   POST /api/payroll-adjustment/save
 *       → Persists the full adjustment (header + lines) for an employee + period.
 *
 *   DELETE /api/payroll-adjustment/line/{lineId}
 *       → Deletes one manual line (and its cascade lines if any).
 *
 *   GET  /api/payroll-adjustment/summary/{salaryPeriodKey}
 *       → Returns one summary row per employee that has adjustments in the period.
 */
@RestController
@RequestMapping("/api/payroll-adjustment")
public class PayrollAdjustmentController {

    private final PayrollAdjustmentService adjustmentService;

    public PayrollAdjustmentController(PayrollAdjustmentService adjustmentService) {
        this.adjustmentService = adjustmentService;
    }

    /**
     * Returns the existing adjustment header + lines for an employee in a salary period.
     *
     * @param employeeNo      e.g. "EMP-00001"
     * @param period          e.g. "2026-6-1"
     */
    @GetMapping("/{employeeNo}")
    public ResponseEntity<PayrollAdjustmentDTO> getAdjustment(
            @PathVariable String employeeNo,
            @RequestParam String period) {
        return adjustmentService.findAdjustment(employeeNo, period)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Previews the cascade impact of one manual line without saving.
     *
     * @param salaryType  e.g. "SEMI_MONTHLY" or "Monthly" — controls which WTX bracket to use
     * @param authHeader  JWT forwarded to the Administrative service for rate lookups
     */
    @PostMapping("/preview")
    public ResponseEntity<AdjustmentPreviewResponse> preview(
            @RequestBody AdjustmentPreviewRequest req,
            @RequestParam(defaultValue = "SEMI_MONTHLY") String salaryType,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        AdjustmentPreviewResponse resp = adjustmentService.previewCascade(req, salaryType, authHeader);
        return ResponseEntity.ok(resp);
    }

    /**
     * Saves (or replaces) the full adjustment for one employee in one period.
     * Auto-computed cascade lines in the DTO are ignored and recomputed server-side.
     */
    @PostMapping("/save")
    public ResponseEntity<PayrollAdjustmentDTO> save(
            @RequestBody PayrollAdjustmentDTO dto,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        PayrollAdjustmentDTO saved = adjustmentService.saveAdjustment(dto, authHeader);
        return ResponseEntity.ok(saved);
    }

    /**
     * Marks an existing active adjustment as POSTED.
     */
    @PatchMapping("/{headerId}/post")
    public ResponseEntity<PayrollAdjustmentDTO> postAdjustment(
            @PathVariable Long headerId,
            @RequestParam(required = false) String postedBy) {
        PayrollAdjustmentDTO posted = adjustmentService.postAdjustment(headerId, postedBy);
        return ResponseEntity.ok(posted);
    }

    /**
     * Deletes one manual line. If the header has no remaining lines after deletion
     * the header is also removed.
     */
    @DeleteMapping("/line/{lineId}")
    public ResponseEntity<Void> deleteLine(@PathVariable Long lineId) {
        adjustmentService.deleteLine(lineId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns a summary row (originalNet, adjustmentDelta, adjustedNet) per employee
     * that has adjustments in the given period. Used by the Payroll Register.
     *
     * @param salaryPeriodKey  e.g. "2026-6-1"
     */
    @GetMapping("/summary/{salaryPeriodKey}")
    public ResponseEntity<List<AdjustmentSummaryDTO>> getSummary(
            @PathVariable String salaryPeriodKey) {
        return ResponseEntity.ok(adjustmentService.getSummaryForPeriod(salaryPeriodKey));
    }

    /**
     * Returns the number of still-PENDING adjustments in a salary period.
     */
    @GetMapping("/pending-count/{salaryPeriodKey}")
    public ResponseEntity<Long> getPendingCount(@PathVariable String salaryPeriodKey) {
        return ResponseEntity.ok(adjustmentService.getPendingCountForPeriod(salaryPeriodKey));
    }

    /**
     * Returns ALL saved versions (newest first) for one employee in a period.
     * Used by the Payroll Register breakdown modal to display the full audit trail.
     *
     * @param employeeNo  e.g. "EMP-00001"
     * @param period      e.g. "2026-6-1"
     */
    @GetMapping("/history/{employeeNo}")
    public ResponseEntity<List<PayrollAdjustmentDTO>> getHistory(
            @PathVariable String employeeNo,
            @RequestParam String period) {
        return ResponseEntity.ok(adjustmentService.getHistoryForEmployee(employeeNo, period));
    }
}
