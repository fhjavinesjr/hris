package com.payroll.controllers;

import com.payroll.dtos.PayrollComputationRequest;
import com.payroll.dtos.PayrollComputationResponse;
import com.payroll.dtos.PayrollJobStatusDTO;
import com.payroll.entitymodels.PayrollDetail;
import com.payroll.repositories.PayrollDetailRepository;
import com.payroll.services.PayrollBatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for payroll batch computation.
 *
 * Endpoints:
 *  POST /api/payroll-computation/batch
 *      → Triggers an async batch; returns jobId immediately (non-blocking).
 *
 *  GET  /api/payroll-computation/status/{jobId}
 *      → Polls job progress (0–100 %) and status (PENDING/FETCHING_DATA/COMPUTING/SAVING/DONE/FAILED).
 *
 *  GET  /api/payroll-computation/results/{salaryPeriodKey}
 *      → Returns all computed PayrollDetail records for a salary period.
 */
@RestController
@RequestMapping("/api/payroll-computation")
public class PayrollComputationController {

    private final PayrollBatchService batchService;
    private final PayrollDetailRepository detailRepository;

    public PayrollComputationController(PayrollBatchService batchService,
                                         PayrollDetailRepository detailRepository) {
        this.batchService = batchService;
        this.detailRepository = detailRepository;
    }

    /**
     * Starts an async payroll batch computation for a given salary period.
     *
     * The JWT token is forwarded to downstream microservices (TimeKeeping, HR, Admin)
     * so they can authenticate the bulk-data requests.
     *
     * @param request  Period parameters and optional filters
     * @param authHeader  The Authorization header value (passed through as-is)
     * @return 202 Accepted + jobId; client polls /status/{jobId} for progress
     */
    @PostMapping("/batch")
    public ResponseEntity<PayrollComputationResponse> startBatch(
            @Valid @RequestBody PayrollComputationRequest request,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {

        PayrollComputationResponse response = batchService.startBatch(request, authHeader);
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Returns current status and progress percentage for a batch job.
     * Clients should poll this every 2–5 seconds while status != DONE and != FAILED.
     *
     * @param jobId UUID returned by /batch
     * @return 200 + PayrollJobStatusDTO (includes progressPct, processedEmployees, summary)
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<PayrollJobStatusDTO> getBatchStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(batchService.getJobStatus(jobId));
    }

    /**
     * Returns all PayrollDetail records for a completed salary period.
     *
     * @param salaryPeriodKey  Format: "YYYY-M-N" (e.g. "2026-6-1" for first half of June 2026)
     * @return 200 + list of PayrollDetail entities
     */
    @GetMapping("/results/{salaryPeriodKey}")
    public ResponseEntity<List<PayrollDetail>> getResults(@PathVariable String salaryPeriodKey) {
        return ResponseEntity.ok(detailRepository.findBySalaryPeriodKey(salaryPeriodKey));
    }
}
