package com.primehr.rsp.application;

import com.primehr.integration.administrative.AdministrativeRspPositionSource;
import com.primehr.integration.administrative.AdministrativeRspPositionSourceClient;
import com.primehr.integration.humanresource.HumanResourcePlantillaOccupancy;
import com.primehr.integration.humanresource.HumanResourcePlantillaOccupancyClient;
import com.primehr.positionprofile.api.PositionProfileResolutionResponse;
import com.primehr.positionprofile.api.PositionProfileResponse;
import com.primehr.positionprofile.application.PositionProfileAdminService;
import com.primehr.positionprofile.domain.PositionProfileStatus;
import com.primehr.positionprofile.domain.PositionTargetType;
import com.primehr.rsp.api.RspPublicationDtos.PublicationTransition;
import com.primehr.rsp.domain.RecruitmentPlan;
import com.primehr.rsp.domain.VacancyPublication;
import com.primehr.rsp.domain.VacancyType;
import com.primehr.rsp.domain.VacancyVisibility;
import com.primehr.rsp.domain.VacancyRequest;
import com.primehr.rsp.infrastructure.VacancyPublicationChannelRepository;
import com.primehr.rsp.infrastructure.VacancyPublicationRepository;
import com.primehr.rsp.infrastructure.VacancyPublicationRequirementRepository;
import com.primehr.rsp.infrastructure.VacancyRequestRepository;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.PublicationConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RspPublicationServiceImplTest {
    private final VacancyPublicationRepository publications = mock(VacancyPublicationRepository.class);
    private final VacancyPublicationChannelRepository channels = mock(VacancyPublicationChannelRepository.class);
    private final VacancyPublicationRequirementRepository requirements =
            mock(VacancyPublicationRequirementRepository.class);
    private final VacancyRequestRepository vacancies = mock(VacancyRequestRepository.class);
    private final AdministrativeRspPositionSourceClient administrative =
            mock(AdministrativeRspPositionSourceClient.class);
    private final HumanResourcePlantillaOccupancyClient humanResource =
            mock(HumanResourcePlantillaOccupancyClient.class);
    private final PositionProfileAdminService profiles = mock(PositionProfileAdminService.class);
    private final PrimeHrAuditService audit = mock(PrimeHrAuditService.class);
    private final RspPublicationServiceImpl service = new RspPublicationServiceImpl(publications, channels,
            requirements, vacancies, administrative, humanResource, profiles, audit);

    private VacancyPublication publication;

    @BeforeEach
    void setUp() {
        VacancyRequest vacancy = authorizedVacancy();
        publication = new VacancyPublication("AGENCY", vacancy, VacancyVisibility.BOTH,
                LocalDate.now(), LocalDate.now().plusDays(10), "Instructions", "Contact HR",
                "Notice", Instant.now());
        ReflectionTestUtils.setField(publication, "id", "publication-1");
        when(publications.findByIdAndAgencyId("publication-1", "AGENCY"))
                .thenReturn(Optional.of(publication));
        when(publications.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(channels.findByPublicationIdAndAgencyIdOrderByPublicationDateAscChannelNameAsc(
                "publication-1", "AGENCY")).thenReturn(List.of(
                new com.primehr.rsp.domain.VacancyPublicationChannel("AGENCY", publication,
                        "Agency website", LocalDate.now(), null)));
        when(requirements.findByPublicationIdAndAgencyIdOrderByDisplayOrderAscIdAsc(
                "publication-1", "AGENCY")).thenReturn(List.of());
        mockSources(vacancy);
    }

    @Test
    void submitterCannotApproveOwnPublicationWithoutAdministratorOverride() {
        when(audit.currentActor()).thenReturn("submitter");
        service.submit("AGENCY", "publication-1", new PublicationTransition(0L, null),
                "Bearer token", "correlation");
        clearInvocations(publications);

        assertThatThrownBy(() -> service.approve("AGENCY", "publication-1",
                new PublicationTransition(0L, null), "Bearer token", false, "correlation"))
                .isInstanceOf(AccessDeniedException.class);
        verify(publications, never()).saveAndFlush(any(VacancyPublication.class));
    }

    @Test
    void administratorSelfApprovalRequiresReasonAndIsAuditedAsOverride() {
        when(audit.currentActor()).thenReturn("submitter");
        service.submit("AGENCY", "publication-1", new PublicationTransition(0L, null),
                "Bearer token", "correlation");
        service.approve("AGENCY", "publication-1", new PublicationTransition(0L, "Emergency override"),
                "Bearer token", true, "correlation");

        verify(audit).record(org.mockito.ArgumentMatchers.eq("AGENCY"),
                org.mockito.ArgumentMatchers.eq("ADMIN_APPROVE_PUBLICATION"),
                org.mockito.ArgumentMatchers.eq("RSP_VACANCY_PUBLICATION"),
                org.mockito.ArgumentMatchers.eq("publication-1"), any(), any(), any(), any(),
                org.mockito.ArgumentMatchers.eq("Emergency override"),
                org.mockito.ArgumentMatchers.eq("correlation"));
    }

    @Test
    void sourceChangeBlocksApprovalBeforeMutationOrAudit() {
        publication.submit("submitter", Instant.now());
        AdministrativeRspPositionSource changed = administrative();
        changed = new AdministrativeRspPositionSource(changed.plantillaId(), changed.plantillaName(),
                changed.jobPositionId(), changed.jobPositionName(), changed.salaryGrade(), changed.salaryStep(),
                changed.businessUnitId(), changed.businessUnitCode(), changed.businessUnitName(),
                changed.qualificationStandardId(), changed.qualificationStandardVersion(),
                changed.education(), changed.training(), changed.experience(), changed.eligibility(),
                changed.licenseRequirement(), changed.sourceBasis(),
                changed.qualificationEffectiveFrom(), changed.qualificationEffectiveTo(),
                "c".repeat(64), changed.fetchedAt());
        when(administrative.get(3L, 2L, LocalDate.now(), "Bearer token")).thenReturn(changed);
        clearInvocations(publications, audit);

        assertThatThrownBy(() -> service.approve("AGENCY", "publication-1",
                new PublicationTransition(0L, null), "Bearer token", false, "correlation"))
                .isInstanceOf(PublicationConflictException.class);
        verify(publications, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test
    void persistenceFailureCannotEmitDecisionAudit() {
        publication.submit("submitter", Instant.now());
        when(audit.currentActor()).thenReturn("approver");
        when(publications.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("constraint"));
        clearInvocations(audit);

        assertThatThrownBy(() -> service.approve("AGENCY", "publication-1",
                new PublicationTransition(0L, null), "Bearer token", false, "correlation"))
                .isInstanceOf(DataIntegrityViolationException.class);
        verify(audit, never()).record(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    private void mockSources(VacancyRequest vacancy) {
        AdministrativeRspPositionSource source = administrative();
        HumanResourcePlantillaOccupancy occupancy = occupancy(false);
        PositionProfileResponse profile = profile();
        when(administrative.get(vacancy.getPlantillaId(), vacancy.getBusinessUnitId(),
                LocalDate.now(), "Bearer token")).thenReturn(source);
        when(humanResource.get(vacancy.getPlantillaId(), "Bearer token")).thenReturn(occupancy);
        when(profiles.resolve("AGENCY", source.jobPositionId(), vacancy.getPlantillaId(), LocalDate.now()))
                .thenReturn(new PositionProfileResolutionResponse(LocalDate.now(),
                        PositionTargetType.PLANTILLA, profile));
    }

    private static VacancyRequest authorizedVacancy() {
        RecruitmentPlan plan = new RecruitmentPlan("AGENCY", "RP-2026", "Plan",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        ReflectionTestUtils.setField(plan, "id", "plan-1");
        plan.submit("submitter", Instant.now());
        plan.approve("approver", Instant.now());
        VacancyRequest vacancy = new VacancyRequest("AGENCY", plan, VacancyType.ACTUAL,
                null, null, null, null, "HIGH", null, "Required",
                administrative(), occupancy(false), profile());
        ReflectionTestUtils.setField(vacancy, "id", "vacancy-1");
        vacancy.submit("submitter", Instant.now());
        vacancy.authorize("approver", Instant.now());
        return vacancy;
    }

    private static AdministrativeRspPositionSource administrative() {
        return new AdministrativeRspPositionSource(3L, "Plantilla #3", 14L, "Accountant III",
                19L, 1L, 2L, "FIN", "Finance", 7L, 2,
                "Bachelor degree", "8 hours", "2 years", "RA 1080", null, "CSC MC",
                LocalDate.of(2026, 1, 1), null, "a".repeat(64), Instant.now());
    }

    private static HumanResourcePlantillaOccupancy occupancy(boolean occupied) {
        return new HumanResourcePlantillaOccupancy(3L, occupied, null, null,
                "b".repeat(64), Instant.now());
    }

    private static PositionProfileResponse profile() {
        return new PositionProfileResponse("profile-1", "Profile", null, PositionProfileStatus.ACTIVE,
                4, null, LocalDate.of(2026, 1, 1), null, 3L, 8L,
                "submitter", Instant.now(), "approver", Instant.now(), null, List.of());
    }
}
