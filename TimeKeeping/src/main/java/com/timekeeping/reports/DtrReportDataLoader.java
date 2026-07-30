package com.timekeeping.reports;

import com.timekeeping.dtos.DtrReportRow;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Loads and assembles DTR report rows without database-vendor date functions,
 * APPLY operators, TOP/LIMIT clauses, or boolean literals in shared SQL.
 */
public final class DtrReportDataLoader {
    private static final LocalTime NOON = LocalTime.NOON;

    private final JdbcTemplate jdbc;

    public DtrReportDataLoader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DtrReportRow> load(String employeeReference, LocalDate fromDate, LocalDate toDate) {
        if (employeeReference == null || employeeReference.trim().isEmpty()) {
            return List.of();
        }
        if (fromDate == null || toDate == null || toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("A valid DTR report date range is required.");
        }

        EmployeeInfo employee = findEmployee(employeeReference.trim());
        if (employee == null) {
            return List.of();
        }

        Set<String> employeeAliases = new LinkedHashSet<>();
        employeeAliases.add(employeeReference.trim());
        employeeAliases.add(String.valueOf(employee.employeeId()));
        if (!isBlank(employee.employeeNo())) {
            employeeAliases.add(employee.employeeNo().trim());
        }

        LocalDate segmentFromDate = fromDate.minusDays(1);
        List<Daily> dailies = loadDailies(employeeAliases, segmentFromDate, toDate);
        List<Segment> segments = loadSegments(employeeAliases, segmentFromDate, toDate);
        List<WorkScheduleDay> schedules =
                loadSchedules(employeeAliases, employee.employeeId(), fromDate, toDate);

        Map<LocalDate, OfficialEngagement> officialEngagements =
                loadOfficialEngagements(employee.employeeId(), fromDate, toDate);
        Set<LocalDate> overtimeDates = loadOvertimeDates(employee.employeeId(), fromDate, toDate);
        Set<LocalDate> ctoDates = loadCtoDates(employee.employeeId(), fromDate, toDate);
        Map<LocalDate, PassSlip> passSlips = loadPassSlips(employee.employeeId(), fromDate, toDate);
        Map<LocalDate, TimeCorrection> timeCorrections =
                loadTimeCorrections(employee.employeeId(), fromDate, toDate);
        Map<LocalDate, Leave> leaves = loadLeaves(employee.employeeId(), fromDate, toDate);

        return assemble(
                employee,
                fromDate,
                toDate,
                dailies,
                segments,
                schedules,
                officialEngagements,
                overtimeDates,
                ctoDates,
                passSlips,
                timeCorrections,
                leaves
        );
    }

