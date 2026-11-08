package com.primehr.evidence;

import com.primehr.evidence.api.EvidenceAssessmentDtos.*;
import com.primehr.evidence.api.EvidenceAssessmentDtos;
import com.primehr.evidence.api.EvidenceFrameworkDtos.*;
import com.primehr.evidence.api.EvidenceReadinessDtos.SignOffInput;
import com.primehr.evidence.application.EvidenceAssessmentService;
import com.primehr.evidence.application.EvidenceFrameworkService;
import com.primehr.evidence.application.EvidenceLiveSourceVerifier;
import com.primehr.evidence.application.EvidenceReadinessService;
import com.primehr.evidence.domain.EvidenceFinding;
import com.primehr.evidence.domain.EvidenceRequirement.EvidenceType;
import com.primehr.integration.administrative.AdministrativePerformancePlanningClient;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.integration.humanresource.HumanResourceAssessmentSubject;
import com.primehr.integration.humanresource.HumanResourceAssessmentSubjectClient;
import com.primehr.integration.humanresource.HumanResourceEmployeeDirectoryClient;
import com.primehr.integration.humanresource.HumanResourceEmployeeDirectoryEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("flyway-h2")
@Transactional
class EvidenceAssessmentServiceIntegrationTest {

    @Autowired EvidenceFrameworkService frameworks;
    @Autowired EvidenceAssessmentService assessments;
    @Autowired EvidenceReadinessService readiness;
    @Autowired com.primehr.notification.infrastructure.PrimeHrNotificationRepository notifications;
    @MockBean HumanResourceEmployeeDirectoryClient directory;
    @MockBean HumanResourceAssessmentSubjectClient subjects;
    @MockBean AdministrativePerformancePlanningClient administrative;
    @MockBean EvidenceLiveSourceVerifier liveSources;

    @BeforeEach
    void authoritativeSources() {
        when(directory.require(anyString(), anyString())).thenAnswer(invocation -> {
            String employeeNo = invocation.getArgument(0);
            long id = "OWNER-1".equals(employeeNo) ? 1L : 2L;
            return new HumanResourceEmployeeDirectoryEntry(id, employeeNo,
                    "OWNER-1".equals(employeeNo) ? "Evidence" : "Independent",
                    "OWNER-1".equals(employeeNo) ? "Owner" : "Validator", null, "staff");
        });
        when(subjects.get(anyLong(), anyString())).thenAnswer(invocation -> {
            long id = invocation.getArgument(0);
            String employeeNo = id == 1L ? "OWNER-1" : "VALIDATOR-1";
            return new HumanResourceAssessmentSubject(id, employeeNo,
                    id == 1L ? "Evidence Owner" : "Independent Validator", true, id * 10,
                    LocalDateTime.of(2020, 1, 1, 8, 0), 100L + id, 200L + id,
                    "person-fingerprint-" + id, LocalDateTime.of(2026, 1, 1, 8, 0), Instant.now());
        });
        when(administrative.organization("BUSINESS_UNIT", 10L, "token"))
                .thenReturn(new AdministrativePerformancePlanningClient.OrganizationTarget(
                        "BUSINESS_UNIT", 10L, "HR", "Human Resource", 1L, "Corporate Services",
                        "organization-fingerprint", Instant.now()));
        actor("admin");
    }

