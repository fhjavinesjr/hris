package com.humanresource.impl;

import com.humanresource.dtos.LeaveInformationDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.entitymodels.LeaveBeginningBalance;
import com.humanresource.entitymodels.LeaveInformation;
import com.humanresource.repositories.*;
import com.humanresource.services.EmployeeService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LeaveProcessPeriodBalanceTest {
    @ParameterizedTest
    @CsvSource({
            "2026-10-01,2026-09-01,2026-09-30",
            "2026-11-01,2026-10-01,2026-10-31",
            "2027-01-01,2026-12-01,2026-12-31",
            "2027-03-01,2027-02-01,2027-02-28",
            "2028-03-01,2028-02-01,2028-02-29"
    })
    void attendanceCutoffFollowsSelectedPostingMonth(String posting, String start, String end) {
        assertEquals(LocalDate.parse(start), LeaveProcessServiceImpl.attendanceStart(LocalDate.parse(posting)));
        assertEquals(LocalDate.parse(end), LeaveProcessServiceImpl.attendanceEnd(LocalDate.parse(posting)));
    }
    private static final LocalDate SEPTEMBER = LocalDate.of(2026, 9, 1);
    private final EmployeeAppointmentRepository appointments = mock(EmployeeAppointmentRepository.class);
    private final LeaveBeginningBalanceRepository beginnings = mock(LeaveBeginningBalanceRepository.class);
    private final LeaveInformationRepository ledger = mock(LeaveInformationRepository.class);
    private final Employee employee = new Employee();
    private final LeaveBeginningBalance sl = beginning(12.0);
    private final LeaveBeginningBalance vl = beginning(10.0);
    private JdbcTemplate jdbc;
    private LeaveProcessServiceImpl service;
    private LeaveInformation saved;
    private final List<String> skipped = new ArrayList<>();

    private void setup(String mode) {
        jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:leave_" + UUID.randomUUID()
                        + ";MODE=" + mode + ";NON_KEYWORDS=DAY;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE dtr_daily (employee_id VARCHAR(40), work_date DATE, "
                + "total_late_minutes INT, total_undertime_minutes INT, attendance_status VARCHAR(40))");
        jdbc.execute("CREATE TABLE work_schedule (wsId BIGINT, employeeId VARCHAR(40), "
                + "wsDateTime TIMESTAMP, tsCode VARCHAR(40), isDayOff BOOLEAN)");
        jdbc.execute("CREATE TABLE time_shift (tsCode VARCHAR(40), timeIn TIME, timeOut TIME, breakOut TIME, breakIn TIME)");
        jdbc.execute("CREATE TABLE earningleave (day INT, earn DOUBLE PRECISION, effectivityDate TIMESTAMP)");
        jdbc.execute("CREATE TABLE dayequivalenthours (hours INT, hoursEquivalent DOUBLE PRECISION, effectivityDate TIMESTAMP)");
        jdbc.execute("CREATE TABLE dayequivalentminutes (minutes INT, minutesEquivalent DOUBLE PRECISION, effectivityDate TIMESTAMP)");
        jdbc.update("INSERT INTO dayequivalenthours VALUES (8, 1.0, TIMESTAMP '2026-01-01 00:00:00')");
        jdbc.update("INSERT INTO dayequivalenthours VALUES (1, 0.125, TIMESTAMP '2026-01-01 00:00:00')");
        jdbc.update("INSERT INTO dayequivalentminutes VALUES (54, 0.113, TIMESTAMP '2026-01-01 00:00:00')");
        for (int days = 0; days <= 2; days++) {
            jdbc.update("INSERT INTO earningleave VALUES (?, 1.25, TIMESTAMP '2026-01-01 00:00:00')", days);
        }
        employee.setEmployeeId(30L);
        employee.setEmployeeNo("392");
        employee.setFirstname("Test");
        employee.setLastname("Employee");
        when(appointments.findTop1ByEmployeeIdAndActiveAppointmentTrueOrderByAssumptionToDutyDateDesc(30L))
                .thenReturn(new EmployeeAppointment());
        when(beginnings.findByEmployeeIdAndLeaveType(30L, "Sick Leave")).thenReturn(Optional.of(sl));
        when(beginnings.findByEmployeeIdAndLeaveType(30L, "Vacation Leave")).thenReturn(Optional.of(vl));
        when(ledger.save(any(LeaveInformation.class))).thenAnswer(invocation -> {
            saved = invocation.getArgument(0);
            return saved;
        });
        service = new LeaveProcessServiceImpl(mock(EmployeeRepository.class), mock(EmployeeService.class),
                appointments, mock(SeparationRepository.class), beginnings, mock(LeaveApplicationRepository.class),
                ledger, mock(LeaveMonetizationRepository.class), mock(PassSlipRepository.class),
                mock(CompensatoryTimeOffRepository.class), mock(OfficialEngagementApplicationRepository.class),
                mock(TimeCorrectionRepository.class), jdbc);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void octoberPostsSeptemberDtrUsingAugustBeginningBalance(String mode) {
        setup(mode);
        septemberAttendance();
        LeaveInformationDTO result = process(SEPTEMBER);
        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 10, 1), result.getCutoffStartDate());
        assertEquals("2026-09-14 [A] | 2026-09-15 [A]", result.getLeaveParticulars());
        assertFalse(result.getIsBegBalance());
        assertEquals(10.0, result.getPreviousVacationLeaveBalance());
        assertEquals(12.0, result.getPreviousSickLeaveBalance());
        assertEquals(534, result.getLateUndertimeMinutes());
        assertEquals(1, result.getLateCount());
        assertEquals(1, result.getUndertimeCount());
        assertEquals(1.113, result.getLateUndertimeEquivalent());
        assertEquals(2.0, result.getAbsentCount());
        assertEquals(8.137, result.getVacationLeaveBalance());
        assertEquals(13.25, result.getSickLeaveBalance());
        assertTrue(skipped.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void novemberCarriesOctoberAndReprocessingDoesNotDeductTwice(String mode) {
        setup(mode);
        septemberAttendance();
        process(SEPTEMBER);
        LeaveInformation september = saved;
        LocalDate october = SEPTEMBER.plusMonths(1);
        when(ledger.findTopByEmployeeIdAndCutoffEndDateBeforeOrderByCutoffEndDateDesc(30L, october.plusMonths(1)))
                .thenReturn(Optional.of(september));
        attendance(october);
        jdbc.update("UPDATE dtr_daily SET total_late_minutes=60 WHERE work_date=?", october);
        jdbc.update("UPDATE dtr_daily SET attendance_status='ABSENT' WHERE work_date=?", october.plusDays(1));
        LeaveInformationDTO first = process(october);
        LeaveInformation octoberRow = saved;
        when(ledger.findByEmployeeIdAndCutoffStartDateAndCutoffEndDate(30L, october.plusMonths(1), october.plusMonths(1).withDayOfMonth(30)))
                .thenReturn(Optional.of(octoberRow));
        LeaveInformationDTO second = process(october);
        assertEquals(8.137, first.getPreviousVacationLeaveBalance());
        assertEquals(13.25, first.getPreviousSickLeaveBalance());
        assertEquals(8.262, first.getVacationLeaveBalance());
        assertEquals(14.5, first.getSickLeaveBalance());
        assertEquals(first.getVacationLeaveBalance(), second.getVacationLeaveBalance());
        assertSame(octoberRow, saved);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void reprocessingRepairsOldFirstPeriodBalanceOnlyRow(String mode) {
        setup(mode);
        septemberAttendance();
        LeaveInformation old = new LeaveInformation();
        old.setLeaveInformationId(99L);
        old.setIsBegBalance(true);
        old.setVacationLeaveBalance(10.0);
        when(ledger.findByEmployeeIdAndCutoffStartDateAndCutoffEndDate(30L, SEPTEMBER.plusMonths(1), SEPTEMBER.plusMonths(1).withDayOfMonth(31)))
                .thenReturn(Optional.of(old));
        LeaveInformationDTO first = process(SEPTEMBER);
        LeaveInformationDTO second = process(SEPTEMBER);
        assertSame(old, saved);
        assertEquals(99L, second.getLeaveInformationId());
        assertFalse(second.getIsBegBalance());
        assertEquals(8.137, second.getVacationLeaveBalance());
        assertEquals(first.getVacationLeaveBalance(), second.getVacationLeaveBalance());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void beginningBalanceCutoffRemainsAnInitializationRow(String mode) {
        setup(mode);
        LeaveInformationDTO august = process(SEPTEMBER.minusMonths(1));
        assertTrue(august.getIsBegBalance());
        assertEquals(10.0, august.getVacationLeaveBalance());
        assertEquals(0, august.getLateUndertimeMinutes());
        assertEquals(0.0, august.getAbsentCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void futureVacationBeginningBalanceIsRejected(String mode) {
        setup(mode);
        vl.setAsOfDate(SEPTEMBER.plusMonths(1));
        assertNull(process(SEPTEMBER));
        assertTrue(skipped.get(0).contains("VL beginning balance date is after period end"));
        verify(ledger, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void lockedPeriodAndLaterPeriodPreventReprocessing(String mode) {
        setup(mode);
        LeaveInformation locked = new LeaveInformation();
        locked.setIsLocked(true);
        when(ledger.findByEmployeeIdAndCutoffStartDateAndCutoffEndDate(30L, SEPTEMBER.plusMonths(1), SEPTEMBER.plusMonths(1).withDayOfMonth(31)))
                .thenReturn(Optional.of(locked));
        assertNull(process(SEPTEMBER));
        assertTrue(skipped.get(0).contains("Period is locked"));
        locked.setIsLocked(false);
        when(ledger.existsByEmployeeIdAndCutoffEndDateGreaterThan(30L, SEPTEMBER.plusMonths(1).withDayOfMonth(31)))
                .thenReturn(true);
        assertNull(process(SEPTEMBER));
        assertTrue(skipped.get(1).contains("A later period already exists"));
        verify(ledger, never()).save(any());
    }

    private void septemberAttendance() {
        attendance(SEPTEMBER);
        jdbc.update("UPDATE dtr_daily SET total_late_minutes=428, total_undertime_minutes=106 WHERE work_date=?",
                SEPTEMBER.withDayOfMonth(11));
        jdbc.update("UPDATE dtr_daily SET attendance_status='ABSENT' WHERE work_date=?", SEPTEMBER.withDayOfMonth(14));
        jdbc.update("DELETE FROM dtr_daily WHERE work_date=?", SEPTEMBER.withDayOfMonth(15));
    }

    private void attendance(LocalDate start) {
        for (LocalDate day = start; day.getMonth() == start.getMonth(); day = day.plusDays(1)) {
            if (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY) {
                jdbc.update("INSERT INTO dtr_daily VALUES ('30', ?, 0, 0, 'PRESENT')", day);
            }
        }
    }

    private LeaveInformationDTO process(LocalDate attendanceMonth) {
        LocalDate start = attendanceMonth.plusMonths(1);
        return service.processEmployee(employee, start, start.withDayOfMonth(start.lengthOfMonth()),
                1L, 1L, Set.of(), skipped, "Test employee");
    }

    private static LeaveBeginningBalance beginning(double balance) {
        LeaveBeginningBalance value = new LeaveBeginningBalance();
        value.setAsOfDate(LocalDate.of(2026, 8, 30));
        value.setBalance(balance);
        return value;
    }
}
