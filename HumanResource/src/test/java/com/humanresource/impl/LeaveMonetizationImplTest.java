package com.humanresource.impl;

import com.humanresource.dtos.LeaveBalanceDTO;
import com.humanresource.dtos.LeaveMonetizationDTO;
import com.humanresource.entitymodels.LeaveMonetization;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.LeaveMonetizationRepository;
import com.humanresource.services.LeaveBalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeaveMonetizationImplTest {

    private LeaveMonetizationRepository repository;
    private LeaveBalanceService leaveBalanceService;
    private LeaveMonetizationImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(LeaveMonetizationRepository.class);
        leaveBalanceService = mock(LeaveBalanceService.class);
        service = new LeaveMonetizationImpl(
                repository,
                leaveBalanceService,
                mock(EmployeeRepository.class)
        );
        when(repository.save(any(LeaveMonetization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void administrativeCreateUsesTheAuthoritativeCurrentBalance() throws Exception {
        when(leaveBalanceService.getCurrentBalance(1L)).thenReturn(balance(18.0, 20.0));

        LeaveMonetizationDTO request = request(10.0, 10.0);
        request.setRecommendationStatus("Approved");
        request.setApprovalStatus("Approved");

        LeaveMonetizationDTO result = service.create(request);

        assertEquals(18.0, result.getVlBalanceBefore());
        assertEquals(20.0, result.getSlBalanceBefore());
        assertEquals(8.0, result.getVlBalanceAfter());
        assertEquals(10.0, result.getSlBalanceAfter());
        verify(leaveBalanceService).getCurrentBalance(1L);
    }

    @Test
    void finalApprovalRefreshesAStaleFilingTimeBalance() throws Exception {
        LeaveMonetization entity = new LeaveMonetization();
        entity.setLeaveMonetizationId(7L);
        entity.setEmployeeId(1L);
        entity.setNoOfDaysVL(10.0);
        entity.setNoOfDaysSL(10.0);
        entity.setTotalDays(20.0);
        entity.setVlBalanceBefore(0.0);
        entity.setSlBalanceBefore(0.0);
        entity.setApprovalStatus("Pending");
        when(repository.findById(7L)).thenReturn(Optional.of(entity));
        when(leaveBalanceService.getCurrentBalanceExcludingMonetization(1L, 7L))
                .thenReturn(balance(18.0, 20.0));

        LeaveMonetizationDTO result = service.approve(7L, 2L, "Approved");

        assertEquals(18.0, result.getVlBalanceBefore());
        assertEquals(20.0, result.getSlBalanceBefore());
        assertEquals(8.0, result.getVlBalanceAfter());
        assertEquals(10.0, result.getSlBalanceAfter());
        assertEquals("Approved", result.getApprovalStatus());
    }

    @Test
    void createRejectsMoreThanTenDaysForEitherLeaveType() throws Exception {
        LeaveMonetizationDTO request = request(10.5, 0.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(request)
        );

        assertEquals(
                "Vacation Leave monetization is limited to 10 days per filing.",
                exception.getMessage()
        );
        verify(leaveBalanceService, never()).getCurrentBalance(any());
        verify(repository, never()).save(any(LeaveMonetization.class));
    }

    private LeaveMonetizationDTO request(double vlDays, double slDays) {
        LeaveMonetizationDTO request = new LeaveMonetizationDTO();
        request.setEmployeeId(1L);
        request.setNoOfDaysVL(vlDays);
        request.setNoOfDaysSL(slDays);
        request.setReason("Test monetization");
        return request;
    }

    private LeaveBalanceDTO balance(double vl, double sl) {
        LeaveBalanceDTO balance = new LeaveBalanceDTO();
        balance.setEmployeeId(1L);
        balance.setVacationLeaveBalance(vl);
        balance.setSickLeaveBalance(sl);
        return balance;
    }
}
