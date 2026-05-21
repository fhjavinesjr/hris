package com.payroll.impl;

import com.payroll.dtos.*;
import com.payroll.entitymodels.*;
import com.payroll.repositories.PayrollBatchJobRepository;
import com.payroll.repositories.PayrollDetailRepository;
import com.payroll.services.PayrollBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Orchestrates batch payroll computation for 5K–10K employees in under 1 hour.
 *
 * ─── Performance strategy ────────────────────────────────────────────────────
 *  1. FETCH_DATA  — Parallel HTTP calls to other microservices fetch ALL needed
 *                   data in bulk (one call per data type, not one per employee).
 *                   Calls run concurrently via CompletableFuture.
 *
 *  2. COMPUTE     — Employees are divided into chunks and processed in parallel
 *                   using a dedicated thread pool.  The computation engine is
 *                   pure in-memory; it never touches the database.
 *
 *  3. SAVE        — Results are bulk-inserted using JPA saveAll() in chunks of
 *                   500, eliminating per-employee INSERT round-trips.
 *
 * Expected throughput:
 *  • Old system:  4–5 hours for 2K–3K employees (sequential, N+1 queries)
 *  • New system:  < 5 minutes for 10K employees (parallel, bulk queries)
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
public class PayrollBatchServiceImpl implements PayrollBatchService {

    private static final Logger log = LoggerFactory.getLogger(PayrollBatchServiceImpl.class);

    /** Number of PayrollDetail records written per saveAll() call. */
    private static final int SAVE_CHUNK_SIZE = 500;

    /** Number of employees processed per parallel task. */
    private static final int COMPUTE_CHUNK_SIZE = 100;

    // ── Injected dependencies ─────────────────────────────────────────────────
    private final PayrollComputationEngine engine;
    private final PayrollDetailRepository detailRepo;
    private final PayrollBatchJobRepository batchJobRepo;
    private final RestTemplate restTemplate;
    private final ExecutorService computeExecutor;

    // ── Downstream service URLs (from application.properties) ─────────────────
    @Value("${hris.services.administrative.url:http://localhost:8080}")
    private String adminServiceUrl;

    @Value("${hris.services.timekeeping.url:http://localhost:8084}")
    private String timeKeepingServiceUrl;

    @Value("${hris.services.hrmanagement.url:http://localhost:8082}")
    private String hrServiceUrl;

