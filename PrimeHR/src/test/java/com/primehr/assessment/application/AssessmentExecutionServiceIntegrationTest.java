package com.primehr.assessment.application;

import com.primehr.assessment.api.AssessmentDtos.*;
import com.primehr.assessment.api.AssessmentExecutionDtos.*;
import com.primehr.assessment.api.AssessmentValidationDtos.*;
import com.primehr.assessment.domain.*;
import com.primehr.assessment.infrastructure.*;
import com.primehr.competency.domain.*;
import com.primehr.competency.infrastructure.*;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.integration.humanresource.*;
import com.primehr.positionprofile.domain.*;
import com.primehr.positionprofile.infrastructure.*;
import com.primehr.shared.exception.*;
import com.primehr.shared.audit.PrimeHrAuditEventRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AssessmentExecutionServiceIntegrationTest {
    private static final String AGENCY = "EXECUTION-AGENCY";
    @Autowired AssessmentAdministrationService administration;
    @Autowired AssessmentExecutionService execution;
    @Autowired AssessmentValidationService validation;
    @Autowired CompetencyCategoryRepository categories;
    @Autowired ProficiencyScaleRepository scales;
    @Autowired CompetencyRepository competencies;
    @Autowired PositionProfileRepository profiles;
    @Autowired PositionProfileRequirementRepository requirements;
    @Autowired AssessmentRatingRepository ratings;
    @Autowired AssessorAssignmentRepository assignments;
    @Autowired AssessmentValidationRepository validations;
    @Autowired PersonCompetencyProfileRepository personProfiles;
    @Autowired PersonCompetencyResultRepository personResults;
    @Autowired PrimeHrAuditEventRepository auditEvents;
    @MockBean HumanResourceAssessmentSubjectClient subjects;

    @BeforeEach void actor() { authenticate("assessment-admin"); }
    @AfterEach void clearActor() { SecurityContextHolder.clearContext(); }

    @Test
    void assignedAssessorSavesStructuredContributionAndSubmitsOnlyWhenComplete() {
        Fixture fixture = openFixture(true);
        authenticate("001");

        assertThat(execution.mine(AGENCY, "001", 0, 20).content()).singleElement()
                .satisfies(item -> assertThat(item.assignmentId()).isEqualTo(fixture.assignmentId()));
        assertThatThrownBy(() -> execution.getAssignedWork(
                AGENCY, fixture.caseId(), "2026002", PermissionDataScope.ASSIGNED_RECORDS))
                .isInstanceOf(AccessDeniedException.class);

        AssessmentWorkResponse initial = execution.getAssignedWork(
                AGENCY, fixture.caseId(), "001", PermissionDataScope.OWN_RECORDS);
        AssessmentWorkResponse rated = execution.saveRating(AGENCY, fixture.caseId(), fixture.assignmentId(),
                fixture.competencyId(), new SaveRatingRequest(fixture.levelId(), "Observed", "Behavior notes",
                        null, initial.contributions().get(0).recordVersion(), initial.caseRecordVersion()),
                "001", PermissionDataScope.OWN_RECORDS, "rating-create");
        assertThat(rated.caseStatus()).isEqualTo(AssessmentCaseStatus.IN_PROGRESS);
        assertThat(rated.contributions().get(0).status()).isEqualTo(AssessorAssignmentStatus.IN_PROGRESS);
        assertThat(rated.contributions().get(0).ratings()).hasSize(1);

        assertThatThrownBy(() -> execution.submit(AGENCY, fixture.caseId(), fixture.assignmentId(),
                transition(rated), "001", PermissionDataScope.OWN_RECORDS, "incomplete-submit"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("structured evidence");
        assertThat(assignments.findByAgencyIdAndId(AGENCY, fixture.assignmentId()).orElseThrow().getStatus())
                .isEqualTo(AssessorAssignmentStatus.IN_PROGRESS);

        AssessmentWorkResponse evidenced = execution.createEvidence(AGENCY, fixture.caseId(),
                fixture.assignmentId(), new CreateEvidenceRequest(fixture.competencyId(), "DOCUMENT",
                        "Performance record", LocalDate.of(2026, 8, 26), "Verified source", "HRIS",
                        "REC-001", rated.contributions().get(0).recordVersion(), rated.caseRecordVersion()),
                "001", PermissionDataScope.OWN_RECORDS, "evidence-create");
        assertThat(evidenced.contributions().get(0).ratings().get(0).evidence()).hasSize(1);

        AssessmentWorkResponse submitted = execution.submit(AGENCY, fixture.caseId(), fixture.assignmentId(),
                transition(evidenced), "001", PermissionDataScope.OWN_RECORDS, "submit");
        assertThat(submitted.caseStatus()).isEqualTo(AssessmentCaseStatus.FOR_VALIDATION);
        assertThat(submitted.forValidationAt()).isNotNull();
        assertThat(submitted.contributions().get(0).status()).isEqualTo(AssessorAssignmentStatus.SUBMITTED);
        assertThat(submitted.contributions().get(0).submittedBy()).isEqualTo("001");
        assertThatThrownBy(() -> execution.submit(AGENCY, fixture.caseId(), fixture.assignmentId(),
                transition(submitted), "001", PermissionDataScope.OWN_RECORDS, "duplicate"))
                .isInstanceOf(IllegalLifecycleTransitionException.class);

        authenticate("validator");
        assertThatThrownBy(() -> execution.returnCase(AGENCY, fixture.caseId(),
                new ReturnCaseRequest(submitted.caseRecordVersion(), " "),
                "validator", "blank-return-reason"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("return reason");
        assertThat(assignments.findByAgencyIdAndId(AGENCY, fixture.assignmentId()).orElseThrow().getStatus())
                .isEqualTo(AssessorAssignmentStatus.SUBMITTED);

        ReturnCaseResponse returned = execution.returnCase(AGENCY, fixture.caseId(),
                new ReturnCaseRequest(submitted.caseRecordVersion(), "Please clarify the evidence"),
                "validator", "return");
        assertThat(returned.status()).isEqualTo(AssessmentCaseStatus.RETURNED);
        assertThat(returned.returnedContributions()).isEqualTo(1);

        authenticate("001");
        AssessmentWorkResponse correction = execution.getAssignedWork(
                AGENCY, fixture.caseId(), "001", PermissionDataScope.OWN_RECORDS);
        assertThat(correction.contributions().get(0).status()).isEqualTo(AssessorAssignmentStatus.RETURNED);
        RatingResponse existingRating = correction.contributions().get(0).ratings().get(0);
        AssessmentWorkResponse corrected = execution.saveRating(AGENCY, fixture.caseId(), fixture.assignmentId(),
                fixture.competencyId(), new SaveRatingRequest(fixture.levelId(), "Clarified", "Updated notes",
                        existingRating.recordVersion(), correction.contributions().get(0).recordVersion(),
                        correction.caseRecordVersion()), "001", PermissionDataScope.OWN_RECORDS, "correction");
        AssessmentWorkResponse resubmitted = execution.submit(AGENCY, fixture.caseId(), fixture.assignmentId(),
                transition(corrected), "001", PermissionDataScope.OWN_RECORDS, "resubmit");
        assertThat(resubmitted.caseStatus()).isEqualTo(AssessmentCaseStatus.FOR_VALIDATION);
        assertThat(resubmitted.contributions().get(0).status()).isEqualTo(AssessorAssignmentStatus.SUBMITTED);
    }

    @Test
    void staleRootVersionsAndWrongScaleCannotOverwriteOrDuplicateRatings() {
        Fixture fixture = openFixture(false);
        authenticate("001");
        AssessmentWorkResponse initial = execution.getAssignedWork(
                AGENCY, fixture.caseId(), "001", PermissionDataScope.OWN_RECORDS);

        ProficiencyScale other = new ProficiencyScale(AGENCY, "OTHER", "Other", null,
                true, 2, LocalDate.of(2026, 1, 1), null);
        other.addLevel(new ProficiencyLevel(AGENCY, "O1", "Other 1", 1, null,
                true, LocalDate.of(2026, 1, 1), null));
        other = scales.saveAndFlush(other);
        String otherLevel = other.getLevels().get(0).getId();
        assertThatThrownBy(() -> execution.saveRating(AGENCY, fixture.caseId(), fixture.assignmentId(),
                fixture.competencyId(), new SaveRatingRequest(otherLevel, null, null, null,
                        initial.contributions().get(0).recordVersion(), initial.caseRecordVersion()),
                "001", PermissionDataScope.OWN_RECORDS, "wrong-level"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("exact published scale");

        AssessmentWorkResponse saved = execution.saveRating(AGENCY, fixture.caseId(), fixture.assignmentId(),
                fixture.competencyId(), new SaveRatingRequest(fixture.levelId(), null, null, null,
                        initial.contributions().get(0).recordVersion(), initial.caseRecordVersion()),
                "001", PermissionDataScope.OWN_RECORDS, "first-save");
        assertThatThrownBy(() -> execution.saveRating(AGENCY, fixture.caseId(), fixture.assignmentId(),
                fixture.competencyId(), new SaveRatingRequest(fixture.levelId(), "stale", null, null,
                        initial.contributions().get(0).recordVersion(), initial.caseRecordVersion()),
                "001", PermissionDataScope.OWN_RECORDS, "stale-save"))
                .isInstanceOf(OptimisticConflictException.class);
        assertThat(ratings.findByAssignmentIdAndActiveTrueOrderByCompetencyCode(fixture.assignmentId()))
                .singleElement().satisfies(rating -> assertThat(rating.getRemarks()).isNull());

        authenticate("assessment-admin");
        CycleResponse cycle = administration.getCycle(AGENCY, fixture.cycleId());
        administration.closeCycle(AGENCY, fixture.cycleId(),
                new TransitionRequest(cycle.recordVersion(), "Closing execution"), "close");
        authenticate("001");
        assertThatThrownBy(() -> execution.saveRating(AGENCY, fixture.caseId(), fixture.assignmentId(),
                fixture.competencyId(), new SaveRatingRequest(fixture.levelId(), null, null,
                        saved.contributions().get(0).ratings().get(0).recordVersion(),
                        saved.contributions().get(0).recordVersion(), saved.caseRecordVersion()),
                "001", PermissionDataScope.OWN_RECORDS, "closed-save"))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
    }

    @Test
    void independentHumanValidationCreatesOfficialImmutablePersonProfile() {
        Fixture fixture = openFixture(false);
        AssessmentWorkResponse submitted = submitCompletedContribution(fixture);
        ValidateCaseRequest decision = validationRequest(submitted, fixture, false, null);

        assertThatThrownBy(() -> validation.validate(AGENCY, fixture.caseId(), decision,
                "001", false, "self-validation"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("own contribution");
        assertThat(validations.findByAgencyIdAndAssessmentCaseId(AGENCY, fixture.caseId())).isEmpty();
        assertThat(personProfiles.existsByAssessmentCaseId(fixture.caseId())).isFalse();

        authenticate("validator");
        long auditCountBeforeConflict = auditEvents.findByAgencyIdAndAggregateTypeAndAggregateId(
                AGENCY, "ASSESSMENT_CASE", fixture.caseId(),
                org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        ValidateCaseRequest staleContribution = new ValidateCaseRequest(decision.caseRecordVersion(),
                decision.validFrom(), decision.reassessmentDate(), decision.validationRemarks(), false, null,
                List.of(new ExpectedContribution(fixture.assignmentId(),
                        decision.expectedContributions().get(0).recordVersion() - 1)), decision.decisions());
        assertThatThrownBy(() -> validation.validate(AGENCY, fixture.caseId(), staleContribution,
                "validator", false, "stale-validation"))
                .isInstanceOf(OptimisticConflictException.class);
        assertThat(validations.findByAgencyIdAndAssessmentCaseId(AGENCY, fixture.caseId())).isEmpty();
        assertThat(personProfiles.existsByAssessmentCaseId(fixture.caseId())).isFalse();
        assertThat(auditEvents.findByAgencyIdAndAggregateTypeAndAggregateId(
                AGENCY, "ASSESSMENT_CASE", fixture.caseId(),
                org.springframework.data.domain.Pageable.unpaged()).getTotalElements())
                .isEqualTo(auditCountBeforeConflict);

        ValidationResultResponse result = validation.validate(AGENCY, fixture.caseId(), decision,
                "validator", false, "independent-validation");

        assertThat(result.status()).isEqualTo(AssessmentCaseStatus.VALIDATED);
        assertThat(result.profileVersion()).isEqualTo(1);
        assertThat(result.decisions()).singleElement().satisfies(rating -> {
            assertThat(rating.competencyId()).isEqualTo(fixture.competencyId());
            assertThat(rating.finalLevelId()).isEqualTo(fixture.levelId());
            assertThat(rating.contributingAssignmentIds()).containsExactly(fixture.assignmentId());
        });
        assertThat(assignments.findByAgencyIdAndId(AGENCY, fixture.assignmentId()).orElseThrow().getStatus())
                .isEqualTo(AssessorAssignmentStatus.VALIDATED);
        assertThat(personResults.findByPersonProfileIdOrderByCompetencyCode(result.personProfileVersionId()))
                .hasSize(1);

        PersonProfileResponse latest = validation.latest(AGENCY, "001", LocalDate.of(2026, 9, 1),
                "001", PermissionDataScope.OWN_RECORDS);
        assertThat(latest.id()).isEqualTo(result.personProfileVersionId());
        assertThat(latest.results()).singleElement()
                .satisfies(rating -> assertThat(rating.attainedLevelId()).isEqualTo(fixture.levelId()));
        assertThat(validation.history(AGENCY, "001", 0, 20,
                "001", PermissionDataScope.OWN_RECORDS).content()).singleElement();
        assertThat(validation.version(AGENCY, latest.id(),
                "001", PermissionDataScope.OWN_RECORDS).id()).isEqualTo(latest.id());

        assertThatThrownBy(() -> validation.latest(AGENCY, "001", LocalDate.of(2026, 9, 1),
                "other-employee", PermissionDataScope.OWN_RECORDS))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> validation.validate(AGENCY, fixture.caseId(), decision,
                "validator", false, "duplicate-validation"))
                .isInstanceOf(OptimisticConflictException.class);
        assertThat(personProfiles.findByAgencyIdAndSubjectEmployeeNoIgnoreCase(
                AGENCY, "001", org.springframework.data.domain.Pageable.unpaged())).hasSize(1);
    }

    @Test
    void administratorOverrideOfOwnContributionRequiresAuditedReason() {
        Fixture fixture = openFixture(false);
        AssessmentWorkResponse submitted = submitCompletedContribution(fixture);

        ValidateCaseRequest missingReason = validationRequest(submitted, fixture, true, " ");
        assertThatThrownBy(() -> validation.validate(AGENCY, fixture.caseId(), missingReason,
                "001", true, "blank-override"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("override reason");
        assertThat(validations.findByAgencyIdAndAssessmentCaseId(AGENCY, fixture.caseId())).isEmpty();

        ValidateCaseRequest approvedOverride = validationRequest(submitted, fixture, true,
                "Independent validator unavailable; administrator reviewed the complete evidence.");
        ValidationResultResponse result = validation.validate(AGENCY, fixture.caseId(), approvedOverride,
                "001", true, "administrator-override");

        assertThat(result.status()).isEqualTo(AssessmentCaseStatus.VALIDATED);
        assertThat(validations.findByAgencyIdAndAssessmentCaseId(AGENCY, fixture.caseId()))
                .get().satisfies(saved -> {
                    assertThat(saved.isAdministratorOverride()).isTrue();
                    assertThat(saved.getOverrideReason()).contains("administrator reviewed");
                    assertThat(saved.getValidatorEmployeeNo()).isEqualTo("001");
                });
    }

    @Test
    void successorValidationClosesPredecessorAndLatestAsOfIsDeterministic() {
        Fixture first = openFixture(false);
        AssessmentWorkResponse firstSubmission = submitCompletedContribution(first);
        authenticate("validator");
        ValidationResultResponse firstResult = validation.validate(AGENCY, first.caseId(),
                validationRequest(firstSubmission, first, false, null),
                "validator", false, "first-validation");

        Fixture successor = openSuccessorFixture(first);
        AssessmentWorkResponse successorSubmission = submitCompletedContribution(successor);
        authenticate("validator");
        ValidateCaseRequest successorRequest = new ValidateCaseRequest(successorSubmission.caseRecordVersion(),
                LocalDate.of(2026, 10, 1), LocalDate.of(2027, 10, 1), "Successor validation",
                false, null, List.of(new ExpectedContribution(successor.assignmentId(),
                successorSubmission.contributions().get(0).recordVersion())),
                List.of(new FinalDecision(successor.competencyId(), successor.levelId(),
                        "Successor final decision", List.of(successor.assignmentId()))));
        ValidationResultResponse successorResult = validation.validate(AGENCY, successor.caseId(),
                successorRequest, "validator", false, "successor-validation");

        PersonCompetencyProfile firstProfile = personProfiles.findById(firstResult.personProfileVersionId())
                .orElseThrow();
        PersonCompetencyProfile secondProfile = personProfiles.findById(successorResult.personProfileVersionId())
                .orElseThrow();
        assertThat(firstProfile.getValidTo()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(secondProfile.getPredecessor().getId()).isEqualTo(firstProfile.getId());
        assertThat(secondProfile.getProfileVersion()).isEqualTo(2);
        assertThat(validation.latest(AGENCY, "001", LocalDate.of(2026, 9, 30),
                "001", PermissionDataScope.OWN_RECORDS).id()).isEqualTo(firstProfile.getId());
        assertThat(validation.latest(AGENCY, "001", LocalDate.of(2026, 10, 1),
                "001", PermissionDataScope.OWN_RECORDS).id()).isEqualTo(secondProfile.getId());
        assertThat(validation.history(AGENCY, "001", 0, 20,
                "001", PermissionDataScope.OWN_RECORDS).content())
                .extracting(PersonProfileResponse::profileVersion).containsExactly(2, 1);
    }

    private AssessmentWorkResponse submitCompletedContribution(Fixture fixture) {
        authenticate("001");
        AssessmentWorkResponse initial = execution.getAssignedWork(
                AGENCY, fixture.caseId(), "001", PermissionDataScope.OWN_RECORDS);
        AssessmentWorkResponse rated = execution.saveRating(AGENCY, fixture.caseId(), fixture.assignmentId(),
                fixture.competencyId(), new SaveRatingRequest(fixture.levelId(), "Observed", "Complete notes",
                        null, initial.contributions().get(0).recordVersion(), initial.caseRecordVersion()),
                "001", PermissionDataScope.OWN_RECORDS, "validation-rating");
        return execution.submit(AGENCY, fixture.caseId(), fixture.assignmentId(), transition(rated),
                "001", PermissionDataScope.OWN_RECORDS, "validation-submit");
    }

    private Fixture openSuccessorFixture(Fixture predecessor) {
        authenticate("assessment-admin");
        PositionProfile profile = profiles.findById(predecessor.positionProfileId()).orElseThrow();
        CycleResponse cycle = administration.createCycle(AGENCY, new CreateCycleRequest(
                "FY2027", "FY 2027", null, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31)),
                "successor-cycle");
        ToolResponse tool = administration.createTool(AGENCY, cycle.id(), new CreateToolRequest(profile.getId(),
                "Successor annual tool", "Complete all competencies",
                List.of(new MethodRequest(AssessmentMethod.SELF_ASSESSMENT, false))), "successor-tool");
        CaseResponse assessmentCase = administration.addSubject(AGENCY, tool.id(),
                new AddSubjectRequest(1L, tool.recordVersion()), "Bearer token", "successor-subject");
        assessmentCase = administration.addAssessor(AGENCY, assessmentCase.id(),
                new AddAssessorRequest(AssessmentMethod.SELF_ASSESSMENT, 1L, null,
                        assessmentCase.recordVersion()), "Bearer token", "successor-assessor");
        administration.publishTool(AGENCY, tool.id(),
                new TransitionRequest(administration.getTool(AGENCY, tool.id()).recordVersion(),
                        "Ready for successor execution"), "successor-publish");
        administration.openCycle(AGENCY, cycle.id(),
                new TransitionRequest(administration.getCycle(AGENCY, cycle.id()).recordVersion(),
                        "Open successor assessment"), "successor-open");
        CaseResponse opened = administration.getCase(AGENCY, assessmentCase.id());
        return new Fixture(cycle.id(), opened.id(), opened.assessors().get(0).id(),
                predecessor.competencyId(), predecessor.levelId(), profile.getId());
    }

    private static ValidateCaseRequest validationRequest(AssessmentWorkResponse submitted, Fixture fixture,
                                                         boolean administratorOverride, String overrideReason) {
        var contribution = submitted.contributions().get(0);
        return new ValidateCaseRequest(submitted.caseRecordVersion(), LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 9, 1), "Human validation decision", administratorOverride,
                overrideReason, List.of(new ExpectedContribution(fixture.assignmentId(),
                contribution.recordVersion())), List.of(new FinalDecision(fixture.competencyId(),
                fixture.levelId(), "Final level selected after reviewing the contribution",
                List.of(fixture.assignmentId()))));
    }

    private Fixture openFixture(boolean evidenceRequired) {
        CompetencyCategory category = categories.saveAndFlush(new CompetencyCategory(
                AGENCY, "CAT", "Category", null, true, 1, LocalDate.of(2026, 1, 1), null));
        ProficiencyScale scale = new ProficiencyScale(AGENCY, "SCALE", "Scale", null,
                true, 1, LocalDate.of(2026, 1, 1), null);
        scale.addLevel(new ProficiencyLevel(AGENCY, "L1", "Basic", 1, null,
                true, LocalDate.of(2026, 1, 1), null));
        scale = scales.saveAndFlush(scale);
        Competency competency = competencies.saveAndFlush(new Competency(AGENCY, "COMP", "Competency",
                "Definition", "ACTIVE", category, scale, true, 1, LocalDate.of(2026, 1, 1), null));
        PositionTargetSnapshot target = new PositionTargetSnapshot(PositionTargetType.JOB_POSITION, 14L,
                14L, "Accountant III", 19L, 1L, null, null, "p".repeat(64), Instant.now());
        PositionProfile profile = profiles.saveAndFlush(PositionProfile.draft(AGENCY, target,
                "Assessment profile", null, LocalDate.of(2026, 1, 1), null));
        requirements.saveAndFlush(new PositionProfileRequirement(AGENCY, profile, competency,
                scale.getLevels().get(0), RequirementClassification.MANDATORY, "HIGH", null, 1));
        profile.submit("submitter", Instant.now(), target);
        profile.approve("approver", Instant.now(), target);
        profile = profiles.saveAndFlush(profile);

        when(subjects.get(anyLong(), anyString())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return new HumanResourceAssessmentSubject(id, "001", "Ferdinand Javines", true, 101L,
                    LocalDateTime.of(2026, 7, 1, 8, 0), 14L, null, "s".repeat(64),
                    LocalDateTime.of(2026, 8, 1, 9, 0), Instant.now());
        });
        CycleResponse cycle = administration.createCycle(AGENCY, new CreateCycleRequest(
                "FY2026", "FY 2026", null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)), "cycle");
        ToolResponse tool = administration.createTool(AGENCY, cycle.id(), new CreateToolRequest(profile.getId(),
                "Annual tool", "Complete all competencies", List.of(
                new MethodRequest(AssessmentMethod.SELF_ASSESSMENT, evidenceRequired))), "tool");
        CaseResponse assessmentCase = administration.addSubject(AGENCY, tool.id(),
                new AddSubjectRequest(1L, tool.recordVersion()), "Bearer token", "subject");
        assessmentCase = administration.addAssessor(AGENCY, assessmentCase.id(),
                new AddAssessorRequest(AssessmentMethod.SELF_ASSESSMENT, 1L, null,
                        assessmentCase.recordVersion()), "Bearer token", "assessor");
        tool = administration.publishTool(AGENCY, tool.id(),
                new TransitionRequest(administration.getTool(AGENCY, tool.id()).recordVersion(),
                        "Ready for execution"), "publish");
        cycle = administration.openCycle(AGENCY, cycle.id(),
                new TransitionRequest(administration.getCycle(AGENCY, cycle.id()).recordVersion(),
                        "Open for assessment"), "open");
        CaseResponse opened = administration.getCase(AGENCY, assessmentCase.id());
        return new Fixture(cycle.id(), opened.id(), opened.assessors().get(0).id(),
                competency.getId(), scale.getLevels().get(0).getId(), profile.getId());
    }

    private static WorkTransitionRequest transition(AssessmentWorkResponse work) {
        return new WorkTransitionRequest(work.contributions().get(0).recordVersion(),
                work.caseRecordVersion(), null, "Completed by assigned assessor");
    }

    private static void authenticate(String employeeNo) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(employeeNo, null, List.of()));
    }

    private record Fixture(String cycleId, String caseId, String assignmentId,
                           String competencyId, String levelId, String positionProfileId) { }
}
