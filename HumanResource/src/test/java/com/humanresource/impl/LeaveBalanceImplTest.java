package com.humanresource.impl;

import com.humanresource.dtos.LeaveBalanceDTO;
import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.entitymodels.LeaveBeginningBalance;
import com.humanresource.entitymodels.LeaveInformation;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.LeaveMonetization;
import com.humanresource.repositories.LeaveApplicationRepository;
import com.humanresource.repositories.LeaveBeginningBalanceRepository;
import com.humanresource.repositories.LeaveInformationRepository;
import com.humanresource.repositories.LeaveMonetizationRepository;
import com.humanresource.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeaveBalanceImplTest {

    private LeaveApplicationRepository leaveApplicationRepository;
    private LeaveMonetizationRepository leaveMonetizationRepository;
    private LeaveBalanceImpl service;
    private LeaveInformationRepository leaveInformationRepository;
    private LeaveBeginningBalanceRepository beginningBalanceRepository;
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        leaveInformationRepository = mock(LeaveInformationRepository.class);
        beginningBalanceRepository = mock(LeaveBeginningBalanceRepository.class);
        leaveApplicationRepository = mock(LeaveApplicationRepository.class);
        leaveMonetizationRepository = mock(LeaveMonetizationRepository.class);
        employeeRepository = mock(EmployeeRepository.class);

        when(leaveInformationRepository
                .findTopByEmployeeIdAndCutoffEndDateBeforeOrderByCutoffEndDateDesc(
                        any(Long.class), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(beginningBalanceRepository.findByEmployeeIdAndLeaveType(1L, "Vacation Leave"))
                .thenReturn(Optional.of(beginningBalance("Vacation Leave", 18.0)));
        when(beginningBalanceRepository.findByEmployeeIdAndLeaveType(1L, "Sick Leave"))
                .thenReturn(Optional.of(beginningBalance("Sick Leave", 20.0)));
        when(beginningBalanceRepository.findByEmployeeIdAndLeaveType(1L, "Special Privilege Leave"))
                .thenReturn(Optional.empty());
        when(beginningBalanceRepository.findByEmployeeIdAndLeaveType(1L, "Forced Leave"))
                .thenReturn(Optional.empty());
        when(leaveApplicationRepository.findByEmployeeId(1L)).thenReturn(Collections.emptyList());
        when(leaveMonetizationRepository.findByEmployeeIdOrderByDateFiledDesc(1L))
                .thenReturn(Collections.emptyList());

        service = new LeaveBalanceImpl(
                leaveInformationRepository,
                beginningBalanceRepository,
                leaveApplicationRepository,
                leaveMonetizationRepository,
                employeeRepository
        );
    }

    @Test
    void bulkBalanceReturnsPostedBalanceAsOfDateByEmployeeNumber() {
        LocalDate asOfDate = LocalDate.of(2026, 6, 30);
        Employee employee = new Employee();
        employee.setEmployeeId(34L);
        employee.setEmployeeNo("202600006");

        LeaveInformation posted = new LeaveInformation();
        posted.setEmployeeId(34L);
        posted.setVacationLeaveBalance(35.0);
        posted.setSickLeaveBalance(39.0);

        when(employeeRepository.findAllById(any())).thenReturn(List.of(employee));
        when(leaveInformationRepository.findLatestPostedBalancesAsOf(asOfDate))
                .thenReturn(List.of(posted));
        when(beginningBalanceRepository
                .findByLeaveTypeIgnoreCaseAndAsOfDateLessThanEqual("Vacation Leave", asOfDate))
                .thenReturn(Collections.emptyList());

        Map<String, Double> balances = service.getPostedBalancesAsOf("VL", asOfDate);

        assertEquals(35.0, balances.get("202600006"));
    }

    @Test
    void bulkBalanceFallsBackToBeginningBalanceWhenNoPeriodIsPosted() {
        LocalDate asOfDate = LocalDate.of(2026, 6, 30);
        Employee employee = new Employee();
        employee.setEmployeeId(34L);
        employee.setEmployeeNo("202600006");
        LeaveBeginningBalance beginning = beginningBalance("Sick Leave", 39.0);
        beginning.setEmployeeId(34L);
        beginning.setAsOfDate(asOfDate);

        when(employeeRepository.findAllById(any())).thenReturn(List.of(employee));
        when(leaveInformationRepository.findLatestPostedBalancesAsOf(asOfDate))
                .thenReturn(Collections.emptyList());
        when(beginningBalanceRepository
                .findByLeaveTypeIgnoreCaseAndAsOfDateLessThanEqual("Sick Leave", asOfDate))
                .thenReturn(List.of(beginning));

        Map<String, Double> balances = service.getPostedBalancesAsOf("SL", asOfDate);

        assertEquals(39.0, balances.get("202600006"));
    }

    @Test
    void bulkBalanceRejectsUnsupportedLeaveType() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getPostedBalancesAsOf("FL", LocalDate.of(2026, 6, 30)));
    }

    @Test
    void pendingMonetizationImmediatelyReservesRunningBalance() throws Exception {
        when(leaveMonetizationRepository.findByEmployeeIdOrderByDateFiledDesc(1L))
                .thenReturn(List.of(monetization(7L, "Pending", 10.0, 10.0)));

        LeaveBalanceDTO balance = service.getCurrentBalance(1L);

        assertEquals(8.0, balance.getVacationLeaveBalance());
        assertEquals(10.0, balance.getSickLeaveBalance());
    }

    @Test
    void disapprovedApplicationsAndMonetizationsReleaseTheirReservation() throws Exception {
        LeaveApplication application = new LeaveApplication();
        application.setStartDate(LocalDate.now());
        application.setEndDate(LocalDate.now());
        application.setNoOfDays(5.0);
        application.setLeaveType("Vacation Leave");
        application.setStatus("Disapproved");
        when(leaveApplicationRepository.findByEmployeeId(1L)).thenReturn(List.of(application));
        when(leaveMonetizationRepository.findByEmployeeIdOrderByDateFiledDesc(1L))
                .thenReturn(List.of(monetization(7L, "Disapproved", 10.0, 10.0)));

        LeaveBalanceDTO balance = service.getCurrentBalance(1L);

        assertEquals(18.0, balance.getVacationLeaveBalance());
        assertEquals(20.0, balance.getSickLeaveBalance());
    }

    @Test
    void approvalValidationCanExcludeItsOwnExistingReservation() throws Exception {
        when(leaveMonetizationRepository.findByEmployeeIdOrderByDateFiledDesc(1L))
                .thenReturn(List.of(monetization(7L, "Pending", 10.0, 10.0)));

        LeaveBalanceDTO balance = service.getCurrentBalanceExcludingMonetization(1L, 7L);

        assertEquals(18.0, balance.getVacationLeaveBalance());
        assertEquals(20.0, balance.getSickLeaveBalance());
    }

    @Test
    void leaveFormCanShowBalanceBeforeItsOwnApplication() throws Exception {
        LeaveApplication application = new LeaveApplication();
        application.setLeaveApplicationId(9L);
        application.setStartDate(LocalDate.now());
        application.setEndDate(LocalDate.now().plusDays(1));
        application.setNoOfDays(2.0);
        application.setLeaveType("Vacation Leave");
        application.setStatus("Approved");
        when(leaveApplicationRepository.findByEmployeeId(1L)).thenReturn(List.of(application));

        LeaveBalanceDTO current = service.getCurrentBalance(1L);
        LeaveBalanceDTO beforeApplication =
                service.getCurrentBalanceExcludingLeaveApplication(1L, 9L);

        assertEquals(16.0, current.getVacationLeaveBalance());
        assertEquals(18.0, beforeApplication.getVacationLeaveBalance());
        assertEquals(20.0, beforeApplication.getSickLeaveBalance());
    }

    @Test
    void leaveWithoutPayDoesNotReduceVlOrSlDashboardBalance() throws Exception {
        LeaveApplication unpaidVl = leaveApplication("Vacation Leave", 2.0, false);
        LeaveApplication unpaidSl = leaveApplication("Sick Leave", 3.0, false);
        when(leaveApplicationRepository.findByEmployeeId(1L))
                .thenReturn(List.of(unpaidVl, unpaidSl));

        LeaveBalanceDTO balance = service.getCurrentBalance(1L);

        assertEquals(18.0, balance.getVacationLeaveBalance());
        assertEquals(20.0, balance.getSickLeaveBalance());
    }

    @Test
    void paidAndLegacyNullPayFlagsStillReserveDashboardCredit() throws Exception {
        LeaveApplication paidVl = leaveApplication("Vacation Leave", 2.0, true);
        LeaveApplication legacySl = leaveApplication("Sick Leave", 3.0, null);
        when(leaveApplicationRepository.findByEmployeeId(1L))
                .thenReturn(List.of(paidVl, legacySl));

        LeaveBalanceDTO balance = service.getCurrentBalance(1L);

        assertEquals(16.0, balance.getVacationLeaveBalance());
        assertEquals(17.0, balance.getSickLeaveBalance());
    }

    private LeaveApplication leaveApplication(String type, double days, Boolean withPay) {
        LeaveApplication application = new LeaveApplication();
        application.setStartDate(LocalDate.now());
        application.setEndDate(LocalDate.now());
        application.setNoOfDays(days);
        application.setLeaveType(type);
        application.setStatus("Pending");
        application.setWithPay(withPay);
        return application;
    }

    private LeaveBeginningBalance beginningBalance(String type, double balance) {
        LeaveBeginningBalance beginning = new LeaveBeginningBalance();
        beginning.setEmployeeId(1L);
        beginning.setLeaveType(type);
        beginning.setBalance(balance);
        beginning.setAsOfDate(LocalDate.now().minusMonths(1));
        return beginning;
    }

    private LeaveMonetization monetization(
            Long id,
            String status,
            double vl,
            double sl) {
        LeaveMonetization monetization = new LeaveMonetization();
        monetization.setLeaveMonetizationId(id);
        monetization.setEmployeeId(1L);
        monetization.setDateFiled(LocalDate.now());
        monetization.setApprovalStatus(status);
        monetization.setNoOfDaysVL(vl);
        monetization.setNoOfDaysSL(sl);
        return monetization;
    }
}
