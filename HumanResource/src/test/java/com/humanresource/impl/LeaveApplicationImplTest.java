package com.humanresource.impl;

import com.humanresource.dtos.LeaveApplicationDTO;
import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.LeaveApplicationRepository;
import com.humanresource.services.DateConflictChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
