package com.timekeeping.reports;

import com.timekeeping.dtos.WorkScheduleReportRow;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Builds Work Schedule report rows in Java so the JRXML does not depend on
 * SQL Server date generators, FORMAT, TRY_CAST, or OUTER APPLY.
 */
public final class WorkScheduleReportDataLoader {
    private static final int APPOINTMENT_QUERY_BATCH_SIZE = 500;
    private static final DateTimeFormatter REPORT_TIME =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final JdbcTemplate jdbc;

    public WorkScheduleReportDataLoader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<WorkScheduleReportRow> load(
            Long areaId,
            Long businessUnitId,
            LocalDate fromDate,
            LocalDate toDate) {
        if (areaId == null) {
            return List.of();
        }
        if (fromDate == null || toDate == null || toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("A valid Work Schedule report date range is required.");
        }

        List<Personnel> personnel = loadPersonnel(areaId, businessUnitId);
        if (personnel.isEmpty()) {
            return List.of();
        }

        Set<Long> employeeIds = new LinkedHashSet<>();
        for (Personnel person : personnel) {
            employeeIds.add(person.employeeId());
        }

        Map<Long, Map<LocalDate, List<Schedule>>> schedules =
                loadSchedules(employeeIds, fromDate, toDate);
        Map<Long, List<Appointment>> appointments = loadAppointments(employeeIds);
        Map<String, TimeShift> shifts = loadTimeShifts();

        List<SortableScheduleRow> result = new ArrayList<>();
        long sequence = 0L;
        for (Personnel person : personnel) {
            for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
                Appointment appointment = appointmentForDate(
                        appointments.getOrDefault(person.employeeId(), List.of()),
                        date
                );
                String position = appointment != null && !isBlank(appointment.jobPosition())
                        ? appointment.jobPosition()
                        : nullToEmpty(person.position());
                Integer salaryGrade = appointment == null ? null : appointment.salaryGrade();

                List<Schedule> dateSchedules = schedules
                        .getOrDefault(person.employeeId(), Map.of())
                        .getOrDefault(date, List.of());
                if (dateSchedules.isEmpty()) {
                    result.add(new SortableScheduleRow(
                            reportRow(person, date, "", position, salaryGrade),
                            person.businessUnitName(),
                            person.fullName(),
                            date,
                            null,
                            null,
                            sequence++
                    ));
                    continue;
                }

                for (Schedule schedule : dateSchedules) {
                    String scheduleText = scheduleText(schedule, shifts);
                    result.add(new SortableScheduleRow(
                            reportRow(person, date, scheduleText, position, salaryGrade),
                            person.businessUnitName(),
                            person.fullName(),
                            date,
                            schedule.dateTime(),
                            schedule.id(),
                            sequence++
                    ));
                }
            }
        }

