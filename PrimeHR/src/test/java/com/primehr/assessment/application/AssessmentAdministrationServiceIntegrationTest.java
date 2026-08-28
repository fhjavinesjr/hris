package com.primehr.assessment.application;

import com.primehr.assessment.api.AssessmentDtos.*;
import com.primehr.assessment.domain.AssessmentMethod;
import com.primehr.integration.humanresource.*;
import com.primehr.positionprofile.domain.*;
import com.primehr.positionprofile.infrastructure.PositionProfileRepository;
import com.primehr.shared.exception.OptimisticConflictException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AssessmentAdministrationServiceIntegrationTest {
    @Autowired AssessmentAdministrationService service;
    @Autowired PositionProfileRepository profiles;
    @MockBean HumanResourceAssessmentSubjectClient subjects;

    @BeforeEach void actor() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("assessment-admin", null, List.of()));
    }
    @AfterEach void clearActor() { SecurityContextHolder.clearContext(); }

    @Test
    void completeDraftAdministrationSnapshotsAuthoritativeFactsAndRejectsStaleMutation() {
        PositionTargetSnapshot target = new PositionTargetSnapshot(PositionTargetType.JOB_POSITION, 14L,
                14L, "Accountant III", 19L, 1L, null, null, "p".repeat(64), Instant.now());
        PositionProfile profile = profiles.saveAndFlush(PositionProfile.draft("TEST-AGENCY", target,
                "Accountant III Profile", null, LocalDate.of(2026, 1, 1), null));
        profile.submit("submitter", Instant.now(), target);
        profile.approve("approver", Instant.now(), target);
        profile = profiles.saveAndFlush(profile);

        CycleResponse cycle = service.createCycle("TEST-AGENCY", new CreateCycleRequest(
                "FY2026", "FY 2026", null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)), "c1");
        ToolResponse tool = service.createTool("TEST-AGENCY", cycle.id(), new CreateToolRequest(
                profile.getId(), "Annual tool", "Draft only", List.of(
                new MethodRequest(AssessmentMethod.SELF_ASSESSMENT, true),
                new MethodRequest(AssessmentMethod.IMMEDIATE_SUPERVISOR, false))), "c2");
        when(subjects.get(anyLong(), anyString())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return new HumanResourceAssessmentSubject(id, id == 1L ? "001" : "2026002",
                    id == 1L ? "Ferdinand Javines" : "Vicky Luces", true, 100L + id,
                    LocalDateTime.of(2026, 7, 1, 8, 0), 14L, null,
                    (id == 1L ? "s" : "a").repeat(64), LocalDateTime.of(2026, 8, 1, 9, 0), Instant.now());
        });

        CaseResponse assessmentCase = service.addSubject("TEST-AGENCY", tool.id(),
                new AddSubjectRequest(1L, tool.recordVersion()), "Bearer token", "c3");
        long originalCaseVersion = assessmentCase.recordVersion();
        assessmentCase = service.addAssessor("TEST-AGENCY", assessmentCase.id(),
                new AddAssessorRequest(AssessmentMethod.SELF_ASSESSMENT, 1L, null,
                        assessmentCase.recordVersion()), "Bearer token", "c4");
        assessmentCase = service.addAssessor("TEST-AGENCY", assessmentCase.id(),
                new AddAssessorRequest(AssessmentMethod.IMMEDIATE_SUPERVISOR, 2L,
                        "Explicit HR assignment; no inferred supervisor", assessmentCase.recordVersion()),
                "Bearer token", "c5");

        assertThat(assessmentCase.subjectEmployeeNo()).isEqualTo("001");
        assertThat(assessmentCase.jobPositionId()).isEqualTo(14L);
        assertThat(assessmentCase.assessors()).extracting(AssignmentResponse::method)
                .containsExactly(AssessmentMethod.SELF_ASSESSMENT, AssessmentMethod.IMMEDIATE_SUPERVISOR);
        String caseId = assessmentCase.id();
        assertThatThrownBy(() -> service.addAssessor("TEST-AGENCY", caseId,
                new AddAssessorRequest(AssessmentMethod.IMMEDIATE_SUPERVISOR, 3L, "Stale attempt",
                        originalCaseVersion), "Bearer token", "stale"))
                .isInstanceOf(OptimisticConflictException.class);
    }
}
