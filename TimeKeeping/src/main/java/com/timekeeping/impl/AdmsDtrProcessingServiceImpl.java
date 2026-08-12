package com.timekeeping.impl;

import com.timekeeping.dtos.AdmsDtrProcessResultDTO;
import com.timekeeping.entitymodels.AdmsPunchLog;
import com.timekeeping.entitymodels.DTRDaily;
import com.timekeeping.entitymodels.DTRSegment;
import com.timekeeping.entitymodels.WorkSchedule;
import com.timekeeping.repositories.AdmsPunchLogRepository;
import com.timekeeping.repositories.DTRDailyRepository;
import com.timekeeping.repositories.DTRSegmentRepository;
import com.timekeeping.repositories.WorkScheduleRepository;
import com.timekeeping.services.AdmsDtrProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdmsDtrProcessingServiceImpl implements AdmsDtrProcessingService {

    private static final Logger log = LoggerFactory.getLogger(AdmsDtrProcessingServiceImpl.class);

    private static final String IMPORTED = "IMPORTED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PENDING_AUTHORITY = "PENDING_AUTHORITY";
    private static final String STATUS_PROCESSED = "PROCESSED";
    private static final String STATUS_DUPLICATE = "DUPLICATE";
    private static final String STATUS_CONFLICT = "CONFLICT";
    private static final String STATUS_SUPPRESSED_BY_REQUEST = "SUPPRESSED_BY_REQUEST";
    private static final String SOURCE_ADMS = "ADMS";

    private final AdmsPunchLogRepository punchLogRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final DTRDailyRepository dtrDailyRepository;
    private final DTRSegmentRepository dtrSegmentRepository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${adms.dtr.enabled:true}")
    private boolean dtrProcessingEnabled;

    @Value("${adms.dtr.early-window-minutes:360}")
    private int earlyWindowMinutes;

    @Value("${adms.dtr.late-window-minutes:480}")
    private int lateWindowMinutes;

    @Value("${adms.dtr.final-out-early-tolerance-minutes:120}")
    private int finalOutEarlyToleranceMinutes;

    @Value("${adms.dtr.settle-grace-minutes:30}")
    private int settleGraceMinutes;

    public AdmsDtrProcessingServiceImpl(
            AdmsPunchLogRepository punchLogRepository,
            WorkScheduleRepository workScheduleRepository,
            DTRDailyRepository dtrDailyRepository,
            DTRSegmentRepository dtrSegmentRepository,
            JdbcTemplate jdbcTemplate) {
        this.punchLogRepository = punchLogRepository;
        this.workScheduleRepository = workScheduleRepository;
        this.dtrDailyRepository = dtrDailyRepository;
        this.dtrSegmentRepository = dtrSegmentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public synchronized AdmsDtrProcessResultDTO processPendingPunches() {
        AdmsDtrProcessResultDTO result = new AdmsDtrProcessResultDTO();

        if (!dtrProcessingEnabled) {
            return result;
        }

        List<AdmsPunchLog> pending = punchLogRepository
                .findByImportStatusAndDtrProcessedFalseAndEmployeeIdIsNotNullOrderByEmployeeIdAscCheckTimeAscAdmsPunchLogIdAsc(
                        IMPORTED
                );

        if (pending.isEmpty()) {
            return result;
        }

        Map<String, List<AdmsPunchLog>> pendingByEmployee = pending.stream()
                .filter(logRow -> logRow.getEmployeeId() != null && logRow.getCheckTime() != null)
                .collect(Collectors.groupingBy(
                        AdmsPunchLog::getEmployeeId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, List<AdmsPunchLog>> entry : pendingByEmployee.entrySet()) {
            String employeeId = entry.getKey();
            List<AdmsPunchLog> triggerPunches = entry.getValue();

            LocalDate earliestTriggerDate = triggerPunches.stream()
                    .map(AdmsPunchLog::getCheckTime)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElseThrow()
                    .toLocalDate();
            LocalDate latestTriggerDate = triggerPunches.stream()
                    .map(AdmsPunchLog::getCheckTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElseThrow()
                    .toLocalDate();

            // A newly-arrived punch can change the meaning of punches that were already
            // finalized earlier. Example: 0,1 was first treated as Time In/Time Out;
            // a later 0 means the earlier 1 was actually Break Out. Reload the complete
            // nearby ADMS history so the existing segment can be rebuilt safely.
            LocalDateTime historyFrom = earliestTriggerDate.minusDays(1).atStartOfDay();
            LocalDateTime historyTo = latestTriggerDate.plusDays(2).atStartOfDay().minusNanos(1);
            List<AdmsPunchLog> relevantHistory = punchLogRepository
                    .findByImportStatusAndEmployeeIdAndCheckTimeBetweenOrderByCheckTimeAscAdmsPunchLogIdAsc(
                            IMPORTED,
                            employeeId,
                            historyFrom,
                            historyTo
                    );

            Set<Long> triggerPunchIds = triggerPunches.stream()
                    .map(AdmsPunchLog::getAdmsPunchLogId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            result.setEmployeesReviewed(result.getEmployeesReviewed() + 1);
            processEmployee(employeeId, relevantHistory, triggerPunchIds, now, result);
        }

        result.setPendingPunches((int) punchLogRepository.countByImportStatusAndDtrProcessedFalse(IMPORTED));
        return result;
    }

    @Override
    @Transactional
    public synchronized AdmsDtrProcessResultDTO processEmployeePunches(
            String employeeId,
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        AdmsDtrProcessResultDTO result = new AdmsDtrProcessResultDTO();

        if (!dtrProcessingEnabled || employeeId == null || employeeId.isBlank()
                || fromDate == null || toDate == null) {
            return result;
        }
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate must not be before fromDate");
        }

        LocalDate requestedFrom = fromDate.toLocalDate();
        LocalDate requestedTo = toDate.toLocalDate();

        // Approved employee requests are authoritative attendance sources. Purge any
        // existing DTR transactions for those dates before reading ADMS history so
        // Search also removes a Manual/legacy DTR even when no biometric punch exists.
        Map<LocalDate, String> authoritativeRequestDates = findAuthoritativeRequestDates(
                employeeId, requestedFrom, requestedTo);
        suppressDtrForAuthoritativeRequests(employeeId, authoritativeRequestDates);

        // Include nearby punches so an overnight Time Out and a late-arriving punch
        // can rebuild the selected work date. Processing is still limited to the
        // user's requested work-date range below.
        LocalDateTime historyFrom = requestedFrom.minusDays(1).atStartOfDay();
        LocalDateTime historyTo = requestedTo.plusDays(2).atStartOfDay().minusNanos(1);

        List<AdmsPunchLog> relevantHistory = punchLogRepository
                .findByImportStatusAndEmployeeIdAndCheckTimeBetweenOrderByCheckTimeAscAdmsPunchLogIdAsc(
                        IMPORTED,
                        employeeId,
                        historyFrom,
                        historyTo
                );

        if (relevantHistory.isEmpty()) {
            return result;
        }

        // Every imported punch on a selected work date acts as a reconciliation
        // trigger, even when it was processed before. This lets Search correct an
        // earlier provisional Time Out after a later Break In/final Time Out arrives.
        LocalDateTime triggerFrom = requestedFrom.atStartOfDay();
        LocalDateTime triggerTo = requestedTo.plusDays(1).atStartOfDay().minusNanos(1);
        Set<Long> triggerPunchIds = relevantHistory.stream()
                .filter(row -> row.getAdmsPunchLogId() != null && row.getCheckTime() != null)
                .filter(row -> !row.getCheckTime().isBefore(triggerFrom)
                        && !row.getCheckTime().isAfter(triggerTo))
                .map(AdmsPunchLog::getAdmsPunchLogId)
                .collect(Collectors.toSet());

        if (triggerPunchIds.isEmpty()) {
            return result;
        }

        result.setEmployeesReviewed(1);
        processEmployee(
                employeeId,
                relevantHistory,
                triggerPunchIds,
                LocalDateTime.now(),
                result,
                requestedFrom,
                requestedTo,
                authoritativeRequestDates
        );

        result.setPendingPunches((int) relevantHistory.stream()
                .filter(row -> !Boolean.TRUE.equals(row.getDtrProcessed()))
                .count());
        return result;
    }

    private void processEmployee(
            String employeeId,
            List<AdmsPunchLog> employeePunches,
            Set<Long> triggerPunchIds,
            LocalDateTime now,
            AdmsDtrProcessResultDTO result) {
        LocalDate requestFrom = employeePunches.stream()
                .map(AdmsPunchLog::getCheckTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElseThrow()
                .toLocalDate()
                .minusDays(1);
        LocalDate requestTo = employeePunches.stream()
                .map(AdmsPunchLog::getCheckTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElseThrow()
                .toLocalDate()
                .plusDays(1);

        Map<LocalDate, String> authoritativeRequestDates = findAuthoritativeRequestDates(
                employeeId, requestFrom, requestTo);
        suppressDtrForAuthoritativeRequests(employeeId, authoritativeRequestDates);

        processEmployee(
                employeeId,
                employeePunches,
                triggerPunchIds,
                now,
                result,
                null,
                null,
                authoritativeRequestDates
        );
    }

    private void processEmployee(
            String employeeId,
            List<AdmsPunchLog> employeePunches,
            Set<Long> triggerPunchIds,
            LocalDateTime now,
            AdmsDtrProcessResultDTO result,
            LocalDate requestedFrom,
            LocalDate requestedTo,
            Map<LocalDate, String> authoritativeRequestDates) {

        employeePunches.sort(Comparator
                .comparing(AdmsPunchLog::getCheckTime)
                .thenComparing(AdmsPunchLog::getAdmsPunchLogId));

        LocalDate minimumDate = employeePunches.get(0).getCheckTime().toLocalDate().minusDays(1);
        LocalDate maximumDate = employeePunches.get(employeePunches.size() - 1).getCheckTime().toLocalDate().plusDays(1);

        List<ShiftPlan> scheduledPlans = loadScheduledPlans(employeeId, minimumDate, maximumDate);
        Set<Long> reservedPunchIds = new HashSet<>();
        Set<LocalDate> datesHavingRegularSchedule = scheduledPlans.stream()
                .map(ShiftPlan::workDate)
                .collect(Collectors.toSet());

        applyPlanBoundaries(scheduledPlans);

        for (ShiftPlan plan : scheduledPlans) {
            if (!isRequestedWorkDate(plan.workDate(), requestedFrom, requestedTo)) {
                continue;
            }

            String authoritativeRequest = authoritativeRequestDates.get(plan.workDate());
            if (authoritativeRequest != null) {
                List<AdmsPunchLog> suppressedPunches = availablePunches(
                        employeePunches, reservedPunchIds, plan.windowStart(), plan.windowEnd());
                suppressedPunches.stream()
                        .map(AdmsPunchLog::getAdmsPunchLogId)
                        .filter(Objects::nonNull)
                        .forEach(reservedPunchIds::add);
                markSuppressedByRequest(suppressedPunches, plan.workDate(), authoritativeRequest);
                continue;
            }

            if (isNonWorkingHoliday(plan.workDate())) {
                continue;
            }

            List<AdmsPunchLog> available = availablePunches(employeePunches, reservedPunchIds, plan.windowStart(), plan.windowEnd());
            boolean affectedByNewPunch = available.stream()
                    .map(AdmsPunchLog::getAdmsPunchLogId)
                    .anyMatch(triggerPunchIds::contains);
            if (!affectedByNewPunch) {
                continue;
            }

            Candidate candidate = evaluateCandidate(plan, available, now);
            handleCandidate(employeeId, plan, candidate, reservedPunchIds, result);
        }

        // Remaining punches are handled by date. This covers employees without a plotted
        // schedule and approved holiday/rest-day duty.
        Map<LocalDate, List<AdmsPunchLog>> remainingByDate = employeePunches.stream()
                .filter(row -> !reservedPunchIds.contains(row.getAdmsPunchLogId()))
                .collect(Collectors.groupingBy(
                        row -> row.getCheckTime().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (Map.Entry<LocalDate, List<AdmsPunchLog>> dateEntry : remainingByDate.entrySet()) {
            LocalDate workDate = dateEntry.getKey();
            List<AdmsPunchLog> datePunches = dateEntry.getValue();

            if (!isRequestedWorkDate(workDate, requestedFrom, requestedTo)) {
                continue;
            }

            String authoritativeRequest = authoritativeRequestDates.get(workDate);
            if (authoritativeRequest != null) {
                markSuppressedByRequest(datePunches, workDate, authoritativeRequest);
                continue;
            }

            boolean affectedByNewPunch = datePunches.stream()
                    .map(AdmsPunchLog::getAdmsPunchLogId)
                    .anyMatch(triggerPunchIds::contains);
            if (!affectedByNewPunch) {
                continue;
            }

            if (datesHavingRegularSchedule.contains(workDate)) {
                markPending(datePunches, STATUS_PENDING, "Waiting for the complete punch sequence for the plotted work schedule.");
                continue;
            }

            boolean dayOff = isDayOff(employeeId, workDate);
            boolean holiday = isNonWorkingHoliday(workDate);

            if (dayOff || holiday) {
                Optional<DutyAuthority> authority = findApprovedDutyAuthority(employeeId, workDate, datePunches, dayOff, holiday);
                if (authority.isEmpty()) {
                    markPending(
                            datePunches,
                            STATUS_PENDING_AUTHORITY,
                            "Biometric punches are on a non-working day but no matching approved Overtime/Duty Order was found."
                    );
                    result.setConflicts(result.getConflicts() + 1);
                    continue;
                }

                int segmentNo = resolveReconciliationSegmentNo(employeeId, workDate, datePunches);
                ShiftPlan dutyPlan = authorityPlan(workDate, authority.get(), segmentNo);
                Candidate candidate = evaluateCandidate(dutyPlan, datePunches, now);
                candidate = candidate.withNonWorkingDuty(true);
                handleCandidate(employeeId, dutyPlan, candidate, reservedPunchIds, result);
                continue;
            }

            ShiftPlan fallbackPlan = chooseFallbackPlan(workDate, employeeId, datePunches, now);
            Candidate candidate = evaluateCandidate(fallbackPlan, datePunches, now);
            handleCandidate(employeeId, fallbackPlan, candidate, reservedPunchIds, result);
        }

        // Give every still-unprocessed row a useful status for troubleshooting.
        List<AdmsPunchLog> stillPending = employeePunches.stream()
                .filter(row -> !Boolean.TRUE.equals(row.getDtrProcessed()))
                .filter(row -> row.getDtrProcessingStatus() == null || row.getDtrProcessingStatus().isBlank())
                .toList();
        markPending(stillPending, STATUS_PENDING, "Waiting for a valid Check In and final Check Out sequence.");
    }

    private boolean isRequestedWorkDate(
            LocalDate workDate,
            LocalDate requestedFrom,
            LocalDate requestedTo) {
        if (requestedFrom == null || requestedTo == null) {
            return true;
        }
        return !workDate.isBefore(requestedFrom) && !workDate.isAfter(requestedTo);
    }

    private List<ShiftPlan> loadScheduledPlans(String employeeId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.plusDays(1).atStartOfDay().minusNanos(1);

        Optional<List<WorkSchedule>> optional = workScheduleRepository
                .findByEmployeeIdAndWsDateTimeBetweenOrderByWsDateTimeAscWsIdAsc(employeeId, from, to);

        if (optional.isEmpty()) {
            return new ArrayList<>();
        }

        Map<LocalDate, Integer> segmentCounters = new HashMap<>();
        List<ShiftPlan> plans = new ArrayList<>();

        optional.get().stream()
                .filter(schedule -> !Boolean.TRUE.equals(schedule.getIsDayOff()))
                .filter(schedule -> schedule.getTsCode() != null && !schedule.getTsCode().isBlank())
                .sorted(Comparator
                        .comparing(WorkSchedule::getWsDateTime)
                        .thenComparing(WorkSchedule::getWsId, Comparator.nullsLast(Long::compareTo)))
                .forEach(schedule -> {
                    LocalDate workDate = schedule.getWsDateTime().toLocalDate();
                    ShiftTemplate template = loadShiftTemplate(schedule.getTsCode());
                    if (template == null) {
                        return;
                    }
                    int segmentNo = segmentCounters.merge(workDate, 1, Integer::sum);
                    plans.add(createPlan(workDate, segmentNo, template, false, "PLOTTED:" + schedule.getTsCode()));
                });

        plans.sort(Comparator.comparing(ShiftPlan::plannedStart));
        return plans;
    }

    private void applyPlanBoundaries(List<ShiftPlan> plans) {
        for (int index = 0; index < plans.size(); index++) {
            ShiftPlan current = plans.get(index);
            LocalDateTime windowStart = current.plannedStart().minusMinutes(Math.max(0, earlyWindowMinutes));
            LocalDateTime windowEnd = current.plannedEnd().plusMinutes(Math.max(0, lateWindowMinutes));

            if (index > 0) {
                ShiftPlan previous = plans.get(index - 1);
                if (!previous.plannedEnd().isAfter(current.plannedStart())) {
                    LocalDateTime midpoint = midpoint(previous.plannedEnd(), current.plannedStart());
                    if (midpoint.isAfter(windowStart)) {
                        windowStart = midpoint;
                    }
                }
            }

            if (index + 1 < plans.size()) {
                ShiftPlan next = plans.get(index + 1);
                if (!next.plannedStart().isBefore(current.plannedEnd())) {
                    LocalDateTime midpoint = midpoint(current.plannedEnd(), next.plannedStart());
                    if (midpoint.isBefore(windowEnd)) {
                        windowEnd = midpoint;
                    }
                }
            }

            plans.set(index, current.withWindow(windowStart, windowEnd));
        }
    }

    private LocalDateTime midpoint(LocalDateTime from, LocalDateTime to) {
        long minutes = ChronoUnit.MINUTES.between(from, to);
        return from.plusMinutes(Math.max(0, minutes / 2));
    }

    private List<AdmsPunchLog> availablePunches(
            List<AdmsPunchLog> punches,
            Set<Long> reservedPunchIds,
            LocalDateTime from,
            LocalDateTime to) {
        return punches.stream()
                .filter(row -> !reservedPunchIds.contains(row.getAdmsPunchLogId()))
                .filter(row -> !row.getCheckTime().isBefore(from) && !row.getCheckTime().isAfter(to))
                .sorted(Comparator
                        .comparing(AdmsPunchLog::getCheckTime)
                        .thenComparing(AdmsPunchLog::getAdmsPunchLogId))
                .toList();
    }

    private Candidate evaluateCandidate(ShiftPlan plan, List<AdmsPunchLog> punches, LocalDateTime now) {
        if (punches.isEmpty()) {
            return Candidate.noData();
        }

        AdmsPunchLog timeIn = punches.stream()
                .filter(row -> Integer.valueOf(0).equals(row.getCheckType()))
                .findFirst()
                .orElse(null);

        if (timeIn == null) {
            return Candidate.pending(punches, "No Check In punch was found for this work period.");
        }

        List<AdmsPunchLog> punchesAfterTimeIn = punches.stream()
                .filter(row -> !row.getCheckTime().isBefore(timeIn.getCheckTime()))
                .toList();

        if (!plan.template().hasBreak()) {
            AdmsPunchLog timeOut = latestOutAfter(punchesAfterTimeIn, timeIn.getCheckTime());
            if (timeOut == null) {
                return Candidate.pending(punchesAfterTimeIn, "Waiting for the final Check Out punch.");
            }
            if (!canFinalizeTwoPunchSequence(plan, timeOut.getCheckTime(), now)) {
                return Candidate.pending(punchesAfterTimeIn, "Check Out received, but the shift is still active. Waiting for the final Check Out.");
            }
            return completeCandidate(plan, punchesAfterTimeIn, timeIn, null, null, timeOut);
        }

        AdmsPunchLog firstOut = punchesAfterTimeIn.stream()
                .filter(row -> Integer.valueOf(1).equals(row.getCheckType()))
                .filter(row -> row.getCheckTime().isAfter(timeIn.getCheckTime()))
                .findFirst()
                .orElse(null);

        if (firstOut == null) {
            return Candidate.pending(punchesAfterTimeIn, "Waiting for Check Out / Break Out.");
        }

        AdmsPunchLog firstInAfterOut = punchesAfterTimeIn.stream()
                .filter(row -> Integer.valueOf(0).equals(row.getCheckType()))
                .filter(row -> row.getCheckTime().isAfter(firstOut.getCheckTime()))
                .findFirst()
                .orElse(null);

        if (firstInAfterOut != null) {
            AdmsPunchLog breakOut = punchesAfterTimeIn.stream()
                    .filter(row -> Integer.valueOf(1).equals(row.getCheckType()))
                    .filter(row -> row.getCheckTime().isAfter(timeIn.getCheckTime()))
                    .filter(row -> !row.getCheckTime().isAfter(firstInAfterOut.getCheckTime()))
                    .reduce((first, second) -> second)
                    .orElse(firstOut);

            AdmsPunchLog breakIn = punchesAfterTimeIn.stream()
                    .filter(row -> Integer.valueOf(0).equals(row.getCheckType()))
                    .filter(row -> row.getCheckTime().isAfter(breakOut.getCheckTime()))
                    .findFirst()
                    .orElse(firstInAfterOut);

            AdmsPunchLog timeOut = latestOutAfter(punchesAfterTimeIn, breakIn.getCheckTime());
            if (timeOut == null) {
                return Candidate.pending(punchesAfterTimeIn, "Break In was received, but the final Check Out is still missing.");
            }

            return completeCandidate(plan, punchesAfterTimeIn, timeIn, breakOut, breakIn, timeOut);
        }

        // A two-punch day is valid when there was no break punch, but a lunch
        // Break Out must not be finalized as Time Out while the shift is active.
        AdmsPunchLog latestOut = latestOutAfter(punchesAfterTimeIn, timeIn.getCheckTime());
        if (latestOut == null || !canFinalizeTwoPunchSequence(plan, latestOut.getCheckTime(), now)) {
            return Candidate.pending(punchesAfterTimeIn, "Possible Break Out detected. Waiting for Break In or final Check Out.");
        }

        return completeCandidate(plan, punchesAfterTimeIn, timeIn, null, null, latestOut);
    }

    private Candidate completeCandidate(
            ShiftPlan plan,
            List<AdmsPunchLog> windowPunches,
            AdmsPunchLog timeIn,
            AdmsPunchLog breakOut,
            AdmsPunchLog breakIn,
            AdmsPunchLog timeOut) {
        List<AdmsPunchLog> consumed = windowPunches.stream()
                .filter(row -> !row.getCheckTime().isBefore(timeIn.getCheckTime()))
                .filter(row -> !row.getCheckTime().isAfter(timeOut.getCheckTime()))
                .toList();

        if (breakOut != null && breakIn != null) {
            if (!(timeIn.getCheckTime().isBefore(breakOut.getCheckTime())
                    && breakOut.getCheckTime().isBefore(breakIn.getCheckTime())
                    && breakIn.getCheckTime().isBefore(timeOut.getCheckTime()))) {
                return Candidate.pending(consumed, "Punch order is invalid. Expected Time In → Break Out → Break In → Time Out.");
            }
        } else if (!timeIn.getCheckTime().isBefore(timeOut.getCheckTime())) {
            return Candidate.pending(consumed, "Time Out must occur after Time In.");
        }

        return Candidate.complete(
                consumed,
                timeIn.getCheckTime(),
                breakOut == null ? null : breakOut.getCheckTime(),
                breakIn == null ? null : breakIn.getCheckTime(),
                timeOut.getCheckTime(),
                plan.nonWorkingDuty()
        );
    }

    private AdmsPunchLog latestOutAfter(List<AdmsPunchLog> punches, LocalDateTime after) {
        return punches.stream()
                .filter(row -> Integer.valueOf(1).equals(row.getCheckType()))
                .filter(row -> row.getCheckTime().isAfter(after))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private boolean canFinalizeTwoPunchSequence(ShiftPlan plan, LocalDateTime actualOut, LocalDateTime now) {
        LocalDateTime nearPlannedEnd = plan.plannedEnd().minusMinutes(Math.max(0, finalOutEarlyToleranceMinutes));
        LocalDateTime settledAt = plan.plannedEnd().plusMinutes(Math.max(0, settleGraceMinutes));
        return !actualOut.isBefore(nearPlannedEnd) || !now.isBefore(settledAt);
    }

    private void handleCandidate(
            String employeeId,
            ShiftPlan plan,
            Candidate candidate,
            Set<Long> reservedPunchIds,
            AdmsDtrProcessResultDTO result) {

        if (candidate.state() == CandidateState.NO_DATA) {
            return;
        }

        candidate.punches().forEach(row -> reservedPunchIds.add(row.getAdmsPunchLogId()));

        if (candidate.state() == CandidateState.PENDING) {
            ProgressSnapshot snapshot = buildProgressSnapshot(plan, candidate.punches());
            if (snapshot != null) {
                upsertProgressSegment(employeeId, plan, snapshot, result);
            }
            markPending(candidate.punches(), STATUS_PENDING, candidate.message());
            return;
        }

        DTRDaily daily = dtrDailyRepository.findByEmployeeIdAndWorkDate(employeeId, plan.workDate())
                .orElseGet(() -> createDaily(employeeId, plan.workDate()));

        List<DTRSegment> sortedSegments = daily.getSegments() == null
                ? new ArrayList<>()
                : daily.getSegments().stream()
                .sorted(Comparator.comparing(DTRSegment::getSegmentNo))
                .collect(Collectors.toCollection(ArrayList::new));

        Optional<DTRSegment> existingAtPosition = sortedSegments.stream()
                .filter(segment -> Objects.equals(segment.getSegmentNo(), plan.segmentNo()))
                .findFirst();

        if (existingAtPosition.isPresent()) {
            DTRSegment existing = existingAtPosition.get();

            // A partial ADMS segment is intentionally updated as more punches arrive.
            if (SOURCE_ADMS.equalsIgnoreCase(existing.getSourceType())) {
                ComputedMinutes minutes = computeMinutes(plan, candidate);
                applyCompleteCandidate(existing, candidate, minutes);
                DTRSegment savedSegment = dtrSegmentRepository.save(existing);
                daily.setAttendanceStatus("Present");
                recomputeDailyTotals(daily);
                DTRDaily savedDaily = dtrDailyRepository.save(daily);

                markProcessed(candidate.punches(), STATUS_PROCESSED,
                        "Updated the live ADMS DTR segment with the completed punch sequence.",
                        savedDaily, savedSegment);

                result.setPunchesProcessed(result.getPunchesProcessed() + candidate.punches().size());
                return;
            }

            if (sameTimes(existing, candidate)) {
                markProcessed(candidate.punches(), STATUS_DUPLICATE,
                        "The same DTR segment already exists.", daily, existing);
                result.setDuplicates(result.getDuplicates() + 1);
                result.setPunchesProcessed(result.getPunchesProcessed() + candidate.punches().size());
            } else {
                markPending(candidate.punches(), STATUS_CONFLICT,
                        "A manual or different DTR segment already occupies segment " + plan.segmentNo()
                                + " for this date. Review the manual and biometric entries.");
                result.setConflicts(result.getConflicts() + 1);
            }
            return;
        }

        ComputedMinutes minutes = computeMinutes(plan, candidate);

        DTRSegment segment = new DTRSegment();
        segment.setDtrDaily(daily);
        segment.setSegmentNo(plan.segmentNo());
        segment.setSourceType(SOURCE_ADMS);
        applyCompleteCandidate(segment, candidate, minutes);

        DTRSegment savedSegment = dtrSegmentRepository.save(segment);
        if (daily.getSegments() == null) {
            daily.setSegments(new ArrayList<>());
        }
        daily.getSegments().add(savedSegment);
        daily.setAttendanceStatus("Present");
        recomputeDailyTotals(daily);
        DTRDaily savedDaily = dtrDailyRepository.save(daily);

        markProcessed(candidate.punches(), STATUS_PROCESSED,
                "Converted ADMS punches into HRIS DTR segment " + plan.segmentNo() + ".",
                savedDaily, savedSegment);

        result.setSegmentsCreated(result.getSegmentsCreated() + 1);
        result.setPunchesProcessed(result.getPunchesProcessed() + candidate.punches().size());
    }

    private void applyCompleteCandidate(
            DTRSegment segment,
            Candidate candidate,
            ComputedMinutes minutes) {
        segment.setSourceType(SOURCE_ADMS);
        segment.setTimeIn(candidate.timeIn().toLocalTime());
        segment.setBreakOut(candidate.breakOut() == null ? null : candidate.breakOut().toLocalTime());
        segment.setBreakIn(candidate.breakIn() == null ? null : candidate.breakIn().toLocalTime());
        segment.setTimeOut(candidate.timeOut().toLocalTime());
        segment.setWorkMinutes(minutes.workMinutes());
        segment.setLateMinutes(minutes.lateMinutes());
        segment.setUndertimeMinutes(minutes.undertimeMinutes());
        segment.setOvertimeMinutes(minutes.overtimeMinutes());
    }

    /**
     * Builds the best current interpretation of an incomplete punch sequence.
     * This allows employees to see their Time In / Break Out / Break In immediately,
     * while the final Time Out and final computation remain open.
     */
    private ProgressSnapshot buildProgressSnapshot(ShiftPlan plan, List<AdmsPunchLog> punches) {
        if (punches == null || punches.isEmpty()) {
            return null;
        }

        List<AdmsPunchLog> ordered = punches.stream()
                .filter(row -> row.getCheckTime() != null)
                .sorted(Comparator
                        .comparing(AdmsPunchLog::getCheckTime)
                        .thenComparing(AdmsPunchLog::getAdmsPunchLogId))
                .toList();

        AdmsPunchLog timeIn = ordered.stream()
                .filter(row -> Integer.valueOf(0).equals(row.getCheckType()))
                .findFirst()
                .orElse(null);

        if (timeIn == null) {
            return null;
        }

        List<AdmsPunchLog> afterTimeIn = ordered.stream()
                .filter(row -> !row.getCheckTime().isBefore(timeIn.getCheckTime()))
                .toList();

        if (!plan.template().hasBreak()) {
            AdmsPunchLog latestOut = latestOutAfter(afterTimeIn, timeIn.getCheckTime());
            return new ProgressSnapshot(
                    timeIn.getCheckTime(),
                    null,
                    null,
                    latestOut == null ? null : latestOut.getCheckTime(),
                    afterTimeIn,
                    latestOut == null ? "IN PROGRESS" : "PENDING FINALIZATION"
            );
        }

        AdmsPunchLog firstOut = afterTimeIn.stream()
                .filter(row -> Integer.valueOf(1).equals(row.getCheckType()))
                .filter(row -> row.getCheckTime().isAfter(timeIn.getCheckTime()))
                .findFirst()
                .orElse(null);

        if (firstOut == null) {
            return new ProgressSnapshot(
                    timeIn.getCheckTime(), null, null, null,
                    afterTimeIn, "IN PROGRESS"
            );
        }

        AdmsPunchLog firstInAfterOut = afterTimeIn.stream()
                .filter(row -> Integer.valueOf(0).equals(row.getCheckType()))
                .filter(row -> row.getCheckTime().isAfter(firstOut.getCheckTime()))
                .findFirst()
                .orElse(null);

        if (firstInAfterOut == null) {
            AdmsPunchLog latestOut = afterTimeIn.stream()
                    .filter(row -> Integer.valueOf(1).equals(row.getCheckType()))
                    .filter(row -> row.getCheckTime().isAfter(timeIn.getCheckTime()))
                    .reduce((first, second) -> second)
                    .orElse(firstOut);

            return new ProgressSnapshot(
                    timeIn.getCheckTime(),
                    latestOut.getCheckTime(),
                    null,
                    null,
                    afterTimeIn,
                    "ON BREAK"
            );
        }

        AdmsPunchLog breakOut = afterTimeIn.stream()
                .filter(row -> Integer.valueOf(1).equals(row.getCheckType()))
                .filter(row -> row.getCheckTime().isAfter(timeIn.getCheckTime()))
                .filter(row -> !row.getCheckTime().isAfter(firstInAfterOut.getCheckTime()))
                .reduce((first, second) -> second)
                .orElse(firstOut);

        AdmsPunchLog breakIn = afterTimeIn.stream()
                .filter(row -> Integer.valueOf(0).equals(row.getCheckType()))
                .filter(row -> row.getCheckTime().isAfter(breakOut.getCheckTime()))
                .findFirst()
                .orElse(firstInAfterOut);

        return new ProgressSnapshot(
                timeIn.getCheckTime(),
                breakOut.getCheckTime(),
                breakIn.getCheckTime(),
                null,
                afterTimeIn,
                "IN PROGRESS"
        );
    }

    private void upsertProgressSegment(
            String employeeId,
            ShiftPlan plan,
            ProgressSnapshot snapshot,
            AdmsDtrProcessResultDTO result) {

        DTRDaily daily = dtrDailyRepository.findByEmployeeIdAndWorkDate(employeeId, plan.workDate())
                .orElseGet(() -> createDaily(employeeId, plan.workDate()));

        if (daily.getSegments() == null) {
            daily.setSegments(new ArrayList<>());
        }

        Optional<DTRSegment> existingAtPosition = daily.getSegments().stream()
                .filter(segment -> Objects.equals(segment.getSegmentNo(), plan.segmentNo()))
                .findFirst();

        DTRSegment segment;
        boolean created = false;

        if (existingAtPosition.isPresent()) {
            segment = existingAtPosition.get();
            if (!SOURCE_ADMS.equalsIgnoreCase(segment.getSourceType())) {
                markPending(snapshot.punches(), STATUS_CONFLICT,
                        "A manual DTR segment already occupies segment " + plan.segmentNo()
                                + ". The live biometric punches were not allowed to overwrite it.");
                result.setConflicts(result.getConflicts() + 1);
                return;
            }
        } else {
            segment = new DTRSegment();
            segment.setDtrDaily(daily);
            segment.setSegmentNo(plan.segmentNo());
            segment.setSourceType(SOURCE_ADMS);
            daily.getSegments().add(segment);
            created = true;
        }

        ComputedMinutes minutes = computeProgressMinutes(plan, snapshot);
        segment.setTimeIn(snapshot.timeIn().toLocalTime());
        segment.setBreakOut(snapshot.breakOut() == null ? null : snapshot.breakOut().toLocalTime());
        segment.setBreakIn(snapshot.breakIn() == null ? null : snapshot.breakIn().toLocalTime());
        segment.setTimeOut(snapshot.provisionalTimeOut() == null ? null : snapshot.provisionalTimeOut().toLocalTime());
        segment.setWorkMinutes(minutes.workMinutes());
        segment.setLateMinutes(minutes.lateMinutes());
        segment.setUndertimeMinutes(minutes.undertimeMinutes());
        segment.setOvertimeMinutes(minutes.overtimeMinutes());

        DTRSegment savedSegment = dtrSegmentRepository.save(segment);
        daily.setAttendanceStatus(snapshot.attendanceStatus());
        recomputeDailyTotals(daily);
        DTRDaily savedDaily = dtrDailyRepository.save(daily);

        linkPendingPunches(snapshot.punches(), savedDaily, savedSegment,
                "Live biometric punches reflected in DTR; waiting for the remaining punch(es).");

        if (created) {
            result.setSegmentsCreated(result.getSegmentsCreated() + 1);
        }
    }

    private ComputedMinutes computeProgressMinutes(ShiftPlan plan, ProgressSnapshot snapshot) {
        long workMinutes = 0;

        if (snapshot.breakOut() != null) {
            workMinutes += nonNegativeMinutes(snapshot.timeIn(), snapshot.breakOut());
        } else if (snapshot.provisionalTimeOut() != null) {
            workMinutes += nonNegativeMinutes(snapshot.timeIn(), snapshot.provisionalTimeOut());
        }

        int lateTimeIn = nonNegativeMinutes(plan.plannedStart(), snapshot.timeIn());
        int lateBreakIn = 0;
        int earlyBreakOut = 0;

        if (snapshot.breakOut() != null && plan.plannedBreakOut() != null) {
            earlyBreakOut = nonNegativeMinutes(snapshot.breakOut(), plan.plannedBreakOut());
        }
        if (snapshot.breakIn() != null && plan.plannedBreakIn() != null) {
            lateBreakIn = nonNegativeMinutes(plan.plannedBreakIn(), snapshot.breakIn());
        }

        if (plan.nonWorkingDuty()) {
            return new ComputedMinutes(safeInt(workMinutes), 0, 0, safeInt(workMinutes));
        }

        // Final Time Out penalties/overtime are intentionally deferred until the
        // punch sequence is complete. Only facts already known are shown.
        return new ComputedMinutes(
                safeInt(workMinutes),
                lateTimeIn + lateBreakIn,
                earlyBreakOut,
                0
        );
    }

    private void linkPendingPunches(
            List<AdmsPunchLog> punches,
            DTRDaily daily,
            DTRSegment segment,
            String message) {
        for (AdmsPunchLog punch : punches) {
            if (Boolean.TRUE.equals(punch.getDtrProcessed())) {
                continue;
            }
            punch.setDtrProcessingStatus(STATUS_PENDING);
            punch.setDtrProcessingMessage(message);
            punch.setDtrDailyId(daily.getDtrDailyId());
            punch.setDtrSegmentId(segment.getDtrSegmentId());
        }
        punchLogRepository.saveAll(punches);
    }

    private DTRDaily createDaily(String employeeId, LocalDate workDate) {
        DTRDaily daily = new DTRDaily();
        daily.setEmployeeId(employeeId);
        daily.setWorkDate(workDate);
        daily.setTotalWorkMinutes(0);
        daily.setTotalLateMinutes(0);
        daily.setTotalUndertimeMinutes(0);
        daily.setTotalOvertimeMinutes(0);
        daily.setAttendanceStatus("Present");
        return dtrDailyRepository.save(daily);
    }

    private void recomputeDailyTotals(DTRDaily daily) {
        int work = 0;
        int late = 0;
        int under = 0;
        int over = 0;

        if (daily.getSegments() != null) {
            for (DTRSegment segment : daily.getSegments()) {
                work += valueOrZero(segment.getWorkMinutes());
                late += valueOrZero(segment.getLateMinutes());
                under += valueOrZero(segment.getUndertimeMinutes());
                over += valueOrZero(segment.getOvertimeMinutes());
            }
        }

        daily.setTotalWorkMinutes(work);
        daily.setTotalLateMinutes(late);
        daily.setTotalUndertimeMinutes(under);
        daily.setTotalOvertimeMinutes(over);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean sameTimes(DTRSegment segment, Candidate candidate) {
        return Objects.equals(segment.getTimeIn(), candidate.timeIn().toLocalTime())
                && Objects.equals(segment.getBreakOut(), candidate.breakOut() == null ? null : candidate.breakOut().toLocalTime())
                && Objects.equals(segment.getBreakIn(), candidate.breakIn() == null ? null : candidate.breakIn().toLocalTime())
                && Objects.equals(segment.getTimeOut(), candidate.timeOut().toLocalTime());
    }

    /**
     * Mirrors the successful Manual Add DTR formula:
     * work = morning work + afternoon work when complete break punches exist;
     * late = late Time In + late Break In;
     * undertime = early Break Out + early final Time Out;
     * overtime = work beyond scheduled Time Out.
     */
    private ComputedMinutes computeMinutes(ShiftPlan plan, Candidate candidate) {
        long workMinutes;
        if (candidate.breakOut() != null && candidate.breakIn() != null) {
            workMinutes = nonNegativeMinutes(candidate.timeIn(), candidate.breakOut())
                    + nonNegativeMinutes(candidate.breakIn(), candidate.timeOut());
        } else {
            workMinutes = nonNegativeMinutes(candidate.timeIn(), candidate.timeOut());
        }

        int lateTimeIn = nonNegativeMinutes(plan.plannedStart(), candidate.timeIn());
        int lateBreakIn = 0;
        int earlyBreakOut = 0;

        if (candidate.breakOut() != null && candidate.breakIn() != null
                && plan.plannedBreakOut() != null && plan.plannedBreakIn() != null) {
            lateBreakIn = nonNegativeMinutes(plan.plannedBreakIn(), candidate.breakIn());
            earlyBreakOut = nonNegativeMinutes(candidate.breakOut(), plan.plannedBreakOut());
        }

        int earlyTimeOut = nonNegativeMinutes(candidate.timeOut(), plan.plannedEnd());
        int overtime = nonNegativeMinutes(plan.plannedEnd(), candidate.timeOut());

        if (candidate.nonWorkingDuty() || plan.nonWorkingDuty()) {
            return new ComputedMinutes(
                    safeInt(workMinutes),
                    0,
                    0,
                    safeInt(workMinutes)
            );
        }

        return new ComputedMinutes(
                safeInt(workMinutes),
                lateTimeIn + lateBreakIn,
                earlyBreakOut + earlyTimeOut,
                overtime
        );
    }

    private int nonNegativeMinutes(LocalDateTime from, LocalDateTime to) {
        return (int) Math.max(0, ChronoUnit.MINUTES.between(from, to));
    }

    private int safeInt(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value));
    }

    private void markPending(List<AdmsPunchLog> punches, String status, String message) {
        if (punches == null || punches.isEmpty()) {
            return;
        }
        for (AdmsPunchLog punch : punches) {
            if (Boolean.TRUE.equals(punch.getDtrProcessed())) {
                continue;
            }
            punch.setDtrProcessingStatus(status);
            punch.setDtrProcessingMessage(message);
        }
        punchLogRepository.saveAll(punches);
    }

    private void markProcessed(
            List<AdmsPunchLog> punches,
            String status,
            String message,
            DTRDaily daily,
            DTRSegment segment) {
        LocalDateTime processedAt = LocalDateTime.now();
        for (AdmsPunchLog punch : punches) {
            punch.setDtrProcessed(true);
            punch.setDtrProcessingStatus(status);
            punch.setDtrProcessingMessage(message);
            punch.setDtrProcessedAt(processedAt);
            punch.setDtrDailyId(daily.getDtrDailyId());
            punch.setDtrSegmentId(segment.getDtrSegmentId());
        }
        punchLogRepository.saveAll(punches);
    }

    private ShiftPlan chooseFallbackPlan(
            LocalDate workDate,
            String employeeId,
            List<AdmsPunchLog> punches,
            LocalDateTime now) {
        List<ShiftTemplate> templates = loadAllShiftTemplates();
        ShiftPlan bestPlan = null;
        long bestScore = Long.MAX_VALUE;

        int segmentNo = resolveReconciliationSegmentNo(employeeId, workDate, punches);
        for (ShiftTemplate template : templates) {
            ShiftPlan plan = createPlan(workDate, segmentNo, template, false, "MATCHED_TIME_SHIFT");
            Candidate candidate = evaluateCandidate(plan, punches, now);
            if (candidate.state() != CandidateState.COMPLETE) {
                continue;
            }
            long score = Math.abs(ChronoUnit.MINUTES.between(plan.plannedStart(), candidate.timeIn()))
                    + Math.abs(ChronoUnit.MINUTES.between(plan.plannedEnd(), candidate.timeOut()));
            if (score < bestScore) {
                bestScore = score;
                bestPlan = plan;
            }
        }

        if (bestPlan != null) {
            return bestPlan;
        }

        ShiftTemplate defaultTemplate = new ShiftTemplate(
                "DEFAULT_8_TO_5",
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalTime.of(17, 0)
        );
        return createPlan(workDate, segmentNo, defaultTemplate, false, "DEFAULT_8_TO_5");
    }

    /**
     * Resolves a stable segment position for date-level ADMS reconciliation.
     *
     * Search deliberately reloads punches that may already have been reflected in DTR.
     * Reusing the punch-to-segment link prevents an incomplete punch sequence from being
     * inserted as Segment 1, Segment 2, Segment 3, ... every time Search is clicked.
     *
     * The exact Time In fallback also protects Manual adjustments when an older punch link
     * is unavailable: reconciliation targets the existing position and lets handleCandidate /
     * upsertProgressSegment preserve MANUAL instead of creating a duplicate ADMS segment.
     */
    private int resolveReconciliationSegmentNo(
            String employeeId,
            LocalDate workDate,
            List<AdmsPunchLog> punches) {

        if (punches != null) {
            for (AdmsPunchLog punch : punches) {
                Long linkedSegmentId = punch.getDtrSegmentId();
                if (linkedSegmentId == null) {
                    continue;
                }

                Optional<DTRSegment> linkedSegment = dtrSegmentRepository.findById(linkedSegmentId);
                if (linkedSegment.isEmpty()) {
                    // The DTR may have been physically deleted while the raw ADMS punch was retained.
                    // Ignore the stale link so Search can rebuild the transaction normally.
                    continue;
                }

                DTRSegment segment = linkedSegment.get();
                DTRDaily daily = segment.getDtrDaily();
                if (daily != null
                        && Objects.equals(employeeId, daily.getEmployeeId())
                        && Objects.equals(workDate, daily.getWorkDate())
                        && segment.getSegmentNo() != null) {
                    return segment.getSegmentNo();
                }
            }
        }

        LocalTime firstCheckIn = punches == null
                ? null
                : punches.stream()
                .filter(punch -> Integer.valueOf(0).equals(punch.getCheckType()))
                .filter(punch -> punch.getCheckTime() != null)
                .map(AdmsPunchLog::getCheckTime)
                .min(LocalDateTime::compareTo)
                .map(LocalDateTime::toLocalTime)
                .orElse(null);

        if (firstCheckIn != null) {
            Optional<DTRDaily> existingDaily = dtrDailyRepository.findByEmployeeIdAndWorkDate(employeeId, workDate);
            if (existingDaily.isPresent() && existingDaily.get().getSegments() != null) {
                Optional<Integer> matchingSegmentNo = existingDaily.get().getSegments().stream()
                        .filter(segment -> segment.getSegmentNo() != null)
                        .filter(segment -> Objects.equals(firstCheckIn, segment.getTimeIn()))
                        .map(DTRSegment::getSegmentNo)
                        .min(Integer::compareTo);

                if (matchingSegmentNo.isPresent()) {
                    return matchingSegmentNo.get();
                }
            }
        }

        return nextSegmentNo(employeeId, workDate);
    }

    private int nextSegmentNo(String employeeId, LocalDate workDate) {
        return dtrDailyRepository.findByEmployeeIdAndWorkDate(employeeId, workDate)
                .map(daily -> daily.getSegments() == null
                        ? 1
                        : daily.getSegments().stream()
                        .map(DTRSegment::getSegmentNo)
                        .filter(Objects::nonNull)
                        .max(Integer::compareTo)
                        .orElse(0) + 1)
                .orElse(1);
    }

    private ShiftPlan createPlan(
            LocalDate workDate,
            int segmentNo,
            ShiftTemplate template,
            boolean nonWorkingDuty,
            String source) {
        LocalDateTime plannedStart = workDate.atTime(template.timeIn());
        LocalDateTime plannedEnd = resolveAfterStart(workDate, template.timeOut(), plannedStart);
        LocalDateTime plannedBreakOut = template.breakOut() == null
                ? null
                : resolveAfterStart(workDate, template.breakOut(), plannedStart);
        LocalDateTime plannedBreakIn = template.breakIn() == null
                ? null
                : resolveAfterStart(workDate, template.breakIn(), plannedStart);

        LocalDateTime windowStart = plannedStart.minusMinutes(Math.max(0, earlyWindowMinutes));
        LocalDateTime windowEnd = plannedEnd.plusMinutes(Math.max(0, lateWindowMinutes));

        return new ShiftPlan(
                workDate,
                segmentNo,
                template,
                plannedStart,
                plannedBreakOut,
                plannedBreakIn,
                plannedEnd,
                windowStart,
                windowEnd,
                nonWorkingDuty,
                source
        );
    }

    private LocalDateTime resolveAfterStart(LocalDate date, LocalTime time, LocalDateTime plannedStart) {
        LocalDateTime resolved = date.atTime(time);
        if (resolved.isBefore(plannedStart)) {
            resolved = resolved.plusDays(1);
        }
        return resolved;
    }

    private ShiftTemplate loadShiftTemplate(String tsCode) {
        if (tsCode == null || tsCode.isBlank()) {
            return null;
        }

        String sql = "SELECT TOP 1 tsCode, timeIn, breakOut, breakIn, timeOut "
                + "FROM time_shift WHERE LOWER(LTRIM(RTRIM(tsCode))) = LOWER(LTRIM(RTRIM(?)))";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tsCode);
        if (rows.isEmpty()) {
            return null;
        }
        return toTemplate(rows.get(0));
    }

    private List<ShiftTemplate> loadAllShiftTemplates() {
        try {
            String sql = "SELECT tsCode, timeIn, breakOut, breakIn, timeOut FROM time_shift";
            return jdbcTemplate.queryForList(sql).stream()
                    .map(this::toTemplate)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (DataAccessException exception) {
            log.warn("Unable to load configured time shifts for ADMS fallback matching: {}", exception.getMessage());
            return List.of();
        }
    }

    private ShiftTemplate toTemplate(Map<String, Object> row) {
        LocalTime timeIn = toLocalTime(row.get("timeIn"));
        LocalTime timeOut = toLocalTime(row.get("timeOut"));
        if (timeIn == null || timeOut == null) {
            return null;
        }
        return new ShiftTemplate(
                Objects.toString(row.get("tsCode"), ""),
                timeIn,
                toLocalTime(row.get("breakOut")),
                toLocalTime(row.get("breakIn")),
                timeOut
        );
    }

    private LocalTime toLocalTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalTime localTime) {
            return localTime;
        }
        if (value instanceof java.sql.Time sqlTime) {
            return sqlTime.toLocalTime();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalTime();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(text.length() >= 8 ? text.substring(0, 8) : text);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    /**
     * Returns every selected date covered by an approved employee request that must
     * replace biometric/manual DTR transactions. The insertion order mirrors the
     * frontend's existing authority order; later request types overwrite only the
     * diagnostic label, while every match suppresses DTR reconciliation.
     *
     * Leave Monetization is deliberately excluded because it has no attendance date
     * range and must never suppress DTR.
     */
    private Map<LocalDate, String> findAuthoritativeRequestDates(
            String employeeId,
            LocalDate fromDate,
            LocalDate toDate) {
        Map<LocalDate, String> dates = new LinkedHashMap<>();
        if (employeeId == null || employeeId.isBlank() || fromDate == null || toDate == null
                || toDate.isBefore(fromDate)) {
            return dates;
        }

        java.sql.Date sqlFrom = java.sql.Date.valueOf(fromDate);
        java.sql.Date sqlTo = java.sql.Date.valueOf(toDate);

        // Lowest display priority first. A higher-priority request may replace the
        // diagnostic label for an overlapping date, but suppression is unchanged.
        addApprovedRangeRequests(
                dates,
                "Approved Leave",
                "SELECT startDate AS request_start, endDate AS request_end, leaveType AS request_type "
                        + "FROM leave_application "
                        + "WHERE CAST(employeeId AS VARCHAR(100)) = ? "
                        + "AND LOWER(LTRIM(RTRIM(approvedStatus))) = 'approved' "
                        + "AND startDate IS NOT NULL AND endDate IS NOT NULL "
                        + "AND LOWER(LTRIM(RTRIM(leaveType))) <> 'leave monetization' "
                        + "AND startDate <= ? AND endDate >= ?",
                employeeId,
                sqlTo,
                sqlFrom,
                fromDate,
                toDate
        );

        addApprovedRangeRequests(
                dates,
                "Approved Official Engagement",
                "SELECT startDate AS request_start, endDate AS request_end, officialType AS request_type "
                        + "FROM official_engagement_application "
                        + "WHERE CAST(employeeId AS VARCHAR(100)) = ? "
                        + "AND LOWER(LTRIM(RTRIM(status))) = 'approved' "
                        + "AND startDate IS NOT NULL AND endDate IS NOT NULL "
                        + "AND startDate <= ? AND endDate >= ?",
                employeeId,
                sqlTo,
                sqlFrom,
                fromDate,
                toDate
        );

        addApprovedSingleDateRequests(
                dates,
                "Approved Pass Slip",
                "SELECT passSlipDate AS request_date FROM pass_slip "
                        + "WHERE CAST(employeeId AS VARCHAR(100)) = ? "
                        + "AND LOWER(LTRIM(RTRIM(status))) = 'approved' "
                        + "AND passSlipDate BETWEEN ? AND ?",
                employeeId,
                sqlFrom,
                sqlTo
        );

        addApprovedSingleDateRequests(
                dates,
                "Approved CTO",
                "SELECT dateOfOffset AS request_date FROM compensatory_time_off "
                        + "WHERE CAST(employeeId AS VARCHAR(100)) = ? "
                        + "AND LOWER(LTRIM(RTRIM(status))) = 'approved' "
                        + "AND dateOfOffset BETWEEN ? AND ?",
                employeeId,
                sqlFrom,
                sqlTo
        );

        addApprovedSingleDateRequests(
                dates,
                "Approved Time Correction",
                "SELECT workDate AS request_date FROM time_correction "
                        + "WHERE CAST(employeeId AS VARCHAR(100)) = ? "
                        + "AND LOWER(LTRIM(RTRIM(status))) = 'approved' "
                        + "AND workDate BETWEEN ? AND ?",
                employeeId,
                sqlFrom,
                sqlTo
        );

        return dates;
    }

    private void addApprovedRangeRequests(
            Map<LocalDate, String> target,
            String fallbackLabel,
            String sql,
            Object employeeId,
            Object sqlTo,
            Object sqlFrom,
            LocalDate selectedFrom,
            LocalDate selectedTo) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, employeeId, sqlTo, sqlFrom);
            for (Map<String, Object> row : rows) {
                LocalDate rowStart = toLocalDate(row.get("request_start"));
                LocalDate rowEnd = toLocalDate(row.get("request_end"));
                if (rowStart == null || rowEnd == null || rowEnd.isBefore(rowStart)) {
                    continue;
                }

                LocalDate start = rowStart.isBefore(selectedFrom) ? selectedFrom : rowStart;
                LocalDate end = rowEnd.isAfter(selectedTo) ? selectedTo : rowEnd;
                String requestType = Objects.toString(row.get("request_type"), "").trim();
                String label = requestType.isEmpty() ? fallbackLabel : requestType;

                for (LocalDate cursor = start; !cursor.isAfter(end); cursor = cursor.plusDays(1)) {
                    target.put(cursor, label);
                }
            }
        } catch (DataAccessException exception) {
            log.warn("Authoritative request range lookup failed for {}: {}", fallbackLabel, exception.getMessage());
        }
    }

    private void addApprovedSingleDateRequests(
            Map<LocalDate, String> target,
            String label,
            String sql,
            Object employeeId,
            Object sqlFrom,
            Object sqlTo) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, employeeId, sqlFrom, sqlTo);
            for (Map<String, Object> row : rows) {
                LocalDate requestDate = toLocalDate(row.get("request_date"));
                if (requestDate != null) {
                    target.put(requestDate, label);
                }
            }
        } catch (DataAccessException exception) {
            log.warn("Authoritative request date lookup failed for {}: {}", label, exception.getMessage());
        }
    }

    /**
     * Physically removes dtr_daily and its cascade-owned dtr_segment rows for approved
     * request dates. Raw punch values remain intact. Only DTR-link/processing metadata
     * is updated so deleted foreign-key targets are not left behind.
     */
    private void suppressDtrForAuthoritativeRequests(
            String employeeId,
            Map<LocalDate, String> authoritativeRequestDates) {
        if (authoritativeRequestDates == null || authoritativeRequestDates.isEmpty()) {
            return;
        }

        for (Map.Entry<LocalDate, String> entry : authoritativeRequestDates.entrySet()) {
            LocalDate workDate = entry.getKey();
            String requestLabel = entry.getValue();

            Optional<DTRDaily> dailyOptional = dtrDailyRepository.findByEmployeeIdAndWorkDate(employeeId, workDate);
            if (dailyOptional.isEmpty()) {
                continue;
            }

            DTRDaily daily = dailyOptional.get();
            Long dailyId = daily.getDtrDailyId();
            String message = suppressionMessage(workDate, requestLabel);

            // Clear ADMS processing links before deleting the referenced DTR rows.
            try {
                jdbcTemplate.update(
                        "UPDATE adms_punch_log SET dtr_daily_id = NULL, dtr_segment_id = NULL, "
                                + "dtr_processed = ?, dtr_processing_status = ?, "
                                + "dtr_processing_message = ?, dtr_processed_at = ? "
                                + "WHERE dtr_daily_id = ?",
                        Boolean.TRUE,
                        STATUS_SUPPRESSED_BY_REQUEST,
                        message,
                        Timestamp.valueOf(LocalDateTime.now()),
                        dailyId
                );
            } catch (DataAccessException exception) {
                log.warn("Unable to clear ADMS DTR links before request suppression for daily {}: {}",
                        dailyId, exception.getMessage());
            }

            // dtr_raw_log is legacy, but clear its segment links so a physical cascade
            // delete cannot be blocked by an existing reference.
            if (daily.getSegments() != null) {
                for (DTRSegment segment : daily.getSegments()) {
                    if (segment.getDtrSegmentId() == null) {
                        continue;
                    }
                    try {
                        jdbcTemplate.update(
                                "UPDATE dtr_raw_log SET dtr_segment_id = NULL WHERE dtr_segment_id = ?",
                                segment.getDtrSegmentId()
                        );
                    } catch (DataAccessException exception) {
                        log.debug("Legacy dtr_raw_log link cleanup was unavailable for segment {}: {}",
                                segment.getDtrSegmentId(), exception.getMessage());
                    }
                }
            }

            dtrDailyRepository.delete(daily);
            dtrDailyRepository.flush();
            log.info("Suppressed DTR employeeId={} workDate={} request={}",
                    employeeId, workDate, requestLabel);
        }
    }

    private void markSuppressedByRequest(
            List<AdmsPunchLog> punches,
            LocalDate workDate,
            String requestLabel) {
        if (punches == null || punches.isEmpty()) {
            return;
        }

        LocalDateTime processedAt = LocalDateTime.now();
        String message = suppressionMessage(workDate, requestLabel);
        for (AdmsPunchLog punch : punches) {
            punch.setDtrProcessed(true);
            punch.setDtrProcessingStatus(STATUS_SUPPRESSED_BY_REQUEST);
            punch.setDtrProcessingMessage(message);
            punch.setDtrProcessedAt(processedAt);
            punch.setDtrDailyId(null);
            punch.setDtrSegmentId(null);
        }
        punchLogRepository.saveAll(punches);
    }

    private String suppressionMessage(LocalDate workDate, String requestLabel) {
        return "DTR suppressed for " + workDate + " because " + requestLabel
                + " is approved and authoritative for attendance.";
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.length() >= 10 ? text.substring(0, 10) : text);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private boolean isDayOff(String employeeId, LocalDate workDate) {
        LocalDateTime from = workDate.atStartOfDay();
        LocalDateTime to = workDate.plusDays(1).atStartOfDay().minusNanos(1);
        Optional<List<WorkSchedule>> rows = workScheduleRepository
                .findByEmployeeIdAndWsDateTimeBetweenOrderByWsDateTimeAscWsIdAsc(employeeId, from, to);
        return rows.orElseGet(List::of).stream().anyMatch(row -> Boolean.TRUE.equals(row.getIsDayOff()));
    }

    private boolean isNonWorkingHoliday(LocalDate workDate) {
        try {
            String sql = "SELECT COUNT(*) FROM holiday "
                    + "WHERE isActive = 1 "
                    + "AND (isWorkingHoliday = 0 OR isWorkingHoliday IS NULL) "
                    + "AND (holidayType IS NULL OR holidayType <> 'SPECIAL_WORKING') "
                    + "AND CAST(CASE WHEN observedDate IS NOT NULL THEN observedDate ELSE holidayDate END AS DATE) = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, java.sql.Date.valueOf(workDate));
            return count != null && count > 0;
        } catch (DataAccessException exception) {
            log.debug("Holiday lookup was unavailable during ADMS processing: {}", exception.getMessage());
            return false;
        }
    }

    private Optional<DutyAuthority> findApprovedDutyAuthority(
            String employeeId,
            LocalDate workDate,
            List<AdmsPunchLog> punches,
            boolean dayOff,
            boolean holiday) {
        try {
            LocalDateTime firstPunch = punches.stream()
                    .map(AdmsPunchLog::getCheckTime)
                    .min(LocalDateTime::compareTo)
                    .orElse(workDate.atStartOfDay());
            LocalDateTime lastPunch = punches.stream()
                    .map(AdmsPunchLog::getCheckTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(firstPunch);

            String sql = "SELECT TOP 1 dateTimeFrom, dateTimeTo, workType, dutyShiftCode "
                    + "FROM overtime_request "
                    + "WHERE CAST(employeeId AS VARCHAR(100)) = ? "
                    + "AND status = 'Approved' "
                    + "AND dateTimeFrom <= ? AND dateTimeTo >= ? "
                    + "ORDER BY dateTimeFrom ASC";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    sql,
                    employeeId,
                    Timestamp.valueOf(firstPunch),
                    Timestamp.valueOf(lastPunch)
            );

            for (Map<String, Object> row : rows) {
                String workType = Objects.toString(row.get("workType"), "").toUpperCase();
                boolean typeMatches = (holiday && "HOLIDAY_DUTY".equals(workType))
                        || (dayOff && ("DAY_OFF_DUTY".equals(workType) || "REST_DAY_DUTY".equals(workType)));
                if (!typeMatches) {
                    continue;
                }
                LocalDateTime from = toLocalDateTime(row.get("dateTimeFrom"));
                LocalDateTime to = toLocalDateTime(row.get("dateTimeTo"));
                if (from != null && to != null) {
                    return Optional.of(new DutyAuthority(
                            from,
                            to,
                            workType,
                            Objects.toString(row.get("dutyShiftCode"), null)
                    ));
                }
            }
        } catch (DataAccessException exception) {
            log.warn("Approved duty authority lookup failed during ADMS processing: {}", exception.getMessage());
        }
        return Optional.empty();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }

    private ShiftPlan authorityPlan(LocalDate workDate, DutyAuthority authority, int segmentNo) {
        ShiftTemplate configured = loadShiftTemplate(authority.dutyShiftCode());
        ShiftTemplate template;
        if (configured != null) {
            template = configured;
        } else {
            template = new ShiftTemplate(
                    authority.workType(),
                    authority.from().toLocalTime(),
                    null,
                    null,
                    authority.to().toLocalTime()
            );
        }

        ShiftPlan base = createPlan(workDate, segmentNo, template, true, "APPROVED_" + authority.workType());
        return new ShiftPlan(
                base.workDate(),
                base.segmentNo(),
                base.template(),
                authority.from(),
                base.plannedBreakOut(),
                base.plannedBreakIn(),
                authority.to(),
                authority.from().minusMinutes(Math.max(0, earlyWindowMinutes)),
                authority.to().plusMinutes(Math.max(0, lateWindowMinutes)),
                true,
                base.source()
        );
    }

    private record ShiftTemplate(
            String code,
            LocalTime timeIn,
            LocalTime breakOut,
            LocalTime breakIn,
            LocalTime timeOut) {
        boolean hasBreak() {
            return breakOut != null && breakIn != null;
        }
    }

    private record ShiftPlan(
            LocalDate workDate,
            int segmentNo,
            ShiftTemplate template,
            LocalDateTime plannedStart,
            LocalDateTime plannedBreakOut,
            LocalDateTime plannedBreakIn,
            LocalDateTime plannedEnd,
            LocalDateTime windowStart,
            LocalDateTime windowEnd,
            boolean nonWorkingDuty,
            String source) {
        ShiftPlan withWindow(LocalDateTime start, LocalDateTime end) {
            return new ShiftPlan(
                    workDate,
                    segmentNo,
                    template,
                    plannedStart,
                    plannedBreakOut,
                    plannedBreakIn,
                    plannedEnd,
                    start,
                    end,
                    nonWorkingDuty,
                    source
            );
        }
    }

    private record ProgressSnapshot(
            LocalDateTime timeIn,
            LocalDateTime breakOut,
            LocalDateTime breakIn,
            LocalDateTime provisionalTimeOut,
            List<AdmsPunchLog> punches,
            String attendanceStatus) {
    }

    private enum CandidateState {
        NO_DATA,
        PENDING,
        COMPLETE
    }

    private record Candidate(
            CandidateState state,
            List<AdmsPunchLog> punches,
            String message,
            LocalDateTime timeIn,
            LocalDateTime breakOut,
            LocalDateTime breakIn,
            LocalDateTime timeOut,
            boolean nonWorkingDuty) {

        static Candidate noData() {
            return new Candidate(CandidateState.NO_DATA, List.of(), null, null, null, null, null, false);
        }

        static Candidate pending(List<AdmsPunchLog> punches, String message) {
            return new Candidate(CandidateState.PENDING, punches, message, null, null, null, null, false);
        }

        static Candidate complete(
                List<AdmsPunchLog> punches,
                LocalDateTime timeIn,
                LocalDateTime breakOut,
                LocalDateTime breakIn,
                LocalDateTime timeOut,
                boolean nonWorkingDuty) {
            return new Candidate(
                    CandidateState.COMPLETE,
                    punches,
                    null,
                    timeIn,
                    breakOut,
                    breakIn,
                    timeOut,
                    nonWorkingDuty
            );
        }

        Candidate withNonWorkingDuty(boolean value) {
            return new Candidate(state, punches, message, timeIn, breakOut, breakIn, timeOut, value);
        }
    }

    private record ComputedMinutes(
            int workMinutes,
            int lateMinutes,
            int undertimeMinutes,
            int overtimeMinutes) {
    }

    private record DutyAuthority(
            LocalDateTime from,
            LocalDateTime to,
            String workType,
            String dutyShiftCode) {
    }
}
