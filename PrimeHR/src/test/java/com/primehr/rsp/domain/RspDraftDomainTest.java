package com.primehr.rsp.domain;

import com.primehr.integration.administrative.AdministrativeRspPositionSource;
import com.primehr.integration.humanresource.HumanResourcePlantillaOccupancy;
import com.primehr.positionprofile.api.PositionProfileResponse;
import com.primehr.positionprofile.domain.PositionProfileStatus;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RspDraftDomainTest {

    @Test
    void recruitmentPlanRequiresAValidPeriodAndCannotMutateAfterArchive() {
        assertThatThrownBy(() -> new RecruitmentPlan("AGENCY", "RP-1", "Plan",
                LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1), null))
                .isInstanceOf(IllegalArgumentException.class);

        RecruitmentPlan plan = plan();
        plan.archive();
        assertThatThrownBy(() -> plan.update("Changed", LocalDate.now(), LocalDate.now(), null))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
    }

    @Test
    void actualVacancyCannotOverrideAuthoritativeOccupancy() {
        assertThatThrownBy(() -> new VacancyRequest("AGENCY", plan(), VacancyType.ACTUAL,
                null, null, null, null, "HIGH", null, "Required",
                administrative(), occupancy(true), profile()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("occupied Plantilla");
    }

    @Test
    void anticipatedVacancyRequiresFutureDateReasonAndExplanation() {
        assertThatThrownBy(() -> new VacancyRequest("AGENCY", plan(), VacancyType.ANTICIPATED,
                LocalDate.now(), "RETIREMENT", "Expected retirement", "Authority", "HIGH", null,
                "Required", administrative(), occupancy(true), profile()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
        assertThatThrownBy(() -> new VacancyRequest("AGENCY", plan(), VacancyType.ANTICIPATED,
                LocalDate.now().plusDays(10), " ", "Expected retirement", "Authority", "HIGH", null,
                "Required", administrative(), occupancy(true), profile()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void draftStoresExactMinimumSourceAndVersionSnapshots() {
        VacancyRequest vacancy = new VacancyRequest("AGENCY", plan(), VacancyType.ANTICIPATED,
                LocalDate.now().plusDays(10), "RETIREMENT", "Expected retirement", "Authority",
                "HIGH", LocalDate.now().plusDays(30), "Continuity", administrative(), occupancy(true),
                profile());

        assertThat(vacancy.getPlantillaId()).isEqualTo(3L);
        assertThat(vacancy.getQualificationStandardVersion()).isEqualTo(2);
        assertThat(vacancy.getPositionProfileDefinitionVersion()).isEqualTo(4);
        assertThat(vacancy.getAdministrativeFingerprint()).hasSize(64);
        assertThat(vacancy.getHrmFingerprint()).hasSize(64);
        assertThat(vacancy.getActiveAppointmentId()).isEqualTo(10L);
        assertThat(vacancy.getOccupancyAssumptionDate())
                .isEqualTo(LocalDateTime.of(2026, 1, 2, 8, 0));
    }

    @Test
    void planAndVacancyFollowSubmitReturnApproveAndAuthorizeLifecycles() {
        RecruitmentPlan plan = plan();
        plan.submit("submitter", Instant.now());
        assertThat(plan.getStatus()).isEqualTo(RecruitmentPlanStatus.SUBMITTED);
        plan.returnSubmission();
        assertThat(plan.getStatus()).isEqualTo(RecruitmentPlanStatus.RETURNED);
        plan.submit("submitter", Instant.now());
        plan.approve("approver", Instant.now());
        assertThat(plan.getStatus()).isEqualTo(RecruitmentPlanStatus.APPROVED);

        VacancyRequest vacancy = new VacancyRequest("AGENCY", plan, VacancyType.ACTUAL,
                null, null, null, null, "HIGH", null, "Required",
                administrative(), occupancy(false), profile());
        vacancy.submit("submitter", Instant.now());
        vacancy.returnSubmission();
        vacancy.submit("submitter", Instant.now());
        vacancy.authorize("approver", Instant.now());

        assertThat(vacancy.getStatus()).isEqualTo(VacancyRequestStatus.AUTHORIZED);
        assertThatThrownBy(() -> vacancy.update(VacancyType.ACTUAL, null, null, null,
                null, "LOW", null, "Changed", administrative(), occupancy(false), profile()))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
    }

    @Test
    void publicationUsesImmutableSourceSnapshotAndEnforcesLifecycle() {
        VacancyRequest vacancy = authorizedVacancy();
        VacancyPublication publication = new VacancyPublication("AGENCY", vacancy,
                VacancyVisibility.BOTH, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15),
                "Submit the required documents", "Contact HR", "Vacancy notice", Instant.now());

        assertThat(publication.getQualificationStandardVersion()).isEqualTo(2);
        assertThat(publication.getPositionProfileDefinitionVersion()).isEqualTo(4);
        assertThat(publication.getPlaceOfAssignment()).isEqualTo("Finance");
        publication.submit("submitter", Instant.now());
        assertThatThrownBy(() -> publication.publish("publisher", Instant.now()))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
        publication.approve("approver", Instant.now());
        publication.publish("publisher", Instant.now());
        publication.close("closer", Instant.now());
        assertThat(publication.getStatus()).isEqualTo(VacancyPublicationStatus.CLOSED);
    }

    @Test
    void terminalVacancyAndPublicationTransitionsRejectFurtherMutation() {
        RecruitmentPlan approvedPlan = plan();
        approvedPlan.submit("submitter", Instant.now());
        approvedPlan.approve("approver", Instant.now());
        VacancyRequest declined = new VacancyRequest("AGENCY", approvedPlan, VacancyType.ACTUAL,
                null, null, null, null, "HIGH", null, "Required",
                administrative(), occupancy(false), profile());
        declined.submit("submitter", Instant.now());
        declined.decline("approver", Instant.now());
        assertThat(declined.getStatus()).isEqualTo(VacancyRequestStatus.DECLINED);
        assertThatThrownBy(() -> declined.submit("submitter", Instant.now()))
                .isInstanceOf(IllegalLifecycleTransitionException.class);

        VacancyPublication cancelled = new VacancyPublication("AGENCY", authorizedVacancy(),
                VacancyVisibility.INTERNAL, LocalDate.now(), LocalDate.now().plusDays(5),
                "Instructions", "Contact", "Notice", Instant.now());
        cancelled.submit("submitter", Instant.now());
        cancelled.approve("approver", Instant.now());
        cancelled.cancel("canceller", Instant.now());
        assertThat(cancelled.getStatus()).isEqualTo(VacancyPublicationStatus.CANCELLED);
        assertThatThrownBy(() -> cancelled.close("closer", Instant.now()))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
    }

    private static VacancyRequest authorizedVacancy() {
        RecruitmentPlan plan = plan();
        plan.submit("submitter", Instant.now());
        plan.approve("approver", Instant.now());
        VacancyRequest vacancy = new VacancyRequest("AGENCY", plan, VacancyType.ACTUAL,
                null, null, null, null, "HIGH", null, "Required",
                administrative(), occupancy(false), profile());
        vacancy.submit("submitter", Instant.now());
        vacancy.authorize("approver", Instant.now());
        return vacancy;
    }

    private static RecruitmentPlan plan() {
        return new RecruitmentPlan("AGENCY", "RP-2026", "Recruitment Plan",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
    }

    private static AdministrativeRspPositionSource administrative() {
        return new AdministrativeRspPositionSource(3L, "Plantilla #3", 14L, "Accountant III",
                19L, 1L, 2L, "FIN", "Finance", 7L, 2,
                "Bachelor degree", "8 hours", "2 years", "RA 1080", null, "CSC MC",
                LocalDate.of(2026, 1, 1), null, "a".repeat(64), Instant.now());
    }

    private static HumanResourcePlantillaOccupancy occupancy(boolean occupied) {
        return new HumanResourcePlantillaOccupancy(3L, occupied, occupied ? 10L : null,
                occupied ? LocalDateTime.of(2026, 1, 2, 8, 0) : null,
                "b".repeat(64), Instant.now());
    }

    private static PositionProfileResponse profile() {
        return new PositionProfileResponse("profile-1", "Profile", null, PositionProfileStatus.ACTIVE,
                4, null, LocalDate.of(2026, 1, 1), null, 3L, 8L,
                "submitter", Instant.now(), "approver", Instant.now(), null, List.of());
    }
}
