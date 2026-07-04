package com.humanresource.impl;

import com.humanresource.dtos.LeaveInformationDTO;
import com.humanresource.dtos.LeaveProcessBatchStartResponseDTO;
import com.humanresource.dtos.LeaveProcessJobStatusDTO;
import com.humanresource.dtos.LeaveProcessQueueItemDTO;
import com.humanresource.dtos.LeaveProcessRequestDTO;
import com.humanresource.dtos.LeaveProcessResultDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.services.LeaveProcessBatchService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class LeaveProcessBatchServiceImpl implements LeaveProcessBatchService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_FETCHING = "FETCHING_DATA";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_DONE = "DONE";
    private static final String STATUS_FAILED = "FAILED";

    private static final int COMPUTE_CHUNK_SIZE = 50;

    private final LeaveProcessServiceImpl leaveProcessService;
    private final ExecutorService computeExecutor;
    private final ThreadPoolTaskExecutor batchAsyncExecutor;

    private final ConcurrentHashMap<String, JobState> jobStates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<LeaveProcessQueueItemDTO>> jobQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> jobQueueSeqs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LeaveProcessResultDTO> jobResults = new ConcurrentHashMap<>();

    public LeaveProcessBatchServiceImpl(
            LeaveProcessServiceImpl leaveProcessService,
            @Qualifier("leaveProcessComputeExecutor") ExecutorService computeExecutor,
            @Qualifier("leaveProcessBatchAsyncExecutor") ThreadPoolTaskExecutor batchAsyncExecutor) {
        this.leaveProcessService = leaveProcessService;
        this.computeExecutor = computeExecutor;
        this.batchAsyncExecutor = batchAsyncExecutor;
    }

    @Override
    public LeaveProcessBatchStartResponseDTO startBatch(LeaveProcessRequestDTO request) {
        if (request.getCutoffStartDate() == null || request.getCutoffEndDate() == null) {
            throw new IllegalArgumentException("cutoffStartDate and cutoffEndDate are required");
        }

        if (request.getScope() == null || request.getScope().isBlank()) {
            request.setScope("ALL");
        }

        final String jobId = UUID.randomUUID().toString();
        JobState state = new JobState();
        state.setJobId(jobId);
        state.setStatus(STATUS_PENDING);
        state.setProgressPct(0);
        state.setStartedAt(LocalDateTime.now());
        jobStates.put(jobId, state);
        jobQueues.put(jobId, Collections.synchronizedList(new ArrayList<>()));
        jobQueueSeqs.put(jobId, new AtomicInteger(0));

        batchAsyncExecutor.execute(() -> runBatchAsync(jobId, request));

        return new LeaveProcessBatchStartResponseDTO(jobId, "Leave processing job started");
    }

    @Override
    public LeaveProcessJobStatusDTO getJobStatus(String jobId) {
        JobState state = jobStates.get(jobId);
        if (state == null) {
            throw new IllegalArgumentException("Unknown job ID: " + jobId);
        }
        return toStatusDto(state);
    }

    @Override
    public List<LeaveProcessQueueItemDTO> getJobQueue(String jobId, int fromSeqNo) {
        List<LeaveProcessQueueItemDTO> queue = jobQueues.get(jobId);
        if (queue == null) {
            return Collections.emptyList();
        }
        return queue.stream()
                .filter(item -> item.getSeqNo() >= fromSeqNo)
                .sorted(Comparator.comparingInt(LeaveProcessQueueItemDTO::getSeqNo))
                .collect(Collectors.toList());
    }

    @Override
    public LeaveProcessResultDTO getJobResult(String jobId) {
        JobState state = jobStates.get(jobId);
        if (state == null) {
            throw new IllegalArgumentException("Unknown job ID: " + jobId);
        }
        if (!STATUS_DONE.equals(state.getStatus()) && !STATUS_FAILED.equals(state.getStatus())) {
            throw new IllegalStateException("Job is not finished yet");
        }
        return jobResults.get(jobId);
    }

    @Async("leaveProcessBatchAsyncExecutor")
    protected void runBatchAsync(String jobId, LeaveProcessRequestDTO req) {
        JobState state = jobStates.get(jobId);
        if (state == null) {
            return;
        }

        try {
            updateState(state, STATUS_FETCHING, 5, null, null);

            final LocalDate periodStart = req.getCutoffStartDate();
            final LocalDate periodEnd = req.getCutoffEndDate();
            final Set<LocalDate> holidayDates = leaveProcessService.loadHolidayDates(periodStart, periodEnd);

            List<Employee> employees = leaveProcessService.resolveEmployeesForRequest(req);

            state.setTotalEmployees(employees.size());
            updateState(state, STATUS_PROCESSING, 10, null, null);

            List<LeaveInformationDTO> processedList = Collections.synchronizedList(new ArrayList<>());
            List<String> skippedReasons = Collections.synchronizedList(new ArrayList<>());

            AtomicInteger doneCount = new AtomicInteger(0);
            AtomicInteger skippedCount = new AtomicInteger(0);

            List<List<Employee>> chunks = partition(employees, COMPUTE_CHUNK_SIZE);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            List<LeaveProcessQueueItemDTO> queue = jobQueues.get(jobId);
            AtomicInteger queueSeq = jobQueueSeqs.get(jobId);

            for (List<Employee> chunk : chunks) {
                CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                    for (Employee emp : chunk) {
                        String empNo = emp.getEmployeeNo();
                        String empName = (emp.getLastname() != null ? emp.getLastname() : "") +
                                ", " +
                                (emp.getFirstname() != null ? emp.getFirstname() : "");
                        String empLabel = empNo + " - " + (emp.getLastname() != null ? emp.getLastname() : "");

                        List<String> localSkipped = new ArrayList<>();
                        String queueStatus = "OK";
                        String queueMessage = "Processed";

                        try {
                            LeaveInformationDTO dto = leaveProcessService.processEmployee(
                                    emp,
                                    periodStart,
                                    periodEnd,
                                    req.getSalaryPeriodSettingId(),
                                    req.getProcessedById(),
                                    holidayDates,
                                    localSkipped,
                                    empLabel
                            );

                            if (dto != null) {
                                processedList.add(dto);
                            } else {
                                skippedCount.incrementAndGet();
                                queueStatus = "SKIPPED";
                                queueMessage = localSkipped.isEmpty() ? "Skipped by guard rules" : localSkipped.get(0);
                                if (!localSkipped.isEmpty()) {
                                    skippedReasons.add(localSkipped.get(0));
                                }
                            }
                        } catch (Exception ex) {
                            skippedCount.incrementAndGet();
                            queueStatus = "FAILED";
                            queueMessage = "Unexpected error: " + ex.getMessage();
                            skippedReasons.add(empLabel + ": " + queueMessage);
                        }

                        if (queue != null && queueSeq != null) {
                            queue.add(new LeaveProcessQueueItemDTO(
                                    queueSeq.getAndIncrement(),
                                    emp.getEmployeeId(),
                                    empNo,
                                    empName,
                                    queueStatus,
                                    queueMessage
                            ));
                        }

                        int done = doneCount.incrementAndGet();
                        int pct = employees.isEmpty() ? 100 : 10 + (int) ((double) done / employees.size() * 85);
                        state.setProcessedEmployees(done);
                        state.setSkippedEmployees(skippedCount.get());
                        state.setProgressPct(Math.min(pct, 95));
                    }
                }, computeExecutor);
                futures.add(f);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            LeaveProcessResultDTO result = new LeaveProcessResultDTO(
                    processedList.size(),
                    skippedReasons.size(),
                    skippedReasons,
                    processedList
            );
            jobResults.put(jobId, result);

            String summary = String.format(
                    "%d processed, %d skipped",
                    result.getTotalProcessed(),
                    result.getTotalSkipped()
            );
            updateState(state, STATUS_DONE, 100, summary, null);
        } catch (Exception ex) {
            LeaveProcessResultDTO failedResult = new LeaveProcessResultDTO(
                    0,
                    1,
                    List.of("Server error: " + ex.getMessage()),
                    List.of()
            );
            jobResults.put(jobId, failedResult);
            updateState(state, STATUS_FAILED, 100, null, ex.getMessage());
        }
    }

    private void updateState(JobState state, String status, int progressPct, String summary, String errorDetail) {
        state.setStatus(status);
        state.setProgressPct(progressPct);
        if (summary != null) {
            state.setSummary(summary);
        }
        if (errorDetail != null) {
            state.setErrorDetail(errorDetail);
        }
        if (STATUS_DONE.equals(status) || STATUS_FAILED.equals(status)) {
            state.setFinishedAt(LocalDateTime.now());
        }
    }

    private LeaveProcessJobStatusDTO toStatusDto(JobState state) {
        LeaveProcessJobStatusDTO dto = new LeaveProcessJobStatusDTO();
        dto.setJobId(state.getJobId());
        dto.setStatus(state.getStatus());
        dto.setProgressPct(state.getProgressPct());
        dto.setTotalEmployees(state.getTotalEmployees());
        dto.setProcessedEmployees(state.getProcessedEmployees());
        dto.setSkippedEmployees(state.getSkippedEmployees());
        dto.setStartedAt(state.getStartedAt());
        dto.setFinishedAt(state.getFinishedAt());
        dto.setSummary(state.getSummary());
        dto.setErrorDetail(state.getErrorDetail());
        return dto;
    }

    private List<List<Employee>> partition(List<Employee> source, int size) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        List<List<Employee>> out = new ArrayList<>();
        for (int i = 0; i < source.size(); i += size) {
            out.add(source.subList(i, Math.min(i + size, source.size())));
        }
        return out;
    }

    private static class JobState {
        private String jobId;
        private String status;
        private Integer progressPct;
        private Integer totalEmployees = 0;
        private Integer processedEmployees = 0;
        private Integer skippedEmployees = 0;
        private LocalDateTime startedAt;
        private LocalDateTime finishedAt;
        private String summary;
        private String errorDetail;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getProgressPct() {
            return progressPct;
        }

        public void setProgressPct(Integer progressPct) {
            this.progressPct = progressPct;
        }

        public Integer getTotalEmployees() {
            return totalEmployees;
        }

        public void setTotalEmployees(Integer totalEmployees) {
            this.totalEmployees = totalEmployees;
        }

        public Integer getProcessedEmployees() {
            return processedEmployees;
        }

        public void setProcessedEmployees(Integer processedEmployees) {
            this.processedEmployees = processedEmployees;
        }

        public Integer getSkippedEmployees() {
            return skippedEmployees;
        }

        public void setSkippedEmployees(Integer skippedEmployees) {
            this.skippedEmployees = skippedEmployees;
        }

        public LocalDateTime getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
        }

        public LocalDateTime getFinishedAt() {
            return finishedAt;
        }

        public void setFinishedAt(LocalDateTime finishedAt) {
            this.finishedAt = finishedAt;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getErrorDetail() {
            return errorDetail;
        }

        public void setErrorDetail(String errorDetail) {
            this.errorDetail = errorDetail;
        }
    }
}
