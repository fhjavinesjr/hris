package com.timekeeping.impl;
import com.timekeeping.entitymodels.DTRDaily;
import com.timekeeping.entitymodels.DTRRawLog;
import com.timekeeping.entitymodels.DTRSegment;
import com.timekeeping.entitymodels.WorkSchedule;
import com.timekeeping.repositories.DTRDailyRepository;
import com.timekeeping.repositories.DTRRawLogRepository;
import com.timekeeping.repositories.DTRSegmentRepository;
import com.timekeeping.repositories.WorkScheduleRepository;
import com.timekeeping.services.DTRProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Service
public class DTRProcessingServiceImpl implements DTRProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(DTRProcessingServiceImpl.class);

    private final DTRRawLogRepository rawLogRepository;
    private final DTRDailyRepository dtrDailyRepository;
    private final DTRSegmentRepository dtrSegmentRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final JdbcTemplate jdbcTemplate;
    public DTRProcessingServiceImpl(
            DTRRawLogRepository rawLogRepository,
            DTRDailyRepository dtrDailyRepository,
            DTRSegmentRepository dtrSegmentRepository,
            WorkScheduleRepository workScheduleRepository,
            JdbcTemplate jdbcTemplate) {
        this.rawLogRepository = rawLogRepository;
        this.dtrDailyRepository = dtrDailyRepository;
        this.dtrSegmentRepository = dtrSegmentRepository;
        this.workScheduleRepository = workScheduleRepository;
        this.jdbcTemplate = jdbcTemplate;
    }
    /**
     * PAIRING LOGIC (processes logs in datetime ASC order):
     *   TIME_IN   - opens a new segment
     *   BREAK_OUT - records break start on open segment
     *   BREAK_IN  - records break end on open segment
     *   TIME_OUT  - closes the open segment, creates DTRSegment + updates DTRDaily
     *
     * OVERNIGHT RULE:
     *   work_date = DATE of TIME_IN punch (always).
     *   A TIME_OUT on April 15 closing an April 14 TIME_IN is saved under April 14 DTR_DAILY.
     *   isOvernightShift() detects this via timeOut < timeIn (24hr) - no stored column needed.
     */
    @Override
    @Transactional
    public void processRawLogs(String employeeId) {
        List<DTRRawLog> rawLogs = rawLogRepository
                .findByEmployeeIdAndIsProcessedFalseOrderByLogDatetimeAscRawLogIdAsc(employeeId);
        if (rawLogs.isEmpty()) return;

        // Multiple TIME_IN punches can overlap around shift boundaries.
        // We keep them in FIFO order and close them as TIME_OUT logs arrive.
        Deque<OpenSegmentState> openSegments = new ArrayDeque<>();

        for (DTRRawLog log : rawLogs) {
            switch (log.getLogType()) {
                case "TIME_IN":
                    openSegments.addLast(new OpenSegmentState(log));
                    break;
                case "BREAK_OUT":
                    if (openSegments.isEmpty()) {
                        markAsProcessedWithoutSegment(log, "BREAK_OUT_WITHOUT_TIME_IN");
                        break;
                    }
                    OpenSegmentState breakOutState = openSegments.peekFirst();
                    if (breakOutState.breakOutLog == null) {
                        breakOutState.breakOutLog = log;
                    } else {
                        markAsProcessedWithoutSegment(log, "DUPLICATE_BREAK_OUT");
                    }
                    break;
                case "BREAK_IN":
                    if (openSegments.isEmpty()) {
                        markAsProcessedWithoutSegment(log, "BREAK_IN_WITHOUT_TIME_IN");
                        break;
                    }
                    OpenSegmentState breakInState = openSegments.peekFirst();
                    if (breakInState.breakOutLog != null && breakInState.breakInLog == null) {
                        breakInState.breakInLog = log;
                    } else {
                        markAsProcessedWithoutSegment(log, "BREAK_IN_WITHOUT_BREAK_OUT_OR_DUPLICATE");
                    }
                    break;
                case "TIME_OUT":
                    if (!openSegments.isEmpty()) {
                        OpenSegmentState state = openSegments.removeFirst();
                        buildAndSaveSegment(state.timeInLog, state.breakOutLog, state.breakInLog, log);
                    } else {
                        markAsProcessedWithoutSegment(log, "TIME_OUT_WITHOUT_TIME_IN");
                    }
                    break;
                default:
                    markAsProcessedWithoutSegment(log, "UNKNOWN_LOG_TYPE");
                    break;
            }
        }

        for (OpenSegmentState unclosedState : openSegments) {
            logger.warn(
                    "DTR_RAWLOG_WARN reason=UNMATCHED_TIME_IN_END_OF_BATCH employeeId={} rawLogId={} logType={} logDatetime={} action=left_unprocessed",
                    unclosedState.timeInLog.getEmployeeId(),
                    unclosedState.timeInLog.getRawLogId(),
                    unclosedState.timeInLog.getLogType(),
                    unclosedState.timeInLog.getLogDatetime());
        }
    }
    @Override
    @Transactional
    public void processAllUnprocessedLogs() {
        List<String> employeeIds = rawLogRepository.findDistinctEmployeeIdsByIsProcessedFalse();
        for (String employeeId : employeeIds) {
            processRawLogs(employeeId);
        }
    }
    private void buildAndSaveSegment(
            DTRRawLog timeInLog,
            DTRRawLog breakOutLog,
            DTRRawLog breakInLog,
            DTRRawLog timeOutLog) {
        LocalDate workDate = timeInLog.getLogDatetime().toLocalDate();
        DTRDaily daily = dtrDailyRepository
                .findByEmployeeIdAndWorkDate(timeInLog.getEmployeeId(), workDate)
                .orElseGet(() -> createDtrDaily(timeInLog.getEmployeeId(), workDate));
        long workMinutes = ChronoUnit.MINUTES.between(
                timeInLog.getLogDatetime(), timeOutLog.getLogDatetime());
        if (breakOutLog != null && breakInLog != null) {
            long breakMinutes = ChronoUnit.MINUTES.between(
                    breakOutLog.getLogDatetime(), breakInLog.getLogDatetime());
            workMinutes -= breakMinutes;
        }
        workMinutes = Math.max(0, workMinutes);

        int segmentNo = daily.getSegments().size() + 1;

        // Resolve the expected shift for this segment based on employee work schedule.
        // If unavailable, we fallback to 0 late/under/overtime to avoid false penalties.
        ShiftTemplate shiftTemplate = resolveShiftTemplateForSegment(
                timeInLog.getEmployeeId(), workDate, segmentNo);

        MinuteResult minuteResult = computeVarianceMinutes(
                workDate,
                timeInLog.getLogDatetime(),
                timeOutLog.getLogDatetime(),
                shiftTemplate);

        DTRSegment segment = new DTRSegment();
        segment.setDtrDaily(daily);
        segment.setSegmentNo(segmentNo);
        segment.setTimeIn(timeInLog.getLogDatetime().toLocalTime());
        segment.setTimeOut(timeOutLog.getLogDatetime().toLocalTime());
        if (breakOutLog != null) segment.setBreakOut(breakOutLog.getLogDatetime().toLocalTime());
        if (breakInLog  != null) segment.setBreakIn(breakInLog.getLogDatetime().toLocalTime());
        segment.setWorkMinutes((int) workMinutes);
        segment.setLateMinutes(minuteResult.lateMinutes);
        segment.setUndertimeMinutes(minuteResult.undertimeMinutes);
        segment.setOvertimeMinutes(minuteResult.overtimeMinutes);
        DTRSegment savedSegment = dtrSegmentRepository.save(segment);
        daily.setTotalWorkMinutes(daily.getTotalWorkMinutes() + (int) workMinutes);
        daily.setTotalLateMinutes(daily.getTotalLateMinutes() + minuteResult.lateMinutes);
        daily.setTotalUndertimeMinutes(daily.getTotalUndertimeMinutes() + minuteResult.undertimeMinutes);
        daily.setTotalOvertimeMinutes(daily.getTotalOvertimeMinutes() + minuteResult.overtimeMinutes);
        daily.setAttendanceStatus("Present");
        daily.getSegments().add(savedSegment);
        dtrDailyRepository.save(daily);
        linkAndMarkProcessed(timeInLog,  savedSegment);
        linkAndMarkProcessed(timeOutLog, savedSegment);
        if (breakOutLog != null) linkAndMarkProcessed(breakOutLog, savedSegment);
        if (breakInLog  != null) linkAndMarkProcessed(breakInLog,  savedSegment);
    }
    private DTRDaily createDtrDaily(String employeeId, LocalDate workDate) {
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
    private void linkAndMarkProcessed(DTRRawLog log, DTRSegment segment) {
        log.setIsProcessed(true);
        log.setDtrSegment(segment);
        rawLogRepository.save(log);
    }

    private void markAsProcessedWithoutSegment(DTRRawLog log, String reason) {
        logger.warn(
                "DTR_RAWLOG_WARN reason={} employeeId={} rawLogId={} logType={} logDatetime={} action=marked_processed_without_segment",
                reason,
                log.getEmployeeId(),
                log.getRawLogId(),
                log.getLogType(),
                log.getLogDatetime());
        log.setIsProcessed(true);
        rawLogRepository.save(log);
    }

    private ShiftTemplate resolveShiftTemplateForSegment(String employeeId, LocalDate workDate, int segmentNo) {
        LocalDateTime dayStart = workDate.atStartOfDay();
        LocalDateTime dayEnd = workDate.plusDays(1).atStartOfDay().minusNanos(1);

        Optional<List<WorkSchedule>> opWorkSchedules =
                workScheduleRepository.findByEmployeeIdAndWsDateTimeBetweenOrderByWsDateTimeAscWsIdAsc(
                        employeeId,
                        dayStart,
                        dayEnd);

        if (!opWorkSchedules.isPresent() || opWorkSchedules.get().isEmpty()) {
            return null;
        }

        List<WorkSchedule> schedules = opWorkSchedules.get();
        schedules.sort(
                Comparator.comparing(WorkSchedule::getWsDateTime)
                        .thenComparing(WorkSchedule::getWsId, Comparator.nullsLast(Long::compareTo)));

        if (segmentNo > schedules.size()) {
            return null;
        }

        String tsCode = schedules.get(segmentNo - 1).getTsCode();
        return shiftTemplateByCode(tsCode);
    }

    private ShiftTemplate shiftTemplateByCode(String tsCode) {
        if (tsCode == null || tsCode.trim().isEmpty()) return null;

        String sql = "SELECT TOP 1 timeIn, timeOut, breakOut, breakIn FROM time_shift WHERE tsCode = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tsCode.trim());
        if (rows.isEmpty()) {
            return null;
        }

        Map<String, Object> row = rows.get(0);
        LocalTime timeIn = toLocalTime(row.get("timeIn"));
        LocalTime timeOut = toLocalTime(row.get("timeOut"));
        LocalTime breakOut = toLocalTime(row.get("breakOut"));
        LocalTime breakIn = toLocalTime(row.get("breakIn"));

        if (timeIn == null || timeOut == null) {
            return null;
        }

        return new ShiftTemplate(timeIn, timeOut, breakOut, breakIn);
    }

    private LocalTime toLocalTime(Object dbTime) {
        if (dbTime == null) return null;
        if (dbTime instanceof LocalTime) return (LocalTime) dbTime;
        if (dbTime instanceof java.sql.Time) return ((java.sql.Time) dbTime).toLocalTime();
        return null;
    }

    private MinuteResult computeVarianceMinutes(
            LocalDate workDate,
            LocalDateTime actualIn,
            LocalDateTime actualOut,
            ShiftTemplate shiftTemplate) {

        if (shiftTemplate == null) {
            return new MinuteResult(0, 0, 0);
        }

        LocalDateTime plannedIn = workDate.atTime(shiftTemplate.timeIn);
        LocalDateTime plannedOut = workDate.atTime(shiftTemplate.timeOut);
        if (!shiftTemplate.timeOut.isAfter(shiftTemplate.timeIn)) {
            plannedOut = plannedOut.plusDays(1);
        }

        int late = nonNegativeMinutes(plannedIn, actualIn);
        int undertime = nonNegativeMinutes(actualOut, plannedOut);
        int overtime = nonNegativeMinutes(plannedOut, actualOut);

        return new MinuteResult(late, undertime, overtime);
    }

    private int nonNegativeMinutes(LocalDateTime from, LocalDateTime to) {
        return (int) Math.max(0, ChronoUnit.MINUTES.between(from, to));
    }

    private static class ShiftTemplate {
        private final LocalTime timeIn;
        private final LocalTime timeOut;
        @SuppressWarnings("unused")
        private final LocalTime breakOut;
        @SuppressWarnings("unused")
        private final LocalTime breakIn;

        private ShiftTemplate(LocalTime timeIn, LocalTime timeOut, LocalTime breakOut, LocalTime breakIn) {
            this.timeIn = timeIn;
            this.timeOut = timeOut;
            this.breakOut = breakOut;
            this.breakIn = breakIn;
        }
    }

    private static class OpenSegmentState {
        private final DTRRawLog timeInLog;
        private DTRRawLog breakOutLog;
        private DTRRawLog breakInLog;

        private OpenSegmentState(DTRRawLog timeInLog) {
            this.timeInLog = timeInLog;
        }
    }

    private static class MinuteResult {
        private final int lateMinutes;
        private final int undertimeMinutes;
        private final int overtimeMinutes;

        private MinuteResult(int lateMinutes, int undertimeMinutes, int overtimeMinutes) {
            this.lateMinutes = lateMinutes;
            this.undertimeMinutes = undertimeMinutes;
            this.overtimeMinutes = overtimeMinutes;
        }
    }
}