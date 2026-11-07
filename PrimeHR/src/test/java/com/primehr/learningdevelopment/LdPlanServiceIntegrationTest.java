package com.primehr.learningdevelopment;

import com.primehr.integration.administrative.*;
import com.primehr.integration.humanresource.*;
import com.primehr.learningdevelopment.plan.api.LdPlanDtos.*;
import com.primehr.learningdevelopment.plan.application.LdPlanService;
import com.primehr.learningdevelopment.plan.domain.LearningNeedSourceType;
import com.primehr.shared.exception.OptimisticConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LdPlanServiceIntegrationTest {
    @Autowired LdPlanService service;
    @MockBean HumanResourceAssessmentSubjectClient humanResource;
    @MockBean AdministrativePerformancePlanningClient administrative;

    @BeforeEach
    void sources() {
        when(humanResource.get(1L, "Bearer test")).thenReturn(subject(1L, "EMP-1", 11L));
        when(humanResource.get(2L, "Bearer test")).thenReturn(subject(2L, "SUP-1", 12L));
        when(humanResource.get(3L, "Bearer test")).thenReturn(subject(3L, "APR-1", 13L));
        when(administrative.membership(anyLong(), eq("Bearer test"))).thenAnswer(invocation ->
                membership(invocation.getArgument(0)));
        when(administrative.approvalRoute(41L, "LEARNING_INDIVIDUAL_DEVELOPMENT_PLAN", "Bearer test"))
                .thenReturn(new AdministrativePerformancePlanningClient.ApprovalRoute(91L,
                        "LEARNING_INDIVIDUAL_DEVELOPMENT_PLAN", 2, 41L, 51L,
                        List.of(new AdministrativePerformancePlanningClient.ApprovalRouteStep(101L, 1, 2L),
                                new AdministrativePerformancePlanningClient.ApprovalRouteStep(102L, 2, 3L)),
                        "route-fingerprint", Instant.now()));
    }

    @Test
    void needAndIdpFollowOwnedDraftAndSnapshotApprovalWorkflow() {
        String agency = "LD-PLAN";
        NeedResponse need = service.createNeed(agency, new NeedInput(1L, LearningNeedSourceType.OTHER,
                null, null, null, null, null, null, "Develop supervisory capability",
                "Requires guidance", "Applies independently", "Completes supervised assignment",
                LocalDate.of(2027, 6, 30), "HIGH", null), "Bearer test", "EMP-1",
                PermissionDataScope.OWN_RECORDS, "need-create");
        IdpResponse plan = service.create(agency, new IdpCreate(1L, 2027, "Build role-critical capability"),
                "Bearer test", "EMP-1", PermissionDataScope.OWN_RECORDS, "idp-create");
        plan = service.addItem(agency, plan.id(), new IdpItemInput(plan.recordVersion(), need.id(),
                "COACHING", "Attend and apply", "Observe and provide feedback", 1), "EMP-1",
                PermissionDataScope.OWN_RECORDS, "idp-item");
        assertThat(plan.items()).singleElement().satisfies(item ->
                assertThat(item.developmentObjective()).isEqualTo("Develop supervisory capability"));

        plan = service.submit(agency, plan.id(), new IdpTransition(plan.recordVersion(), null),
                "Bearer test", "EMP-1", PermissionDataScope.OWN_RECORDS, "idp-submit");
        assertThat(plan.status()).isEqualTo("SUBMITTED");
        assertThat(plan.route()).extracting(RouteStepResponse::status).containsExactly("CURRENT", "PENDING");
        plan = service.recommend(agency, plan.id(), new IdpTransition(plan.recordVersion(), "Recommended"),
                "Bearer test", "SUP-1", "idp-recommend");
        assertThat(plan.status()).isEqualTo("RECOMMENDED");
        plan = service.approve(agency, plan.id(), new IdpTransition(plan.recordVersion(), "Approved"),
                "Bearer test", "APR-1", "idp-approve");
        assertThat(plan.status()).isEqualTo("APPROVED");
        assertThat(plan.approvedBy()).isEqualTo("APR-1");
        IdpResponse approved = plan;
        assertThatThrownBy(() -> service.updateGoal(agency, approved.id(),
                new IdpGoalUpdate(approved.recordVersion(), "Changed"), "EMP-1",
                PermissionDataScope.OWN_RECORDS, "stale"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void staleNeedWriteIsRejected() {
        String agency = "LD-STALE";
        NeedInput create = new NeedInput(1L, LearningNeedSourceType.OTHER, null, null, null,
                null, null, null, "Objective", "Current", "Target", "Measure",
                LocalDate.of(2027, 1, 1), "MEDIUM", null);
        NeedResponse need = service.createNeed(agency, create, "Bearer test", "EMP-1",
                PermissionDataScope.OWN_RECORDS, null);
        NeedInput update = new NeedInput(1L, LearningNeedSourceType.OTHER, null, null, null,
                null, null, null, "Updated", "Current", "Target", "Measure",
                LocalDate.of(2027, 1, 1), "HIGH", need.recordVersion());
        service.updateNeed(agency, need.id(), update, "EMP-1", PermissionDataScope.OWN_RECORDS, null);
        assertThatThrownBy(() -> service.updateNeed(agency, need.id(), update, "EMP-1",
                PermissionDataScope.OWN_RECORDS, null)).isInstanceOf(OptimisticConflictException.class);
    }

    private HumanResourceAssessmentSubject subject(long id, String employeeNo, long appointmentId) {
        return new HumanResourceAssessmentSubject(id, employeeNo, employeeNo, true, appointmentId,
                LocalDateTime.of(2020, 1, 1, 8, 0), 21L, 31L,
                "employee-fingerprint-" + id, LocalDateTime.now(), Instant.now());
    }

    private AdministrativePerformancePlanningClient.PersonnelMembership membership(long employeeId) {
        return new AdministrativePerformancePlanningClient.PersonnelMembership(employeeId, 41L, "BU",
                "Business Unit", 51L, "Area", employeeId == 2L, false, "Main",
                "organization-fingerprint-" + employeeId, Instant.now());
    }
}