    private EmployeeInfo findEmployee(String employeeReference) {
        Long numericId = parseLong(employeeReference);
        List<EmployeeInfo> matches;
        if (numericId == null) {
            matches = jdbc.query(
                    """
                    SELECT
                        e.employeeId,
                        e.employeeNo,
                        e.firstname AS employeeFirstname,
                        e.lastname AS employeeLastname,
                        pd.firstname AS personalFirstname,
                        pd.middlename AS personalMiddlename,
                        pd.surname AS personalSurname
                    FROM employee e
                    LEFT JOIN personaldata pd ON pd.employeeId = e.employeeId
                    WHERE e.employeeNo = ?
                    ORDER BY e.employeeId
                    """,
                    (rs, rowNum) -> employeeInfo(rs),
                    employeeReference
            );
        } else {
            matches = jdbc.query(
                    """
                    SELECT
                        e.employeeId,
                        e.employeeNo,
                        e.firstname AS employeeFirstname,
                        e.lastname AS employeeLastname,
                        pd.firstname AS personalFirstname,
                        pd.middlename AS personalMiddlename,
                        pd.surname AS personalSurname
                    FROM employee e
                    LEFT JOIN personaldata pd ON pd.employeeId = e.employeeId
                    WHERE e.employeeId = ? OR e.employeeNo = ?
                    ORDER BY
                        CASE WHEN e.employeeNo = ? THEN 0 ELSE 1 END,
                        e.employeeId
                    """,
                    (rs, rowNum) -> employeeInfo(rs),
                    numericId,
                    employeeReference,
                    employeeReference
            );
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private EmployeeInfo employeeInfo(ResultSet rs) throws SQLException {
        String firstName = firstNonNull(
                rs.getString("personalFirstname"),
                rs.getString("employeeFirstname")
        );
        String middleName = rs.getString("personalMiddlename");
        String surname = firstNonNull(
                rs.getString("personalSurname"),
                rs.getString("employeeLastname")
        );
        String middlePart = isBlank(middleName) ? " " : " " + middleName + " ";
        return new EmployeeInfo(
                rs.getLong("employeeId"),
                rs.getString("employeeNo"),
                nullToEmpty(firstName) + middlePart + nullToEmpty(surname)
        );
    }

    private List<Daily> loadDailies(Set<String> employeeAliases, LocalDate fromDate, LocalDate toDate) {
        List<Object> args = new ArrayList<>(employeeAliases);
        args.add(java.sql.Date.valueOf(fromDate));
        args.add(java.sql.Date.valueOf(toDate));
        return jdbc.query(
                """
                SELECT
                    dtr_daily_id,
                    work_date,
                    total_work_minutes,
                    total_late_minutes,
                    total_undertime_minutes,
                    total_overtime_minutes,
                    attendance_status
                FROM dtr_daily
                WHERE employee_id IN (%s)
                  AND work_date BETWEEN ? AND ?
                ORDER BY work_date, dtr_daily_id
                """.formatted(placeholders(employeeAliases.size())),
                (rs, rowNum) -> new Daily(
                        rs.getLong("dtr_daily_id"),
                        localDate(rs, "work_date"),
                        intValue(rs.getObject("total_work_minutes")),
                        intValue(rs.getObject("total_late_minutes")),
                        intValue(rs.getObject("total_undertime_minutes")),
                        intValue(rs.getObject("total_overtime_minutes")),
                        rs.getString("attendance_status")
                ),
                args.toArray()
        );
    }

    private List<Segment> loadSegments(Set<String> employeeAliases, LocalDate fromDate, LocalDate toDate) {
        List<Object> args = new ArrayList<>(employeeAliases);
        args.add(java.sql.Date.valueOf(fromDate));
        args.add(java.sql.Date.valueOf(toDate));
        return jdbc.query(
                """
                SELECT
                    ds.dtr_segment_id,
                    ds.dtr_daily_id,
                    d.work_date,
                    ds.segment_no,
                    ds.time_in,
                    ds.break_out,
                    ds.break_in,
                    ds.time_out,
                    ds.late_minutes,
                    ds.undertime_minutes
                FROM dtr_segment ds
                INNER JOIN dtr_daily d ON d.dtr_daily_id = ds.dtr_daily_id
                WHERE d.employee_id IN (%s)
                  AND d.work_date BETWEEN ? AND ?
                ORDER BY d.work_date, ds.segment_no, ds.dtr_segment_id
                """.formatted(placeholders(employeeAliases.size())),
                (rs, rowNum) -> new Segment(
                        rs.getLong("dtr_segment_id"),
                        rs.getLong("dtr_daily_id"),
                        localDate(rs, "work_date"),
                        intValue(rs.getObject("segment_no")),
                        localTime(rs, "time_in"),
                        localTime(rs, "break_out"),
                        localTime(rs, "break_in"),
                        localTime(rs, "time_out"),
                        intValue(rs.getObject("late_minutes")),
                        intValue(rs.getObject("undertime_minutes"))
                ),
                args.toArray()
        );
    }

    private List<WorkScheduleDay> loadSchedules(
            Set<String> employeeAliases,
            long numericEmployeeId,
            LocalDate fromDate,
            LocalDate toDate) {
        List<WorkScheduleSource> sourceRows = jdbc.query(
                """
                SELECT wsId, employeeId, wsDateTime, isDayOff
                FROM work_schedule
                WHERE wsDateTime >= ?
                  AND wsDateTime < ?
                ORDER BY wsId
                """,
                (rs, rowNum) -> new WorkScheduleSource(
                        rs.getLong("wsId"),
                        rs.getString("employeeId"),
                        rs.getTimestamp("wsDateTime").toLocalDateTime().toLocalDate(),
                        dbTrue(rs.getObject("isDayOff"))
                ),
                Timestamp.valueOf(fromDate.atStartOfDay()),
                Timestamp.valueOf(toDate.plusDays(1).atStartOfDay())
        );
        return sourceRows.stream()
                .filter(row -> employeeAliases.contains(row.employeeReference())
                        || Long.valueOf(numericEmployeeId).equals(parseLong(row.employeeReference())))
                .map(row -> new WorkScheduleDay(row.id(), row.date(), row.dayOff()))
                .toList();
    }

    private Map<LocalDate, OfficialEngagement> loadOfficialEngagements(
            long employeeId,
            LocalDate fromDate,
            LocalDate toDate) {
        List<OfficialEngagementRange> ranges = jdbc.query(
                """
                SELECT officialEngagementApplicationId, officialType, startDate, endDate
                FROM official_engagement_application
                WHERE employeeId = ?
                  AND status = 'Approved'
                  AND startDate <= ?
                  AND endDate >= ?
                ORDER BY officialEngagementApplicationId DESC
                """,
                (rs, rowNum) -> new OfficialEngagementRange(
                        rs.getLong("officialEngagementApplicationId"),
                        rs.getString("officialType"),
                        localDate(rs, "startDate"),
                        localDate(rs, "endDate")
                ),
                employeeId,
                java.sql.Date.valueOf(toDate),
                java.sql.Date.valueOf(fromDate)
        );
        Map<LocalDate, OfficialEngagement> result = new LinkedHashMap<>();
        for (OfficialEngagementRange range : ranges) {
            LocalDate start = max(range.startDate(), fromDate);
            LocalDate end = min(range.endDate(), toDate);
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                result.putIfAbsent(date, new OfficialEngagement(range.id(), range.officialType()));
            }
        }
        return result;
    }

    private Set<LocalDate> loadOvertimeDates(long employeeId, LocalDate fromDate, LocalDate toDate) {
        List<LocalDate> dates = jdbc.query(
                """
                SELECT dateTimeFrom
                FROM overtime_request
                WHERE employeeId = ?
                  AND status = 'Approved'
                  AND dateTimeFrom >= ?
                  AND dateTimeFrom < ?
                ORDER BY overtimeRequestId DESC
                """,
                (rs, rowNum) -> rs.getTimestamp("dateTimeFrom").toLocalDateTime().toLocalDate(),
                employeeId,
                Timestamp.valueOf(fromDate.atStartOfDay()),
                Timestamp.valueOf(toDate.plusDays(1).atStartOfDay())
        );
        return new LinkedHashSet<>(dates);
    }

    private Set<LocalDate> loadCtoDates(long employeeId, LocalDate fromDate, LocalDate toDate) {
        List<LocalDate> dates = jdbc.query(
                """
                SELECT dateOfOffset
                FROM compensatory_time_off
                WHERE employeeId = ?
                  AND status = 'Approved'
                  AND dateOfOffset BETWEEN ? AND ?
                ORDER BY ctoId DESC
                """,
                (rs, rowNum) -> localDate(rs, "dateOfOffset"),
                employeeId,
                java.sql.Date.valueOf(fromDate),
                java.sql.Date.valueOf(toDate)
        );
        return new LinkedHashSet<>(dates);
    }

    private Map<LocalDate, PassSlip> loadPassSlips(long employeeId, LocalDate fromDate, LocalDate toDate) {
        List<PassSlip> rows = jdbc.query(
                """
                SELECT passSlipId, passSlipDate, departureTime, arrivalTime
                FROM pass_slip
                WHERE employeeId = ?
                  AND status = 'Approved'
                  AND passSlipDate BETWEEN ? AND ?
                ORDER BY passSlipDate, passSlipId DESC
                """,
                (rs, rowNum) -> new PassSlip(
                        rs.getLong("passSlipId"),
                        localDate(rs, "passSlipDate"),
                        localTime(rs, "departureTime"),
                        localTime(rs, "arrivalTime")
                ),
                employeeId,
                java.sql.Date.valueOf(fromDate),
                java.sql.Date.valueOf(toDate)
        );
        return latestByDate(rows, PassSlip::date);
    }

    private Map<LocalDate, TimeCorrection> loadTimeCorrections(
            long employeeId,
            LocalDate fromDate,
            LocalDate toDate) {
        List<TimeCorrection> rows = jdbc.query(
                """
                SELECT
                    timeCorrectionId,
                    workDate,
                    correctedTimeIn,
                    correctedBreakOut,
                    correctedBreakIn,
                    correctedTimeOut
                FROM time_correction
                WHERE employeeId = ?
                  AND status = 'Approved'
                  AND workDate BETWEEN ? AND ?
                ORDER BY workDate, timeCorrectionId DESC
                """,
                (rs, rowNum) -> new TimeCorrection(
                        rs.getLong("timeCorrectionId"),
                        localDate(rs, "workDate"),
                        localTime(rs, "correctedTimeIn"),
                        localTime(rs, "correctedBreakOut"),
                        localTime(rs, "correctedBreakIn"),
                        localTime(rs, "correctedTimeOut")
                ),
                employeeId,
                java.sql.Date.valueOf(fromDate),
                java.sql.Date.valueOf(toDate)
        );
        return latestByDate(rows, TimeCorrection::date);
    }

    private Map<LocalDate, Leave> loadLeaves(long employeeId, LocalDate fromDate, LocalDate toDate) {
        List<LeaveRange> ranges = jdbc.query(
                """
                SELECT leaveApplicationId, leaveType, startDate, endDate
                FROM leave_application
                WHERE employeeId = ?
                  AND approvedStatus = 'Approved'
                  AND startDate <= ?
                  AND endDate >= ?
                ORDER BY leaveApplicationId DESC
                """,
                (rs, rowNum) -> new LeaveRange(
                        rs.getLong("leaveApplicationId"),
                        rs.getString("leaveType"),
                        localDate(rs, "startDate"),
                        localDate(rs, "endDate")
                ),
                employeeId,
                java.sql.Date.valueOf(toDate),
                java.sql.Date.valueOf(fromDate)
        );
        Map<LocalDate, Leave> result = new LinkedHashMap<>();
        for (LeaveRange range : ranges) {
            LocalDate start = max(range.startDate(), fromDate);
            LocalDate end = min(range.endDate(), toDate);
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                result.putIfAbsent(date, new Leave(range.id(), range.leaveType()));
            }
        }
        return result;
    }

