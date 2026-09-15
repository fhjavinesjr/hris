package com.humanresource.impl;

import com.humanresource.dtos.LeaveInformationDTO;
import com.humanresource.dtos.LeaveProcessRequestDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.entitymodels.LeaveBeginningBalance;
import com.humanresource.entitymodels.LeaveInformation;
import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.entitymodels.OfficialEngagementApplication;
import com.humanresource.entitymodels.PassSlip;
import com.humanresource.repositories.*;
import com.humanresource.services.EmployeeService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private final LeaveApplicationRepository leaveApplications = mock(LeaveApplicationRepository.class);
    private final LeaveInformationRepository ledger = mock(LeaveInformationRepository.class);
    private final OfficialEngagementApplicationRepository officialEngagements =
            mock(OfficialEngagementApplicationRepository.class);
    private final PassSlipRepository passSlips = mock(PassSlipRepository.class);
    private final Employee employee = new Employee();
    private final LeaveBeginningBalance sl = beginning(12.0);
    private final LeaveBeginningBalance vl = beginning(10.0);
    private JdbcTemplate jdbc;
    private LeaveProcessServiceImpl service;
    private LeaveInformation saved;
    private final List<String> skipped = new ArrayList<>();

    private void setup(String mode) {
        reset(leaveApplications, officialEngagements, passSlips);
        jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:leave_" + UUID.randomUUID()
                        + ";MODE=" + mode + ";NON_KEYWORDS=DAY;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE dtr_daily (employee_id VARCHAR(40), work_date DATE, "
                + "total_late_minutes INT, total_undertime_minutes INT, attendance_status VARCHAR(40))");
        jdbc.execute("CREATE TABLE work_schedule (wsId BIGINT, employeeId VARCHAR(40), "
                + "wsDateTime TIMESTAMP, tsCode VARCHAR(40), isDayOff BOOLEAN)");
        jdbc.execute("CREATE TABLE time_shift (tsCode VARCHAR(40), timeIn TIME, timeOut TIME, breakOut TIME, breakIn TIME)");
        // Mirrors the production Administrative entity, where day/earn are text.
        // PostgreSQL does not allow comparing this column directly to an integer parameter.
        jdbc.execute("CREATE TABLE earningleave (day VARCHAR(20), earn VARCHAR(20), effectivityDate TIMESTAMP)");
        jdbc.execute("CREATE TABLE dayequivalenthours (hours INT, hoursEquivalent DOUBLE PRECISION, effectivityDate TIMESTAMP)");
        jdbc.execute("CREATE TABLE dayequivalentminutes (minutes INT, minutesEquivalent DOUBLE PRECISION, effectivityDate TIMESTAMP)");
        jdbc.execute("CREATE TABLE salary_period_setting (salaryPeriodSettingId BIGINT, periodContext VARCHAR(20), "
                + "cutoffStartDay INT, cutoffStartMonthOffset INT, cutoffEndDay INT, cutoffEndMonthOffset INT, isActive BOOLEAN)");
        jdbc.update("INSERT INTO salary_period_setting VALUES (1, 'LEAVE', 1, -1, 31, -1, TRUE)");
        jdbc.update("INSERT INTO dayequivalenthours VALUES (8, 1.0, TIMESTAMP '2026-01-01 00:00:00')");
        jdbc.update("INSERT INTO dayequivalenthours VALUES (1, 0.125, TIMESTAMP '2026-01-01 00:00:00')");
        jdbc.update("INSERT INTO dayequivalentminutes VALUES (54, 0.113, TIMESTAMP '2026-01-01 00:00:00')");
        jdbc.update("INSERT INTO earningleave VALUES ('0.5', '1.229', TIMESTAMP '2026-01-01 00:00:00')");
        jdbc.update("INSERT INTO earningleave VALUES ('1.0', '1.208', TIMESTAMP '2026-01-01 00:00:00')");
        jdbc.update("INSERT INTO earningleave VALUES ('2.0', '1.167', TIMESTAMP '2026-01-01 00:00:00')");
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
                appointments, mock(SeparationRepository.class), beginnings, leaveApplications,
                ledger, mock(LeaveMonetizationRepository.class), passSlips,
                mock(CompensatoryTimeOffRepository.class), officialEngagements,
                mock(TimeCorrectionRepository.class), jdbc);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void octoberPostsSeptemberDtrUsingAugustBeginningBalance(String mode) {
        setup(mode);
        seedOpeningBalanceRow();
        septemberAttendance();
        LeaveInformationDTO result = process(SEPTEMBER);
        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 9, 1), result.getCutoffStartDate());
        assertEquals("14 [A] | 15 [A]", result.getLeaveParticulars());
        assertFalse(result.getIsBegBalance());
        assertEquals(10.0, result.getPreviousVacationLeaveBalance());
        assertEquals(12.0, result.getPreviousSickLeaveBalance());
        assertEquals(534, result.getLateUndertimeMinutes());
        assertEquals(1, result.getLateCount());
        assertEquals(1, result.getUndertimeCount());
        assertEquals(1.113, result.getLateUndertimeEquivalent());
        assertEquals(2.0, result.getAbsentCount());
        assertEquals(1.167, result.getEarnedVl());
        assertEquals(1.167, result.getEarnedSl());
        assertEquals(10.054, result.getVacationLeaveBalance());
        assertEquals(13.167, result.getSickLeaveBalance());
        assertTrue(skipped.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void novemberCarriesOctoberAndReprocessingDoesNotDeductTwice(String mode) {
        setup(mode);
        seedOpeningBalanceRow();
        septemberAttendance();
        process(SEPTEMBER);
        LeaveInformation september = saved;
        LocalDate october = SEPTEMBER.plusMonths(1);
        when(ledger.findTopByEmployeeIdAndCutoffEndDateBeforeOrderByCutoffEndDateDesc(30L, october))
                .thenReturn(Optional.of(september));
        attendance(october);
        jdbc.update("UPDATE dtr_daily SET total_late_minutes=60 WHERE work_date=?", october);
        jdbc.update("UPDATE dtr_daily SET attendance_status='ABSENT' WHERE work_date=?", october.plusDays(1));
        LeaveInformationDTO first = process(october);
        LeaveInformation octoberRow = saved;
        when(ledger.findByEmployeeIdAndCutoffStartDateAndCutoffEndDate(30L, october, october.withDayOfMonth(31)))
                .thenReturn(Optional.of(octoberRow));
        LeaveInformationDTO second = process(october);
        assertEquals(10.054, first.getPreviousVacationLeaveBalance());
        assertEquals(13.167, first.getPreviousSickLeaveBalance());
        assertEquals(11.137, first.getVacationLeaveBalance());
        assertEquals(14.375, first.getSickLeaveBalance());
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
        when(ledger.findByEmployeeIdAndCutoffStartDateAndCutoffEndDate(30L, SEPTEMBER, SEPTEMBER.withDayOfMonth(30)))
                .thenReturn(Optional.of(old));
        LeaveInformationDTO first = process(SEPTEMBER);
        LeaveInformationDTO second = process(SEPTEMBER);
        assertSame(old, saved);
        assertEquals(99L, second.getLeaveInformationId());
        assertFalse(second.getIsBegBalance());
        assertEquals(10.054, second.getVacationLeaveBalance());
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
    void leaveWithoutPayIsRecordedButDoesNotConsumeSickLeaveCredit(String mode) {
        setup(mode);
        seedOpeningBalanceRow();
        attendance(SEPTEMBER);
        LocalDate leaveDate = SEPTEMBER.withDayOfMonth(14);
        jdbc.update("UPDATE dtr_daily SET attendance_status='ABSENT' WHERE work_date=?", leaveDate);
        LeaveApplication leave = new LeaveApplication();
        leave.setEmployeeId(30L);
        leave.setLeaveType("Sick Leave");
        leave.setStartDate(leaveDate);
        leave.setEndDate(leaveDate);
        leave.setNoOfDays(1.0);
        leave.setApprovedStatus("Approved");
        leave.setWithPay(false);
        when(leaveApplications.findByEmployeeId(30L)).thenReturn(List.of(leave));

        LeaveInformationDTO result = process(SEPTEMBER);

        assertNotNull(result);
        assertEquals(1.0, result.getLeaveWithoutPaySl());
        assertEquals(0.0, result.getSickLeaveUsed());
        assertEquals(1.208, result.getEarnedVl());
        assertEquals(1.208, result.getEarnedSl());
        assertEquals(11.208, result.getVacationLeaveBalance());
        assertEquals(13.208, result.getSickLeaveBalance());
        assertTrue(result.getLeaveParticulars().contains("14 [LWOP-SL]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void halfDayWithoutPayUsesDecimalAdministrativeMapping(String mode) {
        setup(mode);
        seedOpeningBalanceRow();
        attendance(SEPTEMBER);
        LocalDate leaveDate = SEPTEMBER.withDayOfMonth(14);
        jdbc.update("UPDATE dtr_daily SET attendance_status='ABSENT' WHERE work_date=?", leaveDate);
        LeaveApplication leave = new LeaveApplication();
        leave.setEmployeeId(30L);
        leave.setLeaveType("Vacation Leave");
        leave.setStartDate(leaveDate);
        leave.setEndDate(leaveDate);
        leave.setNoOfDays(0.5);
        leave.setApprovedStatus("Approved");
        leave.setWithPay(false);
        when(leaveApplications.findByEmployeeId(30L)).thenReturn(List.of(leave));

        LeaveInformationDTO result = process(SEPTEMBER);

        assertEquals(0.5, result.getLeaveWithoutPayVl());
        assertEquals(1.229, result.getEarnedVl());
        assertEquals(1.229, result.getEarnedSl());
        assertEquals(11.229, result.getVacationLeaveBalance());
        assertEquals(13.229, result.getSickLeaveBalance());
        assertTrue(result.getLeaveParticulars().contains("14 [LWOP-VL-HALF]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void missingNonPayMappingDoesNotSilentlyGrantFullCredits(String mode) {
        setup(mode);
        seedOpeningBalanceRow();
        septemberAttendance();
        jdbc.update("UPDATE dtr_daily SET attendance_status='ABSENT' WHERE work_date=?",
                SEPTEMBER.withDayOfMonth(16));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> process(SEPTEMBER));

        assertTrue(error.getMessage().contains("3 day(s) without pay"));
        verify(ledger, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void approvedSickLeaveTakesPrecedenceOverOverlappingOfficialEngagement(String mode) {
        setup(mode);
        seedOpeningBalanceRow();
        attendance(SEPTEMBER);

        LocalDate leaveStart = LocalDate.of(2026, 9, 7);
        LocalDate leaveEnd = LocalDate.of(2026, 9, 11);
        for (LocalDate day = leaveStart; !day.isAfter(leaveEnd); day = day.plusDays(1)) {
            jdbc.update("UPDATE dtr_daily SET attendance_status='ABSENT' WHERE work_date=?", day);
        }

        LeaveApplication sickLeave = new LeaveApplication();
        sickLeave.setEmployeeId(30L);
        sickLeave.setLeaveType("Sick Leave");
        sickLeave.setStartDate(leaveStart);
        sickLeave.setEndDate(leaveEnd);
        sickLeave.setNoOfDays(5.0);
        sickLeave.setApprovedStatus("Approved");
        sickLeave.setWithPay(true);
        when(leaveApplications.findByEmployeeId(30L)).thenReturn(List.of(sickLeave));

        OfficialEngagementApplication engagement = new OfficialEngagementApplication();
        engagement.setEmployeeId(30L);
        engagement.setStartDate(LocalDate.of(2026, 9, 10));
        engagement.setStartTime(LocalTime.of(8, 0));
        engagement.setEndDate(LocalDate.of(2026, 9, 10));
        engagement.setEndTime(LocalTime.of(17, 0));
        engagement.setStatus("Approved");
        when(officialEngagements.findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                30L, SEPTEMBER.withDayOfMonth(30), SEPTEMBER))
                .thenReturn(List.of(engagement));

        LeaveInformationDTO result = process(SEPTEMBER);

        assertEquals(5.0, result.getSickLeaveUsed());
        assertEquals(8.25, result.getSickLeaveBalance());
        assertTrue(result.getLeaveParticulars().contains("10 [SL]"));
        assertFalse(result.getLeaveParticulars().contains("10 [OE]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void personalPassSlipIsChargedAsScheduledUndertime(String mode) {
        setup(mode);
        seedOpeningBalanceRow();
        attendance(SEPTEMBER);
        PassSlip passSlip = passSlip(LocalDate.of(2026, 9, 10), "Personal",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(passSlips.findByEmployeeIdAndPassSlipDateBetween(
                30L, SEPTEMBER, SEPTEMBER.withDayOfMonth(30))).thenReturn(List.of(passSlip));

        LeaveInformationDTO result = process(SEPTEMBER);

        assertEquals(60, result.getLateUndertimeMinutes());
        assertEquals(0.125, result.getLateUndertimeEquivalent());
        assertTrue(result.getLeaveParticulars().contains("10 [PS-P 10:00-11:00]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void officialPassSlipOnlyOffsetsTheCoveredShiftBoundary(String mode) {
        setup(mode);
        seedOpeningBalanceRow();
        attendance(SEPTEMBER);
        LocalDate date = LocalDate.of(2026, 9, 10);
        jdbc.update("UPDATE dtr_daily SET total_late_minutes=60 WHERE work_date=?", date);
        PassSlip passSlip = passSlip(date, "Official", LocalTime.of(8, 0), LocalTime.of(9, 0));
        when(passSlips.findByEmployeeIdAndPassSlipDateBetween(
                30L, SEPTEMBER, SEPTEMBER.withDayOfMonth(30))).thenReturn(List.of(passSlip));

        LeaveInformationDTO result = process(SEPTEMBER);

        assertEquals(0, result.getLateUndertimeMinutes());
        assertTrue(result.getLeaveParticulars().contains("10 [PS-O 08:00-09:00]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void partialPersonalPassSlipDoesNotSuppressAWholeDayAbsence(String mode) {
        setup(mode);
        seedOpeningBalanceRow();
        attendance(SEPTEMBER);
        LocalDate date = LocalDate.of(2026, 9, 10);
        jdbc.update("DELETE FROM dtr_daily WHERE work_date=?", date);
        PassSlip passSlip = passSlip(date, "Personal", LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(passSlips.findByEmployeeIdAndPassSlipDateBetween(
                30L, SEPTEMBER, SEPTEMBER.withDayOfMonth(30))).thenReturn(List.of(passSlip));

        LeaveInformationDTO result = process(SEPTEMBER);

        assertEquals(1.0, result.getAbsentCount());
        assertTrue(result.getLeaveParticulars().contains("10 [PS-P 10:00-11:00]"));
        assertTrue(result.getLeaveParticulars().contains("10 [A]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void fullShiftOfficialPassSlipMayExcuseMissingDtr(String mode) {
        setup(mode);
        seedOpeningBalanceRow();
        attendance(SEPTEMBER);
        LocalDate date = LocalDate.of(2026, 9, 10);
        jdbc.update("DELETE FROM dtr_daily WHERE work_date=?", date);
        PassSlip passSlip = passSlip(date, "Official", LocalTime.of(8, 0), LocalTime.of(17, 0));
        when(passSlips.findByEmployeeIdAndPassSlipDateBetween(
                30L, SEPTEMBER, SEPTEMBER.withDayOfMonth(30))).thenReturn(List.of(passSlip));

        LeaveInformationDTO result = process(SEPTEMBER);

        assertEquals(0.0, result.getAbsentCount());
        assertTrue(result.getLeaveParticulars().contains("10 [PS-O 08:00-17:00]"));
        assertFalse(result.getLeaveParticulars().contains("10 [A]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void administrativeLeaveSettingResolvesAndClampsTheActualCutoff(String mode) {
        setup(mode);
        LeaveProcessRequestDTO request = new LeaveProcessRequestDTO();
        request.setSalaryPeriodSettingId(1L);
        request.setCutoffStartDate(LocalDate.of(2027, 3, 1));
        request.setCutoffEndDate(LocalDate.of(2027, 3, 31));

        LeaveProcessServiceImpl.LeaveProcessingPeriod period = service.resolveProcessingPeriod(request);

        assertEquals(LocalDate.of(2027, 2, 1), period.cutoffStart());
        assertEquals(LocalDate.of(2027, 2, 28), period.cutoffEnd());
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
        when(ledger.findByEmployeeIdAndCutoffStartDateAndCutoffEndDate(30L, SEPTEMBER, SEPTEMBER.withDayOfMonth(30)))
                .thenReturn(Optional.of(locked));
        assertNull(process(SEPTEMBER));
        assertTrue(skipped.get(0).contains("Period is locked"));
        locked.setIsLocked(false);
        when(ledger.existsByEmployeeIdAndCutoffEndDateGreaterThan(30L, SEPTEMBER.withDayOfMonth(30)))
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

    private void seedOpeningBalanceRow() {
        LeaveInformation opening = new LeaveInformation();
        opening.setCutoffStartDate(LocalDate.of(2026, 8, 1));
        opening.setCutoffEndDate(LocalDate.of(2026, 8, 31));
        opening.setVacationLeaveBalance(10.0);
        opening.setSickLeaveBalance(12.0);
        opening.setIsBegBalance(true);
        when(ledger.findTopByEmployeeIdAndCutoffEndDateBeforeOrderByCutoffEndDateDesc(30L, SEPTEMBER))
                .thenReturn(Optional.of(opening));
    }

    private static LeaveBeginningBalance beginning(double balance) {
        LeaveBeginningBalance value = new LeaveBeginningBalance();
        value.setAsOfDate(LocalDate.of(2026, 8, 30));
        value.setBalance(balance);
        return value;
    }

    private PassSlip passSlip(LocalDate date, String purpose, LocalTime departure, LocalTime arrival) {
        PassSlip value = new PassSlip();
        value.setEmployeeId(30L);
        value.setPassSlipDate(date);
        value.setPurpose(purpose);
        value.setDepartureTime(departure);
        value.setArrivalTime(arrival);
        value.setStatus("Approved");
        return value;
    }
}
