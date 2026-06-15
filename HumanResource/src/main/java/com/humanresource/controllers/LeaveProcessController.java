package com.humanresource.controllers;

import com.humanresource.dtos.LeaveProcessBatchStartResponseDTO;
import com.humanresource.dtos.LeaveProcessJobStatusDTO;
import com.humanresource.dtos.LeaveProcessQueueItemDTO;
import com.humanresource.dtos.LeaveProcessRequestDTO;
import com.humanresource.dtos.LeaveProcessResultDTO;
import com.humanresource.services.LeaveProcessBatchService;
import com.humanresource.services.LeaveProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Trigger endpoint for the Leave Information computation engine.
 *
 * Usage:
 *   POST /api/leave-information/process
 *   Body: {
 *     "salaryPeriodSettingId": 1,
 *     "cutoffStartDate": "2024-11-16",
 *     "cutoffEndDate": "2024-11-30",
 *     "scope": "ALL",          // or "EMPLOYEE"
 *     "employeeId": null,      // required when scope = EMPLOYEE
 *     "processedById": 1
 *   }
 *
 * The frontend (HR Management UI) resolves the cutoff dates from the
 * SalaryPeriodSetting, then passes the already-resolved dates here.
 * This keeps the engine independent of date-resolution logic.
 */
@RestController
@RequestMapping("/api/leave-information")
@CrossOrigin("*")
public class LeaveProcessController {

    private final LeaveProcessService processService;
    private final LeaveProcessBatchService batchService;

    public LeaveProcessController(LeaveProcessService processService, LeaveProcessBatchService batchService) {
        this.processService = processService;
        this.batchService = batchService;
    }

    @PostMapping("/process-batch")
    public ResponseEntity<LeaveProcessBatchStartResponseDTO> startProcessBatch(
            @RequestBody LeaveProcessRequestDTO request) {
        try {
            if (request.getCutoffStartDate() == null || request.getCutoffEndDate() == null) {
                return ResponseEntity.badRequest().body(
                        new LeaveProcessBatchStartResponseDTO(null, "cutoffStartDate and cutoffEndDate are required")
                );
            }
            LeaveProcessBatchStartResponseDTO response = batchService.startBatch(request);
            return ResponseEntity.accepted().body(response);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new LeaveProcessBatchStartResponseDTO(null, "Server error: " + ex.getMessage()));
        }
    }

    @GetMapping("/process-status/{jobId}")
    public ResponseEntity<LeaveProcessJobStatusDTO> getProcessStatus(@PathVariable String jobId) {
        try {
            return ResponseEntity.ok(batchService.getJobStatus(jobId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/process-queue/{jobId}")
    public ResponseEntity<List<LeaveProcessQueueItemDTO>> getProcessQueue(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0") int from) {
        try {
            return ResponseEntity.ok(batchService.getJobQueue(jobId, from));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/process-result/{jobId}")
    public ResponseEntity<LeaveProcessResultDTO> getProcessResult(@PathVariable String jobId) {
        try {
            return ResponseEntity.ok(batchService.getJobResult(jobId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            LeaveProcessResultDTO pending = new LeaveProcessResultDTO(
                    0,
                    0,
                    java.util.List.of("Job is still running"),
                    java.util.List.of()
            );
            return ResponseEntity.status(409).body(pending);
        }
    }

    @PostMapping("/process")
    public ResponseEntity<LeaveProcessResultDTO> process(
            @RequestBody LeaveProcessRequestDTO request) {
        try {
            if (request.getCutoffStartDate() == null || request.getCutoffEndDate() == null) {
                LeaveProcessResultDTO err = new LeaveProcessResultDTO(
                        0, 0, java.util.List.of("cutoffStartDate and cutoffEndDate are required"), java.util.List.of());
                return ResponseEntity.badRequest().body(err);
            }
            if (request.getScope() == null) {
                request.setScope("ALL");
            }
            LeaveProcessResultDTO result = processService.process(request);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            LeaveProcessResultDTO errResult = new LeaveProcessResultDTO(
                    0, 0,
                    java.util.List.of("Server error: " + ex.getMessage()),
                    java.util.List.of());
            return ResponseEntity.internalServerError().body(errResult);
        }
    }
}
