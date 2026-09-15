package com.payroll.impl;

import com.payroll.dtos.ApprovedLeaveDTO;
import com.payroll.dtos.EmployeePayrollInfoDTO;
import com.payroll.dtos.DtrDailySummaryDTO;
import com.payroll.dtos.PayrollComputationRequest;
import com.payroll.dtos.PayrollDataSnapshot;
import com.payroll.entitymodels.PayrollDetail;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollComputationEngineLeaveWithoutPayTest {

    @Test
    void approvedLeaveWithoutPayDeductsOneDailyRate() {
        LocalDate leaveDate = LocalDate.of(2026, 9, 14);
        EmployeePayrollInfoDTO employee = employee();
        PayrollComputationRequest request = request(leaveDate);
        PayrollDataSnapshot snapshot = new PayrollDataSnapshot();
        snapshot.setLeavesMap(Map.of(employee.getEmployeeNo(), List.of(leave(leaveDate, false))));

        PayrollDetail result = new PayrollComputationEngine().compute(employee, request, snapshot);

        assertEquals(1.0, result.getAbsentDays());
        assertEquals(0.0, result.getVacationLeaveUsed());
        assertEquals(0.0, result.getActualBasic());
    }

    @Test
    void approvedLeaveWithPayRemainsPaidAndUsesLeaveCredit() {
        LocalDate leaveDate = LocalDate.of(2026, 9, 14);
        EmployeePayrollInfoDTO employee = employee();
        PayrollComputationRequest request = request(leaveDate);
        PayrollDataSnapshot snapshot = new PayrollDataSnapshot();
        snapshot.setLeavesMap(Map.of(employee.getEmployeeNo(), List.of(leave(leaveDate, true))));

        PayrollDetail result = new PayrollComputationEngine().compute(employee, request, snapshot);

        assertEquals(0.0, result.getAbsentDays());
        assertEquals(1.0, result.getVacationLeaveUsed());
        assertEquals(1_000.0, result.getActualBasic());
    }

    @Test
    void personalPassSlipDoesNotTurnMissingAttendanceIntoPaidPresence() {
        LocalDate date = LocalDate.of(2026, 9, 14);
        EmployeePayrollInfoDTO employee = employee();
        PayrollDataSnapshot snapshot = new PayrollDataSnapshot();
        snapshot.setDtrMap(Map.of(employee.getEmployeeNo(), List.of(passSlipDay(date, false))));

        PayrollDetail result = new PayrollComputationEngine().compute(employee, request(date), snapshot);

        assertEquals(1.0, result.getAbsentDays());
        assertEquals(0.0, result.getActualBasic());
    }

    @Test
    void fullShiftOfficialPassSlipIsPaidPresence() {
        LocalDate date = LocalDate.of(2026, 9, 14);
        EmployeePayrollInfoDTO employee = employee();
        PayrollDataSnapshot snapshot = new PayrollDataSnapshot();
        snapshot.setDtrMap(Map.of(employee.getEmployeeNo(), List.of(passSlipDay(date, true))));

        PayrollDetail result = new PayrollComputationEngine().compute(employee, request(date), snapshot);

        assertEquals(0.0, result.getAbsentDays());
        assertEquals(1_000.0, result.getActualBasic());
    }

    @Test
    void personalPassSlipUndertimeFallsToPayrollWhenVlIsUnavailable() {
        LocalDate date = LocalDate.of(2026, 9, 14);
        EmployeePayrollInfoDTO employee = employee();
        DtrDailySummaryDTO dtr = passSlipDay(date, false);
        dtr.setPresent(true);
        dtr.setUndertimeMinutes(60);
        PayrollDataSnapshot snapshot = new PayrollDataSnapshot();
        snapshot.setDtrMap(Map.of(employee.getEmployeeNo(), List.of(dtr)));
        snapshot.setVlBalanceMap(Map.of(employee.getEmployeeNo(), 0.0));
        snapshot.setEarnedLeavePerPeriod(0.0);

        PayrollDetail result = new PayrollComputationEngine().compute(employee, request(date), snapshot);

        assertEquals(60, result.getUndertimeMinutes());
        assertEquals(124.8, result.getUndertimeValue());
        assertEquals(875.2, result.getActualBasic());
    }

    private EmployeePayrollInfoDTO employee() {
        EmployeePayrollInfoDTO employee = new EmployeePayrollInfoDTO();
        employee.setEmployeeNo("EMP-00001");
        employee.setFullName("Test Employee");
        employee.setDepartment("Test Department");
        employee.setSalaryGrade(1);
        employee.setSalaryStep(1);
        employee.setBasicMonthlySalary(2_000.0);
        employee.setBasicPerSalary(1_000.0);
        return employee;
    }

    private PayrollComputationRequest request(LocalDate date) {
        PayrollComputationRequest request = new PayrollComputationRequest();
        request.setSalaryPeriodKey("2026-9-1");
        request.setSalaryType("SEMI_MONTHLY");
        request.setCutoffStartDate(date);
        request.setCutoffEndDate(date);
        request.setSalaryDate(date);
        request.setCutoffDays(1);
        return request;
    }

    private ApprovedLeaveDTO leave(LocalDate date, boolean withPay) {
        ApprovedLeaveDTO leave = new ApprovedLeaveDTO();
        leave.setEmployeeNo("EMP-00001");
        leave.setLeaveDate(date);
        leave.setLeaveType("Vacation Leave");
        leave.setWithPay(withPay);
        leave.setWorkDayType("WHOLEDAY");
        leave.setNoOfDaysApplied(1.0);
        return leave;
    }

    private DtrDailySummaryDTO passSlipDay(LocalDate date, boolean fullDayOfficial) {
        DtrDailySummaryDTO dtr = new DtrDailySummaryDTO();
        dtr.setEmployeeNo("EMP-00001");
        dtr.setDtrDate(date);
        dtr.setPresent(false);
        dtr.setRestDay(false);
        dtr.setHasApprovedPs(fullDayOfficial);
        dtr.setApprovedPassSlipPurpose(fullDayOfficial ? "Official" : "Personal");
        dtr.setApprovedPassSlipMinutes(fullDayOfficial ? 480 : 60);
        return dtr;
    }
}
