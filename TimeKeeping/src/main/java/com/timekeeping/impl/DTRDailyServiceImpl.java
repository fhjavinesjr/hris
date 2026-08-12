package com.timekeeping.impl;

import com.timekeeping.dtos.DTRDailyDTO;
import com.timekeeping.dtos.DTRSegmentEditRequest;
import com.timekeeping.dtos.DtrReportRow;
import com.timekeeping.dtos.DTRSegmentDTO;
import com.timekeeping.entitymodels.DTRDaily;
import com.timekeeping.entitymodels.DTRSegment;
import com.timekeeping.entitymodels.WorkSchedule;
import com.timekeeping.reports.DtrReportDataLoader;
import com.timekeeping.repositories.DTRDailyRepository;
import com.timekeeping.repositories.DTRSegmentRepository;
import com.timekeeping.repositories.WorkScheduleRepository;
import com.timekeeping.services.DTRDailyService;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DTRDailyServiceImpl implements DTRDailyService {
    private static final String SOURCE_MANUAL = "MANUAL";

    private final DTRDailyRepository dtrDailyRepository;
    private final DTRSegmentRepository dtrSegmentRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    public DTRDailyServiceImpl(
            DTRDailyRepository dtrDailyRepository,
            DTRSegmentRepository dtrSegmentRepository,
            WorkScheduleRepository workScheduleRepository,
            JdbcTemplate jdbc,
            DataSource dataSource) {
        this.dtrDailyRepository = dtrDailyRepository;
        this.dtrSegmentRepository = dtrSegmentRepository;
        this.workScheduleRepository = workScheduleRepository;
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    @Override
    @Transactional
    public DTRDailyDTO createOrUpdateDTRDaily(DTRDailyDTO dto) {
        // This endpoint is used by Add Manual DTR. New records are always a
        // protected MANUAL adjustment, even when the client omits sourceType.
        if (dto.getDtrDailyId() == null) {
            forceManualSource(dto.getSegments());
        }

        if (dto.getEmployeeId() == null || dto.getWorkDate() == null) {
            DTRDaily entity = toEntity(dto);
            DTRDaily saved = dtrDailyRepository.save(entity);
            return toDTO(saved);
        }

        LocalDate workDate = dto.getWorkDate().toLocalDate();

        // Manual Add DTR normally sends no dtrDailyId and one new segment.
        // When a daily row already exists for employeeId + workDate, append the
        // incoming segment(s) instead of replacing the whole segment list.
        if (dto.getDtrDailyId() == null) {
            Optional<DTRDaily> existingOpt = dtrDailyRepository.findByEmployeeIdAndWorkDate(dto.getEmployeeId(), workDate);
            if (existingOpt.isPresent()) {
                DTRDaily existing = existingOpt.get();
                appendSegmentsAndRecomputeTotals(existing, dto.getSegments());
                existing.setAttendanceStatus(
                        dto.getAttendanceStatus() != null && !dto.getAttendanceStatus().trim().isEmpty()
                                ? dto.getAttendanceStatus()
                                : "Present"
                );
                DTRDaily saved = dtrDailyRepository.save(existing);
                return toDTO(saved);
            }
        }

        // Existing DTOs with dtrDailyId are full-record updates, used by segment edit/delete.
        // In that case, keep the original replace behavior.
        DTRDaily entity = toEntity(dto);
        DTRDaily saved = dtrDailyRepository.save(entity);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public DTRDailyDTO editDTRSegment(Long dtrSegmentId, DTRSegmentEditRequest request) {
        if (dtrSegmentId == null) {
            throw new IllegalArgumentException("DTR segment ID is required.");
        }
        if (request == null || request.getTimeIn() == null) {
            throw new IllegalArgumentException("Time In is required.");
        }

        DTRSegment segment = dtrSegmentRepository.findById(dtrSegmentId)
                .orElseThrow(() -> new IllegalArgumentException("DTR segment was not found."));
        DTRDaily daily = segment.getDtrDaily();
        if (daily == null) {
            throw new IllegalStateException("DTR segment has no parent daily transaction.");
        }

        validateEditedTimes(request);

        LocalDate workDate = daily.getWorkDate();
        LocalDateTime actualIn = workDate.atTime(request.getTimeIn());
        LocalDateTime actualBreakOut = resolveActualTime(workDate, request.getBreakOut(), actualIn);
        LocalDateTime actualBreakIn = resolveActualTime(workDate, request.getBreakIn(), actualIn);
        LocalDateTime actualOut = resolveActualTime(workDate, request.getTimeOut(), actualIn);

        ShiftTemplate shift = resolveBestShiftTemplate(
                daily.getEmployeeId(),
                workDate,
                segment.getSegmentNo(),
                actualIn,
                actualBreakOut,
                actualBreakIn,
                actualOut
        );

        boolean nonWorkingDuty = isDayOff(daily.getEmployeeId(), workDate)
                || isNonWorkingHoliday(workDate);
        ComputedMinutes minutes = computeEditedMinutes(
                workDate,
                actualIn,
                actualBreakOut,
                actualBreakIn,
                actualOut,
                shift,
                nonWorkingDuty
        );

        segment.setTimeIn(request.getTimeIn());
        segment.setBreakOut(request.getBreakOut());
        segment.setBreakIn(request.getBreakIn());
        segment.setTimeOut(request.getTimeOut());
        segment.setWorkMinutes(minutes.workMinutes);
        segment.setLateMinutes(minutes.lateMinutes);
        segment.setUndertimeMinutes(minutes.undertimeMinutes);
        segment.setOvertimeMinutes(minutes.overtimeMinutes);

        // Any administrator edit is authoritative. Changing ADMS to MANUAL is
        // what protects the corrected transaction from a future Search rebuild.
        segment.setSourceType(SOURCE_MANUAL);
        dtrSegmentRepository.save(segment);

        daily.setAttendanceStatus(resolveAttendanceStatus(request));
        recomputeDailyTotals(daily);
        return toDTO(dtrDailyRepository.save(daily));
    }

    private void forceManualSource(List<DTRSegmentDTO> segments) {
        if (segments == null) {
            return;
        }
        for (DTRSegmentDTO segment : segments) {
            if (segment != null) {
                segment.setSourceType(SOURCE_MANUAL);
            }
        }
    }

    private void validateEditedTimes(DTRSegmentEditRequest request) {
        LocalTime timeIn = request.getTimeIn();
        LocalTime breakOut = request.getBreakOut();
        LocalTime breakIn = request.getBreakIn();
        LocalTime timeOut = request.getTimeOut();

        if (breakIn != null && breakOut == null) {
            throw new IllegalArgumentException("Break In requires Break Out.");
        }
        if (timeOut != null && ((breakOut == null) != (breakIn == null))) {
            throw new IllegalArgumentException(
                    "A completed segment must contain both Break Out and Break In, or neither."
            );
        }
        if (timeOut != null && timeOut.equals(timeIn)) {
            throw new IllegalArgumentException("Time Out cannot be the same as Time In.");
        }

        LocalDate validationDate = LocalDate.of(2000, 1, 1);
        LocalDateTime in = validationDate.atTime(timeIn);
        LocalDateTime bo = resolveActualTime(validationDate, breakOut, in);
        LocalDateTime bi = resolveActualTime(validationDate, breakIn, in);
        LocalDateTime out = resolveActualTime(validationDate, timeOut, in);

        if (bo != null && bo.isBefore(in)) {
            throw new IllegalArgumentException("Break Out must occur after Time In.");
        }
        if (bi != null && (bo == null || bi.isBefore(bo))) {
            throw new IllegalArgumentException("Break In must occur after Break Out.");
        }
        if (out != null) {
            LocalDateTime lastKnown = bi != null ? bi : (bo != null ? bo : in);
            if (!out.isAfter(lastKnown)) {
                throw new IllegalArgumentException(
                        "Time order must be Time In → Break Out → Break In → Time Out."
                );
            }
        }
    }

    private String resolveAttendanceStatus(DTRSegmentEditRequest request) {
        if (request.getTimeOut() != null) {
            return "Present";
        }
        if (request.getBreakOut() != null && request.getBreakIn() == null) {
            return "ON BREAK";
        }
        return "IN PROGRESS";
    }

    private ComputedMinutes computeEditedMinutes(
            LocalDate workDate,
            LocalDateTime actualIn,
            LocalDateTime actualBreakOut,
            LocalDateTime actualBreakIn,
            LocalDateTime actualOut,
            ShiftTemplate shift,
            boolean nonWorkingDuty) {

        long workMinutes = 0;
        if (actualOut != null) {
            if (actualBreakOut != null && actualBreakIn != null) {
                workMinutes = nonNegativeMinutes(actualIn, actualBreakOut)
                        + nonNegativeMinutes(actualBreakIn, actualOut);
            } else {
                workMinutes = nonNegativeMinutes(actualIn, actualOut);
            }
        } else if (actualBreakOut != null) {
            // For an incomplete transaction, only count work that is already known.
            workMinutes = nonNegativeMinutes(actualIn, actualBreakOut);
        }

        if (nonWorkingDuty) {
            int work = safeInt(workMinutes);
            return new ComputedMinutes(work, 0, 0, work);
        }

        LocalDateTime baseScheduledStart = workDate.atTime(shift.timeIn);
        LocalDateTime plannedStart = workDate.atTime(shift.effectiveTimeIn(workDate));
        LocalDateTime plannedOut = resolveAfterStart(workDate, shift.timeOut, baseScheduledStart);
        LocalDateTime plannedBreakOut = shift.breakOut == null
                ? null
                : resolveAfterStart(workDate, shift.breakOut, baseScheduledStart);
        LocalDateTime plannedBreakIn = shift.breakIn == null
                ? null
                : resolveAfterStart(workDate, shift.breakIn, baseScheduledStart);

        int late = nonNegativeMinutes(plannedStart, actualIn);
        if (actualBreakIn != null && plannedBreakIn != null) {
            late += nonNegativeMinutes(plannedBreakIn, actualBreakIn);
        }

        int undertime = 0;
        if (actualBreakOut != null && plannedBreakOut != null) {
            undertime += nonNegativeMinutes(actualBreakOut, plannedBreakOut);
        }
        if (actualOut != null) {
            undertime += nonNegativeMinutes(actualOut, plannedOut);
        }

        int overtime = actualOut == null ? 0 : nonNegativeMinutes(plannedOut, actualOut);
        return new ComputedMinutes(safeInt(workMinutes), late, undertime, overtime);
    }

    private ShiftTemplate resolveBestShiftTemplate(
            String employeeId,
            LocalDate workDate,
            Integer segmentNo,
            LocalDateTime actualIn,
            LocalDateTime actualBreakOut,
            LocalDateTime actualBreakIn,
            LocalDateTime actualOut) {

        List<ShiftTemplate> plotted = loadPlottedShiftTemplates(employeeId, workDate);
        ShiftTemplate selected = chooseBestShift(
                plotted, workDate, segmentNo, actualIn, actualBreakOut, actualBreakIn, actualOut
        );
        if (selected != null) {
            return selected;
        }

        selected = chooseBestShift(
                loadAllShiftTemplates(), workDate, segmentNo,
                actualIn, actualBreakOut, actualBreakIn, actualOut
        );
        if (selected != null) {
            return selected;
        }

        return ShiftTemplate.defaultShift();
    }

    private List<ShiftTemplate> loadPlottedShiftTemplates(String employeeId, LocalDate workDate) {
        LocalDateTime from = workDate.atStartOfDay();
        LocalDateTime to = workDate.plusDays(1).atStartOfDay().minusNanos(1);
        Optional<List<WorkSchedule>> schedules = workScheduleRepository
                .findByEmployeeIdAndWsDateTimeBetweenOrderByWsDateTimeAscWsIdAsc(employeeId, from, to);

        if (schedules.isEmpty()) {
            return List.of();
        }

        return schedules.get().stream()
                .filter(row -> !Boolean.TRUE.equals(row.getIsDayOff()))
                .filter(row -> row.getTsCode() != null && !row.getTsCode().isBlank())
                .map(row -> loadShiftTemplate(row.getTsCode()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ShiftTemplate chooseBestShift(
            List<ShiftTemplate> shifts,
            LocalDate workDate,
            Integer segmentNo,
            LocalDateTime actualIn,
            LocalDateTime actualBreakOut,
            LocalDateTime actualBreakIn,
            LocalDateTime actualOut) {

        if (shifts == null || shifts.isEmpty()) {
            return null;
        }

        LocalDateTime actualEnd = actualOut != null
                ? actualOut
                : actualBreakIn != null
                ? actualBreakIn
                : actualBreakOut != null
                ? actualBreakOut
                : actualIn.plusMinutes(1);

        ShiftTemplate exact = shifts.stream()
                .filter(shift -> {
                    LocalDateTime plannedStart = workDate.atTime(shift.timeIn);
                    LocalDateTime plannedEnd = resolveAfterStart(workDate, shift.timeOut, plannedStart);
                    boolean startMatches = plannedStart.toLocalTime().equals(actualIn.toLocalTime());
                    boolean endMatches = actualOut == null
                            || plannedEnd.toLocalTime().equals(actualOut.toLocalTime());
                    return startMatches && endMatches;
                })
                .findFirst()
                .orElse(null);
        if (exact != null) {
            return exact;
        }

        ShiftTemplate best = null;
        long bestOverlap = 0;
        long bestDistance = Long.MAX_VALUE;

        for (ShiftTemplate shift : shifts) {
            LocalDateTime plannedStart = workDate.atTime(shift.timeIn);
            LocalDateTime plannedEnd = resolveAfterStart(workDate, shift.timeOut, plannedStart);
            LocalDateTime overlapStart = actualIn.isAfter(plannedStart) ? actualIn : plannedStart;
            LocalDateTime overlapEnd = actualEnd.isBefore(plannedEnd) ? actualEnd : plannedEnd;
            long overlap = Math.max(0, ChronoUnit.MINUTES.between(overlapStart, overlapEnd));
            long distance = Math.abs(ChronoUnit.MINUTES.between(plannedStart, actualIn));

            if (overlap > bestOverlap || (overlap == bestOverlap && distance < bestDistance)) {
                best = shift;
                bestOverlap = overlap;
                bestDistance = distance;
            }
        }

        if (best != null && (bestOverlap > 0 || shifts.size() == 1)) {
            return best;
        }

        if (segmentNo != null && segmentNo > 0 && segmentNo <= shifts.size()) {
            return shifts.get(segmentNo - 1);
        }

        return shifts.stream()
                .min(Comparator.comparingLong(shift -> {
                    LocalDateTime plannedStart = workDate.atTime(shift.timeIn);
                    return Math.abs(ChronoUnit.MINUTES.between(plannedStart, actualIn));
                }))
                .orElse(null);
    }

    private ShiftTemplate loadShiftTemplate(String tsCode) {
        if (tsCode == null || tsCode.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT tsCode, timeIn, breakOut, breakIn, timeOut, tsFlexible, "
                    + "monInTimeLimit, tueInTimeLimit, wedInTimeLimit, thuInTimeLimit, "
                    + "friInTimeLimit, satInTimeLimit, sunInTimeLimit "
                    + "FROM time_shift "
                    + "WHERE LOWER(LTRIM(RTRIM(tsCode))) = LOWER(LTRIM(RTRIM(?)))";
            List<Map<String, Object>> rows = jdbc.queryForList(sql, tsCode);
            return rows.isEmpty() ? null : toShiftTemplate(rows.get(0));
        } catch (DataAccessException exception) {
            try {
                String fallbackSql = "SELECT tsCode, timeIn, breakOut, breakIn, timeOut "
                        + "FROM time_shift "
                        + "WHERE LOWER(LTRIM(RTRIM(tsCode))) = LOWER(LTRIM(RTRIM(?)))";
                List<Map<String, Object>> rows = jdbc.queryForList(fallbackSql, tsCode);
                return rows.isEmpty() ? null : toShiftTemplate(rows.get(0));
            } catch (DataAccessException ignored) {
                return null;
            }
        }
    }

    private List<ShiftTemplate> loadAllShiftTemplates() {
        try {
            String sql = "SELECT tsCode, timeIn, breakOut, breakIn, timeOut, tsFlexible, "
                    + "monInTimeLimit, tueInTimeLimit, wedInTimeLimit, thuInTimeLimit, "
                    + "friInTimeLimit, satInTimeLimit, sunInTimeLimit FROM time_shift";
            return jdbc.queryForList(sql).stream()
                    .map(this::toShiftTemplate)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (DataAccessException exception) {
            try {
                return jdbc.queryForList(
                                "SELECT tsCode, timeIn, breakOut, breakIn, timeOut FROM time_shift"
                        ).stream()
                        .map(this::toShiftTemplate)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            } catch (DataAccessException ignored) {
                return List.of();
            }
        }
    }

    private ShiftTemplate toShiftTemplate(Map<String, Object> row) {
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
                timeOut,
                isDbTrue(row.get("tsFlexible")),
                toLocalTime(row.get("monInTimeLimit")),
                toLocalTime(row.get("tueInTimeLimit")),
                toLocalTime(row.get("wedInTimeLimit")),
                toLocalTime(row.get("thuInTimeLimit")),
                toLocalTime(row.get("friInTimeLimit")),
                toLocalTime(row.get("satInTimeLimit")),
                toLocalTime(row.get("sunInTimeLimit"))
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
        if (value instanceof java.sql.Timestamp timestamp) {
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

    private boolean isDayOff(String employeeId, LocalDate workDate) {
        LocalDateTime from = workDate.atStartOfDay();
        LocalDateTime to = workDate.plusDays(1).atStartOfDay().minusNanos(1);
        Optional<List<WorkSchedule>> rows = workScheduleRepository
                .findByEmployeeIdAndWsDateTimeBetweenOrderByWsDateTimeAscWsIdAsc(employeeId, from, to);
        return rows.orElseGet(List::of).stream()
                .anyMatch(row -> Boolean.TRUE.equals(row.getIsDayOff()));
    }

    private boolean isNonWorkingHoliday(LocalDate workDate) {
        try {
            String sql = "SELECT COUNT(*) FROM holiday "
                    + "WHERE isActive = 1 "
                    + "AND (isWorkingHoliday = 0 OR isWorkingHoliday IS NULL) "
                    + "AND (holidayType IS NULL OR holidayType <> 'SPECIAL_WORKING') "
                    + "AND CAST(CASE WHEN observedDate IS NOT NULL THEN observedDate ELSE holidayDate END AS DATE) = ?";
            Integer count = jdbc.queryForObject(sql, Integer.class, java.sql.Date.valueOf(workDate));
            return count != null && count > 0;
        } catch (DataAccessException exception) {
            return false;
        }
    }

    private LocalDateTime resolveActualTime(LocalDate workDate, LocalTime time, LocalDateTime actualStart) {
        if (time == null) {
            return null;
        }
        LocalDateTime resolved = workDate.atTime(time);
        if (resolved.isBefore(actualStart)) {
            resolved = resolved.plusDays(1);
        }
        return resolved;
    }

    private LocalDateTime resolveAfterStart(LocalDate workDate, LocalTime time, LocalDateTime plannedStart) {
        LocalDateTime resolved = workDate.atTime(time);
        if (resolved.isBefore(plannedStart)) {
            resolved = resolved.plusDays(1);
        }
        return resolved;
    }

    private int nonNegativeMinutes(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return 0;
        }
        return (int) Math.max(0, ChronoUnit.MINUTES.between(from, to));
    }

    private int safeInt(long value) {
        if (value <= 0) {
            return 0;
        }
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private static final class ComputedMinutes {
        private final int workMinutes;
        private final int lateMinutes;
        private final int undertimeMinutes;
        private final int overtimeMinutes;

        private ComputedMinutes(int workMinutes, int lateMinutes, int undertimeMinutes, int overtimeMinutes) {
            this.workMinutes = workMinutes;
            this.lateMinutes = lateMinutes;
            this.undertimeMinutes = undertimeMinutes;
            this.overtimeMinutes = overtimeMinutes;
        }
    }

    private static final class ShiftTemplate {
        private final String tsCode;
        private final LocalTime timeIn;
        private final LocalTime breakOut;
        private final LocalTime breakIn;
        private final LocalTime timeOut;
        private final boolean flexible;
        private final LocalTime monLimit;
        private final LocalTime tueLimit;
        private final LocalTime wedLimit;
        private final LocalTime thuLimit;
        private final LocalTime friLimit;
        private final LocalTime satLimit;
        private final LocalTime sunLimit;

        private ShiftTemplate(
                String tsCode,
                LocalTime timeIn,
                LocalTime breakOut,
                LocalTime breakIn,
                LocalTime timeOut,
                boolean flexible,
                LocalTime monLimit,
                LocalTime tueLimit,
                LocalTime wedLimit,
                LocalTime thuLimit,
                LocalTime friLimit,
                LocalTime satLimit,
                LocalTime sunLimit) {
            this.tsCode = tsCode;
            this.timeIn = timeIn;
            this.breakOut = breakOut;
            this.breakIn = breakIn;
            this.timeOut = timeOut;
            this.flexible = flexible;
            this.monLimit = monLimit;
            this.tueLimit = tueLimit;
            this.wedLimit = wedLimit;
            this.thuLimit = thuLimit;
            this.friLimit = friLimit;
            this.satLimit = satLimit;
            this.sunLimit = sunLimit;
        }

        private static ShiftTemplate defaultShift() {
            return new ShiftTemplate(
                    "DEFAULT_8_TO_5",
                    LocalTime.of(8, 0),
                    LocalTime.of(12, 0),
                    LocalTime.of(13, 0),
                    LocalTime.of(17, 0),
                    false,
                    null, null, null, null, null, null, null
            );
        }

        private LocalTime effectiveTimeIn(LocalDate workDate) {
            if (!flexible) {
                return timeIn;
            }
            LocalTime limit;
            switch (workDate.getDayOfWeek()) {
                case MONDAY -> limit = monLimit;
                case TUESDAY -> limit = tueLimit;
                case WEDNESDAY -> limit = wedLimit;
                case THURSDAY -> limit = thuLimit;
                case FRIDAY -> limit = friLimit;
                case SATURDAY -> limit = satLimit;
                case SUNDAY -> limit = sunLimit;
                default -> limit = null;
            }
            return limit == null ? timeIn : limit;
        }
    }

    private void appendSegmentsAndRecomputeTotals(DTRDaily daily, List<DTRSegmentDTO> incomingSegments) {
        if (incomingSegments == null || incomingSegments.isEmpty()) {
            return;
        }

        if (daily.getSegments() == null) {
            daily.setSegments(new ArrayList<>());
        }

        int nextSegmentNo = daily.getSegments().stream()
                .map(DTRSegment::getSegmentNo)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        for (DTRSegmentDTO segmentDTO : incomingSegments) {
            DTRSegment segment = toSegmentEntity(segmentDTO, daily);
            segment.setDtrSegmentId(null);
            segment.setSourceType(SOURCE_MANUAL);
            segment.setSegmentNo(nextSegmentNo++);

            DTRSegment savedSegment = dtrSegmentRepository.save(segment);
            daily.getSegments().add(savedSegment);
        }

        recomputeDailyTotals(daily);
    }

    private void recomputeDailyTotals(DTRDaily daily) {
        int totalWorkMinutes = 0;
        int totalLateMinutes = 0;
        int totalUndertimeMinutes = 0;
        int totalOvertimeMinutes = 0;

        if (daily.getSegments() != null) {
            for (DTRSegment segment : daily.getSegments()) {
                totalWorkMinutes += segment.getWorkMinutes();
                totalLateMinutes += segment.getLateMinutes();
                totalUndertimeMinutes += segment.getUndertimeMinutes();
                totalOvertimeMinutes += segment.getOvertimeMinutes();
            }
        }

        daily.setTotalWorkMinutes(totalWorkMinutes);
        daily.setTotalLateMinutes(totalLateMinutes);
        daily.setTotalUndertimeMinutes(totalUndertimeMinutes);
        daily.setTotalOvertimeMinutes(totalOvertimeMinutes);
    }

    @Override
    public List<DTRDailyDTO> getEmployeeDTRDaily(String employeeId, LocalDateTime fromDate, LocalDateTime toDate) {
        // Convert LocalDateTime to LocalDate for the repository query
        // DTR_DAILY uses LocalDate (work_date) not LocalDateTime
        return dtrDailyRepository
                .findByEmployeeIdAndWorkDateBetween(employeeId, fromDate.toLocalDate(), toDate.toLocalDate())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getBulkDtrSummary(LocalDate from, LocalDate to) {
        // Query combines actual DTR records + rest days + approved requests (OB, OT, Pass Slip, Time Correction, CTO, Training)
        // This ensures the payroll service has complete attendance data with all excused absences
        String sql = "WITH AllDates AS ( " +
                "    SELECT " +
                "        e.employeeNo, " +
                "        e.employeeId, " +
            "        COALESCE(d.work_date, CAST(ws.wsDateTime AS DATE)) AS dtrDate, " +
                "        COALESCE(d.attendance_status, 'REST DAY') AS status, " +
                "        COALESCE(d.total_work_minutes, 0) AS workMinutes, " +
                "        COALESCE(d.total_late_minutes, 0) AS lateMinutes, " +
                "        COALESCE(d.total_undertime_minutes, 0) AS undertimeMinutes, " +
                "        COALESCE(d.total_overtime_minutes, 0) AS overtimeMinutes, " +
            "        CAST(ws.isDayOff AS VARCHAR(5)) AS isRestDay " +
                "    FROM work_schedule ws " +
                "    INNER JOIN employee e ON CAST(e.employeeId AS VARCHAR(64)) = ws.employeeId " +
                "    LEFT JOIN dtr_daily d ON d.employee_id = ws.employeeId " +
            "        AND d.work_date = CAST(ws.wsDateTime AS DATE) " +
            "    WHERE CAST(ws.wsDateTime AS DATE) BETWEEN ? AND ? " +
                "    UNION " +
                "    SELECT " +
                "        e.employeeNo, " +
                "        e.employeeId, " +
                "        d.work_date AS dtrDate, " +
                "        d.attendance_status AS status, " +
                "        d.total_work_minutes AS workMinutes, " +
                "        d.total_late_minutes AS lateMinutes, " +
                "        d.total_undertime_minutes AS undertimeMinutes, " +
                "        d.total_overtime_minutes AS overtimeMinutes, " +
                "        CAST(0 AS VARCHAR(5)) AS isRestDay " +
                "    FROM dtr_daily d " +
                "    INNER JOIN employee e ON CAST(e.employeeId AS VARCHAR(64)) = d.employee_id " +
                "    WHERE d.work_date BETWEEN ? AND ? " +
                "        AND NOT EXISTS ( " +
                "            SELECT 1 FROM work_schedule ws2 " +
                "            WHERE ws2.employeeId = d.employee_id " +
                "            AND CAST(ws2.wsDateTime AS DATE) = d.work_date " +
                "        ) " +
                ") " +
                "SELECT " +
                "    ad.employeeNo, " +
                "    ad.dtrDate, " +
                "    ad.status, " +
                "    ad.workMinutes, " +
                "    ad.lateMinutes, " +
                "    ad.undertimeMinutes, " +
                "    ad.overtimeMinutes, " +
                "    ad.isRestDay, " +
                "    CASE WHEN ob.officialEngagementApplicationId IS NOT NULL THEN 1 ELSE 0 END AS hasApprovedOb, " +
                "    CASE WHEN ot.overtimeRequestId IS NOT NULL THEN 1 ELSE 0 END AS hasApprovedOt, " +
                "    CASE WHEN ps.passSlipId IS NOT NULL THEN 1 ELSE 0 END AS hasApprovedPs, " +
                "    CASE WHEN tc.timeCorrectionId IS NOT NULL THEN 1 ELSE 0 END AS hasApprovedTc, " +
                "    CASE WHEN cto.ctoId IS NOT NULL THEN 1 ELSE 0 END AS hasApprovedCto, " +
                "    CASE WHEN ld.learningAndDevelopmentId IS NOT NULL THEN 1 ELSE 0 END AS hasTraining " +
                "FROM AllDates ad " +
                "LEFT JOIN official_engagement_application ob " +
                "    ON ob.employeeId = ad.employeeId " +
                "    AND ob.status = 'Approved' " +
                "    AND ad.dtrDate BETWEEN ob.startDate AND ob.endDate " +
                "LEFT JOIN overtime_request ot " +
                "    ON ot.employeeId = ad.employeeId " +
                "    AND ot.status = 'Approved' " +
                "    AND ad.dtrDate = CAST(ot.dateTimeFrom AS DATE) " +
                "LEFT JOIN pass_slip ps " +
                "    ON ps.employeeId = ad.employeeId " +
                "    AND ps.status = 'Approved' " +
                "    AND ad.dtrDate = ps.passSlipDate " +
                "LEFT JOIN time_correction tc " +
                "    ON tc.employeeId = ad.employeeId " +
                "    AND tc.status = 'Approved' " +
                "    AND ad.dtrDate = tc.workDate " +
                "LEFT JOIN compensatory_time_off cto " +
                "    ON cto.employeeId = ad.employeeId " +
                "    AND cto.status = 'Approved' " +
                "    AND ad.dtrDate = cto.dateOfOffset " +
                "LEFT JOIN personaldata pd " +
                "    ON pd.employeeId = ad.employeeId " +
                "LEFT JOIN learninganddevelopment ld " +
                "    ON ld.personalDataId = pd.personalDataId " +
                "    AND ad.dtrDate BETWEEN ld.fromDate AND ld.toDate " +
                "ORDER BY ad.employeeNo, ad.dtrDate";

        return jdbc.query(sql, 
                new Object[]{
                    java.sql.Date.valueOf(from), 
                    java.sql.Date.valueOf(to),
                    java.sql.Date.valueOf(from), 
                    java.sql.Date.valueOf(to)
                },
                (rs, rowNum) -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("employeeNo", rs.getString("employeeNo"));
                    row.put("dtrDate", rs.getDate("dtrDate").toLocalDate());
                    
                    String status = rs.getString("status");
                    Integer workMin = rs.getInt("workMinutes");
                    Object isRestDayRaw = rs.getObject("isRestDay");
                    boolean isRestDayFlag = isDbTrue(isRestDayRaw);
                    
                    // Get approval flags from database
                    Integer hasOb = rs.getInt("hasApprovedOb");
                    Integer hasOt = rs.getInt("hasApprovedOt");
                    Integer hasPs = rs.getInt("hasApprovedPs");
                    Integer hasTc = rs.getInt("hasApprovedTc");
                    Integer hasCto = rs.getInt("hasApprovedCto");
                    Integer hasTraining = rs.getInt("hasTraining");
                    
                    // Determine if present (status PRESENT or has work minutes > 0)
                    // But NOT if it's a rest day
                    boolean present = ("PRESENT".equalsIgnoreCase(status) || 
                                      "Present".equalsIgnoreCase(status) ||
                                      (workMin != null && workMin > 0)) && !isRestDayFlag;
                    row.put("present", present);
                    
                    // Check if rest day (from work_schedule isDayOff flag)
                    boolean restDay = isRestDayFlag || 
                                      "REST DAY".equalsIgnoreCase(status) || 
                                      "RESTDAY".equalsIgnoreCase(status);
                    row.put("restDay", restDay);
                    
                    row.put("lateMinutes", rs.getInt("lateMinutes"));
                    row.put("undertimeMinutes", rs.getInt("undertimeMinutes"));
                    
                    // Set approval flags from database query results
                    // These flags tell payroll that the employee had approved requests to excuse absence/tardiness
                    row.put("hasApprovedOb", hasOb == 1);
                    row.put("hasApprovedOt", hasOt == 1);
                    row.put("hasApprovedPs", hasPs == 1);
                    row.put("hasApprovedTc", hasTc == 1);
                    row.put("hasApprovedCto", hasCto == 1);
                    row.put("hasTraining", hasTraining == 1);
                    
                    return row;
                });
    }

    @Override
    public void generateDtrReport(String employeeId, LocalDate fromDate, LocalDate toDate, OutputStream out) throws Exception {
        JasperReport report = compile("reports/dtrNew.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("EMPLOYEE_ID", employeeId);
        params.put("fromDate", java.sql.Date.valueOf(fromDate));
        params.put("toDate", java.sql.Date.valueOf(toDate));
        params.put("lastDtrDate", java.sql.Date.valueOf(toDate));
        params.put("webAppPath", "");

        Map<String, Object> settings = loadLatestReportSettings();

        params.put("currentCompany", settings.getOrDefault("companyName", ""));
        params.put("currentCompanyAddress", settings.getOrDefault("address", ""));
        params.put("isDOH", isDbTrue(settings.get("hospitalAgency")));

        byte[] leftLogo = (byte[]) settings.get("leftHeaderLogo");
        byte[] rightLogo = (byte[]) settings.get("rightHeaderLogo");
        params.put("logoleft", toValidImageInputStream(leftLogo));
        params.put("logoright", toValidImageInputStream(rightLogo));

        List<DtrReportRow> reportRows =
                new DtrReportDataLoader(jdbc).load(employeeId, fromDate, toDate);
        JasperPrint print = JasperFillManager.fillReport(
                report,
                params,
                new JRBeanCollectionDataSource(reportRows)
        );
        JasperExportManager.exportReportToPdfStream(print, out);
    }

    Map<String, Object> loadLatestReportSettings() {
        Map<String, Object> settings = jdbc.query(
                connection -> {
                    java.sql.PreparedStatement statement = connection.prepareStatement(
                            "SELECT companyName, address, hospitalAgency, leftHeaderLogo, rightHeaderLogo "
                                    + "FROM settings ORDER BY settingsId DESC"
                    );
                    statement.setMaxRows(1);
                    return statement;
                },
                rs -> {
                    if (!rs.next()) return Collections.<String, Object>emptyMap();
                    Map<String, Object> result = new HashMap<>();
                    result.put("companyName", rs.getString("companyName"));
                    result.put("address", rs.getString("address"));
                    result.put("hospitalAgency", rs.getObject("hospitalAgency"));
                    result.put("leftHeaderLogo", rs.getBytes("leftHeaderLogo"));
                    result.put("rightHeaderLogo", rs.getBytes("rightHeaderLogo"));
                    return result;
                }
        );
        return settings == null ? Collections.emptyMap() : settings;
    }

    private InputStream toValidImageInputStream(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }

        try (ByteArrayInputStream validationStream = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(validationStream);
            if (image == null) {
                return null;
            }
            return new ByteArrayInputStream(imageBytes);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isDbTrue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        if (value == null) {
            return false;
        }
        String text = value.toString().trim();
        return "true".equalsIgnoreCase(text) || "t".equalsIgnoreCase(text)
                || "yes".equalsIgnoreCase(text) || "y".equalsIgnoreCase(text)
                || "1".equals(text);
    }

    private JasperReport compile(String classpathPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        try (InputStream is = resource.getInputStream()) {
            return JasperCompileManager.compileReport(is);
        }
    }

    private DTRDailyDTO toDTO(DTRDaily entity) {
        DTRDailyDTO dto = new DTRDailyDTO();
        dto.setDtrDailyId(entity.getDtrDailyId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setWorkDate(entity.getWorkDate().atStartOfDay());
        dto.setTotalWorkMinutes(entity.getTotalWorkMinutes());
        dto.setTotalLateMinutes(entity.getTotalLateMinutes());
        dto.setTotalUndertimeMinutes(entity.getTotalUndertimeMinutes());
        dto.setTotalOvertimeMinutes(entity.getTotalOvertimeMinutes());
        dto.setAttendanceStatus(entity.getAttendanceStatus());
        if (entity.getSegments() != null) {
            dto.setSegments(entity.getSegments().stream().map(this::toSegmentDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private DTRSegmentDTO toSegmentDTO(DTRSegment segment) {
        DTRSegmentDTO dto = new DTRSegmentDTO();
        dto.setDtrSegmentId(segment.getDtrSegmentId());
        dto.setSegmentNo(segment.getSegmentNo());
        dto.setTimeIn(segment.getTimeIn());
        dto.setBreakOut(segment.getBreakOut());
        dto.setBreakIn(segment.getBreakIn());
        dto.setTimeOut(segment.getTimeOut());
        dto.setWorkMinutes(segment.getWorkMinutes());
        dto.setLateMinutes(segment.getLateMinutes());
        dto.setUndertimeMinutes(segment.getUndertimeMinutes());
        dto.setOvertimeMinutes(segment.getOvertimeMinutes());
        dto.setSourceType(segment.getSourceType());
        return dto;
    }

    private DTRDaily toEntity(DTRDailyDTO dto) {
        DTRDaily entity = new DTRDaily();
        entity.setDtrDailyId(dto.getDtrDailyId());
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setWorkDate(dto.getWorkDate().toLocalDate());
        entity.setTotalWorkMinutes(dto.getTotalWorkMinutes());
        entity.setTotalLateMinutes(dto.getTotalLateMinutes());
        entity.setTotalUndertimeMinutes(dto.getTotalUndertimeMinutes());
        entity.setTotalOvertimeMinutes(dto.getTotalOvertimeMinutes());
        entity.setAttendanceStatus(dto.getAttendanceStatus());
        if (dto.getSegments() != null) {
            entity.setSegments(dto.getSegments().stream().map(s -> toSegmentEntity(s, entity)).collect(Collectors.toList()));
        }
        return entity;
    }

    private DTRSegment toSegmentEntity(DTRSegmentDTO dto, DTRDaily parent) {
        DTRSegment entity = new DTRSegment();
        entity.setDtrSegmentId(dto.getDtrSegmentId());
        entity.setDtrDaily(parent);
        entity.setSegmentNo(dto.getSegmentNo());
        entity.setTimeIn(dto.getTimeIn());
        entity.setBreakOut(dto.getBreakOut());
        entity.setBreakIn(dto.getBreakIn());
        entity.setTimeOut(dto.getTimeOut());
        entity.setWorkMinutes(dto.getWorkMinutes());
        entity.setLateMinutes(dto.getLateMinutes());
        entity.setUndertimeMinutes(dto.getUndertimeMinutes());
        entity.setOvertimeMinutes(dto.getOvertimeMinutes());
        entity.setSourceType(
                dto.getSourceType() == null || dto.getSourceType().isBlank()
                        ? SOURCE_MANUAL
                        : dto.getSourceType()
        );
        return entity;
    }
}
