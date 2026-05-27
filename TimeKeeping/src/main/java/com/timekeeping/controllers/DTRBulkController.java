package com.timekeeping.controllers;

import com.timekeeping.services.DTRDailyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Bulk DTR endpoints for Payroll service integration.
 * These endpoints return aggregated attendance data for batch payroll computation.
 */
@RestController
@RequestMapping("/api/dtr")
public class DTRBulkController {
    private final DTRDailyService dtrDailyService;

    public DTRBulkController(DTRDailyService dtrDailyService) {
        this.dtrDailyService = dtrDailyService;
    }

    /**
     * Bulk fetch DTR summaries for all employees within a date range.
     * Called by Payroll service before batch computation.
     * 
     * Example: GET /api/dtr/bulk-summary?from=2026-05-01&to=2026-05-31
     * 
     * @param from start date (inclusive), format: yyyy-MM-dd
     * @param to end date (inclusive), format: yyyy-MM-dd  
     * @return List of DTR daily summaries for all employees
     */
    @GetMapping("/bulk-summary")
    public ResponseEntity<List<Map<String, Object>>> getBulkSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<Map<String, Object>> result = dtrDailyService.getBulkDtrSummary(from, to);
        return ResponseEntity.ok(result);
    }
}
