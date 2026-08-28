package com.primehr.assessment.application;

import com.primehr.assessment.api.AssessmentDtos.*;
import com.primehr.assessment.domain.*;
import com.primehr.assessment.infrastructure.*;
import com.primehr.integration.humanresource.*;
import com.primehr.positionprofile.domain.PositionProfile;
import com.primehr.positionprofile.domain.PositionProfileStatus;
import com.primehr.positionprofile.infrastructure.PositionProfileRepository;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.Instant;

@Service
@Transactional
public class AssessmentAdministrationServiceImpl implements AssessmentAdministrationService {
    private static final Sort CYCLE_ORDER = Sort.by("effectiveFrom").descending().and(Sort.by("code"));
    private final AssessmentCycleRepository cycles;
    private final AssessmentToolRepository tools;
    private final AssessmentToolMethodRepository methods;
    private final AssessmentCaseRepository cases;
    private final AssessorAssignmentRepository assignments;
    private final PositionProfileRepository profiles;
    private final HumanResourceAssessmentSubjectClient subjects;
    private final PrimeHrAuditService audit;

    public AssessmentAdministrationServiceImpl(AssessmentCycleRepository cycles,
            AssessmentToolRepository tools, AssessmentToolMethodRepository methods,
            AssessmentCaseRepository cases, AssessorAssignmentRepository assignments,
            PositionProfileRepository profiles, HumanResourceAssessmentSubjectClient subjects,
            PrimeHrAuditService audit) {
        this.cycles = cycles; this.tools = tools; this.methods = methods; this.cases = cases;
        this.assignments = assignments; this.profiles = profiles; this.subjects = subjects;
        this.audit = audit;
    }

    @Override @Transactional(readOnly = true)
    public PageResponse<CycleResponse> listCycles(String agencyId, AssessmentCycleStatus status, int page, int size) {
        validatePage(page, size);
        var pageable = PageRequest.of(page, size, CYCLE_ORDER);
        var result = status == null ? cycles.findByAgencyId(agencyId, pageable)
                : cycles.findByAgencyIdAndStatus(agencyId, status.name(), pageable);
        return PageResponse.from(result, this::cycleResponse);
    }

    @Override @Transactional(readOnly = true)
    public CycleResponse getCycle(String agencyId, String cycleId) { return cycleResponse(cycle(agencyId, cycleId)); }

    @Override
    public CycleResponse createCycle(String agencyId, CreateCycleRequest request, String correlationId) {
        if (cycles.existsByAgencyIdAndCodeIgnoreCase(agencyId, request.code())) {
            throw new IllegalArgumentException("Assessment cycle code already exists");
        }
        AssessmentCycle entity = cycles.saveAndFlush(new AssessmentCycle(agencyId, request.code(), request.name(),
                request.description(), request.effectiveFrom(), request.effectiveTo()));
        CycleResponse after = cycleResponse(entity);
        record(agencyId, "CREATE_DRAFT", "ASSESSMENT_CYCLE", entity.getId(), entity.getVersion(), null, after,
                null, correlationId);
        return after;
    }

    @Override
    public CycleResponse updateCycle(String agencyId, String cycleId, UpdateCycleRequest request,
                                     String correlationId) {
        AssessmentCycle entity = cycle(agencyId, cycleId);
        version(entity.getVersion(), request.recordVersion());
        if (tools.existsByCycleId(cycleId)) {
            throw new IllegalArgumentException("A cycle with tools cannot change its definition dates");
        }
        CycleResponse before = cycleResponse(entity);
        entity.updateDraft(request.name(), request.description(), request.effectiveFrom(), request.effectiveTo());
        entity = cycles.saveAndFlush(entity);
        CycleResponse after = cycleResponse(entity);
        record(agencyId, "UPDATE_DRAFT", "ASSESSMENT_CYCLE", cycleId, entity.getVersion(), before, after,
                null, correlationId);
        return after;
    }

    @Override
    public CycleResponse archiveCycle(String agencyId, String cycleId, TransitionRequest request,
                                      String correlationId) {
        AssessmentCycle entity = cycle(agencyId, cycleId);
        version(entity.getVersion(), request.recordVersion());
        if (tools.existsByCycleId(cycleId)) throw new IllegalArgumentException("Archive the cycle's tools first");
        CycleResponse before = cycleResponse(entity);
        entity.archiveDraft(); entity = cycles.saveAndFlush(entity);
        CycleResponse after = cycleResponse(entity);
        record(agencyId, "ARCHIVE_DRAFT", "ASSESSMENT_CYCLE", cycleId, entity.getVersion(), before, after,
                request.reason(), correlationId);
        return after;
    }

