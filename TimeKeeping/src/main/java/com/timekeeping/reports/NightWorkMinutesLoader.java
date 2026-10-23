package com.timekeeping.reports;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/** Observed DTR work within 18:00-06:00, not a determination of payable NSDP. */
final class NightWorkMinutesLoader {
    private final JdbcTemplate jdbc;

    NightWorkMinutesLoader(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    Map<LocalDate, Integer> load(long employeeId, String employeeNo, LocalDate from, LocalDate to) {
        Map<LocalDate, Integer> minutesByWorkDate = new HashMap<>();
        jdbc.query("SELECT d.work_date, s.time_in, s.time_out, s.break_out, s.break_in "
                + "FROM dtr_segment s JOIN dtr_daily d ON d.dtr_daily_id = s.dtr_daily_id "
                + "WHERE (d.employee_id = ? OR d.employee_id = ?) AND d.work_date BETWEEN ? AND ? "
                + "ORDER BY d.work_date, s.segment_no, s.dtr_segment_id",
                rs -> {
                    LocalDate date = rs.getDate("work_date").toLocalDate();
                    int minutes = calculate(date, time(rs, "time_in"), time(rs, "time_out"),
                            time(rs, "break_out"), time(rs, "break_in"));
                    minutesByWorkDate.merge(date, minutes, Integer::sum);
                }, String.valueOf(employeeId), employeeNo,
                java.sql.Date.valueOf(from), java.sql.Date.valueOf(to));
        return minutesByWorkDate;
    }

    static int calculate(LocalDate workDate, LocalTime timeIn, LocalTime timeOut,
                         LocalTime breakOut, LocalTime breakIn) {
        if (timeIn == null || timeOut == null || (breakOut == null) != (breakIn == null)) return 0;
        LocalDateTime start = workDate.atTime(timeIn);
        LocalDateTime end = workDate.atTime(timeOut);
        if (timeOut.isBefore(timeIn)) end = end.plusDays(1);
        if (!end.isAfter(start)) return 0;

        LocalDateTime pauseStart = null;
        LocalDateTime pauseEnd = null;
        if (breakOut != null) {
            pauseStart = workDate.atTime(breakOut);
            if (breakOut.isBefore(timeIn)) pauseStart = pauseStart.plusDays(1);
            pauseEnd = workDate.atTime(breakIn);
            if (pauseEnd.isBefore(pauseStart)) pauseEnd = pauseEnd.plusDays(1);
            if (pauseStart.isBefore(start) || pauseEnd.isAfter(end) || !pauseEnd.isAfter(pauseStart)) return 0;
        }

        int minutes = 0;
        for (int offset = -1; offset <= 1; offset++) {
            LocalDateTime nightStart = workDate.plusDays(offset).atTime(18, 0);
            LocalDateTime nightEnd = nightStart.plusHours(12);
            minutes += overlap(start, end, nightStart, nightEnd);
            if (pauseStart != null) minutes -= overlap(pauseStart, pauseEnd, nightStart, nightEnd);
        }
        return Math.max(0, minutes);
    }

    private static int overlap(LocalDateTime start, LocalDateTime end,
                               LocalDateTime windowStart, LocalDateTime windowEnd) {
        LocalDateTime overlapStart = start.isAfter(windowStart) ? start : windowStart;
        LocalDateTime overlapEnd = end.isBefore(windowEnd) ? end : windowEnd;
        return overlapEnd.isAfter(overlapStart)
                ? Math.toIntExact(Duration.between(overlapStart, overlapEnd).toMinutes()) : 0;
    }

    private static LocalTime time(ResultSet rs, String column) throws SQLException {
        java.sql.Time value = rs.getTime(column);
        return value == null ? null : value.toLocalTime();
    }
}