        Comparator<String> textComparator = Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER);
        result.sort(Comparator
                .comparing(SortableScheduleRow::businessUnitName, textComparator)
                .thenComparing(SortableScheduleRow::fullName, textComparator)
                .thenComparing(SortableScheduleRow::date)
                .thenComparing(
                        SortableScheduleRow::scheduleDateTime,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                )
                .thenComparing(
                        SortableScheduleRow::scheduleId,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                )
                .thenComparingLong(SortableScheduleRow::sequence));
        return result.stream().map(SortableScheduleRow::row).toList();
    }

    private List<Personnel> loadPersonnel(Long areaId, Long businessUnitId) {
        String sql = """
                SELECT DISTINCT
                    mp.employeeId,
                    mp.areaId,
                    mp.businessUnitId,
                    a.areasName,
                    bu.businessUnitsName,
                    e.employeeNo,
                    e.position,
                    e.lastname,
                    e.suffix,
                    e.firstname
                FROM manage_personnel mp
                INNER JOIN areas a ON a.areasId = mp.areaId
                INNER JOIN businessunits bu ON bu.businessUnitsId = mp.businessUnitId
                INNER JOIN employee e ON e.employeeId = mp.employeeId
                WHERE mp.areaId = ?
                """;
        List<Object> args = new ArrayList<>();
        args.add(areaId);
        if (businessUnitId != null) {
            sql += " AND mp.businessUnitId = ?";
            args.add(businessUnitId);
        }
        sql += " ORDER BY bu.businessUnitsName, e.lastname, e.firstname, mp.employeeId";

        return jdbc.query(
                sql,
                (rs, rowNum) -> new Personnel(
                        rs.getLong("employeeId"),
                        rs.getLong("areaId"),
                        rs.getLong("businessUnitId"),
                        rs.getString("areasName"),
                        rs.getString("businessUnitsName"),
                        rs.getString("employeeNo"),
                        rs.getString("position"),
                        fullName(
                                rs.getString("lastname"),
                                rs.getString("suffix"),
                                rs.getString("firstname")
                        )
                ),
                args.toArray()
        );
    }

    private Map<Long, Map<LocalDate, List<Schedule>>> loadSchedules(
            Set<Long> employeeIds,
            LocalDate fromDate,
            LocalDate toDate) {
        List<ScheduleSource> sourceRows = jdbc.query(
                """
                SELECT wsId, employeeId, tsCode, isDayOff, wsDateTime
                FROM work_schedule
                WHERE wsDateTime >= ?
                  AND wsDateTime < ?
                ORDER BY wsDateTime, wsId
                """,
                (rs, rowNum) -> new ScheduleSource(
                        rs.getLong("wsId"),
                        rs.getString("employeeId"),
                        rs.getString("tsCode"),
                        dbTrue(rs.getObject("isDayOff")),
                        rs.getTimestamp("wsDateTime").toLocalDateTime()
                ),
                Timestamp.valueOf(fromDate.atStartOfDay()),
                Timestamp.valueOf(toDate.plusDays(1).atStartOfDay())
        );

        Map<Long, Map<LocalDate, List<Schedule>>> result = new LinkedHashMap<>();
        for (ScheduleSource source : sourceRows) {
            Long employeeId = parseLong(source.employeeReference());
            if (employeeId == null || !employeeIds.contains(employeeId)) {
                continue;
            }
            Schedule schedule = new Schedule(
                    source.id(),
                    source.tsCode(),
                    source.dayOff(),
                    source.dateTime()
            );
            result.computeIfAbsent(employeeId, ignored -> new LinkedHashMap<>())
                    .computeIfAbsent(source.dateTime().toLocalDate(), ignored -> new ArrayList<>())
                    .add(schedule);
        }
        return result;
    }

    private Map<Long, List<Appointment>> loadAppointments(Set<Long> employeeIds) {
        List<Long> ids = new ArrayList<>(employeeIds);
        List<Appointment> rows = new ArrayList<>();
        for (int start = 0; start < ids.size(); start += APPOINTMENT_QUERY_BATCH_SIZE) {
            List<Long> batch = ids.subList(
                    start,
                    Math.min(start + APPOINTMENT_QUERY_BATCH_SIZE, ids.size())
            );
            String placeholders = String.join(", ", Collections.nCopies(batch.size(), "?"));
            rows.addAll(jdbc.query(
                    """
                    SELECT
                        ea.employeeAppointmentId,
                        ea.employeeId,
                        ea.assumptionToDutyDate,
                        ea.activeAppointment,
                        ea.salaryGrade,
                        jp.jobPositionName
                    FROM employeeAppointment ea
                    LEFT JOIN job_position jp ON jp.jobPositionId = ea.jobPositionId
                    WHERE ea.employeeId IN (%s)
                    ORDER BY ea.employeeId, ea.employeeAppointmentId
                    """.formatted(placeholders),
                    (rs, rowNum) -> new Appointment(
                            rs.getLong("employeeAppointmentId"),
                            rs.getLong("employeeId"),
                            localDateTime(rs, "assumptionToDutyDate"),
                            dbTrue(rs.getObject("activeAppointment")),
                            integerValue(rs.getObject("salaryGrade")),
                            rs.getString("jobPositionName")
                    ),
                    batch.toArray()
            ));
        }

        Map<Long, List<Appointment>> result = new LinkedHashMap<>();
        for (Appointment row : rows) {
            result.computeIfAbsent(row.employeeId(), ignored -> new ArrayList<>()).add(row);
        }
        return result;
    }

    private Map<String, TimeShift> loadTimeShifts() {
        List<TimeShift> rows = jdbc.query(
                """
                SELECT tsCode, timeIn, breakOut, breakIn, timeOut
                FROM time_shift
                ORDER BY tsCode
                """,
                (rs, rowNum) -> new TimeShift(
                        rs.getString("tsCode"),
                        localTime(rs, "timeIn"),
                        localTime(rs, "breakOut"),
                        localTime(rs, "breakIn"),
                        localTime(rs, "timeOut")
                )
        );
        Map<String, TimeShift> result = new LinkedHashMap<>();
        for (TimeShift row : rows) {
            result.putIfAbsent(normalizedCode(row.code()), row);
        }
        return result;
    }

    private Appointment appointmentForDate(List<Appointment> appointments, LocalDate date) {
        return appointments.stream()
                .filter(appointment -> appointment.assumptionToDuty() == null
                        || !appointment.assumptionToDuty().toLocalDate().isAfter(date))
                .min(Comparator
                        .comparing((Appointment appointment) -> !appointment.active())
                        .thenComparing(
                                Appointment::assumptionToDuty,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                        .thenComparing(Appointment::id, Comparator.reverseOrder()))
                .orElse(null);
    }

    private String scheduleText(Schedule schedule, Map<String, TimeShift> shifts) {
        if (schedule.dayOff()) {
            return "DAY OFF / REST DAY";
        }
        TimeShift shift = shifts.get(normalizedCode(schedule.tsCode()));
        if (shift == null) {
            return nullToEmpty(schedule.tsCode());
        }

        String breakText = shift.breakOut() == null || shift.breakIn() == null
                ? " - "
                : " / " + formatTime(shift.breakOut())
                        + " - " + formatTime(shift.breakIn()) + " / ";
        return nullToEmpty(shift.code())
                + " - "
                + formatTime(shift.timeIn())
                + breakText
                + formatTime(shift.timeOut());
    }

    private WorkScheduleReportRow reportRow(
            Personnel person,
            LocalDate date,
            String schedule,
            String position,
            Integer salaryGrade) {
        return new WorkScheduleReportRow(
                nullToEmpty(person.areaName()),
                nullToEmpty(person.fullName()),
                java.sql.Date.valueOf(date),
                schedule,
                "",
                nullToEmpty(position),
                salaryGrade
        );
    }

    private static String fullName(String lastName, String suffix, String firstName) {
        String suffixPart = isBlank(suffix) ? "" : " " + suffix;
        String firstNamePart = isBlank(firstName) ? "" : ", " + firstName;
        return (nullToEmpty(lastName) + suffixPart + firstNamePart).trim();
    }

    private static String formatTime(LocalTime time) {
        return time == null ? "" : REPORT_TIME.format(time);
    }

    private static String normalizedCode(String code) {
        return nullToEmpty(code).trim().toUpperCase(Locale.ROOT);
    }

    private static LocalTime localTime(ResultSet rs, String column) throws SQLException {
        java.sql.Time value = rs.getTime(column);
        return value == null ? null : value.toLocalTime();
    }

    private static LocalDateTime localDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    private static Integer integerValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null || value.toString().trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.toString().trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
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
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record Personnel(
            long employeeId,
            long areaId,
            long businessUnitId,
            String areaName,
            String businessUnitName,
            String employeeNo,
            String position,
            String fullName) {}

    private record ScheduleSource(
            long id,
            String employeeReference,
            String tsCode,
            boolean dayOff,
            LocalDateTime dateTime) {}

    private record Schedule(
            long id,
            String tsCode,
            boolean dayOff,
            LocalDateTime dateTime) {}

    private record Appointment(
            Long id,
            long employeeId,
            LocalDateTime assumptionToDuty,
            boolean active,
            Integer salaryGrade,
            String jobPosition) {}

    private record TimeShift(
            String code,
            LocalTime timeIn,
            LocalTime breakOut,
            LocalTime breakIn,
            LocalTime timeOut) {}

    private record SortableScheduleRow(
            WorkScheduleReportRow row,
            String businessUnitName,
            String fullName,
            LocalDate date,
            LocalDateTime scheduleDateTime,
            Long scheduleId,
            long sequence) {}
}