    @AfterEach
    void clearActor() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void humanAssessmentFindingsGapsAndCorrectiveActionsKeepIndependentDecisionHistory() {
        var draft = frameworks.create("A", new FrameworkInput("PRIME-HRM", "PRIME-HRM Internal Readiness",
                null, "CSC", "MC reference", "Agency office order",
                "Internal self-assessment only; not an official CSC determination", null), "corr");
        var content = frameworks.replaceContent("A", draft.id(), new ContentInput(draft.recordVersion(),
                List.of(new PillarInput("HRG", "HR Governance", null, 1)),
                List.of(new LevelInput("L1", "Level 1", "Reference only", 1)),
                List.of(new IndicatorInput("HRG", "IND-1", "Policy governance", null,
                        "Human review is required", 1)),
                List.of(new RequirementInput("IND-1", "REQ-1", "Approved policy",
                        "Provide approved policy evidence", EvidenceType.POLICY, 12, 365, true, false, 1))), "corr");
        var framework = frameworks.publish("A", draft.id(), new Publish(content.recordVersion(),
                LocalDate.of(2026, 1, 1), null, "Approved for internal assessment"), "corr");

        var cycle = assessments.createCycle("A", new CycleInput("2026", "2026 Assessment", null,
                framework.id(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                LocalDate.of(2027, 1, 31), null), "corr");
        cycle = assessments.transitionCycle("A", cycle.id(),
                new EvidenceAssessmentDtos.Transition(cycle.recordVersion(), "Open approved cycle"), "open", "corr");

        actor("OWNER-1");
        var assessment = assessments.createCase("A", new CaseInput(cycle.id(), "HR-2026", "HR Evidence Case",
                "BUSINESS_UNIT", 10L, "OWNER-1", "VALIDATOR-1", null), "token", "OWNER-1",
                PermissionDataScope.ASSIGNED_RECORDS, "corr");
        assessment = assessments.submit("A", assessment.id(),
                new EvidenceAssessmentDtos.Transition(assessment.recordVersion(), "Evidence packet ready"), "OWNER-1",
                PermissionDataScope.ASSIGNED_RECORDS, "corr");
        assertThat(activeActions(com.primehr.notification.domain.PrimeHrNotification.SourceDomain.EVIDENCE,
                "EVIDENCE_ASSESSMENT_CASE", assessment.id())).singleElement()
                .satisfies(n -> assertThat(n.getRecipientEmployeeNo()).isEqualTo("VALIDATOR-1"));

        actor("VALIDATOR-1");
        String requirementId = framework.requirements().get(0).id();
        assessment = assessments.findings("A", assessment.id(), new FindingsInput(assessment.recordVersion(),
                List.of(new FindingInput(requirementId, EvidenceFinding.Outcome.NOT_MET,
                        "The approved policy is not yet available", null))), "VALIDATOR-1",
                PermissionDataScope.ASSIGNED_RECORDS, "corr");
        var currentFinding = assessment.findings().stream().filter(FindingResponse::currentRecord).findFirst().orElseThrow();
        var gap = assessments.createGap("A", assessment.id(), new GapInput(assessment.recordVersion(),
                requirementId, currentFinding.id(), "Approved policy is missing", "HIGH"), "VALIDATOR-1",
                PermissionDataScope.ASSIGNED_RECORDS, "corr");
        var action = assessments.createAction("A", assessment.id(), new ActionInput(assessment.recordVersion(),
                gap.id(), "Draft and approve the missing policy", "OWNER-1", LocalDate.of(2026, 6, 30)),
                "token", "VALIDATOR-1", PermissionDataScope.ASSIGNED_RECORDS, "corr");
        assertThat(activeActions(com.primehr.notification.domain.PrimeHrNotification.SourceDomain.EVIDENCE,
                "EVIDENCE_CORRECTIVE_ACTION", action.id())).singleElement()
                .satisfies(n -> assertThat(n.getRecipientEmployeeNo()).isEqualTo("OWNER-1"));

        actor("OWNER-1");
        action = assessments.transitionAction("A", assessment.id(), action.id(),
                new EvidenceAssessmentDtos.Transition(action.recordVersion(), "Policy drafting started"), "start", "OWNER-1",
                PermissionDataScope.ASSIGNED_RECORDS, "corr");
        action = assessments.submitAction("A", assessment.id(), action.id(),
                new ActionSubmit(action.recordVersion(), "Policy approved", "document-control-reference"),
                "OWNER-1", PermissionDataScope.ASSIGNED_RECORDS, "corr");
        assertThat(activeActions(com.primehr.notification.domain.PrimeHrNotification.SourceDomain.EVIDENCE,
                "EVIDENCE_CORRECTIVE_ACTION", action.id())).singleElement()
                .satisfies(n -> assertThat(n.getRecipientEmployeeNo()).isEqualTo("VALIDATOR-1"));

        actor("VALIDATOR-1");
        action = assessments.transitionAction("A", assessment.id(), action.id(),
                new EvidenceAssessmentDtos.Transition(action.recordVersion(), "Approved document independently checked"), "verify",
                "VALIDATOR-1", PermissionDataScope.ASSIGNED_RECORDS, "corr");
        assessment = assessments.finalizeCase("A", assessment.id(),
                new EvidenceAssessmentDtos.Transition(assessment.recordVersion(), "Internal validation complete"), "VALIDATOR-1",
                PermissionDataScope.ASSIGNED_RECORDS, "corr");

        assertThat(action.status()).isEqualTo("VERIFIED");
        assertThat(assessment.status()).isEqualTo("FINALIZED");
        assertThat(activeActions(com.primehr.notification.domain.PrimeHrNotification.SourceDomain.EVIDENCE,
                "EVIDENCE_CORRECTIVE_ACTION", action.id())).isEmpty();
        assertThat(activeActions(com.primehr.notification.domain.PrimeHrNotification.SourceDomain.EVIDENCE,
                "EVIDENCE_ASSESSMENT_CASE", assessment.id())).isEmpty();
        assertThat(notifications.feed("A", com.primehr.notification.domain.PrimeHrNotification.recipientKey("OWNER-1"),
                false, true, org.springframework.data.domain.PageRequest.of(0, 20)).getContent())
                .filteredOn(n -> n.getEventCode()
                        == com.primehr.notification.domain.PrimeHrNotification.EventCode.OUTCOME_AVAILABLE)
                .extracting(com.primehr.notification.domain.PrimeHrNotification::getSourceAggregateId)
                .contains(action.id(), assessment.id());
        assertThat(assessment.history()).extracting(EventResponse::eventType)
                .contains("CREATE", "SUBMIT", "RECORD_FINDINGS", "OPEN", "CREATE", "START", "VERIFY", "FINALIZE");

        var snapshot = readiness.signOff("A", assessment.id(), new SignOffInput(
                        assessment.recordVersion(), null, null, "Validator signs the internal facts",
                        LocalDate.of(2026, 12, 31)), "VALIDATOR-1", PermissionDataScope.ASSIGNED_RECORDS, "corr");
        assertThat(snapshot.snapshotRevision()).isOne();
        assertThat(snapshot.signedBy()).isEqualTo("VALIDATOR-1");
        assertThat(snapshot.disclaimer()).contains("not an official CSC PRIME-ESG score");
        assertThat(readiness.snapshots("A", assessment.id(), "VALIDATOR-1",
                PermissionDataScope.ASSIGNED_RECORDS)).containsExactly(snapshot);
        String finalizedCaseId = assessment.id();
        long finalizedCaseVersion = assessment.recordVersion();
        assertThatThrownBy(() -> readiness.signOff("A", finalizedCaseId, new SignOffInput(
                        finalizedCaseVersion, null, null, "Correction without lineage",
                        LocalDate.of(2026, 12, 31)), "VALIDATOR-1", PermissionDataScope.ASSIGNED_RECORDS, "corr"))
                .hasMessageContaining("supersede the latest");
    }

    private void actor(String employeeNo) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(employeeNo, null, List.of()));
    }

    private java.util.List<com.primehr.notification.domain.PrimeHrNotification> activeActions(
            com.primehr.notification.domain.PrimeHrNotification.SourceDomain domain,
            String aggregateType, String aggregateId) {
        return notifications.findByAgencyIdAndSourceDomainAndSourceAggregateTypeAndSourceAggregateIdAndKindAndActionState(
                "A", domain, aggregateType, aggregateId,
                com.primehr.notification.domain.PrimeHrNotification.Kind.ACTION_REQUIRED,
                com.primehr.notification.domain.PrimeHrNotification.ActionState.ACTIVE);
    }
}