    public PayrollBatchServiceImpl(PayrollComputationEngine engine,
                                   PayrollDetailRepository detailRepo,
                                   PayrollBatchJobRepository batchJobRepo,
                                   RestTemplate restTemplate,
                                   @Qualifier("payrollComputeExecutor") ExecutorService computeExecutor) {
        this.engine = engine;
        this.detailRepo = detailRepo;
        this.batchJobRepo = batchJobRepo;
        this.restTemplate = restTemplate;
        this.computeExecutor = computeExecutor;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public PayrollComputationResponse startBatch(PayrollComputationRequest request, String authToken) {
        String jobId = UUID.randomUUID().toString();

        PayrollBatchJob job = new PayrollBatchJob();
        job.setJobId(jobId);
        job.setSalaryPeriodKey(request.getSalaryPeriodKey());
        job.setStatus(BatchJobStatus.PENDING);
        job.setProgressPct(0);
        job.setStartedAt(LocalDateTime.now());
        batchJobRepo.save(job);

        // Launch the batch asynchronously so we can return the jobId immediately
        runBatchAsync(jobId, request, authToken);

        return new PayrollComputationResponse(jobId, request.getSalaryPeriodKey());
    }

    @Override
    public PayrollJobStatusDTO getJobStatus(String jobId) {
        PayrollBatchJob job = batchJobRepo.findByJobId(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown job ID: " + jobId));
        return mapToStatusDTO(job);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Async batch pipeline
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * The three-phase pipeline:
     *   PHASE 1 – Bulk fetch all data from downstream services (parallel HTTP calls)
     *   PHASE 2 – Compute each employee in parallel (pure in-memory)
     *   PHASE 3 – Bulk save all results (chunked saveAll)
     *
     * Progress is persisted to DB after each phase so the UI can display live status.
     */
    @Async("payrollBatchAsyncExecutor")
    protected void runBatchAsync(String jobId, PayrollComputationRequest request, String authToken) {
        PayrollBatchJob job = findJob(jobId);
        long startMs = System.currentTimeMillis();

        try {
            // ──── PHASE 1: Fetch all data ────────────────────────────────────
            updateJob(job, BatchJobStatus.FETCHING_DATA, 5, null);
            PayrollDataSnapshot snapshot = fetchAllData(request, authToken);

            List<EmployeePayrollInfoDTO> employees = resolveEmployeeList(request, snapshot);
            int total = employees.size();
            if (total == 0) {
                finishJob(job, 0, 0, "No eligible employees found for " + request.getSalaryPeriodKey());
                return;
            }

            updateJob(job, BatchJobStatus.FETCHING_DATA, 15, null);
            job.setTotalEmployees(total);
            batchJobRepo.save(job);

            log.info("[PayrollBatch {}] Phase 1 complete — {} employees to process, snapshot loaded in {}ms",
                    jobId, total, System.currentTimeMillis() - startMs);

            // ──── PHASE 2: Parallel computation ──────────────────────────────
            updateJob(job, BatchJobStatus.COMPUTING, 20, null);

            AtomicInteger processed = new AtomicInteger(0);
            AtomicInteger failed    = new AtomicInteger(0);
            List<PayrollDetail> results = Collections.synchronizedList(new ArrayList<>(total));

            // Partition employees into chunks and submit to thread pool
            List<List<EmployeePayrollInfoDTO>> chunks = partition(employees, COMPUTE_CHUNK_SIZE);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (List<EmployeePayrollInfoDTO> chunk : chunks) {
                CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                    for (EmployeePayrollInfoDTO emp : chunk) {
                        try {
                            PayrollDetail pd = engine.compute(emp, request, snapshot);
                            results.add(pd);
                        } catch (PayrollComputationEngine.PayrollComputationException ex) {
                            log.warn("[PayrollBatch {}] Skipping {} — {}", jobId, ex.getEmployeeNo(), ex.getMessage());
                            failed.incrementAndGet();
                        }
                        int done = processed.incrementAndGet();
                        // Update progress every 50 employees
                        if (done % 50 == 0) {
                            int pct = 20 + (int) ((double) done / total * 55); // 20%→75%
                            updateProgress(jobId, done, failed.get(), pct);
                        }
                    }
                }, computeExecutor);
                futures.add(f);
            }

            // Wait for all chunks to finish
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            log.info("[PayrollBatch {}] Phase 2 complete — {}/{} computed, {} failed, took {}ms",
                    jobId, results.size(), total, failed.get(), System.currentTimeMillis() - startMs);

            // ──── PHASE 3: Bulk save ──────────────────────────────────────────
            updateJob(findJob(jobId), BatchJobStatus.SAVING, 75, null);
            saveInChunks(results, jobId);

            log.info("[PayrollBatch {}] Phase 3 complete — all records saved, total time {}ms",
                    jobId, System.currentTimeMillis() - startMs);

            // ──── Done ────────────────────────────────────────────────────────
            long elapsedSec = (System.currentTimeMillis() - startMs) / 1000;
            String summary = String.format(
                    "%d of %d employees processed in %d sec (%d min). %d failed.",
                    results.size(), total, elapsedSec, elapsedSec / 60, failed.get());

            finishJob(findJob(jobId), results.size(), failed.get(), summary);

        } catch (Exception ex) {
            log.error("[PayrollBatch {}] FATAL — {}", jobId, ex.getMessage(), ex);
            PayrollBatchJob j = findJob(jobId);
            j.setStatus(BatchJobStatus.FAILED);
            j.setFinishedAt(LocalDateTime.now());
            j.setErrorDetail(truncate(ex.getMessage(), 2000));
            batchJobRepo.save(j);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Phase 1 — Bulk data fetch (concurrent HTTP calls)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fires all downstream service calls in parallel using CompletableFuture.
     * Each call fetches ALL data for the entire cutoff window in a single request.
     * This replaces tens of thousands of individual per-employee DB/API queries.
     */
    private PayrollDataSnapshot fetchAllData(PayrollComputationRequest req, String token) {
        HttpHeaders headers = bearerHeaders(token);
        LocalDate from = req.getCutoffStartDate();
        LocalDate to   = req.getCutoffEndDate();

        // Launch all fetches concurrently
        CompletableFuture<List<EmployeePayrollInfoDTO>> empFuture =
                CompletableFuture.supplyAsync(() -> fetchEmployees(req, headers), computeExecutor);

        CompletableFuture<List<DtrDailySummaryDTO>> dtrFuture =
                CompletableFuture.supplyAsync(() -> fetchDtrSummaries(from, to, headers), computeExecutor);

        CompletableFuture<List<ApprovedLeaveDTO>> leaveFuture =
                CompletableFuture.supplyAsync(() -> fetchApprovedLeaves(from, to, headers), computeExecutor);

        CompletableFuture<Map<String, Double>> vlBalFuture =
                CompletableFuture.supplyAsync(() -> fetchVlBalances(headers), computeExecutor);

        CompletableFuture<Map<String, Double>> slBalFuture =
                CompletableFuture.supplyAsync(() -> fetchSlBalances(headers), computeExecutor);

        CompletableFuture<List<HolidayDTO>> holidayFuture =
                CompletableFuture.supplyAsync(() -> fetchHolidays(from, to, headers), computeExecutor);

        CompletableFuture<List<AllowanceDTO>> allowanceFuture =
                CompletableFuture.supplyAsync(() -> fetchAllowances(from, to, headers), computeExecutor);

        CompletableFuture<List<PayrollDataSnapshot.PhilHealthBracketDTO>> philHealthFuture =
                CompletableFuture.supplyAsync(() -> fetchPhilHealthBrackets(headers), computeExecutor);

        CompletableFuture<Map<String, List<PayrollDataSnapshot.WHoldingTaxBracketDTO>>> taxFuture =
                CompletableFuture.supplyAsync(() -> fetchTaxBrackets(headers), computeExecutor);

        CompletableFuture<Map<Integer, Double>> hazardFuture =
                CompletableFuture.supplyAsync(() -> fetchHazardPayRates(headers), computeExecutor);

        // Wait for all concurrent fetches
        CompletableFuture.allOf(empFuture, dtrFuture, leaveFuture, vlBalFuture, slBalFuture,
                holidayFuture, allowanceFuture, philHealthFuture, taxFuture, hazardFuture).join();

        // Build snapshot — index list results into Maps for O(1) lookup
        PayrollDataSnapshot snap = new PayrollDataSnapshot();

        List<EmployeePayrollInfoDTO> empList = empFuture.join();
        snap.setEmployeeMap(empList.stream()
                .collect(Collectors.toMap(EmployeePayrollInfoDTO::getEmployeeNo, e -> e, (a, b) -> a)));

        snap.setDtrMap(dtrFuture.join().stream()
                .collect(Collectors.groupingBy(DtrDailySummaryDTO::getEmployeeNo)));

        snap.setLeavesMap(leaveFuture.join().stream()
                .collect(Collectors.groupingBy(ApprovedLeaveDTO::getEmployeeNo)));

        snap.setVlBalanceMap(vlBalFuture.join());
        snap.setSlBalanceMap(slBalFuture.join());

        snap.setHolidayMap(holidayFuture.join().stream()
                .collect(Collectors.toMap(HolidayDTO::getDate, h -> h, (a, b) -> a)));

        snap.setAllowancesMap(allowanceFuture.join().stream()
                .collect(Collectors.groupingBy(AllowanceDTO::getEmployeeNo)));

        snap.setPhilHealthBrackets(philHealthFuture.join());
        snap.setTaxBrackets(taxFuture.join());
        snap.setHazardPayRateByGrade(hazardFuture.join());

        // Load previous payroll balances from this module's own DB
        snap.setPreviousBalanceMap(loadPreviousBalances(snap.getEmployeeMap().keySet(), req));

        return snap;
    }

    // ── Individual HTTP fetchers ───────────────────────────────────────────────

    private List<EmployeePayrollInfoDTO> fetchEmployees(PayrollComputationRequest req, HttpHeaders h) {
        String url = adminServiceUrl + "/api/employee/payroll-info/bulk";
        if (req.getDepartmentCode() != null && !req.getDepartmentCode().isBlank()) {
            url += "?department=" + req.getDepartmentCode();
        } else if (req.getEmployeeNo() != null && !req.getEmployeeNo().isBlank()) {
            url += "?employeeNo=" + req.getEmployeeNo();
        }
        try {
            ResponseEntity<List<EmployeePayrollInfoDTO>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<List<EmployeePayrollInfoDTO>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.error("Failed to fetch employees from Administrative service: {}", ex.getMessage());
            throw new RuntimeException("Cannot fetch employee list: " + ex.getMessage(), ex);
        }
    }

    private List<DtrDailySummaryDTO> fetchDtrSummaries(LocalDate from, LocalDate to, HttpHeaders h) {
        String url = timeKeepingServiceUrl + "/api/dtr/bulk-summary?from=" + from + "&to=" + to;
        try {
            ResponseEntity<List<DtrDailySummaryDTO>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<List<DtrDailySummaryDTO>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.error("Failed to fetch DTR summaries from TimeKeeping service: {}", ex.getMessage());
            return Collections.emptyList();  // non-fatal: employees will show 0 late/undertime
        }
    }

    private List<ApprovedLeaveDTO> fetchApprovedLeaves(LocalDate from, LocalDate to, HttpHeaders h) {
        String url = hrServiceUrl + "/api/leave/bulk-approved?from=" + from + "&to=" + to;
        try {
            ResponseEntity<List<ApprovedLeaveDTO>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<List<ApprovedLeaveDTO>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.error("Failed to fetch approved leaves from HR service: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private Map<String, Double> fetchVlBalances(HttpHeaders h) {
        String url = hrServiceUrl + "/api/leave-balance/bulk?leaveType=VL";
        try {
            ResponseEntity<Map<String, Double>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<Map<String, Double>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyMap();
        } catch (Exception ex) {
            log.warn("Failed to fetch VL balances: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<String, Double> fetchSlBalances(HttpHeaders h) {
        String url = hrServiceUrl + "/api/leave-balance/bulk?leaveType=SL";
        try {
            ResponseEntity<Map<String, Double>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<Map<String, Double>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyMap();
        } catch (Exception ex) {
            log.warn("Failed to fetch SL balances: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    private List<HolidayDTO> fetchHolidays(LocalDate from, LocalDate to, HttpHeaders h) {
        String url = adminServiceUrl + "/api/holiday/range?from=" + from + "&to=" + to;
        try {
            ResponseEntity<List<HolidayDTO>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<List<HolidayDTO>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.warn("Failed to fetch holidays: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private List<AllowanceDTO> fetchAllowances(LocalDate from, LocalDate to, HttpHeaders h) {
        String url = adminServiceUrl + "/api/earningAllowance/bulk?effectiveFrom=" + from + "&effectiveTo=" + to;
        try {
            ResponseEntity<List<AllowanceDTO>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<List<AllowanceDTO>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.warn("Failed to fetch allowances: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private List<PayrollDataSnapshot.PhilHealthBracketDTO> fetchPhilHealthBrackets(HttpHeaders h) {
        String url = adminServiceUrl + "/api/philhealth/brackets";
        try {
            ResponseEntity<List<PayrollDataSnapshot.PhilHealthBracketDTO>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<List<PayrollDataSnapshot.PhilHealthBracketDTO>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.warn("Failed to fetch PhilHealth brackets: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private Map<String, List<PayrollDataSnapshot.WHoldingTaxBracketDTO>> fetchTaxBrackets(HttpHeaders h) {
        String url = adminServiceUrl + "/api/wh-tax/brackets";
        try {
            ResponseEntity<Map<String, List<PayrollDataSnapshot.WHoldingTaxBracketDTO>>> resp =
                    restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(h),
                            new ParameterizedTypeReference<Map<String, List<PayrollDataSnapshot.WHoldingTaxBracketDTO>>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyMap();
        } catch (Exception ex) {
            log.warn("Failed to fetch WH-tax brackets: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Integer, Double> fetchHazardPayRates(HttpHeaders h) {
        String url = adminServiceUrl + "/api/hazard-pay/rates-by-grade";
        try {
            ResponseEntity<Map<Integer, Double>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<Map<Integer, Double>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyMap();
        } catch (Exception ex) {
            log.warn("Failed to fetch hazard pay rates: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Loads previous period's VL/SL balances from this module's own PayrollDetail table.
     * Used as fallback when the HR service's leave-balance endpoint returns no entry.
     */
    private Map<String, PayrollDataSnapshot.PreviousPeriodBalanceDTO> loadPreviousBalances(
            Set<String> employeeNos, PayrollComputationRequest req) {
        // Find the most recent payroll detail before this cutoff start
        List<PayrollDetail> prevDetails = detailRepo
                .findLatestBeforeCutoff(new ArrayList<>(employeeNos), req.getCutoffStartDate());

        Map<String, PayrollDataSnapshot.PreviousPeriodBalanceDTO> result = new HashMap<>();
        for (PayrollDetail pd : prevDetails) {
            PayrollDataSnapshot.PreviousPeriodBalanceDTO dto =
                    new PayrollDataSnapshot.PreviousPeriodBalanceDTO();
            dto.setVlBalance(pd.getVlBalance() != null ? pd.getVlBalance() : 0.0);
            dto.setSlBalance(pd.getSlBalance() != null ? pd.getSlBalance() : 0.0);
            dto.setEarnedLeave(pd.getEarnedLeave() != null ? pd.getEarnedLeave() : 0.0);
            dto.setVlDeductedDays(pd.getVlDeductedDays() != null ? pd.getVlDeductedDays() : 0.0);
            result.put(pd.getEmployeeNo(), dto);
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Phase 2 helpers
    // ─────────────────────────────────────────────────────────────────────────

    private List<EmployeePayrollInfoDTO> resolveEmployeeList(PayrollComputationRequest req,
                                                              PayrollDataSnapshot snap) {
        List<EmployeePayrollInfoDTO> all = new ArrayList<>(snap.getEmployeeMap().values());

        // Resolve basicPerSalary based on salary type (semi-monthly = half of monthly)
        boolean isSemiMonthly = "SEMI_MONTHLY".equalsIgnoreCase(req.getSalaryType());
        for (EmployeePayrollInfoDTO e : all) {
            double monthly = e.getBasicMonthlySalary() != null ? e.getBasicMonthlySalary() : 0;
            e.setBasicPerSalary(isSemiMonthly ? monthly / 2 : monthly);
        }

        // Filter out excluded employees unless the request explicitly includes them
        if (!Boolean.TRUE.equals(req.getIncludeExcluded())) {
            all.removeIf(e -> Boolean.TRUE.equals(e.getIsExcludedFromPayroll()));
        }

        return all;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Phase 3 — Chunked bulk save
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Saves PayrollDetail records in chunks to avoid overwhelming the DB
     * with a single massive INSERT.  Each chunk runs in a separate transaction.
     */
    private void saveInChunks(List<PayrollDetail> results, String jobId) {
        List<List<PayrollDetail>> chunks = partition(results, SAVE_CHUNK_SIZE);
        int saved = 0;
        for (List<PayrollDetail> chunk : chunks) {
            saveChunk(chunk);
            saved += chunk.size();
            int pct = 75 + (int) ((double) saved / results.size() * 25); // 75%→100%
            updateProgress(jobId, saved, 0, pct);
        }
    }

    @Transactional
    protected void saveChunk(List<PayrollDetail> chunk) {
        // Delete existing records for the same (employeeNo, salaryPeriodKey) to allow recompute
        for (PayrollDetail pd : chunk) {
            detailRepo.deleteByEmployeeNoAndSalaryPeriodKey(pd.getEmployeeNo(), pd.getSalaryPeriodKey());
        }
        detailRepo.saveAll(chunk);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Progress helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void updateJob(PayrollBatchJob job, BatchJobStatus status, int pct, String summary) {
        job.setStatus(status);
        job.setProgressPct(pct);
        if (summary != null) job.setSummary(summary);
        batchJobRepo.save(job);
    }

    private void updateProgress(String jobId, int processed, int failed, int pct) {
        batchJobRepo.updateProgress(jobId, processed, failed, pct);
    }

    private void finishJob(PayrollBatchJob job, int processed, int failed, String summary) {
        job.setStatus(BatchJobStatus.DONE);
        job.setProgressPct(100);
        job.setProcessedEmployees(processed);
        job.setFailedEmployees(failed);
        job.setSummary(summary);
        job.setFinishedAt(LocalDateTime.now());
        batchJobRepo.save(job);
    }

    private PayrollBatchJob findJob(String jobId) {
        return batchJobRepo.findByJobId(jobId)
                .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Utility helpers
    // ─────────────────────────────────────────────────────────────────────────

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.AUTHORIZATION, token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> parts = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            parts.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return parts;
    }

    private static String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) : s;
    }

    private PayrollJobStatusDTO mapToStatusDTO(PayrollBatchJob job) {
        PayrollJobStatusDTO dto = new PayrollJobStatusDTO();
        dto.setJobId(job.getJobId());
        dto.setSalaryPeriodKey(job.getSalaryPeriodKey());
        dto.setStatus(job.getStatus());
        dto.setProgressPct(job.getProgressPct());
        dto.setTotalEmployees(job.getTotalEmployees());
        dto.setProcessedEmployees(job.getProcessedEmployees());
        dto.setFailedEmployees(job.getFailedEmployees());
        dto.setStartedAt(job.getStartedAt());
        dto.setFinishedAt(job.getFinishedAt());
        dto.setSummary(job.getSummary());
        dto.setErrorDetail(job.getErrorDetail());
        return dto;
    }
}
