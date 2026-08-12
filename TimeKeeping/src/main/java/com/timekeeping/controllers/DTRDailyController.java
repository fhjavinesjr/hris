package com.timekeeping.controllers;

import com.timekeeping.dtos.DTRDailyDTO;
import com.timekeeping.dtos.DTRSegmentEditRequest;
import com.timekeeping.services.DTRDailyService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dtr-daily")
public class DTRDailyController {
    private final DTRDailyService dtrDailyService;

    public DTRDailyController(DTRDailyService dtrDailyService) {
        this.dtrDailyService = dtrDailyService;
    }

    @PostMapping
    public ResponseEntity<DTRDailyDTO> createOrUpdateDTRDaily(@RequestBody DTRDailyDTO dtrDailyDTO) {
        DTRDailyDTO saved = dtrDailyService.createOrUpdateDTRDaily(dtrDailyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Edits one DTR segment. The server recalculates the minute totals and
     * converts the result into a protected MANUAL adjustment so a later ADMS
     * Search cannot overwrite the administrator's correction.
     */
    @PutMapping("/segment/{dtrSegmentId}")
    public ResponseEntity<DTRDailyDTO> editDTRSegment(
            @PathVariable Long dtrSegmentId,
            @RequestBody DTRSegmentEditRequest request) {
        return ResponseEntity.ok(dtrDailyService.editDTRSegment(dtrSegmentId, request));
    }

    /**
     * Read-only DTR retrieval. This endpoint never rebuilds attendance from ADMS.
     * It is used after Edit/Delete so the UI can reload the current transaction
     * state without immediately recreating a deliberately deleted DTR.
     */
    @GetMapping
    public ResponseEntity<List<DTRDailyDTO>> getEmployeeDTRDaily(
            @RequestParam String employeeId,
            @RequestParam @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime toDate) {
        return ResponseEntity.ok(dtrDailyService.getEmployeeDTRDaily(employeeId, fromDate, toDate));
    }

    @GetMapping("/report")
    public void downloadDtrReport(@RequestParam String employeeId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                  HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=\"DTR_" + employeeId + "_" + fromDate + "_" + toDate + ".pdf\"");
        dtrDailyService.generateDtrReport(employeeId, fromDate, toDate, response.getOutputStream());
    }
}