    @Override
    public CycleResponse openCycle(String agencyId, String cycleId, TransitionRequest request,
                                   String correlationId) {
        AssessmentCycle entity = cycle(agencyId, cycleId);
        version(entity.getVersion(), request.recordVersion()); entity.requireDraft();
        List<AssessmentTool> cycleTools = tools.findByAgencyIdAndCycleId(agencyId, cycleId, Sort.by("name"));
        List<AssessmentTool> activeTools = cycleTools.stream().filter(AssessmentTool::isActive).toList();
        List<AssessmentTool> published = activeTools.stream()
                .filter(item -> item.getStatus() == AssessmentToolStatus.PUBLISHED).toList();
        if (published.isEmpty() || published.size() != activeTools.size()) {
            throw new IllegalArgumentException("Every cycle tool must be PUBLISHED before the cycle opens");
        }
        List<AssessmentCase> cycleCases = cases.findByAgencyIdAndToolCycleIdAndActiveTrue(agencyId, cycleId);
        if (cycleCases.isEmpty()) throw new IllegalArgumentException("The cycle must contain assessment subjects");
        for (AssessmentTool tool : published) {
            List<AssessmentCase> toolCases = cycleCases.stream()
                    .filter(item -> item.getTool().getId().equals(tool.getId())).toList();
            if (toolCases.isEmpty()) throw new IllegalArgumentException("Every published tool requires a subject");
            Set<AssessmentMethod> required = methods.findByToolIdAndActiveTrueOrderByMethodCode(tool.getId())
                    .stream().map(AssessmentToolMethod::getMethod).collect(java.util.stream.Collectors.toSet());
            for (AssessmentCase assessmentCase : toolCases) {
                List<AssessorAssignment> activeAssignments = assignments
                        .findByAssessmentCaseIdAndActiveTrueOrderByCreatedAtAsc(assessmentCase.getId());
                Set<AssessmentMethod> assigned = activeAssignments.stream().map(AssessorAssignment::getMethod)
                        .collect(java.util.stream.Collectors.toSet());
                if (!assigned.containsAll(required)) throw new IllegalArgumentException(
                        "Every subject requires an assessor for each tool method before opening");
            }
        }
        CycleResponse before = cycleResponse(entity);
        entity.open(audit.currentActor(), Instant.now()); cycles.saveAndFlush(entity);
        cycleCases.forEach(AssessmentCase::assignForOpenCycle); cases.saveAll(cycleCases); cases.flush();
        List<AssessorAssignment> activeAssignments = cycleCases.stream().flatMap(item -> assignments
                .findByAssessmentCaseIdAndActiveTrueOrderByCreatedAtAsc(item.getId()).stream()).toList();
        activeAssignments.forEach(AssessorAssignment::assignForOpenCycle);
        assignments.saveAll(activeAssignments); assignments.flush();
        CycleResponse after = cycleResponse(entity);
        record(agencyId, "OPEN_CYCLE", "ASSESSMENT_CYCLE", cycleId, entity.getVersion(), before, after,
                request.reason(), correlationId);
        return after;
    }

    @Override
    public CycleResponse closeCycle(String agencyId, String cycleId, TransitionRequest request,
                                    String correlationId) {
        AssessmentCycle entity = cycle(agencyId, cycleId); version(entity.getVersion(), request.recordVersion());
        CycleResponse before = cycleResponse(entity);
        entity.close(audit.currentActor(), Instant.now()); entity = cycles.saveAndFlush(entity);
        CycleResponse after = cycleResponse(entity);
        record(agencyId, "CLOSE_CYCLE", "ASSESSMENT_CYCLE", cycleId, entity.getVersion(), before, after,
                request.reason(), correlationId);
        return after;
    }

