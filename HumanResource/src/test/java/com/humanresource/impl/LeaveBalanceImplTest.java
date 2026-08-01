package com.humanresource.impl;

import com.humanresource.dtos.LeaveBalanceDTO;
import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.entitymodels.LeaveBeginningBalance;
import com.humanresource.entitymodels.LeaveMonetization;
import com.humanresource.repositories.LeaveApplicationRepository;
import com.humanresource.repositories.LeaveBeginningBalanceRepository;
import com.humanresource.repositories.LeaveInformationRepository;
import com.humanresource.repositories.LeaveMonetizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeaveBalanceImplTest {

    private LeaveApplicationRepository leaveApplicationRepository;
    private LeaveMonetizationRepository leaveMonetizationRepository;
    private LeaveBalanceImpl service;

    @BeforeEach
    void setUp() {
        LeaveInformationRepository leaveInformationRepository = mock(LeaveInformationRepository.class);
        LeaveBeginningBalanceRepository beginningBalanceRepository = mock(LeaveBeginningBalanceRepository.class);
        leaveApplicationRepository = mock(LeaveApplicationRepository.class);
        leaveMonetizationRepository = mock(LeaveMonetizationRepository.class);

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
                leaveMonetizationRepository
        );
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
