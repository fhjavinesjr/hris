package com.timekeeping.impl;

import com.timekeeping.repositories.DTRDailyRepository;
import com.timekeeping.repositories.DTRSegmentRepository;
import com.timekeeping.repositories.WorkScheduleRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DTRDailyServicePassSlipTest {

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void bulkSummaryChargesPersonalMinutesAndOnlyFlagsFullDayOfficial(String mode) {
        DriverManagerDataSource dataSource = dataSource(mode);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        createSchema(jdbc);

        jdbc.update("INSERT INTO employee (employeeId, employeeNo) VALUES (?, ?)", 1L, "001");
        jdbc.update("INSERT INTO time_shift (tsCode, timeIn, breakOut, breakIn, timeOut) VALUES (?, ?, ?, ?, ?)",
                "DAY", Time.valueOf("08:00:00"), Time.valueOf("12:00:00"),
                Time.valueOf("13:00:00"), Time.valueOf("17:00:00"));
        insertWorkDay(jdbc, 1L, "2026-09-10");
        insertWorkDay(jdbc, 2L, "2026-09-11");
        insertDtr(jdbc, 1L, "2026-09-10");
        jdbc.update("INSERT INTO pass_slip VALUES (?, ?, ?, ?, ?, ?, ?)",
                10L, 1L, Date.valueOf("2026-09-10"), "Personal",
                Time.valueOf("10:00:00"), Time.valueOf("11:00:00"), "Approved");
        jdbc.update("INSERT INTO pass_slip VALUES (?, ?, ?, ?, ?, ?, ?)",
                11L, 1L, Date.valueOf("2026-09-11"), "Official",
                Time.valueOf("08:00:00"), Time.valueOf("17:00:00"), "Approved");

        DTRDailyServiceImpl service = new DTRDailyServiceImpl(
                mock(DTRDailyRepository.class), mock(DTRSegmentRepository.class),
                mock(WorkScheduleRepository.class), jdbc, dataSource);
        List<Map<String, Object>> rows = service.getBulkDtrSummary(
                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 11));

        Map<String, Object> personal = row(rows, LocalDate.of(2026, 9, 10));
        assertThat(personal.get("undertimeMinutes")).isEqualTo(60);
        assertThat(personal.get("hasApprovedPs")).isEqualTo(false);
        assertThat(personal.get("approvedPassSlipPurpose")).isEqualTo("Personal");

        Map<String, Object> official = row(rows, LocalDate.of(2026, 9, 11));
        assertThat(official.get("hasApprovedPs")).isEqualTo(true);
        assertThat(official.get("approvedPassSlipMinutes")).isEqualTo(480);
    }

    private Map<String, Object> row(List<Map<String, Object>> rows, LocalDate date) {
        return rows.stream().filter(value -> date.equals(value.get("dtrDate"))).findFirst().orElseThrow();
    }

    private void insertWorkDay(JdbcTemplate jdbc, long id, String date) {
        jdbc.update("INSERT INTO work_schedule VALUES (?, ?, ?, ?, ?)", id, "1", "DAY", false,
                Timestamp.valueOf(date + " 00:00:00"));
    }

    private void insertDtr(JdbcTemplate jdbc, long id, String date) {
        jdbc.update("INSERT INTO dtr_daily VALUES (?, ?, ?, ?, ?, ?, ?, ?)", id, "1",
                Date.valueOf(date), "Present", 480, 0, 0, 0);
    }

    private DriverManagerDataSource dataSource(String mode) {
        String name = "pass_slip_bulk_" + mode + "_" + UUID.randomUUID().toString().replace("-", "");
        DriverManagerDataSource result = new DriverManagerDataSource(
                "jdbc:h2:mem:" + name + ";MODE=" + mode + ";DB_CLOSE_DELAY=-1", "sa", "");
        result.setDriverClassName("org.h2.Driver");
        return result;
    }

    private void createSchema(JdbcTemplate jdbc) {
        jdbc.execute("CREATE TABLE employee (employeeId BIGINT PRIMARY KEY, employeeNo VARCHAR(100))");
        jdbc.execute("CREATE TABLE work_schedule (wsId BIGINT PRIMARY KEY, employeeId VARCHAR(100), tsCode VARCHAR(100), isDayOff BOOLEAN, wsDateTime TIMESTAMP)");
        jdbc.execute("CREATE TABLE time_shift (tsCode VARCHAR(100), timeIn TIME, breakOut TIME, breakIn TIME, timeOut TIME)");
        jdbc.execute("CREATE TABLE dtr_daily (dtr_daily_id BIGINT PRIMARY KEY, employee_id VARCHAR(100), work_date DATE, attendance_status VARCHAR(50), total_work_minutes INT, total_late_minutes INT, total_undertime_minutes INT, total_overtime_minutes INT)");
        jdbc.execute("CREATE TABLE official_engagement_application (officialEngagementApplicationId BIGINT, employeeId BIGINT, status VARCHAR(50), startDate DATE, endDate DATE)");
        jdbc.execute("CREATE TABLE overtime_request (overtimeRequestId BIGINT, employeeId BIGINT, status VARCHAR(50), dateTimeFrom TIMESTAMP)");
        jdbc.execute("CREATE TABLE pass_slip (passSlipId BIGINT, employeeId BIGINT, passSlipDate DATE, purpose VARCHAR(50), departureTime TIME, arrivalTime TIME, status VARCHAR(50))");
        jdbc.execute("CREATE TABLE time_correction (timeCorrectionId BIGINT, employeeId BIGINT, status VARCHAR(50), workDate DATE)");
        jdbc.execute("CREATE TABLE compensatory_time_off (ctoId BIGINT, employeeId BIGINT, status VARCHAR(50), dateOfOffset DATE)");
        jdbc.execute("CREATE TABLE personaldata (personalDataId BIGINT, employeeId BIGINT)");
        jdbc.execute("CREATE TABLE learninganddevelopment (learningAndDevelopmentId BIGINT, personalDataId BIGINT, fromDate DATE, toDate DATE)");
    }
}
