package com.primehr.positionprofile.application;

import com.primehr.competency.domain.Competency;
import com.primehr.competency.domain.CompetencyCategory;
import com.primehr.competency.domain.ProficiencyLevel;
import com.primehr.competency.domain.ProficiencyScale;
import com.primehr.competency.infrastructure.CompetencyCategoryRepository;
import com.primehr.competency.infrastructure.CompetencyRepository;
import com.primehr.competency.infrastructure.ProficiencyScaleRepository;
import com.primehr.integration.administrative.AdministrativePositionTarget;
import com.primehr.integration.administrative.AdministrativePositionTargetClient;
import com.primehr.integration.administrative.PositionTargetDependencyException;
import com.primehr.positionprofile.api.*;
import com.primehr.positionprofile.domain.PositionProfileStatus;
import com.primehr.positionprofile.domain.PositionTargetType;
import com.primehr.positionprofile.domain.RequirementClassification;
import com.primehr.positionprofile.infrastructure.PositionProfileRepository;
import com.primehr.shared.audit.PrimeHrAuditEventRepository;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import com.primehr.shared.exception.PublicationConflictException;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.security.access.AccessDeniedException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PositionProfileAdminServiceIntegrationTest {
    private static final String AGENCY = "TEST-AGENCY";
    private static final String TOKEN = "Bearer test-token";

    @Autowired private PositionProfileAdminService service;
    @Autowired private CompetencyCategoryRepository categories;
    @Autowired private ProficiencyScaleRepository scales;
    @Autowired private CompetencyRepository competencies;
    @Autowired private PositionProfileRepository profiles;
    @Autowired private PrimeHrAuditEventRepository audits;
    @MockBean private AdministrativePositionTargetClient targets;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("EMP-00001", null, List.of()));
        when(targets.get(PositionTargetType.JOB_POSITION, 14L, TOKEN)).thenReturn(jobTarget(14L));
        when(targets.get(PositionTargetType.PLANTILLA, 25L, TOKEN)).thenReturn(plantillaTarget(25L, 14L));
    }

    @Test
    void createsRefreshesAndArchivesAnAuditedDraftWithoutDuplicatingThePositionMaster() {
        PositionProfileResponse created = service.create(AGENCY,
                new CreatePositionProfileRequest(PositionTargetType.PLANTILLA, 25L, "HRMO Profile",
                        "Initial draft", LocalDate.of(2026, 8, 12), null, null), TOKEN, "create-correlation");

        assertThat(created.status()).isEqualTo(PositionProfileStatus.DRAFT);
        assertThat(created.targetSnapshot().plantillaId()).isEqualTo(25L);
        assertThat(created.targetSnapshot().jobPositionId()).isEqualTo(14L);
        assertThat(profiles.count()).isEqualTo(1);

        when(targets.get(PositionTargetType.PLANTILLA, 25L, TOKEN)).thenReturn(
                new AdministrativePositionTarget(PositionTargetType.PLANTILLA, 25L, 14L,
                        "Administrative Officer IV - updated", 15L, 2L, 25L, "HRMO-001",
                        "updated-fingerprint", Instant.now()));
        PositionProfileResponse updated = service.update(AGENCY, created.id(),
                new UpdatePositionProfileRequest("HRMO Profile", "Updated draft",
                        LocalDate.of(2026, 8, 12), null, created.recordVersion()), TOKEN, "update-correlation");

        assertThat(updated.targetSnapshot().jobPositionName()).endsWith("updated");
        assertThat(updated.targetSnapshot().sourceFingerprint()).isEqualTo("updated-fingerprint");

        PositionProfileResponse archived = service.archive(AGENCY, created.id(),
                new PositionProfileTransitionRequest(updated.recordVersion(), "Draft withdrawn"),
                "archive-correlation");
        assertThat(archived.status()).isEqualTo(PositionProfileStatus.ARCHIVED);
        assertThatThrownBy(() -> service.createSuccessor(AGENCY, archived.id(),
                new PositionProfileTransitionRequest(archived.recordVersion(), "Restart archived draft"),
                TOKEN, "replacement-correlation"))
                .isInstanceOf(com.primehr.shared.exception.IllegalLifecycleTransitionException.class)
                .hasMessageContaining("Only ACTIVE");
        assertThat(audits.findAll()).filteredOn(event -> event.getAggregateType().equals("POSITION_PROFILE"))
                .extracting(event -> event.getAction()).contains("CREATE_DRAFT", "UPDATE_DRAFT", "ARCHIVE_DRAFT")
                .doesNotContain("CREATE_SUCCESSOR_DRAFT");
    }

    @Test
    void enforcesOneProfileChainPerAuthoritativeTargetAndOptimisticRootVersion() {
        PositionProfileResponse created = createJobProfile();

        assertThatThrownBy(this::createJobProfile)
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("chain already exists");
        assertThatThrownBy(() -> service.update(AGENCY, created.id(),
                new UpdatePositionProfileRequest("Stale", null, null, null, created.recordVersion() + 1),
                TOKEN, null)).isInstanceOf(OptimisticConflictException.class);
    }

    @Test
    void requirementUsesExactPublishedCompetencyScaleAndPreservesArchivedHistory() {
        PositionProfileResponse profile = createJobProfile();
        Fixture fixture = competency("REQ");

        PositionProfileResponse withRequirement = service.addRequirement(AGENCY, profile.id(),
                new CreatePositionRequirementRequest(fixture.competency().getId(), fixture.level().getId(),
                        RequirementClassification.MANDATORY, "HIGH", "Essential role behavior", 1,
                        profile.recordVersion()), "requirement-create");

        assertThat(withRequirement.requirements()).singleElement().satisfies(requirement -> {
            assertThat(requirement.competencyVersionId()).isEqualTo(fixture.competency().getId());
            assertThat(requirement.requiredProficiencyLevelId()).isEqualTo(fixture.level().getId());
            assertThat(requirement.classification()).isEqualTo(RequirementClassification.MANDATORY);
        });

        assertThatThrownBy(() -> service.addRequirement(AGENCY, profile.id(),
                new CreatePositionRequirementRequest(fixture.competency().getId(), fixture.level().getId(),
                        RequirementClassification.DESIRABLE, null, null, 2,
                        withRequirement.recordVersion()), null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already in");

        PositionRequirementResponse requirement = withRequirement.requirements().get(0);
        PositionProfileResponse archived = service.archiveRequirement(AGENCY, profile.id(), requirement.id(),
                new PositionRequirementTransitionRequest(requirement.recordVersion(),
                        withRequirement.recordVersion(), "Requirement removed from draft"), "requirement-archive");
        assertThat(archived.requirements()).singleElement()
                .satisfies(item -> assertThat(item.active()).isFalse());
    }

    @Test
    void rejectsLevelFromAnotherScaleAndLeavesNoPartialRequirementOrAudit() {
        PositionProfileResponse profile = createJobProfile();
        Fixture required = competency("RIGHT");
        Fixture other = competency("OTHER");
        long auditsBefore = audits.count();

        assertThatThrownBy(() -> service.addRequirement(AGENCY, profile.id(),
                new CreatePositionRequirementRequest(required.competency().getId(), other.level().getId(),
                        RequirementClassification.MANDATORY, null, null, 1, profile.recordVersion()), null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("exact published scale");

        assertThat(service.get(AGENCY, profile.id()).requirements()).isEmpty();
        assertThat(audits.count()).isEqualTo(auditsBefore);
    }

    @Test
    void changingProfileStartDateCannotInvalidateAnExistingExactRequirement() {
        PositionProfileResponse profile = createJobProfile();
        Fixture fixture = competency("DATED", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        PositionProfileResponse withRequirement = service.addRequirement(AGENCY, profile.id(),
                new CreatePositionRequirementRequest(fixture.competency().getId(), fixture.level().getId(),
                        RequirementClassification.MANDATORY, null, null, 1, profile.recordVersion()), null);

        assertThatThrownBy(() -> service.update(AGENCY, profile.id(),
                new UpdatePositionProfileRequest("Profile", null, LocalDate.of(2027, 1, 1), null,
                        withRequirement.recordVersion()), TOKEN, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not effective");
    }

    @Test
    void submitAndIndependentApprovalLockContentAndWriteCompleteAuditMetadata() {
        PositionProfileResponse profile = addRequirement(createJobProfile(), competency("FLOW"),
                RequirementClassification.MANDATORY, "HIGH", 1);

        PositionProfileResponse submitted = service.submit(AGENCY, profile.id(),
                new SubmitPositionProfileRequest(profile.recordVersion()), TOKEN, "submit-flow");
        assertThat(submitted.status()).isEqualTo(PositionProfileStatus.SUBMITTED);
        assertThat(submitted.submittedBy()).isEqualTo("EMP-00001");
        assertThat(submitted.submittedAt()).isNotNull();
        assertThat(submitted.approvedBy()).isNull();
        assertThatThrownBy(() -> service.update(AGENCY, submitted.id(),
                new UpdatePositionProfileRequest("Changed while submitted", null,
                        submitted.effectiveFrom(), submitted.effectiveTo(), submitted.recordVersion()),
                TOKEN, null)).isInstanceOf(IllegalLifecycleTransitionException.class);

        actor("EMP-APPROVER");
        PositionProfileResponse approved = service.approve(AGENCY, submitted.id(),
                new ApprovePositionProfileRequest(submitted.recordVersion(), "Reviewed and approved"),
                TOKEN, false, "approve-flow");

        assertThat(approved.status()).isEqualTo(PositionProfileStatus.ACTIVE);
        assertThat(approved.approvedBy()).isEqualTo("EMP-APPROVER");
        assertThat(approved.approvedAt()).isNotNull();
        assertThatThrownBy(() -> service.addRequirement(AGENCY, approved.id(),
                new CreatePositionRequirementRequest(competency("LATE").competency().getId(),
                        competency("OTHER-LATE").level().getId(), RequirementClassification.DESIRABLE,
                        null, null, 2, approved.recordVersion()), null))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
        assertThat(audits.findAll()).filteredOn(event -> event.getAggregateId().equals(profile.id()))
                .extracting(event -> event.getAction()).contains("SUBMIT_PROFILE", "APPROVE_PROFILE");
    }

    @Test
    void approverCanReturnWithReasonAndResubmissionPreservesNoStaleApprovalMetadata() {
        PositionProfileResponse profile = addRequirement(createJobProfile(), competency("RETURN"),
                RequirementClassification.MANDATORY, null, 1);
        PositionProfileResponse submitted = service.submit(AGENCY, profile.id(),
                new SubmitPositionProfileRequest(profile.recordVersion()), TOKEN, null);

        actor("EMP-APPROVER");
        PositionProfileResponse returned = service.returnSubmission(AGENCY, submitted.id(),
                new PositionProfileTransitionRequest(submitted.recordVersion(), "Clarify the criticality"),
                "return-flow");
        assertThat(returned.status()).isEqualTo(PositionProfileStatus.DRAFT);
        assertThat(returned.submittedBy()).isNull();
        assertThat(returned.submittedAt()).isNull();
        assertThat(returned.approvedBy()).isNull();
        assertThat(audits.findAll()).filteredOn(event -> event.getAction().equals("RETURN_SUBMISSION"))
                .singleElement().satisfies(event -> assertThat(event.getReason())
                        .isEqualTo("Clarify the criticality"));

        PositionProfileResponse resubmitted = service.submit(AGENCY, returned.id(),
                new SubmitPositionProfileRequest(returned.recordVersion()), TOKEN, null);
        assertThat(resubmitted.submittedBy()).isEqualTo("EMP-APPROVER");
    }

    @Test
    void incompleteAndSelfApprovalFailuresLeaveLifecycleAndAuditUnchanged() {
        PositionProfileResponse incomplete = createJobProfile();
        long beforeIncomplete = audits.count();
        assertThatThrownBy(() -> service.submit(AGENCY, incomplete.id(),
                new SubmitPositionProfileRequest(incomplete.recordVersion()), TOKEN, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("requirement");
        assertThat(service.get(AGENCY, incomplete.id()).status()).isEqualTo(PositionProfileStatus.DRAFT);
        assertThat(audits.count()).isEqualTo(beforeIncomplete);

        Fixture fixture = competency("SELF");
        PositionProfileResponse complete = addRequirement(incomplete, fixture,
                RequirementClassification.MANDATORY, null, 1);
        PositionProfileResponse submitted = service.submit(AGENCY, complete.id(),
                new SubmitPositionProfileRequest(complete.recordVersion()), TOKEN, null);
        long beforeApproval = audits.count();
        assertThatThrownBy(() -> service.approve(AGENCY, submitted.id(),
                new ApprovePositionProfileRequest(submitted.recordVersion(), null), TOKEN, false, null))
                .isInstanceOf(AccessDeniedException.class).hasMessageContaining("own");
        assertThatThrownBy(() -> service.approve(AGENCY, submitted.id(),
                new ApprovePositionProfileRequest(submitted.recordVersion(), null), TOKEN, true, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("requires a reason");
        assertThat(service.get(AGENCY, submitted.id()).status()).isEqualTo(PositionProfileStatus.SUBMITTED);
        assertThat(audits.count()).isEqualTo(beforeApproval);

        PositionProfileResponse override = service.approve(AGENCY, submitted.id(),
                new ApprovePositionProfileRequest(submitted.recordVersion(), "Urgent administrator override"),
                TOKEN, true, "admin-override");
        assertThat(override.status()).isEqualTo(PositionProfileStatus.ACTIVE);
        assertThat(audits.findAll()).filteredOn(event -> event.getAggregateId().equals(submitted.id()))
                .extracting(event -> event.getAction()).contains("ADMIN_APPROVE_PROFILE");
    }

    @Test
    void successorApprovalClosesPredecessorAndResolutionAndComparisonUseExactVersions() {
        PositionProfileResponse first = createJobProfile();
        Fixture changed = competency("CHANGED");
        Fixture removed = competency("REMOVED");
        Fixture unchanged = competency("UNCHANGED");
        first = addRequirement(first, changed, RequirementClassification.MANDATORY, "HIGH", 1);
        first = addRequirement(first, removed, RequirementClassification.DESIRABLE, null, 2);
        first = addRequirement(first, unchanged, RequirementClassification.MANDATORY, "MEDIUM", 3);
        first = service.submit(AGENCY, first.id(), new SubmitPositionProfileRequest(first.recordVersion()),
                TOKEN, null);
        actor("EMP-APPROVER");
        first = service.approve(AGENCY, first.id(),
                new ApprovePositionProfileRequest(first.recordVersion(), null), TOKEN, false, null);

        PositionProfileResponse plantilla = service.create(AGENCY,
                new CreatePositionProfileRequest(PositionTargetType.PLANTILLA, 25L, "Plantilla profile",
                        null, LocalDate.of(2026, 8, 12), null, null), TOKEN, null);
        plantilla = addRequirement(plantilla, unchanged, RequirementClassification.MANDATORY, null, 1);
        plantilla = service.submit(AGENCY, plantilla.id(),
                new SubmitPositionProfileRequest(plantilla.recordVersion()), TOKEN, null);
        actor("PLANTILLA-APPROVER");
        plantilla = service.approve(AGENCY, plantilla.id(),
                new ApprovePositionProfileRequest(plantilla.recordVersion(), null), TOKEN, false, null);
        assertThat(service.resolve(AGENCY, 14L, 25L, LocalDate.of(2026, 9, 1)).resolvedBy())
                .isEqualTo(PositionTargetType.PLANTILLA);
        assertThat(service.resolve(AGENCY, 14L, null, LocalDate.of(2026, 9, 1)).profile().id())
                .isEqualTo(first.id());

        PositionProfileResponse successor = service.createSuccessor(AGENCY, first.id(),
                new PositionProfileTransitionRequest(first.recordVersion(), "Annual revision"), TOKEN, null);
        successor = service.update(AGENCY, successor.id(),
                new UpdatePositionProfileRequest(successor.name(), successor.description(),
                        LocalDate.of(2027, 1, 1), null, successor.recordVersion()), TOKEN, null);
        PositionRequirementResponse changedLine = requirement(successor, changed.competency().getId());
        successor = service.updateRequirement(AGENCY, successor.id(), changedLine.id(),
                new UpdatePositionRequirementRequest(changed.advancedLevel().getId(),
                        RequirementClassification.DESIRABLE, "CRITICAL", "Raised expectation", 1,
                        changedLine.recordVersion(), successor.recordVersion()), null);
        PositionRequirementResponse removedLine = requirement(successor, removed.competency().getId());
        successor = service.archiveRequirement(AGENCY, successor.id(), removedLine.id(),
                new PositionRequirementTransitionRequest(removedLine.recordVersion(),
                        successor.recordVersion(), "No longer required"), null);
        Fixture added = competency("ADDED");
        successor = addRequirement(successor, added, RequirementClassification.MANDATORY, "HIGH", 4);

        PositionProfileComparisonResponse comparison = service.compare(AGENCY, first.id(), successor.id());
        assertThat(comparison.added()).isEqualTo(1);
        assertThat(comparison.removed()).isEqualTo(1);
        assertThat(comparison.changed()).isEqualTo(1);
        assertThat(comparison.unchanged()).isEqualTo(1);

        actor("EMP-SUBMITTER-2");
        successor = service.submit(AGENCY, successor.id(),
                new SubmitPositionProfileRequest(successor.recordVersion()), TOKEN, null);
        actor("EMP-APPROVER-2");
        successor = service.approve(AGENCY, successor.id(),
                new ApprovePositionProfileRequest(successor.recordVersion(), "Approved revision"),
                TOKEN, false, null);
        PositionProfileResponse closedFirst = service.get(AGENCY, first.id());
        assertThat(closedFirst.effectiveTo()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(service.resolve(AGENCY, 14L, null, LocalDate.of(2027, 1, 1)).profile().id())
                .isEqualTo(successor.id());
    }

    @Test
    void staleSuccessorApprovalDoesNotCloseTheApprovedPredecessorOrWriteApprovalAudit() {
        PositionProfileResponse first = addRequirement(createJobProfile(), competency("BASE"),
                RequirementClassification.MANDATORY, null, 1);
        first = service.submit(AGENCY, first.id(), new SubmitPositionProfileRequest(first.recordVersion()),
                TOKEN, null);
        actor("EMP-APPROVER");
        first = service.approve(AGENCY, first.id(),
                new ApprovePositionProfileRequest(first.recordVersion(), null), TOKEN, false, null);
        PositionProfileResponse successor = service.createSuccessor(AGENCY, first.id(),
                new PositionProfileTransitionRequest(first.recordVersion(), "Revision"), TOKEN, null);
        successor = service.update(AGENCY, successor.id(),
                new UpdatePositionProfileRequest(successor.name(), successor.description(),
                        LocalDate.of(2027, 1, 1), null, successor.recordVersion()), TOKEN, null);
        actor("EMP-SUBMITTER-2");
        successor = service.submit(AGENCY, successor.id(),
                new SubmitPositionProfileRequest(successor.recordVersion()), TOKEN, null);
        long auditCount = audits.count();

        actor("EMP-APPROVER-2");
        PositionProfileResponse staleSuccessor = successor;
        assertThatThrownBy(() -> service.approve(AGENCY, staleSuccessor.id(),
                new ApprovePositionProfileRequest(staleSuccessor.recordVersion() + 1, "Stale approval"),
                TOKEN, false, null)).isInstanceOf(PublicationConflictException.class);

        assertThat(service.get(AGENCY, first.id()).effectiveTo()).isNull();
        assertThat(service.get(AGENCY, successor.id()).status()).isEqualTo(PositionProfileStatus.SUBMITTED);
        assertThat(audits.count()).isEqualTo(auditCount);
    }

    @Test
    void unavailableAuthoritativeTargetDuringApprovalLeavesSubmissionAndPredecessorUntouched() {
        PositionProfileResponse first = addRequirement(createJobProfile(), competency("DEPENDENCY"),
                RequirementClassification.MANDATORY, null, 1);
        first = service.submit(AGENCY, first.id(), new SubmitPositionProfileRequest(first.recordVersion()),
                TOKEN, null);
        actor("EMP-APPROVER");
        first = service.approve(AGENCY, first.id(),
                new ApprovePositionProfileRequest(first.recordVersion(), null), TOKEN, false, null);
        PositionProfileResponse successor = service.createSuccessor(AGENCY, first.id(),
                new PositionProfileTransitionRequest(first.recordVersion(), "Revision"), TOKEN, null);
        successor = service.update(AGENCY, successor.id(),
                new UpdatePositionProfileRequest(successor.name(), successor.description(),
                        LocalDate.of(2027, 1, 1), null, successor.recordVersion()), TOKEN, null);
        actor("EMP-SUBMITTER-2");
        successor = service.submit(AGENCY, successor.id(),
                new SubmitPositionProfileRequest(successor.recordVersion()), TOKEN, null);
        long auditCount = audits.count();
        when(targets.get(PositionTargetType.JOB_POSITION, 14L, TOKEN))
                .thenThrow(new PositionTargetDependencyException("Administrative unavailable", null));

        actor("EMP-APPROVER-2");
        PositionProfileResponse pending = successor;
        assertThatThrownBy(() -> service.approve(AGENCY, pending.id(),
                new ApprovePositionProfileRequest(pending.recordVersion(), "Approve"), TOKEN, false, null))
                .isInstanceOf(PositionTargetDependencyException.class);

        assertThat(service.get(AGENCY, first.id()).effectiveTo()).isNull();
        assertThat(service.get(AGENCY, successor.id()).status()).isEqualTo(PositionProfileStatus.SUBMITTED);
        assertThat(audits.count()).isEqualTo(auditCount);
    }

    private PositionProfileResponse createJobProfile() {
        return service.create(AGENCY, new CreatePositionProfileRequest(PositionTargetType.JOB_POSITION,
                14L, "Profile", null, LocalDate.of(2026, 8, 12), null, null), TOKEN, null);
    }

    private PositionProfileResponse addRequirement(PositionProfileResponse profile, Fixture fixture,
                                                    RequirementClassification classification,
                                                    String criticality, int order) {
        return service.addRequirement(AGENCY, profile.id(),
                new CreatePositionRequirementRequest(fixture.competency().getId(), fixture.level().getId(),
                        classification, criticality, null, order, profile.recordVersion()), null);
    }

    private static PositionRequirementResponse requirement(PositionProfileResponse profile, String competencyId) {
        return profile.requirements().stream().filter(item -> item.competencyVersionId().equals(competencyId))
                .findFirst().orElseThrow();
    }

    private static void actor(String employeeNo) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(employeeNo, null, List.of()));
    }

    private Fixture competency(String prefix) {
        return competency(prefix, LocalDate.of(2026, 1, 1), null);
    }

    private Fixture competency(String prefix, LocalDate from, LocalDate to) {
        String suffix = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        CompetencyCategory category = categories.saveAndFlush(new CompetencyCategory(
                AGENCY, "C" + suffix, "Category", null, true, 1, from, to));
        ProficiencyScale scale = new ProficiencyScale(AGENCY, "S" + suffix, "Scale", null,
                true, 1, from, to);
        scale.addLevel(new ProficiencyLevel(AGENCY, "L1", "Basic", 1, null, true, from, to));
        scale.addLevel(new ProficiencyLevel(AGENCY, "L2", "Advanced", 2, null, true, from, to));
        scale = scales.saveAndFlush(scale);
        Competency competency = competencies.saveAndFlush(new Competency(AGENCY, "K" + suffix,
                "Competency", "Published definition", "ACTIVE", category, scale, true, 1, from, to));
        return new Fixture(competency, scale.getLevels().get(0), scale.getLevels().get(1));
    }

    private static AdministrativePositionTarget jobTarget(long id) {
        return new AdministrativePositionTarget(PositionTargetType.JOB_POSITION, id, id,
                "Administrative Officer IV", 15L, 2L, null, null,
                "fingerprint-job-" + id, Instant.now());
    }

    private static AdministrativePositionTarget plantillaTarget(long id, long jobId) {
        return new AdministrativePositionTarget(PositionTargetType.PLANTILLA, id, jobId,
                "Administrative Officer IV", 15L, 2L, id, "HRMO-001",
                "fingerprint-plantilla-" + id, Instant.now());
    }

    private record Fixture(Competency competency, ProficiencyLevel level, ProficiencyLevel advancedLevel) {
    }
}