    private List<DtrReportRow> assemble(
            EmployeeInfo employee,
            LocalDate fromDate,
            LocalDate toDate,
            List<Daily> dailies,
            List<Segment> segments,
            List<WorkScheduleDay> schedules,
            Map<LocalDate, OfficialEngagement> officialEngagements,
            Set<LocalDate> overtimeDates,
            Set<LocalDate> ctoDates,
            Map<LocalDate, PassSlip> passSlips,
            Map<LocalDate, TimeCorrection> timeCorrections,
            Map<LocalDate, Leave> leaves) {

        Map<LocalDate, List<Daily>> dailiesByDate = new LinkedHashMap<>();
        for (Daily daily : dailies) {
            dailiesByDate.computeIfAbsent(daily.date(), ignored -> new ArrayList<>()).add(daily);
        }

        Map<LocalDate, WorkScheduleDay> scheduleByDate = new LinkedHashMap<>();
        for (WorkScheduleDay schedule : schedules) {
            scheduleByDate.merge(
                    schedule.date(),
                    schedule,
                    (left, right) -> left.id() <= right.id() ? left : right
            );
        }

        List<SegmentPart> segmentParts = expandSegments(segments);
        Map<Long, List<SegmentPart>> segmentPartsByDaily = new LinkedHashMap<>();
        for (SegmentPart part : segmentParts) {
            segmentPartsByDaily.computeIfAbsent(part.dailyId(), ignored -> new ArrayList<>()).add(part);
        }

        List<SortableRow> reportRows = new ArrayList<>();
        Set<Long> emittedOvernightEndSegmentIds = new LinkedHashSet<>();
        long sequence = 0L;
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            LocalDate currentDate = date;
            List<Daily> dateDailies = dailiesByDate.get(currentDate);
            if (dateDailies == null || dateDailies.isEmpty()) {
                dateDailies = Collections.singletonList(null);
            }

            for (Daily daily : dateDailies) {
                BaseDay base = baseDay(
                        employee,
                        currentDate,
                        daily,
                        scheduleByDate.get(currentDate),
                        officialEngagements.get(currentDate),
                        overtimeDates.contains(currentDate),
                        ctoDates.contains(currentDate),
                        passSlips.get(currentDate),
                        timeCorrections.get(currentDate),
                        leaves.get(currentDate)
                );

                List<SegmentPart> sameDateParts = daily == null
                        ? List.of()
                        : segmentPartsByDaily.getOrDefault(daily.id(), List.of()).stream()
                                .filter(part -> part.reportDate().equals(currentDate))
                                .toList();

                boolean priorOvernightEndsHere = daily == null && segmentParts.stream().anyMatch(
                        part -> part.splitOrder() == 1 && part.reportDate().equals(currentDate)
                );
                if (!priorOvernightEndsHere
                        && !(base.passSlip() != null && sameDateParts.isEmpty())
                        && base.timeCorrection() == null) {
                    if (sameDateParts.isEmpty()) {
                        reportRows.add(new SortableRow(
                                baseReportRow(base, null),
                                currentDate,
                                0,
                                0,
                                sequence++
                        ));
                    } else {
                        for (SegmentPart part : sameDateParts) {
                            reportRows.add(new SortableRow(
                                    baseReportRow(base, part),
                                    currentDate,
                                    part.segmentNo(),
                                    part.splitOrder(),
                                    sequence++
                            ));
                        }
                    }
                }

                if (daily != null) {
                    for (SegmentPart part : segmentPartsByDaily.getOrDefault(daily.id(), List.of())) {
                        if (part.reportDate().equals(currentDate)
                                || part.reportDate().isBefore(fromDate)
                                || part.reportDate().isAfter(toDate)
                                || timeCorrections.containsKey(part.reportDate())) {
                            continue;
                        }
                        reportRows.add(new SortableRow(
                                overnightEndReportRow(employee.fullName(), part),
                                part.reportDate(),
                                part.segmentNo(),
                                part.splitOrder(),
                                sequence++
                        ));
                        emittedOvernightEndSegmentIds.add(part.id());
                    }
                }

                if (base.timeCorrection() != null) {
                    reportRows.add(new SortableRow(
                            timeCorrectionReportRow(base),
                            currentDate,
                            0,
                            0,
                            sequence++
                    ));
                }

                if (base.passSlip() != null) {
                    reportRows.add(new SortableRow(
                            passSlipReportRow(base),
                            currentDate,
                            9999,
                            0,
                            sequence++
                    ));
                }
            }
        }

