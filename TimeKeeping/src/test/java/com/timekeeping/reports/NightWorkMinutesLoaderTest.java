package com.timekeeping.reports;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class NightWorkMinutesLoaderTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 1);

    @Test
    void countsOnlyObservedMinutesInsideGovernmentNightWindow() {
        assertThat(minutes("17:00", "20:00", null, null)).isEqualTo(120);
        assertThat(minutes("05:00", "09:00", null, null)).isEqualTo(60);
        assertThat(minutes("08:00", "17:00", null, null)).isZero();
    }

    @Test
    void handlesOvernightWorkAndExcludesRecordedBreaks() {
        assertThat(minutes("22:00", "06:00", "00:00", "01:00")).isEqualTo(420);
        assertThat(minutes("18:00", "06:00", "23:30", "00:30")).isEqualTo(660);
    }

    @Test
    void excludesIncompletePunchesAndBreaksRatherThanOverstatingNightWork() {
        assertThat(minutes("22:00", null, null, null)).isZero();
        assertThat(minutes("22:00", "06:00", "00:00", null)).isZero();
    }

    @Test
    void readsOnlyRequestedEmployeesWorkDatesFromSegments() {
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:night_work_test;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE dtr_daily (dtr_daily_id BIGINT PRIMARY KEY, employee_id VARCHAR(100), work_date DATE)");
        jdbc.execute("CREATE TABLE dtr_segment (dtr_segment_id BIGINT PRIMARY KEY, dtr_daily_id BIGINT, "
                + "segment_no INT, time_in TIME, time_out TIME, break_out TIME, break_in TIME)");
        jdbc.update("INSERT INTO dtr_daily VALUES (?, ?, ?)", 1L, "0001", java.sql.Date.valueOf(DATE));
        jdbc.update("INSERT INTO dtr_daily VALUES (?, ?, ?)", 2L, "0002", java.sql.Date.valueOf(DATE));
        jdbc.update("INSERT INTO dtr_segment VALUES (?, ?, ?, ?, ?, ?, ?)", 1L, 1L, 1,
                java.sql.Time.valueOf("22:00:00"), java.sql.Time.valueOf("06:00:00"), null, null);
        jdbc.update("INSERT INTO dtr_segment VALUES (?, ?, ?, ?, ?, ?, ?)", 2L, 2L, 1,
                java.sql.Time.valueOf("22:00:00"), java.sql.Time.valueOf("06:00:00"), null, null);
        assertThat(new NightWorkMinutesLoader(jdbc).load(1L, "0001", DATE, DATE))
                .containsEntry(DATE, 480).hasSize(1);
    }

    private static int minutes(String in, String out, String breakOut, String breakIn) {
        return NightWorkMinutesLoader.calculate(DATE, time(in), time(out),
                time(breakOut), time(breakIn));
    }

    private static LocalTime time(String value) {
        return value == null ? null : LocalTime.parse(value);
    }
}
