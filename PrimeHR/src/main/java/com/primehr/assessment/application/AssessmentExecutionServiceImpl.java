package com.primehr.assessment.application;

import com.primehr.assessment.api.AssessmentExecutionDtos.*;
import com.primehr.assessment.domain.*;
import com.primehr.assessment.infrastructure.*;
import com.primehr.competency.domain.*;
import com.primehr.competency.infrastructure.ProficiencyLevelRepository;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.positionprofile.domain.PositionProfileRequirement;
import com.primehr.positionprofile.infrastructure.PositionProfileRequirementRepository;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class AssessmentExecutionServiceImpl implements AssessmentExecutionService {
    private final AssessmentCaseRepository cases;
    private final AssessorAssignmentRepository assignments;
    private final AssessmentToolMethodRepository methods;
    private final AssessmentRatingRepository ratings;
    private final AssessmentEvidenceRepository evidence;
    private final PositionProfileRequirementRepository requirements;
    private final ProficiencyLevelRepository levels;
    private final PrimeHrAuditService audit;

    public AssessmentExecutionServiceImpl(AssessmentCaseRepository cases,
            AssessorAssignmentRepository assignments, AssessmentToolMethodRepository methods,
            AssessmentRatingRepository ratings, AssessmentEvidenceRepository evidence,
            PositionProfileRequirementRepository requirements, ProficiencyLevelRepository levels,
            PrimeHrAuditService audit) {
        this.cases = cases; this.assignments = assignments; this.methods = methods; this.ratings = ratings;
        this.evidence = evidence; this.requirements = requirements; this.levels = levels; this.audit = audit;
    }

    @Override @Transactional(readOnly = true)
    public PageResponse<InboxItemResponse> mine(String agencyId, String actor, int page, int size) {
        validatePage(page, size);
        return PageResponse.from(assignments.findByAgencyIdAndAssessorEmployeeNoIgnoreCaseAndActiveTrueAndStatusIn(
                agencyId, requireActor(actor), List.of(AssessorAssignmentStatus.ASSIGNED.name(),
                        AssessorAssignmentStatus.IN_PROGRESS.name(), AssessorAssignmentStatus.SUBMITTED.name(),
                        AssessorAssignmentStatus.RETURNED.name(), AssessorAssignmentStatus.VALIDATED.name()),
                PageRequest.of(page, size, Sort.by("createdAt").descending())),
                this::inboxItem);
    }

    @Override @Transactional(readOnly = true)
    public AssessmentWorkResponse getAssignedWork(String agencyId, String caseId, String actor,
                                                    PermissionDataScope scope) {
        AssessorAssignment assignment = actorAssignment(agencyId, caseId, actor, scope);
        assignment.getAssessmentCase().getTool().requirePublishedForExecution();
        return work(assignment.getAssessmentCase(), assignment);
    }

    @Override
    public AssessmentWorkResponse saveRating(String agencyId, String caseId, String assignmentId,
            String competencyId, SaveRatingRequest request, String actor, PermissionDataScope scope,
            String correlationId) {
        AssessorAssignment assignment = participantAssignment(agencyId, caseId, assignmentId, actor, scope);
        version(assignment.getVersion(), request.assignmentRecordVersion());
        AssessmentCase assessmentCase = assignment.getAssessmentCase();
        version(assessmentCase.getVersion(), request.caseRecordVersion());
        PositionProfileRequirement requirement = requirement(agencyId, assessmentCase, competencyId);
        ProficiencyLevel level = levels.findByIdAndAgencyId(request.attainedLevelId(), agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Attained proficiency level not found"));
        if (!level.isActive() || !requirement.getCompetency().getProficiencyScale().getId()
                .equals(level.getScale().getId())) {
            throw new IllegalArgumentException(
                    "The attained level must belong to the competency's exact published scale");
        }
        AssessmentRating existing = ratings.findByAgencyIdAndAssignmentIdAndCompetencyId(
                agencyId, assignmentId, competencyId).orElse(null);
        RatingResponse before = existing == null ? null : ratingResponse(existing);
        beginWork(agencyId, assessmentCase, assignment, request.caseRecordVersion(),
                request.assignmentRecordVersion(), actor);
        assignment = assignment(agencyId, caseId, assignmentId);
        if (existing == null) {
            if (request.recordVersion() != null) throw new OptimisticConflictException(
                    "recordVersion must be omitted when creating a rating");
            existing = new AssessmentRating(agencyId, assignment, requirement.getCompetency(), level,
                    request.remarks(), request.behavioralNotes(), actor);
        } else {
            existing = ratings.findByAgencyIdAndId(agencyId, existing.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assessment rating not found"));
            if (request.recordVersion() == null) throw new OptimisticConflictException(
                    "recordVersion is required when updating a rating");
            existing.update(level, request.remarks(), request.behavioralNotes(), request.recordVersion(), actor);
        }
        existing = ratings.saveAndFlush(existing);
        record(agencyId, before == null ? "CREATE_RATING" : "UPDATE_RATING", "ASSESSMENT_RATING",
                existing.getId(), existing.getVersion(), before, ratingResponse(existing), null, correlationId);
        return ownWork(agencyId, caseId, assignmentId, actor, scope);
    }

    @Override
    public AssessmentWorkResponse createEvidence(String agencyId, String caseId, String assignmentId,
            CreateEvidenceRequest request, String actor, PermissionDataScope scope, String correlationId) {
        AssessorAssignment assignment = participantAssignment(agencyId, caseId, assignmentId, actor, scope);
        version(assignment.getVersion(), request.assignmentRecordVersion());
        version(assignment.getAssessmentCase().getVersion(), request.caseRecordVersion());
        AssessmentRating rating = ratings.findByAgencyIdAndAssignmentIdAndCompetencyId(
                agencyId, assignmentId, request.competencyVersionId())
                .orElseThrow(() -> new IllegalArgumentException("Save the competency rating before its evidence"));
        beginWork(agencyId, assignment.getAssessmentCase(), assignment, request.caseRecordVersion(),
                request.assignmentRecordVersion(), actor);
        rating = ratings.findByAgencyIdAndId(agencyId, rating.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Assessment rating not found"));
        AssessmentEvidence entity = evidence.saveAndFlush(new AssessmentEvidence(agencyId, rating,
                request.evidenceType(), request.titleReference(), request.evidenceDate(), request.description(),
                request.sourceSystem(), request.sourceReference(), actor));
        record(agencyId, "CREATE_EVIDENCE", "ASSESSMENT_EVIDENCE", entity.getId(), entity.getVersion(),
                null, evidenceResponse(entity), null, correlationId);
        return ownWork(agencyId, caseId, assignmentId, actor, scope);
    }

    @Override
    public AssessmentWorkResponse updateEvidence(String agencyId, String caseId, String assignmentId,
            String evidenceId, UpdateEvidenceRequest request, String actor, PermissionDataScope scope,
            String correlationId) {
        AssessorAssignment assignment = participantAssignment(agencyId, caseId, assignmentId, actor, scope);
        version(assignment.getVersion(), request.assignmentRecordVersion());
        version(assignment.getAssessmentCase().getVersion(), request.caseRecordVersion());
        AssessmentEvidence entity = ownedEvidence(agencyId, assignmentId, evidenceId);
        version(entity.getVersion(), request.recordVersion());
        EvidenceResponse before = evidenceResponse(entity);
        beginWork(agencyId, assignment.getAssessmentCase(), assignment, request.caseRecordVersion(),
                request.assignmentRecordVersion(), actor);
        entity = ownedEvidence(agencyId, assignmentId, evidenceId);
        entity.update(request.evidenceType(), request.titleReference(), request.evidenceDate(),
                request.description(), request.sourceSystem(), request.sourceReference(), request.recordVersion(), actor);
        entity = evidence.saveAndFlush(entity);
        record(agencyId, "UPDATE_EVIDENCE", "ASSESSMENT_EVIDENCE", evidenceId, entity.getVersion(), before,
                evidenceResponse(entity), null, correlationId);
        return ownWork(agencyId, caseId, assignmentId, actor, scope);
    }

    @Override
    public AssessmentWorkResponse archiveEvidence(String agencyId, String caseId, String assignmentId,
            String evidenceId, WorkTransitionRequest request, String actor, PermissionDataScope scope,
            String correlationId) {
        AssessorAssignment assignment = participantAssignment(agencyId, caseId, assignmentId, actor, scope);
        version(assignment.getVersion(), request.assignmentRecordVersion());
        version(assignment.getAssessmentCase().getVersion(), request.caseRecordVersion());
        AssessmentEvidence entity = ownedEvidence(agencyId, assignmentId, evidenceId);
        EvidenceResponse before = evidenceResponse(entity);
        if (request.recordVersion() == null) throw new OptimisticConflictException(
                "recordVersion is required when archiving evidence");
        beginWork(agencyId, assignment.getAssessmentCase(), assignment, request.caseRecordVersion(),
                request.assignmentRecordVersion(), actor);
        entity = ownedEvidence(agencyId, assignmentId, evidenceId);
        entity.archive(request.recordVersion(), actor); entity = evidence.saveAndFlush(entity);
        record(agencyId, "ARCHIVE_EVIDENCE", "ASSESSMENT_EVIDENCE", evidenceId, entity.getVersion(), before,
                null, request.reason(), correlationId);
        return ownWork(agencyId, caseId, assignmentId, actor, scope);
    }

    @Override
    public AssessmentWorkResponse submit(String agencyId, String caseId, String assignmentId,
            WorkTransitionRequest request, String actor, PermissionDataScope scope, String correlationId) {
        AssessorAssignment assignment = participantAssignment(agencyId, caseId, assignmentId, actor, scope);
        version(assignment.getVersion(), request.assignmentRecordVersion());
        AssessmentCase assessmentCase = assignment.getAssessmentCase();
        version(assessmentCase.getVersion(), request.caseRecordVersion());
        assessmentCase.getTool().requirePublishedForExecution();
        requireComplete(agencyId, assessmentCase, assignment);
        ContributionResponse before = contribution(assignment);
        assignment.submit(actor, Instant.now()); assignments.saveAndFlush(assignment);
        long active = assignments.countByAssessmentCaseIdAndActiveTrue(caseId);
        long submitted = assignments.countByAssessmentCaseIdAndActiveTrueAndStatus(
                caseId, AssessorAssignmentStatus.SUBMITTED.name());
        if (active == submitted) {
            assessmentCase.markForValidation(Instant.now()); cases.saveAndFlush(assessmentCase);
        } else if (assessmentCase.getStatus() == AssessmentCaseStatus.ASSIGNED) {
            assessmentCase.markInProgress(); cases.saveAndFlush(assessmentCase);
        }
        record(agencyId, "SUBMIT_ASSESSMENT", "ASSESSOR_ASSIGNMENT", assignmentId,
                assignment.getVersion(), before, contribution(assignment), request.reason(), correlationId);
        return work(assessmentCase, assignment);
    }

    @Override
    public ReturnCaseResponse returnCase(String agencyId, String caseId, ReturnCaseRequest request,
                                         String actor, String correlationId) {
        requireActor(actor);
        if (request.reason() == null || request.reason().isBlank()) {
            throw new IllegalArgumentException("A return reason is required");
        }
        AssessmentCase assessmentCase = assessmentCase(agencyId, caseId);
        version(assessmentCase.getVersion(), request.caseRecordVersion());
        assessmentCase.getTool().requirePublishedForExecution();
        if (assessmentCase.getStatus() != AssessmentCaseStatus.FOR_VALIDATION) {
            throw new IllegalLifecycleTransitionException(
                    "Only a FOR_VALIDATION assessment case may be returned for correction");
        }
        List<AssessorAssignment> contributions = assignments
                .findByAssessmentCaseIdAndActiveTrueOrderByCreatedAtAsc(caseId);
        if (contributions.isEmpty() || contributions.stream()
                .anyMatch(item -> item.getStatus() != AssessorAssignmentStatus.SUBMITTED)) {
            throw new IllegalLifecycleTransitionException(
                    "Every active contribution must be SUBMITTED before the case can be returned");
        }
        ReturnCaseResponse before = new ReturnCaseResponse(caseId, assessmentCase.getStatus(),
                assessmentCase.getVersion(), 0);
        contributions.forEach(AssessorAssignment::returnForCorrection);
        assignments.saveAll(contributions);
        assignments.flush();
        assessmentCase.returnForCorrection();
        assessmentCase = cases.saveAndFlush(assessmentCase);
        ReturnCaseResponse after = new ReturnCaseResponse(caseId, assessmentCase.getStatus(),
                assessmentCase.getVersion(), contributions.size());
        record(agencyId, "RETURN_ASSESSMENT", "ASSESSMENT_CASE", caseId,
                assessmentCase.getVersion(), before, after, request.reason(), correlationId);
        return after;
    }

    private void beginWork(String agencyId, AssessmentCase assessmentCase, AssessorAssignment assignment,
                           long caseVersion, long assignmentVersion, String actor) {
        assessmentCase.getTool().requirePublishedForExecution();
        if (assignments.beginWork(agencyId, assignment.getId(), actor, assignmentVersion) != 1) {
            throw new OptimisticConflictException("The assignment changed or is no longer editable");
        }
        if (cases.beginWork(agencyId, assessmentCase.getId(), caseVersion) != 1) {
            throw new OptimisticConflictException("The assessment case changed or is no longer editable");
        }
    }

    private void requireComplete(String agencyId, AssessmentCase assessmentCase, AssessorAssignment assignment) {
        List<PositionProfileRequirement> required = activeRequirements(agencyId, assessmentCase);
        List<AssessmentRating> contribution = ratings
                .findByAssignmentIdAndActiveTrueOrderByCompetencyCode(assignment.getId());
        Set<String> rated = contribution.stream().map(item -> item.getCompetency().getId())
                .collect(java.util.stream.Collectors.toSet());
        if (required.isEmpty() || required.stream().anyMatch(item -> !rated.contains(item.getCompetency().getId()))) {
            throw new IllegalArgumentException("Every active competency requirement must have a rating");
        }
        boolean evidenceRequired = methods.findByToolIdAndActiveTrueOrderByMethodCode(
                assessmentCase.getTool().getId()).stream()
                .anyMatch(item -> item.getMethod() == assignment.getMethod() && item.isEvidenceRequired());
        if (evidenceRequired && contribution.stream().anyMatch(item ->
                !evidence.existsByRatingIdAndActiveTrue(item.getId()))) {
            throw new IllegalArgumentException("Every rating requires structured evidence for this method");
        }
    }

    private PositionProfileRequirement requirement(String agencyId, AssessmentCase assessmentCase,
                                                   String competencyId) {
        return activeRequirements(agencyId, assessmentCase).stream()
                .filter(item -> item.getCompetency().getId().equals(competencyId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "The competency is not part of this assessment tool's exact profile"));
    }

    private List<PositionProfileRequirement> activeRequirements(String agencyId, AssessmentCase assessmentCase) {
        return requirements.findByProfileIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAscIdAsc(
                assessmentCase.getTool().getPositionProfile().getId(), agencyId);
    }

    private AssessmentWorkResponse ownWork(String agencyId, String caseId, String assignmentId,
                                            String actor, PermissionDataScope scope) {
        return work(assessmentCase(agencyId, caseId), participantAssignment(
                agencyId, caseId, assignmentId, actor, scope));
    }

    private AssessmentWorkResponse work(AssessmentCase entity, AssessorAssignment visible) {
        AssessmentTool tool = entity.getTool(); AssessmentCycle cycle = tool.getCycle();
        return new AssessmentWorkResponse(entity.getId(), cycle.getId(), cycle.getCode(), cycle.getName(),
                tool.getId(), tool.getName(), tool.getInstructions(), entity.getSubjectEmployeeNo(),
                entity.getSubjectDisplayName(), entity.getStatus(), entity.getVersion(), entity.getForValidationAt(),
                activeRequirements(entity.getAgencyId(), entity).stream().map(this::requirementResponse).toList(),
                List.of(contribution(visible)));
    }

    private RequirementResponse requirementResponse(PositionProfileRequirement item) {
        Competency competency = item.getCompetency(); ProficiencyLevel required = item.getRequiredProficiencyLevel();
        return new RequirementResponse(competency.getId(), competency.getCode(), competency.getName(),
                competency.getDefinitionVersion(), required.getId(), required.getCode(),
                item.getClassification().name(), item.getCriticalityCode(),
                levels.findByScaleIdAndActiveTrueOrderByLevelOrderAsc(competency.getProficiencyScale().getId()).stream()
                        .map(level -> new LevelOptionResponse(level.getId(), level.getCode(), level.getLabel(),
                                level.getLevelOrder())).toList());
    }

    private ContributionResponse contribution(AssessorAssignment entity) {
        return new ContributionResponse(entity.getId(), entity.getMethod(), entity.getAssessorEmployeeNo(),
                entity.getAssessorDisplayName(), entity.getStatus(), entity.getVersion(), entity.getSubmittedBy(),
                entity.getSubmittedAt(), ratings.findByAssignmentIdAndActiveTrueOrderByCompetencyCode(entity.getId())
                        .stream().map(this::ratingResponse).toList());
    }

    private RatingResponse ratingResponse(AssessmentRating entity) {
        return new RatingResponse(entity.getId(), entity.getCompetency().getId(), entity.getAttainedLevel().getId(),
                entity.getAttainedLevel().getCode(), entity.getRemarks(), entity.getBehavioralNotes(),
                entity.getVersion(), evidence.findByRatingIdAndActiveTrueOrderByEvidenceDateDescIdAsc(entity.getId())
                        .stream().map(this::evidenceResponse).toList());
    }

    private EvidenceResponse evidenceResponse(AssessmentEvidence entity) {
        return new EvidenceResponse(entity.getId(), entity.getEvidenceType(), entity.getTitleReference(),
                entity.getEvidenceDate(), entity.getDescription(), entity.getSourceSystem(),
                entity.getSourceReference(), entity.getVersion());
    }

    private InboxItemResponse inboxItem(AssessorAssignment entity) {
        AssessmentCase assessmentCase = entity.getAssessmentCase(); AssessmentTool tool = assessmentCase.getTool();
        AssessmentCycle cycle = tool.getCycle();
        return new InboxItemResponse(assessmentCase.getId(), entity.getId(), cycle.getCode(), cycle.getName(),
                tool.getName(), assessmentCase.getSubjectEmployeeNo(), assessmentCase.getSubjectDisplayName(),
                entity.getMethod(), assessmentCase.getStatus(), entity.getStatus(), assessmentCase.getVersion(),
                entity.getVersion());
    }

    private AssessorAssignment actorAssignment(String agencyId, String caseId, String actor,
                                                PermissionDataScope scope) {
        String employeeNo = requireActor(actor);
        return assignments.findByAssessmentCaseIdAndActiveTrueOrderByCreatedAtAsc(caseId).stream()
                .filter(item -> item.getAgencyId().equals(agencyId)
                        && item.getAssessorEmployeeNo().equalsIgnoreCase(employeeNo))
                .filter(item -> scopeAllowed(item, scope)).findFirst()
                .orElseThrow(() -> new AccessDeniedException("This assessment is not assigned to the current user"));
    }

    private AssessorAssignment participantAssignment(String agencyId, String caseId, String assignmentId,
            String actor, PermissionDataScope scope) {
        AssessorAssignment entity = assignment(agencyId, caseId, assignmentId);
        entity.requireActor(requireActor(actor));
        if (!scopeAllowed(entity, scope)) throw new AccessDeniedException(
                "The configured data scope does not allow this assessment contribution");
        return entity;
    }

    private static boolean scopeAllowed(AssessorAssignment assignment, PermissionDataScope scope) {
        if (scope == PermissionDataScope.AGENCY_WIDE || scope == PermissionDataScope.ASSIGNED_RECORDS) return true;
        return scope == PermissionDataScope.OWN_RECORDS
                && assignment.getMethod() == AssessmentMethod.SELF_ASSESSMENT
                && assignment.getAssessmentCase().getSubjectEmployeeNo()
                    .equalsIgnoreCase(assignment.getAssessorEmployeeNo());
    }

    private AssessmentEvidence ownedEvidence(String agencyId, String assignmentId, String id) {
        AssessmentEvidence entity = evidence.findByAgencyIdAndId(agencyId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment evidence not found"));
        if (!entity.getRating().getAssignment().getId().equals(assignmentId)) {
            throw new ResourceNotFoundException("Assessment evidence not found");
        }
        return entity;
    }
    private AssessmentCase assessmentCase(String agencyId, String id) { return cases.findByAgencyIdAndId(agencyId, id)
            .orElseThrow(() -> new ResourceNotFoundException("Assessment case not found")); }
    private AssessorAssignment assignment(String agencyId, String caseId, String id) {
        AssessorAssignment entity = assignments.findByAgencyIdAndId(agencyId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessor assignment not found"));
        if (!entity.getAssessmentCase().getId().equals(caseId)) {
            throw new ResourceNotFoundException("Assessor assignment not found");
        }
        return entity;
    }
    private static String requireActor(String actor) { if (actor == null || actor.isBlank())
        throw new AccessDeniedException("Authenticated employee identity is required"); return actor.trim(); }
    private static void version(long actual, Long expected) { if (expected == null || actual != expected)
        throw new OptimisticConflictException("Expected recordVersion " + expected + " but current version is " + actual); }
    private static void validatePage(int page, int size) { if (page < 0 || size < 1 || size > 100)
        throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100"); }
    private void record(String agency, String action, String type, String id, long version, Object before,
                        Object after, String reason, String correlation) {
        audit.record(agency, action, type, id, null, version, before, after,
                reason == null || reason.isBlank() ? null : reason.trim(), correlation);
    }
}
