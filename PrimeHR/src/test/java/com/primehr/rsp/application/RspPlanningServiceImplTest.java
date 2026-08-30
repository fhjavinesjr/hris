package com.primehr.rsp.application;

import com.primehr.integration.administrative.AdministrativeRspPositionSourceClient;
import com.primehr.integration.humanresource.HumanResourcePlantillaOccupancyClient;
import com.primehr.positionprofile.application.PositionProfileAdminService;
import com.primehr.rsp.api.RspPlanningDtos.CreatePlan;
import com.primehr.rsp.api.RspPlanningDtos.SaveVacancy;
import com.primehr.rsp.domain.RecruitmentPlan;
import com.primehr.rsp.domain.VacancyRequest;
import com.primehr.rsp.domain.VacancyRequestStatus;
import com.primehr.rsp.domain.VacancyType;
import com.primehr.rsp.infrastructure.RecruitmentPlanRepository;
import com.primehr.rsp.infrastructure.VacancyRequestRepository;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RspPlanningServiceImplTest {
    private final RecruitmentPlanRepository plans = mock(RecruitmentPlanRepository.class);
    private final VacancyRequestRepository vacancies = mock(VacancyRequestRepository.class);
    private final AdministrativeRspPositionSourceClient administrative =
            mock(AdministrativeRspPositionSourceClient.class);
    private final HumanResourcePlantillaOccupancyClient humanResource =
            mock(HumanResourcePlantillaOccupancyClient.class);
    private final PositionProfileAdminService profiles = mock(PositionProfileAdminService.class);
    private final PrimeHrAuditService audit = mock(PrimeHrAuditService.class);
    private final RspPlanningServiceImpl service = new RspPlanningServiceImpl(
            plans, vacancies, administrative, humanResource, profiles, audit);

    @Test
    void overlappingPlantillaIsRejectedBeforeSourceCallsOrMutation() {
        RecruitmentPlan plan = plan("plan-1");
        when(plans.findByIdAndAgencyId("plan-1", "AGENCY-A")).thenReturn(Optional.of(plan));
        when(vacancies.existsActiveForOverlappingPeriod("AGENCY-A", 3L,
                plan.getPeriodStart(), plan.getPeriodEnd(), null)).thenReturn(true);

        assertThatThrownBy(() -> service.addVacancy("AGENCY-A", "plan-1", vacancy(null),
                "Bearer token", "correlation"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("overlapping");

        verifyNoInteractions(administrative, humanResource, profiles, audit);
        verify(vacancies, never()).saveAndFlush(any());
    }

    @Test
    void tenantScopedLookupDoesNotFallBackToAnUnscopedIdentifier() {
        when(plans.findByIdAndAgencyId("plan-1", "AGENCY-B")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get("AGENCY-B", "plan-1"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(plans).findByIdAndAgencyId("plan-1", "AGENCY-B");
        verifyNoInteractions(vacancies);
    }

    @Test
    void failedPlanPersistenceCannotEmitAnAuditEvent() {
        when(plans.existsByAgencyIdAndCodeIgnoreCase("AGENCY-A", "RP-2026")).thenReturn(false);
        when(plans.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("constraint"));

        assertThatThrownBy(() -> service.create("AGENCY-A", new CreatePlan("RP-2026", "Plan",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null), "correlation"))
                .isInstanceOf(DataIntegrityViolationException.class);
        verifyNoInteractions(audit);
    }

    @Test
    void planApprovalRejectsAPlanWhenAnyActiveVacancyIsNoLongerSubmitted() {
        RecruitmentPlan plan = plan("plan-1");
        plan.submit("submitter", java.time.Instant.parse("2026-08-29T00:00:00Z"));
        VacancyRequest submitted = mock(VacancyRequest.class);
        VacancyRequest returned = mock(VacancyRequest.class);
        when(submitted.isActive()).thenReturn(true);
        when(submitted.getStatus()).thenReturn(VacancyRequestStatus.SUBMITTED);
        when(returned.isActive()).thenReturn(true);
        when(returned.getStatus()).thenReturn(VacancyRequestStatus.RETURNED);
        when(plans.findByIdAndAgencyId("plan-1", "AGENCY-A")).thenReturn(Optional.of(plan));
        when(vacancies.findByPlanIdAndAgencyIdOrderByCreatedAtAsc("plan-1", "AGENCY-A"))
                .thenReturn(List.of(submitted, returned));

        assertThatThrownBy(() -> service.approvePlan("AGENCY-A", "plan-1",
                new com.primehr.rsp.api.RspPlanningDtos.Transition(0L, null),
                "Bearer token", false, "correlation"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Every active vacancy request");

        verifyNoInteractions(administrative, humanResource, profiles, audit);
        verify(plans, never()).saveAndFlush(any());
    }

    private static RecruitmentPlan plan(String id) {
        RecruitmentPlan plan = new RecruitmentPlan("AGENCY-A", "RP-2026", "Plan",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        ReflectionTestUtils.setField(plan, "id", id);
        return plan;
    }

    private static SaveVacancy vacancy(Long recordVersion) {
        return new SaveVacancy(VacancyType.ACTUAL, 3L, 2L, null, null, null, null,
                "HIGH", null, "Continuity", recordVersion);
    }
}