    @Override @Transactional(readOnly = true)
    public List<ToolResponse> listTools(String agencyId, String cycleId) {
        cycle(agencyId, cycleId);
        return tools.findByAgencyIdAndCycleId(agencyId, cycleId, Sort.by("name"))
                .stream().map(this::toolResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public ToolResponse getTool(String agencyId, String toolId) { return toolResponse(tool(agencyId, toolId)); }

    @Override
    public ToolResponse createTool(String agencyId, String cycleId, CreateToolRequest request,
                                   String correlationId) {
        AssessmentCycle cycle = cycle(agencyId, cycleId); cycle.requireDraft();
        PositionProfile profile = profiles.findByIdAndAgencyId(request.positionProfileId(), agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Active position profile not found"));
        if (profile.getStatus() != PositionProfileStatus.ACTIVE || !profile.isActive()
                || cycle.getEffectiveFrom() != null && !profile.isEffectiveOn(cycle.getEffectiveFrom())) {
            throw new IllegalArgumentException("The position profile must be ACTIVE on the cycle effective date");
        }
        validateMethods(request.methods());
        if (tools.existsByCycleIdAndNameIgnoreCase(cycleId, request.name())) {
            throw new IllegalArgumentException("Assessment tool name already exists in this cycle");
        }
        AssessmentTool entity = tools.saveAndFlush(new AssessmentTool(agencyId, cycle, profile,
                request.name(), request.instructions()));
        for (MethodRequest method : request.methods()) methods.save(new AssessmentToolMethod(
                agencyId, entity, method.method(), method.evidenceRequired()));
        methods.flush();
        ToolResponse after = toolResponse(entity);
        record(agencyId, "CREATE_DRAFT", "ASSESSMENT_TOOL", entity.getId(), entity.getVersion(), null, after,
                null, correlationId);
        return after;
    }

    @Override
    public ToolResponse updateTool(String agencyId, String toolId, UpdateToolRequest request,
                                   String correlationId) {
        AssessmentTool entity = tool(agencyId, toolId);
        version(entity.getVersion(), request.recordVersion()); validateMethods(request.methods());
        if (cases.existsByToolId(toolId)) throw new IllegalArgumentException("A tool with subjects cannot be changed");
        ToolResponse before = toolResponse(entity);
        entity.updateDraft(request.name(), request.instructions());
        syncMethods(agencyId, entity, request.methods());
        entity = tools.saveAndFlush(entity); methods.flush();
        ToolResponse after = toolResponse(entity);
        record(agencyId, "UPDATE_DRAFT", "ASSESSMENT_TOOL", toolId, entity.getVersion(), before, after,
                null, correlationId);
        return after;
    }

    @Override
    public ToolResponse archiveTool(String agencyId, String toolId, TransitionRequest request,
                                    String correlationId) {
        AssessmentTool entity = tool(agencyId, toolId); version(entity.getVersion(), request.recordVersion());
        if (cases.existsByToolId(toolId)) throw new IllegalArgumentException("Archive subjects before the tool");
        ToolResponse before = toolResponse(entity); entity.archiveDraft(); entity = tools.saveAndFlush(entity);
        ToolResponse after = toolResponse(entity);
        record(agencyId, "ARCHIVE_DRAFT", "ASSESSMENT_TOOL", toolId, entity.getVersion(), before, after,
                request.reason(), correlationId);
        return after;
    }

    @Override
    public ToolResponse publishTool(String agencyId, String toolId, TransitionRequest request,
                                    String correlationId) {
        AssessmentTool entity = tool(agencyId, toolId); version(entity.getVersion(), request.recordVersion());
        PositionProfile profile = entity.getPositionProfile();
        if (profile.getStatus() != PositionProfileStatus.ACTIVE || !profile.isActive()
                || entity.getCycle().getEffectiveFrom() != null
                && !profile.isEffectiveOn(entity.getCycle().getEffectiveFrom())
                || profile.getDefinitionVersion() != entity.getProfileDefinitionVersion()
                || profile.getContentRevision() != entity.getProfileContentRevision()
                || !Objects.equals(profile.getSourceFingerprint(), entity.getProfileSourceFingerprint())) {
            throw new IllegalArgumentException(
                    "The assessment tool's exact position profile is no longer active or unchanged");
        }
        List<AssessmentToolMethod> activeMethods = methods.findByToolIdAndActiveTrueOrderByMethodCode(toolId);
        if (activeMethods.isEmpty()) throw new IllegalArgumentException("An assessment tool requires a method");
        List<AssessmentCase> toolCases = cases.findByAgencyIdAndToolIdAndActiveTrue(agencyId, toolId);
        if (toolCases.isEmpty()) throw new IllegalArgumentException("An assessment tool requires a subject");
        Set<AssessmentMethod> required = activeMethods.stream().map(AssessmentToolMethod::getMethod)
                .collect(java.util.stream.Collectors.toSet());
        for (AssessmentCase assessmentCase : toolCases) {
            Set<AssessmentMethod> assigned = assignments
                    .findByAssessmentCaseIdAndActiveTrueOrderByCreatedAtAsc(assessmentCase.getId()).stream()
                    .map(AssessorAssignment::getMethod).collect(java.util.stream.Collectors.toSet());
            if (!assigned.containsAll(required)) throw new IllegalArgumentException(
                    "Every subject requires an assessor for each tool method before publication");
        }
        ToolResponse before = toolResponse(entity); entity.publish(audit.currentActor(), Instant.now());
        entity = tools.saveAndFlush(entity); ToolResponse after = toolResponse(entity);
        record(agencyId, "PUBLISH_TOOL", "ASSESSMENT_TOOL", toolId, entity.getVersion(), before, after,
                request.reason(), correlationId);
        return after;
    }

    @Override @Transactional(readOnly = true)
    public PageResponse<CaseResponse> listCases(String agencyId, String toolId, int page, int size) {
        validatePage(page, size); tool(agencyId, toolId);
        return PageResponse.from(cases.findByAgencyIdAndToolId(agencyId, toolId,
                PageRequest.of(page, size, Sort.by("subjectEmployeeNo"))), this::caseResponse);
    }

    @Override @Transactional(readOnly = true)
    public CaseResponse getCase(String agencyId, String caseId) { return caseResponse(assessmentCase(agencyId, caseId)); }

    @Override
    public CaseResponse addSubject(String agencyId, String toolId, AddSubjectRequest request,
                                   String authorizationHeader, String correlationId) {
        AssessmentTool tool = tool(agencyId, toolId); version(tool.getVersion(), request.toolRecordVersion());
        tool.requireDraft();
        if (cases.existsByToolIdAndSubjectEmployeeId(toolId, request.employeeId())) {
            throw new IllegalArgumentException("Employee is already assigned to this assessment tool");
        }
        AssessmentSubjectSnapshot snapshot = snapshot(subjects.get(request.employeeId(), authorizationHeader));
        AssessmentCase entity = cases.saveAndFlush(new AssessmentCase(agencyId, tool, snapshot));
        bumpToolVersion(agencyId, tool.getId(), request.toolRecordVersion());
        CaseResponse after = caseResponse(entity);
        record(agencyId, "ADD_DRAFT_SUBJECT", "ASSESSMENT_CASE", entity.getId(), entity.getVersion(), null, after,
                null, correlationId);
        return after;
    }

    @Override
    public CaseResponse archiveCase(String agencyId, String caseId, TransitionRequest request,
                                    String correlationId) {
        AssessmentCase entity = assessmentCase(agencyId, caseId); version(entity.getVersion(), request.recordVersion());
        if (assignments.existsByAssessmentCaseIdAndActiveTrue(caseId)) {
            throw new IllegalArgumentException("Archive active assessor assignments before the subject");
        }
        CaseResponse before = caseResponse(entity); entity.archiveDraft(); entity = cases.saveAndFlush(entity);
        CaseResponse after = caseResponse(entity);
        record(agencyId, "ARCHIVE_DRAFT_SUBJECT", "ASSESSMENT_CASE", caseId, entity.getVersion(), before, after,
                request.reason(), correlationId);
        return after;
    }

    @Override
    public CaseResponse addAssessor(String agencyId, String caseId, AddAssessorRequest request,
                                    String authorizationHeader, String correlationId) {
        AssessmentCase entity = assessmentCase(agencyId, caseId); version(entity.getVersion(), request.caseRecordVersion());
        requireAssignmentReason(request.method(), request.reason());
        if (assignments.existsByAssessmentCaseIdAndMethodCodeAndAssessorEmployeeId(
                caseId, request.method().name(), request.employeeId())) {
            throw new IllegalArgumentException("This assessor and method are already assigned");
        }
        boolean supported = methods.findByToolIdAndActiveTrueOrderByMethodCode(entity.getTool().getId())
                .stream().anyMatch(method -> method.getMethod() == request.method());
        if (!supported) throw new IllegalArgumentException("The method is not enabled for this assessment tool");
        AssessmentSubjectSnapshot snapshot = snapshot(subjects.get(request.employeeId(), authorizationHeader));
        AssessorAssignment assignment = assignments.saveAndFlush(new AssessorAssignment(
                agencyId, entity, request.method(), snapshot, request.reason()));
        bumpCaseVersion(agencyId, caseId, request.caseRecordVersion());
        entity = assessmentCase(agencyId, caseId);
        record(agencyId, "ADD_DRAFT_ASSESSOR", "ASSESSOR_ASSIGNMENT", assignment.getId(),
                assignment.getVersion(), null, assignmentResponse(assignment), request.reason(), correlationId);
        return caseResponse(entity);
    }

    @Override
    public CaseResponse updateAssessor(String agencyId, String caseId, String assignmentId,
                                       UpdateAssessorRequest request, String correlationId) {
        AssessmentCase entity = assessmentCase(agencyId, caseId); version(entity.getVersion(), request.caseRecordVersion());
        AssessorAssignment assignment = assignment(agencyId, caseId, assignmentId);
        version(assignment.getVersion(), request.recordVersion()); requireAssignmentReason(assignment.getMethod(), request.reason());
        AssignmentResponse before = assignmentResponse(assignment); assignment.updateDraft(request.reason());
        assignment = assignments.saveAndFlush(assignment);
        bumpCaseVersion(agencyId, caseId, request.caseRecordVersion());
        entity = assessmentCase(agencyId, caseId);
        record(agencyId, "UPDATE_DRAFT_ASSESSOR", "ASSESSOR_ASSIGNMENT", assignmentId,
                assignment.getVersion(), before, assignmentResponse(assignment), request.reason(), correlationId);
        return caseResponse(entity);
    }

    @Override
    public CaseResponse archiveAssessor(String agencyId, String caseId, String assignmentId,
                                        TransitionRequest request, String correlationId) {
        AssessmentCase entity = assessmentCase(agencyId, caseId);
        AssessorAssignment assignment = assignment(agencyId, caseId, assignmentId);
        version(assignment.getVersion(), request.recordVersion());
        AssignmentResponse before = assignmentResponse(assignment); assignment.archiveDraft();
        assignment = assignments.saveAndFlush(assignment);
        bumpCaseVersion(agencyId, caseId, entity.getVersion());
        entity = assessmentCase(agencyId, caseId);
        record(agencyId, "ARCHIVE_DRAFT_ASSESSOR", "ASSESSOR_ASSIGNMENT", assignmentId,
                assignment.getVersion(), before, assignmentResponse(assignment), request.reason(), correlationId);
        return caseResponse(entity);
    }

    private AssessmentCycle cycle(String agencyId, String id) { return cycles.findByAgencyIdAndId(agencyId, id)
            .orElseThrow(() -> new ResourceNotFoundException("Assessment cycle not found")); }
    private AssessmentTool tool(String agencyId, String id) { return tools.findByAgencyIdAndId(agencyId, id)
            .orElseThrow(() -> new ResourceNotFoundException("Assessment tool not found")); }
    private AssessmentCase assessmentCase(String agencyId, String id) { return cases.findByAgencyIdAndId(agencyId, id)
            .orElseThrow(() -> new ResourceNotFoundException("Assessment case not found")); }
    private AssessorAssignment assignment(String agencyId, String caseId, String id) {
        AssessorAssignment result = assignments.findByAgencyIdAndId(agencyId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessor assignment not found"));
        if (!result.getAssessmentCase().getId().equals(caseId)) throw new ResourceNotFoundException("Assessor assignment not found");
        return result;
    }

    private void syncMethods(String agencyId, AssessmentTool tool, List<MethodRequest> requested) {
        Map<AssessmentMethod, AssessmentToolMethod> existing = methods.findByToolIdAndActiveTrueOrderByMethodCode(tool.getId())
                .stream().collect(java.util.stream.Collectors.toMap(AssessmentToolMethod::getMethod, item -> item));
        for (MethodRequest item : requested) {
            AssessmentToolMethod method = existing.remove(item.method());
            if (method == null) methods.save(new AssessmentToolMethod(agencyId, tool, item.method(), item.evidenceRequired()));
            else method.updateDraft(item.evidenceRequired());
        }
        existing.values().forEach(AssessmentToolMethod::archiveDraft);
    }

    private ToolResponse toolResponse(AssessmentTool entity) {
        return new ToolResponse(entity.getId(), entity.getCycle().getId(), entity.getPositionProfile().getId(),
                entity.getName(), entity.getInstructions(), entity.getStatus(), entity.getProfileDefinitionVersion(),
                entity.getProfileContentRevision(), entity.getProfileTargetKey(), entity.getProfileName(),
                entity.getProfileSourceFingerprint(), entity.getEffectiveFrom(), entity.getEffectiveTo(),
                entity.getVersion(), entity.getPublishedBy(), entity.getPublishedAt(),
                methods.findByToolIdAndActiveTrueOrderByMethodCode(entity.getId()).stream()
                        .map(item -> new MethodResponse(item.getId(), item.getMethod(), item.isEvidenceRequired(),
                                item.getVersion())).toList());
    }
    private CycleResponse cycleResponse(AssessmentCycle entity) { return new CycleResponse(entity.getId(),
            entity.getCode(), entity.getName(), entity.getDescription(), entity.getStatus(), entity.getEffectiveFrom(),
            entity.getEffectiveTo(), entity.getVersion(), entity.getCreatedBy(), entity.getCreatedAt(),
            entity.getUpdatedBy(), entity.getUpdatedAt(), entity.getOpenedBy(), entity.getOpenedAt(),
            entity.getClosedBy(), entity.getClosedAt()); }
    private CaseResponse caseResponse(AssessmentCase entity) { return new CaseResponse(entity.getId(),
            entity.getTool().getId(), entity.getSubjectEmployeeId(), entity.getSubjectEmployeeNo(),
            entity.getSubjectDisplayName(), entity.getAppointmentId(), entity.getAssumptionToDutyDate(),
            entity.getJobPositionId(), entity.getPlantillaId(), entity.getSubjectSourceFingerprint(),
            entity.getSubjectSourceUpdatedAt(), entity.getSubjectSnapshotAt(), entity.getStatus(), entity.isActive(),
            entity.getVersion(), entity.getForValidationAt(), assignments.findByAssessmentCaseIdOrderByCreatedAtAsc(entity.getId()).stream()
                    .map(this::assignmentResponse).toList()); }
    private AssignmentResponse assignmentResponse(AssessorAssignment entity) { return new AssignmentResponse(
            entity.getId(), entity.getMethod(), entity.getAssessorEmployeeId(), entity.getAssessorEmployeeNo(),
            entity.getAssessorDisplayName(), entity.getAssignmentReason(), entity.getAssessorSourceFingerprint(),
            entity.getAssessorSnapshotAt(), entity.getStatus(), entity.isActive(), entity.getVersion(),
            entity.getSubmittedBy(), entity.getSubmittedAt()); }
    private static AssessmentSubjectSnapshot snapshot(HumanResourceAssessmentSubject source) {
        return new AssessmentSubjectSnapshot(source.employeeId(), source.employeeNo(), source.displayName(),
                source.appointmentId(), source.assumptionToDutyDate(), source.jobPositionId(), source.plantillaId(),
                source.sourceFingerprint(), source.sourceUpdatedAt(), source.fetchedAt()); }
    private static void validateMethods(List<MethodRequest> methods) {
        Set<AssessmentMethod> unique = EnumSet.noneOf(AssessmentMethod.class);
        for (MethodRequest method : methods) if (!unique.add(method.method()))
            throw new IllegalArgumentException("Assessment methods cannot be duplicated");
    }
    private static void requireAssignmentReason(AssessmentMethod method, String reason) {
        if ((method == AssessmentMethod.IMMEDIATE_SUPERVISOR || method == AssessmentMethod.PANEL)
                && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("Supervisor and panel assignments require an explicit reason");
        }
    }
    private void bumpToolVersion(String agencyId, String id, long expected) {
        if (tools.bumpVersion(agencyId, id, expected) != 1) throw new OptimisticConflictException(
                "Assessment tool changed while the subject was being assigned");
    }
    private void bumpCaseVersion(String agencyId, String id, long expected) {
        if (cases.bumpVersion(agencyId, id, expected) != 1) throw new OptimisticConflictException(
                "Assessment case changed while the assessor assignment was being changed");
    }
    private static void version(long actual, Long supplied) { if (supplied == null || actual != supplied)
        throw new OptimisticConflictException("Expected recordVersion " + supplied + " but current version is " + actual); }
    private static void validatePage(int page, int size) { if (page < 0 || size < 1 || size > 100)
        throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100"); }
    private void record(String agency, String action, String type, String id, long version, Object before,
                        Object after, String reason, String correlation) {
        audit.record(agency, action, type, id, null, version, before, after,
                reason == null || reason.isBlank() ? null : reason.trim(), correlation);
    }
}
