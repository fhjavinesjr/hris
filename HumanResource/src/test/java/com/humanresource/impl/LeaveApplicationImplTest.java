package com.humanresource.impl;

import com.humanresource.dtos.LeaveApplicationDTO;
import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.LeaveApplicationRepository;
import com.humanresource.services.DateConflictChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeaveApplicationImplTest {

    private LeaveApplicationRepository leaveApplicationRepository;
    private LeaveApplicationImpl service;

    @BeforeEach
    void setUp() {
        leaveApplicationRepository = mock(LeaveApplicationRepository.class);
        service = new LeaveApplicationImpl(
                leaveApplicationRepository,
                mock(EmployeeRepository.class),
                mock(DateConflictChecker.class)
        );
        when(leaveApplicationRepository.save(any(LeaveApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void fullUpdateUsesTheMandatoryOverallStatusInsteadOfNullableApprovedStatus()
            throws Exception {
        LeaveApplication entity = pendingLeaveApplication();
        LeaveApplicationDTO update = fullUpdate(entity);
        update.setStatus("Pending");
        update.setApprovedStatus(null);
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(entity));

        LeaveApplicationDTO result = service.updateLeaveApplication(1L, update);

        assertEquals("Pending", result.getStatus());
        assertNull(result.getApprovedStatus());
    }

    @Test
    void updatePersistsExplicitLeaveWithoutPay() throws Exception {
        LeaveApplication entity = pendingLeaveApplication();
        LeaveApplicationDTO update = fullUpdate(entity);
        update.setWithPay(false);
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(entity));

        LeaveApplicationDTO result = service.updateLeaveApplication(1L, update);

        assertFalse(result.getWithPay());
        assertFalse(entity.getWithPay());
    }

    @Test
    void recommendationPreservesPendingOverallStatus() throws Exception {
        LeaveApplication entity = pendingLeaveApplication();
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(entity));

        LeaveApplicationDTO result =
                service.recommendLeaveApplication(1L, 10L, "Recommended");

        assertEquals("Pending", result.getStatus());
        assertEquals("Recommended", result.getRecommendationStatus());
        assertEquals(10L, result.getRecommendingApprovalById());
        assertEquals("Recommended", result.getRecommendationMessage());
        assertNull(result.getApprovedStatus());
    }

    @Test
    void finalApprovalSetsBothOverallAndFinalApprovalStatuses() throws Exception {
        LeaveApplication entity = pendingLeaveApplication();
        entity.setRecommendationStatus("Recommended");
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(entity));

        LeaveApplicationDTO result =
                service.approveLeaveApplication(1L, 20L, "Approved");

        assertEquals("Approved", result.getStatus());
        assertEquals("Approved", result.getApprovedStatus());
        assertEquals(20L, result.getApprovedById());
        assertEquals("Approved", result.getApprovalMessage());
    }

    @Test
    void completedLeaveCannotBeRecommendedAgain() {
        LeaveApplication entity = pendingLeaveApplication();
        entity.setStatus("Approved");
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(entity));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.recommendLeaveApplication(1L, 10L, "")
        );

        assertEquals(
                "Only pending leave applications may be recommended.",
                exception.getMessage()
        );
        verify(leaveApplicationRepository, never()).save(any(LeaveApplication.class));
    }

    @Test
    void paternityLeaveCannotExceedSevenWorkingDays() {
        LeaveApplicationDTO request = leaveRequest(
                "Paternity Leave",
                LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 16)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createLeaveApplication(request)
        );

        assertEquals(
                "Paternity Leave cannot exceed 7 working days per filing. It may be availed continuously or intermittently.",
                exception.getMessage()
        );
        verify(leaveApplicationRepository, never()).save(any(LeaveApplication.class));
    }

    @Test
    void maternityLeaveCannotExceedOneHundredFiveCalendarDays() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LeaveApplicationDTO request = leaveRequest("Maternity Leave", start, start.plusDays(105));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createLeaveApplication(request)
        );

        assertEquals(
                "Maternity Leave for live childbirth cannot exceed 105 calendar days and must be continuous and uninterrupted.",
                exception.getMessage()
        );
    }

    @Test
    void soloParentLeaveCountsStaggeredApplicationsAgainstAnnualLimit() {
        LeaveApplication existing = pendingLeaveApplication();
        existing.setLeaveType("Solo Parent Leave");
        existing.setStartDate(LocalDate.of(2026, 1, 5));
        existing.setEndDate(LocalDate.of(2026, 1, 9));
        when(leaveApplicationRepository.findByEmployeeId(100L)).thenReturn(List.of(existing));

        LeaveApplicationDTO request = leaveRequest(
                "Solo Parent Leave",
                LocalDate.of(2026, 2, 2),
                LocalDate.of(2026, 2, 4)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createLeaveApplication(request)
        );

        assertEquals(
                "Solo Parent Leave is limited to 7 working days in 2026. Only 2 working day(s) remain.",
                exception.getMessage()
        );
    }

    @Test
    void soloParentLeaveMayBeFiledInStaggeredApplicationsWithinAnnualLimit() throws Exception {
        LeaveApplication existing = pendingLeaveApplication();
        existing.setLeaveType("Solo Parent Leave");
        existing.setStartDate(LocalDate.of(2026, 1, 5));
        existing.setEndDate(LocalDate.of(2026, 1, 7));
        when(leaveApplicationRepository.findByEmployeeId(100L)).thenReturn(List.of(existing));

        LeaveApplicationDTO request = leaveRequest(
                "Solo Parent Leave",
                LocalDate.of(2026, 2, 2),
                LocalDate.of(2026, 2, 5)
        );

        service.createLeaveApplication(request);

        verify(leaveApplicationRepository).save(any(LeaveApplication.class));
    }

    private LeaveApplication pendingLeaveApplication() {
        return new LeaveApplication(
                1L,
                100L,
                LocalDate.of(2026, 7, 23),
                "Vacation Leave",
                LocalDate.of(2026, 7, 30),
                LocalDate.of(2026, 7, 30),
                1.0,
                "Requested",
                "Personal",
                "Pending",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }

    private LeaveApplicationDTO fullUpdate(LeaveApplication entity) {
        return new LeaveApplicationDTO(
                entity.getLeaveApplicationId(),
                entity.getEmployeeId(),
                entity.getDateFiled(),
                entity.getLeaveType(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getNoOfDays(),
                entity.getCommutation(),
                entity.getDetails(),
                entity.getStatus(),
                entity.getRecommendingApprovalById(),
                entity.getAuthorizedOfficialId(),
                entity.getApprovedById(),
                entity.getRecommendationStatus(),
                entity.getRecommendationMessage(),
                entity.getApprovedStatus(),
                entity.getApprovalMessage(),
                entity.getDueExigencyService()
        );
    }

    private LeaveApplicationDTO leaveRequest(String leaveType, LocalDate start, LocalDate end) {
        LeaveApplicationDTO request = new LeaveApplicationDTO();
        request.setEmployeeId(100L);
        request.setDateFiled(LocalDate.of(2026, 1, 1));
        request.setLeaveType(leaveType);
        request.setStartDate(start);
        request.setEndDate(end);
        request.setStatus("Pending");
        request.setWithPay(true);
        return request;
    }
}
