package com.timekeeping.reports;

import com.timekeeping.services.WorkScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MraReportServiceTest {
    @Test
    void buildsPdfFromCurrentDtrAndClearsRequestSnapshot() throws Exception {
        DriverManagerDataSource source = new DriverManagerDataSource(
                "jdbc:h2:mem:mra_service;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(source);
        ReportDataLoaderTest.createDtrSchema(jdbc);
        jdbc.execute("CREATE TABLE areas (areasId BIGINT PRIMARY KEY, areasName VARCHAR(100))");
        jdbc.execute("CREATE TABLE businessunits (businessUnitsId BIGINT PRIMARY KEY, areasId BIGINT, businessUnitsName VARCHAR(100))");
        jdbc.execute("CREATE TABLE manage_personnel (employeeId BIGINT, areaId BIGINT, businessUnitId BIGINT)");
        jdbc.execute("CREATE TABLE settings (settingsId BIGINT PRIMARY KEY, companyName VARCHAR(100))");
        String migration = new String(new ClassPathResource(
                "db/timekeeping-migration/postgresql/automatic/V1301__mra_print_snapshot.sql")
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        jdbc.execute(migration);
        String summaryMigration = new String(new ClassPathResource(
                "db/timekeeping-migration/postgresql/automatic/V1302__attendance_summary_metric.sql")
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        jdbc.execute(summaryMigration);

        jdbc.update("INSERT INTO areas VALUES (?, ?)", 10L, "Nursing");
        jdbc.update("INSERT INTO businessunits VALUES (?, ?, ?)", 20L, 10L, "Ward A");
        jdbc.update("INSERT INTO manage_personnel VALUES (?, ?, ?)", 1L, 10L, 20L);
        jdbc.update("INSERT INTO settings VALUES (?, ?)", 1L, "Sample Agency");
        jdbc.update("INSERT INTO employee (employeeId, employeeNo, firstname, lastname) VALUES (?, ?, ?, ?)",
                1L, "0001", "Sample", "Employee");
        jdbc.update("INSERT INTO dtr_daily VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                1L, "1", Date.valueOf("2026-10-01"), 480, 10, 5, 30, "Present");
        jdbc.update("INSERT INTO dtr_segment VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                1L, 1L, 1, java.sql.Time.valueOf("18:00:00"), null, null,
                java.sql.Time.valueOf("02:00:00"), 10, 5);
        jdbc.update("INSERT INTO work_schedule VALUES (?, ?, ?, ?, ?)",
                1L, "1", null, false, Timestamp.valueOf("2026-10-01 00:00:00"));
        WorkScheduleService signatories = mock(WorkScheduleService.class);
        when(signatories.getWorkScheduleSignatoryInfo(anyString()))
                .thenReturn(Map.of("fullName", "Sample Officer", "position", "Administrative Officer"));
        MraReportService service = new MraReportService(jdbc, source, signatories);

        byte[] pdf = service.generate(2026, 10, 10L, 20L, 1L, 1L, null);
        assertThat(pdf).startsWith("%PDF".getBytes());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mra_print_day", Integer.class)).isZero();
        for (AttendanceSummaryType type : AttendanceSummaryType.values()) {
            byte[] summary = service.generateSummary(type, 2026, 10, 10L, 20L, 1L, 1L, null);
            assertThat(summary).startsWith("%PDF".getBytes());
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mra_print_day", Integer.class)).isZero();
        }
        jdbc.update("INSERT INTO dtr_daily VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                2L, "1", Date.valueOf("2026-10-02"), 0, 0, 0, 0, "Present");
        jdbc.update("INSERT INTO dtr_segment VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                2L, 2L, 1, java.sql.Time.valueOf("08:00:00"), null, null, null, 0, 0);
        jdbc.update("INSERT INTO work_schedule VALUES (?, ?, ?, ?, ?)",
                2L, "1", null, false, Timestamp.valueOf("2026-10-02 00:00:00"));
        byte[] incomplete = new IncompleteDtrReportService(jdbc, signatories).generate(
                java.time.LocalDate.of(2026, 10, 1), java.time.LocalDate.of(2026, 10, 2),
                10L, 20L, 1L, 1L);
        assertThat(incomplete).startsWith("%PDF".getBytes());
        assertThatThrownBy(() -> service.generate(2026, 10, 10L, 999L, 1L, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong");
    }
}
