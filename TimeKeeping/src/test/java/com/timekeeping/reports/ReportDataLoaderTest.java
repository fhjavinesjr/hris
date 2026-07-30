package com.timekeeping.reports;

import com.timekeeping.dtos.DtrReportRow;
import com.timekeeping.dtos.WorkScheduleReportRow;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportDataLoaderTest {

    @Test
    void dtrRowsPreserveOvernightCorrectionPassSlipRestDayLeaveAndAbsentBehavior() {
        JdbcTemplate jdbc = inMemoryJdbc("dtr-report");
        createDtrSchema(jdbc);

        jdbc.update(
                "INSERT INTO employee (employeeId, employeeNo, firstname, lastname) VALUES (?, ?, ?, ?)",
                1L, "001", "Ferdinand", "Javines"
        );
        jdbc.update(
                "INSERT INTO personaldata (employeeId, firstname, middlename, surname) VALUES (?, ?, ?, ?)",
                1L, "Ferdinand", "R", "Javines"
        );
        jdbc.update(
                """
                INSERT INTO dtr_daily (
                    dtr_daily_id, employee_id, work_date, total_work_minutes,
                    total_late_minutes, total_undertime_minutes,
                    total_overtime_minutes, attendance_status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                101L, "1", Date.valueOf("2026-07-01"), 720, 15, 30, 60, "Present"
        );
        jdbc.update(
                """
                INSERT INTO dtr_segment (
                    dtr_segment_id, dtr_daily_id, segment_no, time_in,
                    break_out, break_in, time_out, late_minutes, undertime_minutes
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                201L, 101L, 1, Time.valueOf("18:00:00"),
                null, null, Time.valueOf("06:00:00"), 15, 30
        );
        jdbc.update(
                "INSERT INTO pass_slip (passSlipId, employeeId, passSlipDate, departureTime, arrivalTime, status) VALUES (?, ?, ?, ?, ?, ?)",
                301L, 1L, Date.valueOf("2026-07-03"),
                Time.valueOf("10:00:00"), Time.valueOf("11:00:00"), "Approved"
        );
        jdbc.update(
                """
                INSERT INTO time_correction (
                    timeCorrectionId, employeeId, workDate, correctedTimeIn,
                    correctedBreakOut, correctedBreakIn, correctedTimeOut, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                401L, 1L, Date.valueOf("2026-07-04"),
                Time.valueOf("08:00:00"), Time.valueOf("12:00:00"),
                Time.valueOf("13:00:00"), Time.valueOf("17:00:00"), "Approved"
        );
        jdbc.update(
                "INSERT INTO work_schedule (wsId, employeeId, tsCode, isDayOff, wsDateTime) VALUES (?, ?, ?, ?, ?)",
                501L, "0001", null, true, Timestamp.valueOf("2026-07-05 00:00:00")
        );
        jdbc.update(
                "INSERT INTO leave_application (leaveApplicationId, employeeId, leaveType, startDate, endDate, approvedStatus) VALUES (?, ?, ?, ?, ?, ?)",
                601L, 1L, "Vacation Leave",
                Date.valueOf("2026-07-06"), Date.valueOf("2026-07-06"), "Approved"
        );

        List<DtrReportRow> rows = new DtrReportDataLoader(jdbc).load(
                "001",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 7)
        );

        DtrReportRow overnightStart = onlyRow(rows, "2026-07-01");
        assertThat(overnightStart.getIn2nd()).isEqualTo(Time.valueOf("18:00:00"));
        assertThat(overnightStart.getOut2nd()).isNull();
        assertThat(overnightStart.getUnderMin()).isEqualTo(45);
        assertThat(overnightStart.getOtReg()).isEqualTo(1.0d);

        DtrReportRow overnightEnd = onlyRow(rows, "2026-07-02");
        assertThat(overnightEnd.getOut1st()).isEqualTo(Time.valueOf("06:00:00"));
        assertThat(overnightEnd.getIsForNextDay()).isTrue();

        DtrReportRow passSlip = onlyRow(rows, "2026-07-03");
        assertThat(passSlip.getRemarks()).isEqualTo("PASS SLIP");
        assertThat(passSlip.getIn1st()).isEqualTo(Time.valueOf("11:00:00"));
        assertThat(passSlip.getOut1st()).isEqualTo(Time.valueOf("10:00:00"));

        DtrReportRow correction = onlyRow(rows, "2026-07-04");
        assertThat(correction.getRemarks()).isEqualTo("TIME CORRECTED");
        assertThat(correction.getIn1st()).isEqualTo(Time.valueOf("08:00:00"));
        assertThat(correction.getOut2nd()).isEqualTo(Time.valueOf("17:00:00"));

        assertThat(onlyRow(rows, "2026-07-05").getRemarks()).isEqualTo("REST DAY");
        assertThat(onlyRow(rows, "2026-07-06").getRemarks()).isEqualTo("Vacation Leave");
        assertThat(onlyRow(rows, "2026-07-07").getAbsentMin()).isEqualTo(480);
    }

    @Test
    void dtrIncludesPreviousPeriodOvernightDepartureOnTheFirstReportDate() {
        JdbcTemplate jdbc = inMemoryJdbc("dtr-boundary-report");
        createDtrSchema(jdbc);
        jdbc.update(
                "INSERT INTO employee (employeeId, employeeNo, firstname, lastname) VALUES (?, ?, ?, ?)",
                1L, "001", "Ferdinand", "Javines"
        );
        jdbc.update(
                """
                INSERT INTO dtr_daily (
                    dtr_daily_id, employee_id, work_date, total_work_minutes,
                    total_late_minutes, total_undertime_minutes,
                    total_overtime_minutes, attendance_status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                101L, "1", Date.valueOf("2026-06-30"), 420, 0, 0, 0, "Present"
        );
        jdbc.update(
                """
                INSERT INTO dtr_segment (
                    dtr_segment_id, dtr_daily_id, segment_no, time_in,
                    break_out, break_in, time_out, late_minutes, undertime_minutes
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                201L, 101L, 1, Time.valueOf("22:00:00"),
                null, null, Time.valueOf("05:00:00"), 0, 0
        );

        List<DtrReportRow> rows = new DtrReportDataLoader(jdbc).load(
                "001",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 1)
        );

        DtrReportRow firstDay = onlyRow(rows, "2026-07-01");
        assertThat(firstDay.getOut1st()).isEqualTo(Time.valueOf("05:00:00"));
        assertThat(firstDay.getIsForNextDay()).isTrue();
    }

    @Test
    void workScheduleRowsExpandDatesAndUseTheAppointmentEffectiveOnEachDate() {
        JdbcTemplate jdbc = inMemoryJdbc("work-schedule-report");
        createWorkScheduleSchema(jdbc);

        jdbc.update("INSERT INTO areas (areasId, areasName) VALUES (?, ?)", 10L, "Nursing");
        jdbc.update(
                "INSERT INTO businessunits (businessUnitsId, businessUnitsName) VALUES (?, ?)",
                20L, "Ward A"
        );
        jdbc.update(
                "INSERT INTO employee (employeeId, employeeNo, position, lastname, suffix, firstname) VALUES (?, ?, ?, ?, ?, ?)",
                1L, "001", "Fallback Position", "JAVINES", "JR", "FERDINAND"
        );
        jdbc.update(
                "INSERT INTO manage_personnel (employeeId, areaId, businessUnitId) VALUES (?, ?, ?)",
                1L, 10L, 20L
        );
        jdbc.update("INSERT INTO job_position (jobPositionId, jobPositionName) VALUES (?, ?)", 100L, "Nurse I");
        jdbc.update("INSERT INTO job_position (jobPositionId, jobPositionName) VALUES (?, ?)", 101L, "Nurse II");
        jdbc.update(
                """
                INSERT INTO employeeAppointment (
                    employeeAppointmentId, employeeId, assumptionToDutyDate,
                    activeAppointment, salaryGrade, jobPositionId
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
                1000L, 1L, Timestamp.valueOf("2026-06-01 00:00:00"), false, 10, 100L
        );
        jdbc.update(
                """
                INSERT INTO employeeAppointment (
                    employeeAppointmentId, employeeId, assumptionToDutyDate,
                    activeAppointment, salaryGrade, jobPositionId
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
                1001L, 1L, Timestamp.valueOf("2026-07-02 00:00:00"), true, 11, 101L
        );
        jdbc.update(
                """
                INSERT INTO time_shift (tsCode, timeIn, breakOut, breakIn, timeOut)
                VALUES (?, ?, ?, ?, ?)
                """,
                "1Q", Time.valueOf("08:00:00"), Time.valueOf("12:00:00"),
                Time.valueOf("13:00:00"), Time.valueOf("17:00:00")
        );
        jdbc.update(
                "INSERT INTO work_schedule (wsId, employeeId, tsCode, isDayOff, wsDateTime) VALUES (?, ?, ?, ?, ?)",
                2000L, "001", "1q", false, Timestamp.valueOf("2026-07-01 00:00:00")
        );
        jdbc.update(
                "INSERT INTO work_schedule (wsId, employeeId, tsCode, isDayOff, wsDateTime) VALUES (?, ?, ?, ?, ?)",
                2001L, "1", null, true, Timestamp.valueOf("2026-07-02 00:00:00")
        );

        List<WorkScheduleReportRow> rows = new WorkScheduleReportDataLoader(jdbc).load(
                10L,
                null,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3)
        );

        assertThat(rows).hasSize(3);
        assertThat(rows.get(0).getFullname()).isEqualTo("JAVINES JR, FERDINAND");
        assertThat(rows.get(0).getJobPosition()).isEqualTo("Nurse I");
        assertThat(rows.get(0).getSalaryGrade()).isEqualTo(10);
        assertThat(rows.get(0).getSched())
                .isEqualTo("1Q - 08:00 AM / 12:00 PM - 01:00 PM / 05:00 PM");

        assertThat(rows.get(1).getJobPosition()).isEqualTo("Nurse II");
        assertThat(rows.get(1).getSalaryGrade()).isEqualTo(11);
        assertThat(rows.get(1).getSched()).isEqualTo("DAY OFF / REST DAY");

        assertThat(rows.get(2).getDtrDate()).isEqualTo(Date.valueOf("2026-07-03"));
        assertThat(rows.get(2).getSched()).isEmpty();
    }

    private static DtrReportRow onlyRow(List<DtrReportRow> rows, String date) {
        List<DtrReportRow> matches = rows.stream()
                .filter(row -> row.getDtrDate().equals(Date.valueOf(date)))
                .toList();
        assertThat(matches).hasSize(1);
        return matches.get(0);
    }

    private static JdbcTemplate inMemoryJdbc(String name) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        dataSource.setDriverClassName("org.h2.Driver");
        return new JdbcTemplate(dataSource);
    }

    private static void createDtrSchema(JdbcTemplate jdbc) {
        jdbc.execute("CREATE TABLE employee (employeeId BIGINT PRIMARY KEY, employeeNo VARCHAR(100), firstname VARCHAR(100), lastname VARCHAR(100))");
        jdbc.execute("CREATE TABLE personaldata (employeeId BIGINT, firstname VARCHAR(100), middlename VARCHAR(100), surname VARCHAR(100))");
        jdbc.execute("CREATE TABLE dtr_daily (dtr_daily_id BIGINT PRIMARY KEY, employee_id VARCHAR(100), work_date DATE, total_work_minutes INT, total_late_minutes INT, total_undertime_minutes INT, total_overtime_minutes INT, attendance_status VARCHAR(50))");
        jdbc.execute("CREATE TABLE dtr_segment (dtr_segment_id BIGINT PRIMARY KEY, dtr_daily_id BIGINT, segment_no INT, time_in TIME, break_out TIME, break_in TIME, time_out TIME, late_minutes INT, undertime_minutes INT)");
        jdbc.execute("CREATE TABLE work_schedule (wsId BIGINT PRIMARY KEY, employeeId VARCHAR(100), tsCode VARCHAR(100), isDayOff BOOLEAN, wsDateTime TIMESTAMP)");
        jdbc.execute("CREATE TABLE official_engagement_application (officialEngagementApplicationId BIGINT PRIMARY KEY, employeeId BIGINT, officialType VARCHAR(50), startDate DATE, endDate DATE, status VARCHAR(50))");
        jdbc.execute("CREATE TABLE overtime_request (overtimeRequestId BIGINT PRIMARY KEY, employeeId BIGINT, dateTimeFrom TIMESTAMP, status VARCHAR(50))");
        jdbc.execute("CREATE TABLE compensatory_time_off (ctoId BIGINT PRIMARY KEY, employeeId BIGINT, dateOfOffset DATE, status VARCHAR(50))");
        jdbc.execute("CREATE TABLE pass_slip (passSlipId BIGINT PRIMARY KEY, employeeId BIGINT, passSlipDate DATE, departureTime TIME, arrivalTime TIME, status VARCHAR(50))");
        jdbc.execute("CREATE TABLE time_correction (timeCorrectionId BIGINT PRIMARY KEY, employeeId BIGINT, workDate DATE, correctedTimeIn TIME, correctedBreakOut TIME, correctedBreakIn TIME, correctedTimeOut TIME, status VARCHAR(50))");
        jdbc.execute("CREATE TABLE leave_application (leaveApplicationId BIGINT PRIMARY KEY, employeeId BIGINT, leaveType VARCHAR(100), startDate DATE, endDate DATE, approvedStatus VARCHAR(50))");
    }

    private static void createWorkScheduleSchema(JdbcTemplate jdbc) {
        jdbc.execute("CREATE TABLE areas (areasId BIGINT PRIMARY KEY, areasName VARCHAR(100))");
        jdbc.execute("CREATE TABLE businessunits (businessUnitsId BIGINT PRIMARY KEY, businessUnitsName VARCHAR(100))");
        jdbc.execute("CREATE TABLE employee (employeeId BIGINT PRIMARY KEY, employeeNo VARCHAR(100), position VARCHAR(100), lastname VARCHAR(100), suffix VARCHAR(100), firstname VARCHAR(100))");
        jdbc.execute("CREATE TABLE manage_personnel (employeeId BIGINT, areaId BIGINT, businessUnitId BIGINT)");
        jdbc.execute("CREATE TABLE job_position (jobPositionId BIGINT PRIMARY KEY, jobPositionName VARCHAR(100))");
        jdbc.execute("CREATE TABLE employeeAppointment (employeeAppointmentId BIGINT PRIMARY KEY, employeeId BIGINT, assumptionToDutyDate TIMESTAMP, activeAppointment BOOLEAN, salaryGrade INT, jobPositionId BIGINT)");
        jdbc.execute("CREATE TABLE work_schedule (wsId BIGINT PRIMARY KEY, employeeId VARCHAR(100), tsCode VARCHAR(100), isDayOff BOOLEAN, wsDateTime TIMESTAMP)");
        jdbc.execute("CREATE TABLE time_shift (tsCode VARCHAR(100), timeIn TIME, breakOut TIME, breakIn TIME, timeOut TIME)");
    }
}
