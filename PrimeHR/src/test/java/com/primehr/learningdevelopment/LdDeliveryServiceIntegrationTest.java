package com.primehr.learningdevelopment;

import com.primehr.integration.administrative.*;
import com.primehr.integration.humanresource.*;
import com.primehr.learningdevelopment.api.LdFoundationDtos.*;
import com.primehr.learningdevelopment.application.LdFoundationService;
import com.primehr.learningdevelopment.completion.api.LdCompletionDtos;
import com.primehr.learningdevelopment.completion.application.LdCompletionService;
import com.primehr.learningdevelopment.completion.domain.LdCompletionEvidence;
import com.primehr.learningdevelopment.completion.domain.LdSessionAttendance;
import com.primehr.learningdevelopment.delivery.api.LdDeliveryDtos.*;
import com.primehr.learningdevelopment.delivery.application.LdDeliveryService;
import com.primehr.learningdevelopment.delivery.domain.*;
import com.primehr.learningdevelopment.domain.*;
import com.primehr.learningdevelopment.plan.api.LdPlanDtos.*;
import com.primehr.learningdevelopment.plan.application.LdPlanService;
import com.primehr.learningdevelopment.plan.domain.LearningNeedSourceType;
import com.primehr.learningdevelopment.participation.api.LdParticipationDtos;
import com.primehr.learningdevelopment.participation.application.LdParticipationService;
import com.primehr.learningdevelopment.participation.domain.LdNomination;
import com.primehr.rsp.applicant.storage.DocumentStorage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LdDeliveryServiceIntegrationTest {
    @Autowired LdFoundationService foundation;
    @Autowired LdPlanService planning;
    @Autowired LdDeliveryService delivery;
    @Autowired LdParticipationService participation;
    @Autowired LdCompletionService completion;
    @MockBean HumanResourceAssessmentSubjectClient humanResource;
    @MockBean HumanResourcePerformanceParticipantClient performanceParticipants;
    @MockBean AdministrativePerformancePlanningClient administrative;
    @MockBean DocumentStorage documentStorage;

    @BeforeEach
    void authoritativeSources() {
        when(documentStorage.provider()).thenReturn("test");
        when(humanResource.get(1L, "Bearer test")).thenReturn(subject(1L, "EMP-1", 11L));
        when(humanResource.get(2L, "Bearer test")).thenReturn(subject(2L, "SUP-1", 12L));
        when(humanResource.get(3L, "Bearer test")).thenReturn(subject(3L, "APR-1", 13L));
        when(humanResource.get(4L, "Bearer test")).thenReturn(subject(4L, "EMP-4", 14L));
        when(performanceParticipants.getByEmployeeNo("SUP-1", "Bearer test"))
                .thenReturn(subject(2L, "SUP-1", 12L));
        when(administrative.organization("BUSINESS_UNIT", 41L, "Bearer test"))
                .thenReturn(new AdministrativePerformancePlanningClient.OrganizationTarget("BUSINESS_UNIT", 41L,
                        "BU-41", "Business Unit", 51L, "Area", "organization-fingerprint", Instant.now()));
        when(administrative.membership(anyLong(), eq("Bearer test"))).thenAnswer(invocation ->
                membership(invocation.getArgument(0)));
        when(administrative.approvalRoute(41L, "LEARNING_INDIVIDUAL_DEVELOPMENT_PLAN", "Bearer test"))
                .thenReturn(new AdministrativePerformancePlanningClient.ApprovalRoute(91L,
                        "LEARNING_INDIVIDUAL_DEVELOPMENT_PLAN", 2, 41L, 51L,
                        List.of(new AdministrativePerformancePlanningClient.ApprovalRouteStep(101L, 1, 2L),
                                new AdministrativePerformancePlanningClient.ApprovalRouteStep(102L, 2, 3L)),
                        "route-fingerprint", Instant.now()));
        when(administrative.approvalRoute(41L, "LEARNING_NOMINATION", "Bearer test"))
                .thenReturn(new AdministrativePerformancePlanningClient.ApprovalRoute(92L,
                        "LEARNING_NOMINATION", 2, 41L, 51L,
                        List.of(new AdministrativePerformancePlanningClient.ApprovalRouteStep(201L, 1, 2L),
                                new AdministrativePerformancePlanningClient.ApprovalRouteStep(202L, 2, 3L)),
                        "nomination-route-fingerprint", Instant.now()));
    }

    @Test
    void approvedIdpConsolidatesIntoApprovedProgramAndPublishedOffering() {
        String agency = "LD-DELIVERY";
        PolicyResponse policy = foundation.createPolicy(agency, new PolicyInput("POL-1", "L&D Policy", null,
                "CSC and agency policy", 1, 1, new BigDecimal("40"), 90, 180, true, true, true, null), null);
        policy = foundation.publishPolicy(agency, policy.id(),
                new PublishTransition(policy.recordVersion(), LocalDate.of(2027, 1, 1), null, "Approved"), null);
        InterventionResponse intervention = foundation.createIntervention(agency,
                new InterventionInput("COACH", "Structured Coaching", null, LdInterventionVersion.Category.COACHING,
                        LdInterventionVersion.Modality.BLENDED, "Demonstrated application", "Coach and LMS", null), null);
        intervention = foundation.publishIntervention(agency, intervention.id(),
                new PublishTransition(intervention.recordVersion(), LocalDate.of(2027, 1, 1), null, "Approved"), null);
        EvaluationFrameworkResponse evaluation = foundation.createEvaluationFramework(agency,
                new EvaluationFrameworkInput("EVAL", "Evaluation", null, "Agency policy", null), null);
        evaluation = foundation.replaceEvaluationLevels(agency, evaluation.id(),
                new EvaluationLevelsInput(evaluation.recordVersion(), List.of(new EvaluationLevelInput(
                        LdEvaluationLevel.Type.REACTION, true, 0, LdEvaluationLevel.ScoreMethod.PERCENTAGE,
                        new BigDecimal("80"), "Completed instrument", "Use approved form", 1))), null);
        evaluation = foundation.publishEvaluationFramework(agency, evaluation.id(),
                new PublishTransition(evaluation.recordVersion(), LocalDate.of(2027, 1, 1), null, "Approved"), null);
        ProviderResponse provider = foundation.createProvider(agency, new ProviderInput("CSC", "CSC",
                LdProvider.Type.EXTERNAL_GOVERNMENT, null, null, null), null);
        ResourcePersonResponse resource = foundation.createResourcePerson(agency, new ResourcePersonInput("RP-1",
                LdResourcePerson.Source.EXTERNAL, null, "Resource Person", provider.id(), "Coaching", "Certified",
                null, null), null);

        NeedResponse need = planning.createNeed(agency, new NeedInput(1L, LearningNeedSourceType.OTHER,
                null, null, null, null, null, null, "Develop supervisory capability", "Requires guidance",
                "Applies independently", "Completes supervised assignment", LocalDate.of(2027, 6, 30), "HIGH", null),
                "Bearer test", "EMP-1", PermissionDataScope.OWN_RECORDS, null);
        IdpResponse idp = planning.create(agency, new IdpCreate(1L, 2027, "Build capability"), "Bearer test",
                "EMP-1", PermissionDataScope.OWN_RECORDS, null);
        idp = planning.addItem(agency, idp.id(), new IdpItemInput(idp.recordVersion(), need.id(), "COACHING",
                "Attend and apply", "Observe and support", 1), "EMP-1", PermissionDataScope.OWN_RECORDS, null);
        idp = planning.submit(agency, idp.id(), new IdpTransition(idp.recordVersion(), null), "Bearer test", "EMP-1",
                PermissionDataScope.OWN_RECORDS, null);
        idp = planning.recommend(agency, idp.id(), new IdpTransition(idp.recordVersion(), "Recommended"),
                "Bearer test", "SUP-1", null);
        idp = planning.approve(agency, idp.id(), new IdpTransition(idp.recordVersion(), "Approved"),
                "Bearer test", "APR-1", null);

        PlanResponse plan = delivery.createPlan(agency, new PlanCreate(LdConsolidatedPlan.ScopeType.OFFICE,
                "BUSINESS_UNIT", 41L, policy.id(), 2027, "Office L&D Plan", "Close priority needs",
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31), new BigDecimal("100000"), "GAA", 1L),
                "Bearer test", "EMP-1", PermissionDataScope.ASSIGNED_RECORDS, null);
        plan = delivery.addPlanSource(agency, plan.id(), new PlanSourceInput(plan.recordVersion(),
                LdConsolidatedPlanSource.SourceType.APPROVED_IDP_ITEM, idp.items().get(0).id(), 1), "Bearer test",
                "EMP-1", PermissionDataScope.ASSIGNED_RECORDS, null);
        plan = delivery.submitPlan(agency, plan.id(), new Transition(plan.recordVersion(), null), "EMP-1",
                PermissionDataScope.ASSIGNED_RECORDS, null);
        PlanResponse submittedPlan = plan;
        assertThatThrownBy(() -> delivery.approvePlan(agency, submittedPlan.id(),
                new Transition(submittedPlan.recordVersion(), null), "Bearer test", "EMP-1",
                PermissionDataScope.AGENCY_WIDE, null)).isInstanceOf(AccessDeniedException.class);
        plan = delivery.approvePlan(agency, plan.id(), new Transition(plan.recordVersion(), "Approved"),
                "Bearer test", "PLAN-APP", PermissionDataScope.AGENCY_WIDE, null);
        plan = delivery.publishPlan(agency, plan.id(), new Transition(plan.recordVersion(), "Published"),
                "PLAN-PUBLISH", PermissionDataScope.AGENCY_WIDE, null);
        assertThat(plan.status()).isEqualTo("PUBLISHED");
        assertThat(plan.publishedBy()).isEqualTo("PLAN-PUBLISH");

        ProgramResponse program = delivery.createProgram(agency, new ProgramCreate(plan.id(), "PROG-1",
                "Supervisory Program", intervention.id(), evaluation.id(), "Apply supervisory capability",
                "Blended coaching", LocalDate.of(2027, 5, 1), LocalDate.of(2027, 5, 31), new BigDecimal("16"),
                new BigDecimal("50000"), "GAA", 1L), "Bearer test", "EMP-1",
                PermissionDataScope.ASSIGNED_RECORDS, null);
        program = delivery.addProgramSource(agency, program.id(), new ProgramSourceInput(program.recordVersion(),
                plan.sources().get(0).id(), 1), "EMP-1", PermissionDataScope.ASSIGNED_RECORDS, null);
        program = delivery.submitProgram(agency, program.id(), new Transition(program.recordVersion(), null),
                "EMP-1", PermissionDataScope.ASSIGNED_RECORDS, null);
        program = delivery.approveProgram(agency, program.id(), new Transition(program.recordVersion(), "Approved"),
                "Bearer test", "PROGRAM-APP", PermissionDataScope.AGENCY_WIDE, null);

        OfferingResponse offering = delivery.createOffering(agency, new OfferingCreate(program.id(), "OFF-1",
                "May Offering", LdInterventionVersion.Modality.BLENDED, "Training Room and LMS", 25, provider.id(),
                new BigDecimal("50000"), "GAA", "PHP", Instant.parse("2027-03-01T00:00:00Z"),
                Instant.parse("2027-04-01T00:00:00Z")), "EMP-1", PermissionDataScope.ASSIGNED_RECORDS, null);
        offering = delivery.replaceSessions(agency, offering.id(), new SessionsInput(offering.recordVersion(), List.of(
                new SessionInput("Workshop", OffsetDateTime.parse("2027-05-10T08:00:00+08:00"),
                        OffsetDateTime.parse("2027-05-10T17:00:00+08:00"), "Training Room", new BigDecimal("8"), 1))),
                "EMP-1", PermissionDataScope.ASSIGNED_RECORDS, null);
        offering = delivery.replaceResources(agency, offering.id(), new ResourcesInput(offering.recordVersion(),
                List.of(new ResourceInput(resource.id(), LdOfferingResource.Role.LEAD_FACILITATOR, 1))), "EMP-1",
                PermissionDataScope.ASSIGNED_RECORDS, null);
        offering = delivery.submitOffering(agency, offering.id(), new Transition(offering.recordVersion(), null),
                "EMP-1", PermissionDataScope.ASSIGNED_RECORDS, null);
        offering = delivery.approveOffering(agency, offering.id(), new Transition(offering.recordVersion(), "Approved"),
                "OFFERING-APP", PermissionDataScope.AGENCY_WIDE, null);
        offering = delivery.publishOffering(agency, offering.id(), new Transition(offering.recordVersion(), "Publish"),
                "PUBLISHER", PermissionDataScope.AGENCY_WIDE, null);

        assertThat(offering.status()).isEqualTo("PUBLISHED");
        assertThat(offering.sessions()).hasSize(1);
        assertThat(offering.resources()).singleElement().satisfies(row ->
                assertThat(row.resourceFingerprint()).isNotBlank());

        var supervisorNomination=participation.create(agency,new LdParticipationDtos.NominationCreate(offering.id(),4L,
                LdNomination.SourceType.JUSTIFIED,null,"Supervisor-observed development priority",
                BigDecimal.ZERO),"Bearer test","SUP-1",PermissionDataScope.ASSIGNED_RECORDS,null);
        assertThat(supervisorNomination.initiatorType()).isEqualTo("SUPERVISOR");
        supervisorNomination=participation.cancel(agency,supervisorNomination.id(),new LdParticipationDtos.Transition(
                supervisorNomination.recordVersion(),"No longer required"),"SUP-1",
                PermissionDataScope.ASSIGNED_RECORDS,null);
        assertThat(supervisorNomination.status()).isEqualTo("CANCELLED");

        var nomination=participation.create(agency,new LdParticipationDtos.NominationCreate(offering.id(),1L,
                LdNomination.SourceType.IDP_ITEM,idp.items().get(0).id(),"Approved development need",
                new BigDecimal("5000")),"Bearer test","EMP-1",PermissionDataScope.OWN_RECORDS,null);
        nomination=participation.submit(agency,nomination.id(),new LdParticipationDtos.Transition(
                nomination.recordVersion(),null),"Bearer test","EMP-1",PermissionDataScope.OWN_RECORDS,null);
        nomination=participation.recommend(agency,nomination.id(),new LdParticipationDtos.Transition(
                nomination.recordVersion(),"Recommended"),"Bearer test","SUP-1",null);
        nomination=participation.approve(agency,nomination.id(),new LdParticipationDtos.Transition(
                nomination.recordVersion(),"Approved"),"Bearer test","APR-1",null);
        assertThat(nomination.status()).isEqualTo("APPROVED");
        var enrollment=participation.enrollments(agency,"EMP-1",PermissionDataScope.OWN_RECORDS).get(0);
        assertThat(enrollment.status()).isEqualTo("INVITED");
        enrollment=participation.confirm(agency,enrollment.id(),new LdParticipationDtos.Transition(
                enrollment.recordVersion(),null),"Bearer test","EMP-1",null);
        assertThat(enrollment.status()).isEqualTo("CONFIRMED");
        var order=participation.finalizeTrainingOrder(agency,offering.id(),
                new LdParticipationDtos.TrainingOrderInput("TO-2027-001","Participation in May Offering",
                        "Attend all scheduled sessions","Business attire",null),"LD-ADMIN",
                PermissionDataScope.AGENCY_WIDE,null);
        assertThat(order.participants()).singleElement().satisfies(row -> {
            assertThat(row.employeeNo()).isEqualTo("EMP-1");
            assertThat(row.enrollmentStatus()).isEqualTo("CONFIRMED");
        });

        var completionCase = completion.open(agency, enrollment.id(),
                new LdCompletionDtos.OpenCaseInput(enrollment.recordVersion()), "EMP-1",
                PermissionDataScope.ASSIGNED_RECORDS, null);
        assertThat(completionCase.attendance()).singleElement().satisfies(row -> {
            assertThat(row.status()).isEqualTo("UNRECORDED");
            assertThat(row.plannedHours()).isEqualByComparingTo("8.00");
        });
        var attendance = completionCase.attendance().get(0);
        completionCase = completion.recordAttendance(agency, completionCase.id(), attendance.sessionId(),
                new LdCompletionDtos.AttendanceInput(attendance.recordVersion(),
                        LdSessionAttendance.Status.PRESENT, new BigDecimal("8"), new BigDecimal("8"),
                        "Attendance sheet verified"), "EMP-1", PermissionDataScope.ASSIGNED_RECORDS, null);
        completionCase = completion.addOutput(agency, completionCase.id(),
                new LdCompletionDtos.OutputInput(completionCase.recordVersion(), "ACTION-PLAN",
                        "Action Plan", "Document intended workplace application"), "EMP-1",
                PermissionDataScope.ASSIGNED_RECORDS, null);
        var output = completionCase.outputs().get(0);
        completion.upload(agency, completionCase.id(), LdCompletionEvidence.Type.OUTPUT, output.id(),
                null, null, null, new MockMultipartFile("file", "action-plan.pdf", "application/pdf",
                        "%PDF-1.4 action plan".getBytes()), "EMP-1", PermissionDataScope.OWN_RECORDS,
                true, null);
        completionCase = completion.get(agency, completionCase.id(), "EMP-1",
                PermissionDataScope.ASSIGNED_RECORDS);
        output = completionCase.outputs().get(0);
        completionCase = completion.decideOutput(agency, completionCase.id(), output.id(),
                new LdCompletionDtos.OutputDecision(completionCase.recordVersion(), output.recordVersion(),
                        "Evidence reviewed"), false, "EMP-1", PermissionDataScope.ASSIGNED_RECORDS, null);
        completionCase = completion.submit(agency, completionCase.id(),
                new LdCompletionDtos.CaseTransition(completionCase.recordVersion(), "Ready for verification"),
                "EMP-1", PermissionDataScope.ASSIGNED_RECORDS, null);
        assertThat(completionCase.status()).isEqualTo("FOR_VERIFICATION");
        assertThat(completion.mine(agency, "EMP-1")).extracting(LdCompletionDtos.CompletionCaseResponse::id)
                .contains(completionCase.id());
        var submittedCase = completionCase;
        assertThatThrownBy(() -> completion.recordAttendance(agency, submittedCase.id(), attendance.sessionId(),
                new LdCompletionDtos.AttendanceInput(attendance.recordVersion(),
                        LdSessionAttendance.Status.PRESENT, new BigDecimal("8"), new BigDecimal("8"), null),
                "EMP-1", PermissionDataScope.ASSIGNED_RECORDS, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("DRAFT");
        completionCase = completion.returnForChanges(agency, completionCase.id(),
                new LdCompletionDtos.CaseTransition(completionCase.recordVersion(), "Correct attendance details"),
                "LD-ADMIN", PermissionDataScope.AGENCY_WIDE, null);
        assertThat(completionCase.status()).isEqualTo("DRAFT");
    }

    private HumanResourceAssessmentSubject subject(long id, String employeeNo, long appointmentId) {
        return new HumanResourceAssessmentSubject(id, employeeNo, employeeNo, true, appointmentId,
                LocalDateTime.of(2020, 1, 1, 8, 0), 21L, 31L, "employee-fingerprint-" + id,
                LocalDateTime.now(), Instant.now());
    }

    private AdministrativePerformancePlanningClient.PersonnelMembership membership(long employeeId) {
        return new AdministrativePerformancePlanningClient.PersonnelMembership(employeeId, 41L, "BU-41",
                "Business Unit", 51L, "Area", employeeId == 2L, false, "Main",
                "organization-fingerprint-" + employeeId, Instant.now());
    }
}
