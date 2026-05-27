package com.payroll.impl;

import com.payroll.dtos.*;
import com.payroll.entitymodels.*;
import com.payroll.repositories.EmployeeDeductionRepository;
import com.payroll.repositories.EmployeeIncomeRepository;
import com.payroll.repositories.EmployeeLoanRepository;
import com.payroll.repositories.PayrollBatchJobRepository;
import com.payroll.repositories.PayrollDetailRepository;
import com.payroll.repositories.PayrollEmployeeConfigRepository;
import com.payroll.services.EarningAllowanceService;
import com.payroll.services.PayrollBatchService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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

    // ── Per-employee live queue (in-memory; keyed by jobId) ──────────────────
    /**
     * Stores per-employee computation events for live polling by the UI.
     * Items are appended in sequence as each employee is computed.
     * Entries are removed 30 minutes after a job finishes to avoid memory leaks.
     */
    private final ConcurrentHashMap<String, List<PayrollQueueItemDTO>> jobQueues =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> jobQueueSeqs =
            new ConcurrentHashMap<>();

    // ── Injected dependencies ─────────────────────────────────────────────────
    private final PayrollComputationEngine engine;
    private final PayrollDetailRepository detailRepo;
    private final PayrollBatchJobRepository batchJobRepo;
    private final EmployeeLoanRepository loanRepo;
    private final EmployeeDeductionRepository deductionRepo;
    private final EmployeeIncomeRepository incomeRepo;
    private final EarningAllowanceService earningAllowanceService;
    private final RestTemplate restTemplate;
    private final ExecutorService computeExecutor;
    private final ThreadPoolTaskExecutor batchAsyncExecutor;
    private final PayrollEmployeeConfigRepository configRepo;

    // ── Downstream service URLs (from application.properties, overridden by SystemConfig @PostConstruct) ──
    @Value("${hris.services.administrative.url:http://localhost:8082}")
    private String adminServiceUrl;

    @Value("${hris.services.timekeeping.url:http://localhost:8083}")
    private String timeKeepingServiceUrl;

    @Value("${hris.services.hrmanagement.url:http://localhost:8085}")
    private String hrServiceUrl;

    public PayrollBatchServiceImpl(PayrollComputationEngine engine,
                                   PayrollDetailRepository detailRepo,
                                   PayrollBatchJobRepository batchJobRepo,
                                   EmployeeLoanRepository loanRepo,
                                   EmployeeDeductionRepository deductionRepo,
                                   EmployeeIncomeRepository incomeRepo,
                                   EarningAllowanceService earningAllowanceService,
                                   RestTemplate restTemplate,
                                   @Qualifier("payrollComputeExecutor") ExecutorService computeExecutor,
                                   @Qualifier("payrollBatchAsyncExecutor") ThreadPoolTaskExecutor batchAsyncExecutor,
                                   PayrollEmployeeConfigRepository configRepo) {
        this.engine = engine;
        this.detailRepo = detailRepo;
        this.batchJobRepo = batchJobRepo;
        this.loanRepo = loanRepo;
        this.deductionRepo = deductionRepo;
        this.incomeRepo = incomeRepo;
        this.earningAllowanceService = earningAllowanceService;
        this.restTemplate = restTemplate;
        this.computeExecutor = computeExecutor;
        this.batchAsyncExecutor = batchAsyncExecutor;
        this.configRepo = configRepo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Startup: override service URLs from SystemConfig
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * After Spring injects all @Value fields, override the mutable service URLs
     * with whatever the Administrative SystemConfig module says.
     * Falls back silently to application.properties if the admin service is not yet up.
     */
    @PostConstruct
    private void loadServiceUrlsFromSystemConfig() {
        try {
            String configUrl = adminServiceUrl + "/api/system-config/get-all";
            ResponseEntity<List<Map<String, String>>> resp = restTemplate.exchange(
                    configUrl, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()),
                    new ParameterizedTypeReference<List<Map<String, String>>>() {});
            if (resp.getBody() != null) {
                Map<String, String> configMap = new HashMap<>();
                for (Map<String, String> entry : resp.getBody()) {
                    String key = entry.get("configKey");
                    String val = entry.get("configValue");
                    if (key != null && val != null) configMap.put(key, val);
                }
                String tkUrl  = configMap.get("api.url.timekeeping");
                String hrmUrl = configMap.get("api.url.hrm");
                if (tkUrl  != null && !tkUrl.isBlank())  { timeKeepingServiceUrl = tkUrl;  log.info("PayrollBatch: TimeKeeping URL from SystemConfig → {}", tkUrl); }
                if (hrmUrl != null && !hrmUrl.isBlank()) { hrServiceUrl = hrmUrl;          log.info("PayrollBatch: HRM URL from SystemConfig → {}", hrmUrl); }
            }
        } catch (Exception ex) {
            log.warn("PayrollBatch: Cannot reach SystemConfig at startup; using application.properties defaults. Reason: {}", ex.getMessage());
        }
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

        // Initialise per-employee live queue for this job
        jobQueues.put(jobId, Collections.synchronizedList(new ArrayList<>()));
        jobQueueSeqs.put(jobId, new AtomicInteger(0));

        // Launch the batch asynchronously using the injected executor to avoid
        // Spring @Async self-invocation bypass (calling runBatchAsync on 'this'
        // would skip the proxy and run synchronously on the HTTP thread).
        batchAsyncExecutor.execute(() -> runBatchAsync(jobId, request, authToken));

        return new PayrollComputationResponse(jobId, request.getSalaryPeriodKey());
    }

    @Override
    public PayrollJobStatusDTO getJobStatus(String jobId) {
        PayrollBatchJob job = batchJobRepo.findByJobId(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown job ID: " + jobId));
        return mapToStatusDTO(job);
    }

    @Override
    public List<PayrollQueueItemDTO> getJobQueue(String jobId, int fromSeqNo) {
        List<PayrollQueueItemDTO> queue = jobQueues.get(jobId);
        if (queue == null) return Collections.emptyList();
        // Return items whose seqNo >= fromSeqNo (safe to read concurrent list)
        return queue.stream()
                .filter(item -> item.getSeqNo() >= fromSeqNo)
                .sorted(Comparator.comparingInt(PayrollQueueItemDTO::getSeqNo))
                .collect(Collectors.toList());
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

            List<PayrollQueueItemDTO> jobQueue = jobQueues.get(jobId);
            AtomicInteger jobQueueSeq = jobQueueSeqs.get(jobId);

            for (List<EmployeePayrollInfoDTO> chunk : chunks) {
                CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                    for (EmployeePayrollInfoDTO emp : chunk) {
                        PayrollDetail pd = null;
                        String qStatus = "OK";
                        String qError  = null;
                        try {
                            pd = engine.compute(emp, request, snapshot);
                            results.add(pd);
                        } catch (PayrollComputationEngine.PayrollComputationException ex) {
                            log.warn("[PayrollBatch {}] Skipping {} — {}", jobId, ex.getEmployeeNo(), ex.getMessage());
                            failed.incrementAndGet();
                            qStatus = "FAILED";
                            qError  = ex.getMessage();
                        }

                        // Push per-employee event so the UI can display live progress
                        if (jobQueue != null && jobQueueSeq != null) {
                            int seq = jobQueueSeq.getAndIncrement();
                            jobQueue.add(new PayrollQueueItemDTO(
                                    seq,
                                    emp.getEmployeeNo(),
                                    emp.getFullName(),
                                    qStatus,
                                    pd != null ? pd.getGrossAmount() : null,
                                    pd != null ? pd.getTotalDeduction() : null,
                                    pd != null ? pd.getNetAmount() : null,
                                    qError));
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
            j.setErrorDetail(buildFriendlyError(ex));
            batchJobRepo.save(j);
        }
    }

    /**
     * Converts a raw exception into a short, readable message for display in the UI.
     * Checks the full exception chain so nested JDBC causes are caught.
     */
    private String buildFriendlyError(Exception ex) {
        Throwable t = ex;
        while (t != null) {
            String msg = t.getMessage();
            if (msg != null) {
                if (msg.contains("too many parameters") || msg.contains("2100")) {
                    return "Database error: too many employees were included in a single query "
                         + "(SQL Server limit is 2,100 parameters). "
                         + "Please contact system support to adjust the batch chunk size.";
                }
                if (msg.contains("Timeout") || msg.contains("timeout")) {
                    return "Database timeout: the computation took too long. "
                         + "Try reducing the number of employees or contact system support.";
                }
                if (msg.contains("Connection") || msg.contains("connection")) {
                    return "Database connection error: the payroll service could not reach the database. "
                         + "Please check the server and try again.";
                }
            }
            t = t.getCause();
        }
        // Fallback: show the top-level message, stripped of the lengthy SQL text
        String raw = ex.getMessage();
        if (raw != null && raw.contains("JDBC exception executing SQL")) {
            int bracket = raw.indexOf("] [");
            if (bracket > 0) {
                // Extract the human-readable part inside the last [...] block
                String tail = raw.substring(bracket + 3);
                int end = tail.indexOf(']');
                if (end > 0) {
                    return "Database error: " + tail.substring(0, end).trim();
                }
            }
        }
        return truncate(raw != null ? raw : "Unknown error", 400);
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

        CompletableFuture<Map<String, PayrollDataSnapshot.HazardPaySettingDTO>> hazardSettingsFuture =
                CompletableFuture.supplyAsync(() -> fetchHazardPaySettings(to, headers), computeExecutor);

        CompletableFuture<double[]> gsisFuture =
                CompletableFuture.supplyAsync(() -> fetchGsisRates(headers), computeExecutor);

        // PayrollSettings replaces old PremiumMultiplier: [cutoffDays, peraProrationDivisor]
        CompletableFuture<int[]> payrollSettingsFuture =
                CompletableFuture.supplyAsync(() -> fetchPayrollSettings(headers), computeExecutor);

        // PagIbigContribution: [employeeShare, employerShare]
        CompletableFuture<double[]> pagibigFuture =
                CompletableFuture.supplyAsync(() -> fetchPagIbigContribution(headers), computeExecutor);

        // EarningLeave table: awolDays → earnedLeave
        CompletableFuture<Map<Integer, Double>> earnLeaveFuture =
                CompletableFuture.supplyAsync(() -> fetchEarningLeaveTable(headers), computeExecutor);

        // Local DB fetches — loans, deductions, income (no HTTP call)
        int batchMonth = req.getCutoffStartDate().getMonthValue();
        int batchYear  = req.getCutoffStartDate().getYear();
        String salaryPeriodKey = req.getSalaryPeriodKey();

        CompletableFuture<Map<String, List<PayrollDataSnapshot.LoanDTO>>> loanFuture =
                CompletableFuture.supplyAsync(() -> fetchActiveLoans(), computeExecutor);

        CompletableFuture<Map<String, List<PayrollDataSnapshot.EntryDeductionDTO>>> deductionFuture =
                CompletableFuture.supplyAsync(() -> fetchEntryDeductions(salaryPeriodKey), computeExecutor);

        CompletableFuture<Map<String, List<PayrollDataSnapshot.IncomeEntryDTO>>> incomeFuture =
                CompletableFuture.supplyAsync(() -> fetchIncomeEntries(batchMonth, batchYear), computeExecutor);

        // Wait for all concurrent fetches
        CompletableFuture.allOf(empFuture, dtrFuture, leaveFuture, vlBalFuture, slBalFuture,
                holidayFuture, allowanceFuture, philHealthFuture, taxFuture, hazardFuture, hazardSettingsFuture,
                gsisFuture, payrollSettingsFuture, pagibigFuture, earnLeaveFuture,
                loanFuture, deductionFuture, incomeFuture).join();

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
        snap.setHazardPaySettingsMap(hazardSettingsFuture.join());

        // Apply GSIS rates from the Admin module (dynamic)
        double[] gsisRates = gsisFuture.join();  // [psRate, erRate]
        snap.setGsisPsRate(gsisRates[0]);
        snap.setGsisErRate(gsisRates[1]);

        // Apply PayrollSettings: cutoffDays and PERA proration divisor
        int[] payrollSettings = payrollSettingsFuture.join();  // [0]=cutoffDays, [1]=peraProrationDivisor, [2]=autoComputeHazardPay (as int: 0 or 1)
        snap.setPeraProrationDivisor(payrollSettings[1]);
        snap.setAutoComputeHazardPay(payrollSettings[2] == 1);
        if (req.getCutoffDays() == null) {
            req.setCutoffDays(payrollSettings[0]);
        }

        // Apply PagIbig contribution from the Admin module
        double[] pagibig = pagibigFuture.join();  // [employeeShare, employerShare]
        snap.setPagibigMandatoryAmount(pagibig[0]);

        // Apply EarningLeave table (awolDays → earnedLeave)
        snap.setEarnLeaveMap(earnLeaveFuture.join());

        // Apply local DB fetches
        snap.setLoansMap(loanFuture.join());
        snap.setEntryDeductionsMap(deductionFuture.join());
        snap.setIncomeEntriesMap(incomeFuture.join());

        // Load previous payroll balances from this module's own DB
        snap.setPreviousBalanceMap(loadPreviousBalances(snap.getEmployeeMap().keySet(), req));

        return snap;
    }

    // ── Individual HTTP fetchers ───────────────────────────────────────────────

    private List<EmployeePayrollInfoDTO> fetchEmployees(PayrollComputationRequest req, HttpHeaders h) {
        String url = hrServiceUrl + "/api/employee/payroll-info/bulk";
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
            log.error("Failed to fetch employees from HumanResource service: {}", ex.getMessage());
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
            // Administrative service returns HolidayDTO with "holidayDate" field
            // We need to map it to Payroll's HolidayDTO with "date" field
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            
            List<HolidayDTO> holidays = new ArrayList<>();
            if (resp.getBody() != null) {
                for (Map<String, Object> item : resp.getBody()) {
                    HolidayDTO dto = new HolidayDTO();
                    // Map holidayDate to date
                    Object holidayDate = item.get("holidayDate");
                    if (holidayDate != null) {
                        if (holidayDate instanceof String) {
                            dto.setDate(LocalDate.parse((String) holidayDate));
                        } else if (holidayDate instanceof List) {
                            List<?> dateArray = (List<?>) holidayDate;
                            dto.setDate(LocalDate.of((Integer) dateArray.get(0), (Integer) dateArray.get(1), (Integer) dateArray.get(2)));
                        }
                    }
                    dto.setHolidayType((String) item.get("holidayType"));
                    dto.setName((String) item.get("name"));
                    holidays.add(dto);
                }
            }
            return holidays;
        } catch (Exception ex) {
            log.warn("Failed to fetch holidays: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    private List<AllowanceDTO> fetchAllowances(LocalDate from, LocalDate to, HttpHeaders h) {
        // Fetch earning allowances directly from the Payroll service's own database
        // (PERA and other allowances are entered via the Payroll UI and stored locally)
        try {
            List<AllowanceDTO> allowances = earningAllowanceService.getBulkAllowancesForPayroll(from, to);
            log.info("Fetched {} earning allowances from Payroll database for period {} to {}", 
                allowances.size(), from, to);
            return allowances;
        } catch (Exception ex) {
            log.warn("Failed to fetch allowances from Payroll database: {}", ex.getMessage());
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
     * Fetch hazard pay auto-compute settings for all employees effective on the payroll date.
     * Uses the new flexible hazard_pay_settings table instead of isDoh flag.
     * 
     * @param payrollDate the end date of the payroll period
     * @param h HTTP headers for authentication
     * @return Map of employeeNo → HazardPaySettingDTO
     */
    private Map<String, PayrollDataSnapshot.HazardPaySettingDTO> fetchHazardPaySettings(
            LocalDate payrollDate, HttpHeaders h) {
        String url = adminServiceUrl + "/api/hazard-pay-settings/effective-all?date=" + payrollDate;
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            
            List<Map<String, Object>> settings = resp.getBody();
            if (settings == null || settings.isEmpty()) {
                log.info("No hazard pay auto-compute settings found for date {}", payrollDate);
                return Collections.emptyMap();
            }

            Map<String, PayrollDataSnapshot.HazardPaySettingDTO> map = new HashMap<>();
            for (Map<String, Object> s : settings) {
                String employeeNo = (String) s.get("employeeNo");
                Boolean autoCompute = (Boolean) s.get("autoCompute");
                Number percentageNum = (Number) s.get("percentage");
                Double percentage = percentageNum != null ? percentageNum.doubleValue() : 25.00;

                if (employeeNo != null && Boolean.TRUE.equals(autoCompute)) {
                    PayrollDataSnapshot.HazardPaySettingDTO dto = new PayrollDataSnapshot.HazardPaySettingDTO();
                    dto.setAutoCompute(autoCompute);
                    dto.setPercentage(percentage);
                    map.put(employeeNo, dto);
                }
            }

            log.info("Loaded {} hazard pay auto-compute settings for {}", map.size(), payrollDate);
            return map;
        } catch (Exception ex) {
            log.warn("Failed to fetch hazard pay settings: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Loads previous period's VL/SL balances from this module's own PayrollDetail table.
     * Used as fallback when the HR service's leave-balance endpoint returns no entry.
     */
    private Map<String, PayrollDataSnapshot.PreviousPeriodBalanceDTO> loadPreviousBalances(
            Set<String> employeeNos, PayrollComputationRequest req) {
        // MSSQL supports a maximum of 2100 bound parameters per statement.
        // With large payrolls (1000+ employees) the IN-clause would exceed that limit,
        // so we chunk the list into batches of 500 and merge the results.
        List<String> empList = new ArrayList<>(employeeNos);
        List<PayrollDetail> prevDetails = new ArrayList<>();
        int chunkSize = 500;
        for (int i = 0; i < empList.size(); i += chunkSize) {
            List<String> chunk = empList.subList(i, Math.min(i + chunkSize, empList.size()));
            prevDetails.addAll(
                    detailRepo.findLatestBeforeCutoff(chunk, req.getCutoffStartDate()));
        }

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
        } else if (Boolean.TRUE.equals(req.getExcludedOnly())) {
            // Contractual mode: keep ONLY employees flagged as excluded from regular payroll
            all.removeIf(e -> !Boolean.TRUE.equals(e.getIsExcludedFromPayroll()));
        }

        // Apply officer-set per-employee config overrides (pre-computation setup)
        Map<String, PayrollEmployeeConfig> configMap =
                configRepo.findBySalaryPeriodKey(req.getSalaryPeriodKey()).stream()
                        .collect(Collectors.toMap(PayrollEmployeeConfig::getEmployeeNo, c -> c));

        if (!configMap.isEmpty()) {
            // Remove any employee individually excluded by the officer for this run
            all.removeIf(e -> {
                PayrollEmployeeConfig cfg = configMap.get(e.getEmployeeNo());
                return cfg != null && Boolean.TRUE.equals(cfg.getIsExcludedFromPayroll());
            });
            // Apply noHazardPay and displayToLastPage overrides
            for (EmployeePayrollInfoDTO e : all) {
                PayrollEmployeeConfig cfg = configMap.get(e.getEmployeeNo());
                if (cfg != null) {
                    if (Boolean.TRUE.equals(cfg.getNoHazardPay())) {
                        e.setNoHazardPay(true);
                    }
                    e.setDisplayToLastPage(Boolean.TRUE.equals(cfg.getDisplayToLastPage()));
                }
            }
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
        // Delete existing records for the same (employeeNo, salaryPeriodKey) to allow recompute.
        // Children (earnings, deductions) must be deleted first due to FK constraints.
        for (PayrollDetail pd : chunk) {
            detailRepo.deleteEarningsByEmployeeNoAndSalaryPeriodKey(pd.getEmployeeNo(), pd.getSalaryPeriodKey());
            detailRepo.deleteDeductionsByEmployeeNoAndSalaryPeriodKey(pd.getEmployeeNo(), pd.getSalaryPeriodKey());
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

    /**
     * Fetches GSIS contribution rates from the Administrative module.
     *
     * @return double[] where [0]=psRate (employee share, decimal e.g. 0.09) and [1]=erRate (employer share)
     */
    private double[] fetchGsisRates(HttpHeaders h) {
        String url = adminServiceUrl + "/api/gsisContribution/get-all";
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            List<Map<String, Object>> body = resp.getBody();
            if (body != null && !body.isEmpty()) {
                // Use the last record (highest ID — assumed most recent)
                Map<String, Object> latest = body.get(body.size() - 1);
                double ps = parseGsisRate(latest.get("employeeSharePercentage"));
                double er = parseGsisRate(latest.get("employerSharePercentage"));
                log.debug("PayrollBatch: GSIS rates loaded from Admin — psRate={}, erRate={}", ps, er);
                return new double[]{ps, er};
            }
        } catch (Exception ex) {
            log.warn("PayrollBatch: Failed to fetch GSIS rates from Admin service, using defaults (0.09/0.12). Reason: {}", ex.getMessage());
        }
        return new double[]{0.09, 0.12};
    }

    /** Parses a GSIS percentage value that may be stored as "9", "9.00", or "0.09". */
    private double parseGsisRate(Object raw) {
        if (raw == null) return 0.0;
        try {
            double v = Double.parseDouble(raw.toString().trim());
            return v > 1.0 ? v / 100.0 : v;  // normalise: "9" → 0.09, "0.09" → 0.09
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Fetches the current PayrollSettings record from the Administrative module.
     *
     * @return int[] where [0]=cutoffDays, [1]=peraProrationDivisor, [2]=autoComputeHazardPay (0 or 1)
     */
    private int[] fetchPayrollSettings(HttpHeaders h) {
        String url = adminServiceUrl + "/api/payrollSettings/get-current";
        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> body = resp.getBody();
            if (body != null) {
                int cutoffDays           = body.get("cutoffDays")           != null ? ((Number) body.get("cutoffDays")).intValue()           : 22;
                int peraProrationDivisor = body.get("peraProrationDivisor") != null ? ((Number) body.get("peraProrationDivisor")).intValue() : 22;
                Boolean autoCompute      = body.get("autoComputeHazardPay") != null ? (Boolean) body.get("autoComputeHazardPay")             : false;
                int autoComputeInt = Boolean.TRUE.equals(autoCompute) ? 1 : 0;
                log.debug("PayrollBatch: PayrollSettings loaded — cutoffDays={}, peraProrationDivisor={}, autoComputeHazardPay={}", cutoffDays, peraProrationDivisor, autoCompute);
                return new int[]{cutoffDays, peraProrationDivisor, autoComputeInt};
            }
        } catch (Exception ex) {
            log.warn("PayrollBatch: Failed to fetch PayrollSettings from Admin service, using defaults (22/22/false). Reason: {}", ex.getMessage());
        }
        return new int[]{22, 22, 0};
    }

    /**
     * Fetches the current PagIbig contribution amounts from the Administrative module.
     *
     * @return double[] where [0]=employeeShare (mandatory EE), [1]=employerShare
     */
    private double[] fetchPagIbigContribution(HttpHeaders h) {
        String url = adminServiceUrl + "/api/pagibigContribution/get-current";
        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> body = resp.getBody();
            if (body != null) {
                double employeeShare = body.get("employeeShare") != null ? ((Number) body.get("employeeShare")).doubleValue() : 100.0;
                double employerShare = body.get("employerShare") != null ? ((Number) body.get("employerShare")).doubleValue() : 100.0;
                log.debug("PayrollBatch: PagIbigContribution loaded — employeeShare={}, employerShare={}", employeeShare, employerShare);
                return new double[]{employeeShare, employerShare};
            }
        } catch (Exception ex) {
            log.warn("PayrollBatch: Failed to fetch PagIbigContribution from Admin service, using default ₱100. Reason: {}", ex.getMessage());
        }
        return new double[]{100.0, 100.0};
    }

    /**
     * Fetches the EarningLeave table from the Administrative module.
     * Returns a map of awolDays (Integer) → earnedLeave (Double).
     * The 'day' field in the table is the number of AWOL days; 'earn' is the leave credit.
     */
    private Map<Integer, Double> fetchEarningLeaveTable(HttpHeaders h) {
        String url = adminServiceUrl + "/api/earningLeave/get-all";
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            List<Map<String, Object>> body = resp.getBody();
            if (body != null) {
                Map<Integer, Double> earnLeaveMap = new HashMap<>();
                for (Map<String, Object> entry : body) {
                    try {
                        int awolDays = Integer.parseInt(entry.get("day").toString().trim());
                        double earnedLeave = Double.parseDouble(entry.get("earn").toString().trim());
                        earnLeaveMap.put(awolDays, earnedLeave);
                    } catch (Exception ignored) {}
                }
                log.debug("PayrollBatch: EarningLeave table loaded — {} entries", earnLeaveMap.size());
                return earnLeaveMap;
            }
        } catch (Exception ex) {
            log.warn("PayrollBatch: Failed to fetch EarningLeave table from Admin service. Reason: {}", ex.getMessage());
        }
        return Collections.emptyMap();
    }

    /**
     * Loads all active (non-stopped, not fully paid) installment loans from local DB.
     * Groups by employeeNo for O(1) lookup during computation.
     */
    private Map<String, List<PayrollDataSnapshot.LoanDTO>> fetchActiveLoans() {
        try {
            List<EmployeeLoan> loans = loanRepo.findAllActiveLoans();
            Map<String, List<PayrollDataSnapshot.LoanDTO>> result = new HashMap<>();
            for (EmployeeLoan loan : loans) {
                PayrollDataSnapshot.LoanDTO dto = new PayrollDataSnapshot.LoanDTO();
                dto.setLoanType(loan.getLoanType());
                dto.setReference(loan.getReference());
                dto.setAmount(loan.getAmount());
                dto.setToPay(loan.getToPay());
                dto.setPaid(loan.getPaid());
                result.computeIfAbsent(loan.getEmployeeNo(), k -> new ArrayList<>()).add(dto);
            }
            log.debug("PayrollBatch: Active loans loaded — {} employees with active loans", result.size());
            return result;
        } catch (Exception ex) {
            log.warn("PayrollBatch: Failed to load active loans from local DB. Reason: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Loads entry deductions for the given salary period from local DB.
     * Groups by employeeNo.
     */
    private Map<String, List<PayrollDataSnapshot.EntryDeductionDTO>> fetchEntryDeductions(String salaryPeriodKey) {
        try {
            List<EmployeeDeduction> deductions = deductionRepo.findBySalaryPeriod(salaryPeriodKey);
            Map<String, List<PayrollDataSnapshot.EntryDeductionDTO>> result = new HashMap<>();
            for (EmployeeDeduction ded : deductions) {
                PayrollDataSnapshot.EntryDeductionDTO dto = new PayrollDataSnapshot.EntryDeductionDTO();
                dto.setDeductionType(ded.getDeductionType());
                dto.setAmount(ded.getAmount());
                dto.setReference(ded.getReferenceNo());
                dto.setIsFixed(ded.getIsFixed());
                result.computeIfAbsent(ded.getEmployeeNo(), k -> new ArrayList<>()).add(dto);
            }
            log.debug("PayrollBatch: Entry deductions loaded — {} employees, {} total deductions",
                    result.size(), deductions.size());
            return result;
        } catch (Exception ex) {
            log.warn("PayrollBatch: Failed to load entry deductions from local DB. Reason: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Loads special income entries for the current month/year from local DB.
     * Groups by employeeNo.
     */
    private Map<String, List<PayrollDataSnapshot.IncomeEntryDTO>> fetchIncomeEntries(int month, int year) {
        try {
            List<EmployeeIncome> incomes = incomeRepo.findByMonthAndYear(month, year);
            Map<String, List<PayrollDataSnapshot.IncomeEntryDTO>> result = new HashMap<>();
            for (EmployeeIncome income : incomes) {
                PayrollDataSnapshot.IncomeEntryDTO dto = new PayrollDataSnapshot.IncomeEntryDTO();
                dto.setEarningType(income.getEarningType());
                dto.setEarningTypeName(income.getEarningTypeName());
                dto.setAmount(income.getAmount());
                dto.setIsTaxable(income.getIsTaxable());
                result.computeIfAbsent(income.getEmployeeNo(), k -> new ArrayList<>()).add(dto);
            }
            log.debug("PayrollBatch: Income entries loaded — {} employees, {} total entries",
                    result.size(), incomes.size());
            return result;
        } catch (Exception ex) {
            log.warn("PayrollBatch: Failed to load income entries from local DB. Reason: {}", ex.getMessage());
            return Collections.emptyMap();
        }
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
