package com.timekeeping.controllers;

import com.timekeeping.dtos.DTRDailyDTO;
import com.timekeeping.services.AdmsDtrProcessingService;
import com.timekeeping.services.DTRDailyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dtr-daily")
public class AdmsDtrReconciliationController {

    private final AdmsDtrProcessingService admsDtrProcessingService;
    private final DTRDailyService dtrDailyService;

    public AdmsDtrReconciliationController(
            AdmsDtrProcessingService admsDtrProcessingService,
            DTRDailyService dtrDailyService) {
        this.admsDtrProcessingService = admsDtrProcessingService;
        this.dtrDailyService = dtrDailyService;
    }

    /**
     * Explicit Search action:
     * adms_punch_log -> DTR for only the selected employee/date range,
     * then return the refreshed DTR records in the same request.
     */
    @PostMapping("/reconcile")
    public ResponseEntity<List<DTRDailyDTO>> reconcileEmployeeDtr(
            @RequestParam String employeeId,
            @RequestParam @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime toDate) {
        admsDtrProcessingService.processEmployeePunches(employeeId, fromDate, toDate);
        return ResponseEntity.ok(dtrDailyService.getEmployeeDTRDaily(employeeId, fromDate, toDate));
    }
}
