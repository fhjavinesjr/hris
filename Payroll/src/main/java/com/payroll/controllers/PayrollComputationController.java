package com.payroll.controllers;

import com.payroll.dtos.*;
import com.payroll.entitymodels.PayrollDetail;
import com.payroll.repositories.PayrollDetailRepository;
import com.payroll.services.PayrollBatchService;
import com.payroll.services.PayrollEmployeeConfigService;
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
 *
 *  GET  /api/payroll-computation/employee-config
 *      → Returns the employee pre-setup list (merged with previous period defaults).
 *
 *  POST /api/payroll-computation/employee-config/bulk-save
 *      → Saves officer-set per-employee flags before computation.
 */
@RestController
@RequestMapping("/api/payroll-computation")
public class PayrollComputationController {

    private final PayrollBatchService batchService;
    private final PayrollDetailRepository detailRepository;
    private final PayrollEmployeeConfigService configService;

    public PayrollComputationController(PayrollBatchService batchService,
                                         PayrollDetailRepository detailRepository,
                                         PayrollEmployeeConfigService configService) {
        this.batchService = batchService;
        this.detailRepository = detailRepository;
        this.configService = configService;
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
     * Returns per-employee computation events for a batch job from a given sequence offset.
     * Use {@code from=0} on the first poll; pass the last received seqNo + 1 on each subsequent
     * call to receive only new items (no duplicates).
     *
     * @param jobId UUID returned by /batch
     * @param from  inclusive lower-bound sequence number (default 0)
     */
    @GetMapping("/queue/{jobId}")
    public ResponseEntity<List<PayrollQueueItemDTO>> getJobQueue(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0") int from) {
        return ResponseEntity.ok(batchService.getJobQueue(jobId, from));
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

    /**
     * Returns the employee pre-setup list for a salary period and payroll group.
     * Merges HR employee data with saved config; falls back to previous period defaults.
     *
     * @param salaryPeriodKey  e.g. "2026-6-1"
     * @param employeeGroup    "regular" or "contractual"
     * @param authHeader       JWT forwarded to HumanResource service
     */
    @GetMapping("/employee-config")
    public ResponseEntity<List<PayrollEmployeeConfigDTO>> getEmployeeConfig(
            @RequestParam String salaryPeriodKey,
            @RequestParam(defaultValue = "regular") String employeeGroup,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(configService.getConfigForSetup(salaryPeriodKey, employeeGroup, authHeader));
    }

    /**
     * Saves officer-set per-employee flags before computation.
     */
    @PostMapping("/employee-config/bulk-save")
    public ResponseEntity<Void> saveEmployeeConfig(
            @RequestBody PayrollEmployeeConfigSaveRequest request) {
        configService.bulkSave(request.getSalaryPeriodKey(), request.getConfigs());
        return ResponseEntity.ok().build();
    }

    /**
     * Returns the full PayrollDetail (including earnings and deductions breakdown)
     * for a single employee in a specific salary period.
     * Used by the Payroll Register module's drill-down view.
     *
     * @param employeeNo      Employee number
     * @param salaryPeriodKey Format: "YYYY-M-N" (e.g. "2026-6-1")
     * @return 200 + PayrollDetail with nested earnings and deductions, or 404 if not found
     */
    @GetMapping("/breakdown/{employeeNo}")
    public ResponseEntity<PayrollDetail> getBreakdown(
            @PathVariable String employeeNo,
            @RequestParam String salaryPeriodKey) {
        return detailRepository.findByEmployeeNoAndSalaryPeriodKey(employeeNo, salaryPeriodKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