        // Segments are loaded from one day before the requested range. Emit the
        // following-morning departure even when its night-time arrival belongs
        // to the prior report period.
        for (SegmentPart part : segmentParts) {
            if (part.splitOrder() != 1
                    || part.reportDate().isBefore(fromDate)
                    || part.reportDate().isAfter(toDate)
                    || timeCorrections.containsKey(part.reportDate())
                    || !emittedOvernightEndSegmentIds.add(part.id())) {
                continue;
            }
            reportRows.add(new SortableRow(
                    overnightEndReportRow(employee.fullName(), part),
                    part.reportDate(),
                    part.segmentNo(),
                    part.splitOrder(),
                    sequence++
            ));
        }

        reportRows.sort(Comparator
                .comparing(SortableRow::date)
                .thenComparingInt(SortableRow::segmentNo)
                .thenComparingInt(SortableRow::splitOrder)
                .thenComparingLong(SortableRow::sequence));
        return reportRows.stream().map(SortableRow::row).toList();
    }

    private BaseDay baseDay(
            EmployeeInfo employee,
            LocalDate date,
            Daily daily,
            WorkScheduleDay schedule,
            OfficialEngagement officialEngagement,
            boolean overtime,
            boolean cto,
            PassSlip passSlip,
            TimeCorrection timeCorrection,
            Leave leave) {
        int workMinutes = daily == null ? 0 : daily.workMinutes();
        int lateMinutes = daily == null ? 0 : daily.lateMinutes();
        int undertimeMinutes = daily == null ? 0 : daily.undertimeMinutes();
        int overtimeMinutes = daily == null ? 0 : daily.overtimeMinutes();
        int totalUnder = lateMinutes + undertimeMinutes;
        boolean dayOff = schedule != null && schedule.dayOff();

        int absentMinutes;
        if (daily != null && "ABSENT".equalsIgnoreCase(nullToEmpty(daily.status()))) {
            absentMinutes = 480;
        } else if (daily == null
                && !dayOff
                && leave == null
                && officialEngagement == null
                && !cto
                && timeCorrection == null
                && passSlip == null) {
            absentMinutes = 480;
        } else {
            absentMinutes = 0;
        }

        String officialCode = officialCode(officialEngagement);
        String remarks;
        if (daily == null && dayOff) {
            remarks = "REST DAY";
        } else if (daily == null && leave != null) {
            remarks = nullToEmpty(leave.leaveType());
        } else if (daily == null && officialEngagement != null) {
            remarks = officialCode;
        } else if (daily == null && cto) {
            remarks = "CTO";
        } else if (daily == null && timeCorrection != null) {
            remarks = "TIME CORRECTED";
        } else if (daily == null && passSlip != null) {
            remarks = "PASS SLIP";
        } else if (daily == null) {
            remarks = "ABSENT";
        } else {
            remarks = nullToEmpty(daily.status());
        }

        return new BaseDay(
                date,
                employee.fullName(),
                daily,
                workMinutes,
                overtimeMinutes / 60.0d,
                lateMinutes,
                totalUnder % 60,
                totalUnder / 60,
                absentMinutes,
                officialCode,
                overtime ? "OT" : "",
                officialEngagement == null ? null : date,
                overtime ? date : null,
                dayOff ? "REST DAY" : "",
                remarks,
                leave == null ? "" : nullToEmpty(leave.leaveType()),
                cto ? "CTO" : "",
                passSlip,
                timeCorrection
        );
    }

    private List<SegmentPart> expandSegments(List<Segment> segments) {
        List<SegmentPart> result = new ArrayList<>();
        for (Segment segment : segments) {
            int totalUnder = segment.lateMinutes() + segment.undertimeMinutes();
            if (isOvernight(segment)) {
                result.add(new SegmentPart(
                        segment.id(),
                        segment.dailyId(),
                        segment.workDate(),
                        segment.segmentNo(),
                        0,
                        null,
                        null,
                        segment.timeIn(),
                        null,
                        totalUnder % 60,
                        totalUnder / 60
                ));
                result.add(new SegmentPart(
                        segment.id(),
                        segment.dailyId(),
                        segment.workDate().plusDays(1),
                        segment.segmentNo(),
                        1,
                        null,
                        segment.timeOut(),
                        null,
                        null,
                        0,
                        0
                ));
                continue;
            }

            LocalTime in1st;
            LocalTime out1st;
            LocalTime in2nd;
            LocalTime out2nd;
            if (segment.breakIn() != null) {
                in1st = segment.timeIn();
                out1st = segment.breakOut();
                in2nd = segment.breakIn();
                out2nd = segment.timeOut();
            } else {
                in1st = isBeforeNoon(segment.timeIn()) ? segment.timeIn() : null;
                out1st = isAtOrBeforeNoon(segment.timeOut()) ? segment.timeOut() : null;
                in2nd = isAtOrAfterNoon(segment.timeIn()) ? segment.timeIn() : null;
                out2nd = isAtOrAfterNoon(segment.timeIn())
                        || (isBeforeNoon(segment.timeIn()) && isAfterNoon(segment.timeOut()))
                        ? segment.timeOut()
                        : null;
            }
            result.add(new SegmentPart(
                    segment.id(),
                    segment.dailyId(),
                    segment.workDate(),
                    segment.segmentNo(),
                    0,
                    in1st,
                    out1st,
                    in2nd,
                    out2nd,
                    totalUnder % 60,
                    totalUnder / 60
            ));
        }
        return result;
    }

    private DtrReportRow baseReportRow(BaseDay base, SegmentPart part) {
        return new DtrReportRow(
                java.sql.Date.valueOf(base.date()),
                base.fullName(),
                sqlTime(part == null ? null : part.in1st()),
                sqlTime(part == null ? null : part.out1st()),
                sqlTime(part == null ? null : part.in2nd()),
                sqlTime(part == null ? null : part.out2nd()),
                base.regularMinutes(),
                0,
                0.0d,
                base.overtimeHours(),
                0.0d,
                base.overtimeHours(),
                0.0d,
                base.lateMinutes(),
                part == null ? base.underMinutes() : part.underMinutes(),
                base.absentMinutes(),
                base.ob(),
                base.ot(),
                sqlDate(base.obDate()),
                sqlDate(base.otDate()),
                base.dayOff(),
                part == null ? base.underHours() : part.underHours(),
                base.remarks(),
                base.leaveType(),
                null,
                false,
                base.cto(),
                dayName(base.date())
        );
    }

    private DtrReportRow overnightEndReportRow(String fullName, SegmentPart part) {
        return new DtrReportRow(
                java.sql.Date.valueOf(part.reportDate()),
                fullName,
                sqlTime(part.in1st()),
                sqlTime(part.out1st()),
                sqlTime(part.in2nd()),
                sqlTime(part.out2nd()),
                0,
                0,
                0.0d,
                0.0d,
                0.0d,
                0.0d,
                0.0d,
                0,
                part.underMinutes(),
                0,
                "",
                "",
                null,
                null,
                "",
                part.underHours(),
                "Present",
                "",
                null,
                true,
                "",
                dayName(part.reportDate())
        );
    }

    private DtrReportRow timeCorrectionReportRow(BaseDay base) {
        TimeCorrection correction = base.timeCorrection();
        return new DtrReportRow(
                java.sql.Date.valueOf(base.date()),
                base.fullName(),
                sqlTime(correction.timeIn()),
                sqlTime(correction.breakOut()),
                sqlTime(correction.breakIn()),
                sqlTime(correction.timeOut()),
                base.regularMinutes(),
                0,
                0.0d,
                base.overtimeHours(),
                0.0d,
                base.overtimeHours(),
                0.0d,
                base.lateMinutes(),
                base.underMinutes(),
                0,
                "",
                "",
                null,
                null,
                "",
                base.underHours(),
                "TIME CORRECTED",
                "",
                null,
                false,
                "",
                dayName(base.date())
        );
    }

    private DtrReportRow passSlipReportRow(BaseDay base) {
        PassSlip passSlip = base.passSlip();
        LocalTime arrival = passSlip.arrival();
        LocalTime departure = passSlip.departure();
        return new DtrReportRow(
                java.sql.Date.valueOf(base.date()),
                base.fullName(),
                sqlTime(isBeforeNoon(arrival) ? arrival : null),
                sqlTime(isBeforeNoon(departure) ? departure : null),
                sqlTime(isAtOrAfterNoon(arrival) ? arrival : null),
                sqlTime(isAtOrAfterNoon(departure) ? departure : null),
                0,
                0,
                0.0d,
                0.0d,
                0.0d,
                0.0d,
                0.0d,
                0,
                0,
                0,
                "",
                "",
                null,
                null,
                "",
                0,
                "PASS SLIP",
                "",
                null,
                false,
                "",
                dayName(base.date())
        );
    }

    private static <T> Map<LocalDate, T> latestByDate(
            Collection<T> rows,
            java.util.function.Function<T, LocalDate> dateExtractor) {
        Map<LocalDate, T> result = new LinkedHashMap<>();
        for (T row : rows) {
            result.putIfAbsent(dateExtractor.apply(row), row);
        }
        return result;
    }

    private static String placeholders(int size) {
        return String.join(", ", Collections.nCopies(size, "?"));
    }

    private static LocalDate localDate(ResultSet rs, String column) throws SQLException {
        java.sql.Date value = rs.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    private static LocalTime localTime(ResultSet rs, String column) throws SQLException {
        Time value = rs.getTime(column);
        return value == null ? null : value.toLocalTime();
    }

    private static Time sqlTime(LocalTime value) {
        return value == null ? null : Time.valueOf(value);
    }

    private static java.sql.Date sqlDate(LocalDate value) {
        return value == null ? null : java.sql.Date.valueOf(value);
    }

    private static String dayName(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private static String officialCode(OfficialEngagement engagement) {
        if (engagement == null) {
            return "";
        }
        return "OFFICIAL TIME".equalsIgnoreCase(nullToEmpty(engagement.officialType()).trim())
                ? "OT"
                : "OB";
    }

    private static boolean isOvernight(Segment segment) {
        return segment.timeIn() != null
                && segment.timeOut() != null
                && segment.timeOut().isBefore(segment.timeIn());
    }

    private static boolean isBeforeNoon(LocalTime value) {
        return value != null && value.isBefore(NOON);
    }

    private static boolean isAfterNoon(LocalTime value) {
        return value != null && value.isAfter(NOON);
    }

    private static boolean isAtOrBeforeNoon(LocalTime value) {
        return value != null && !value.isAfter(NOON);
    }

    private static boolean isAtOrAfterNoon(LocalTime value) {
        return value != null && !value.isBefore(NOON);
    }

    private static int intValue(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private static boolean dbTrue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        if (value == null) {
            return false;
        }
        String text = value.toString().trim();
        return "true".equalsIgnoreCase(text)
                || "t".equalsIgnoreCase(text)
                || "yes".equalsIgnoreCase(text)
                || "y".equalsIgnoreCase(text)
                || "1".equals(text);
    }

    private static Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static LocalDate max(LocalDate left, LocalDate right) {
        return left.isAfter(right) ? left : right;
    }

    private static LocalDate min(LocalDate left, LocalDate right) {
        return left.isBefore(right) ? left : right;
    }

    private static String firstNonNull(String preferred, String fallback) {
        return preferred != null ? preferred : fallback;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record EmployeeInfo(long employeeId, String employeeNo, String fullName) {}

    private record Daily(
            long id,
            LocalDate date,
            int workMinutes,
            int lateMinutes,
            int undertimeMinutes,
            int overtimeMinutes,
            String status) {}

    private record Segment(
            long id,
            long dailyId,
            LocalDate workDate,
            int segmentNo,
            LocalTime timeIn,
            LocalTime breakOut,
            LocalTime breakIn,
            LocalTime timeOut,
            int lateMinutes,
            int undertimeMinutes) {}

    private record SegmentPart(
            long id,
            long dailyId,
            LocalDate reportDate,
            int segmentNo,
            int splitOrder,
            LocalTime in1st,
            LocalTime out1st,
            LocalTime in2nd,
            LocalTime out2nd,
            int underMinutes,
            int underHours) {}

    private record WorkScheduleDay(long id, LocalDate date, boolean dayOff) {}

    private record WorkScheduleSource(
            long id,
            String employeeReference,
            LocalDate date,
            boolean dayOff) {}

    private record OfficialEngagement(long id, String officialType) {}

    private record OfficialEngagementRange(
            long id,
            String officialType,
            LocalDate startDate,
            LocalDate endDate) {}

    private record PassSlip(
            long id,
            LocalDate date,
            LocalTime departure,
            LocalTime arrival) {}

    private record TimeCorrection(
            long id,
            LocalDate date,
            LocalTime timeIn,
            LocalTime breakOut,
            LocalTime breakIn,
            LocalTime timeOut) {}

    private record Leave(long id, String leaveType) {}

    private record LeaveRange(
            long id,
            String leaveType,
            LocalDate startDate,
            LocalDate endDate) {}

    private record BaseDay(
            LocalDate date,
            String fullName,
            Daily daily,
            int regularMinutes,
            double overtimeHours,
            int lateMinutes,
            int underMinutes,
            int underHours,
            int absentMinutes,
            String ob,
            String ot,
            LocalDate obDate,
            LocalDate otDate,
            String dayOff,
            String remarks,
            String leaveType,
            String cto,
            PassSlip passSlip,
            TimeCorrection timeCorrection) {}

    private record SortableRow(
            DtrReportRow row,
            LocalDate date,
            int segmentNo,
            int splitOrder,
            long sequence) {}
}
