package com.primehr.assessment.api;

import com.primehr.assessment.api.AssessmentDtos.*;
import com.primehr.assessment.application.AssessmentAdministrationService;
import com.primehr.assessment.domain.AssessmentCycleStatus;
import com.primehr.security.*;
import com.primehr.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/primehr/v1/admin")
public class AssessmentAdministrationController {
    private final AssessmentAdministrationService service;
    private final AssessmentPermissionGuard permission;
    private final AgencyScopeResolver agencyScope;

    public AssessmentAdministrationController(AssessmentAdministrationService service,
            AssessmentPermissionGuard permission, AgencyScopeResolver agencyScope) {
        this.service = service; this.permission = permission; this.agencyScope = agencyScope;
    }

    @GetMapping("/assessment-cycles")
    public PageResponse<CycleResponse> listCycles(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "status", required = false) AssessmentCycleStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.requireAdministration(PrimeHrAction.ACCESS, token);
        return service.listCycles(agency(authentication), status, page, size);
    }

    @GetMapping("/assessment-cycles/{cycleId}")
    public CycleResponse getCycle(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable("cycleId") String cycleId) {
        permission.requireAdministration(PrimeHrAction.ACCESS, token);
        return service.getCycle(agency(authentication), cycleId);
    }

    @PostMapping("/assessment-cycles") @ResponseStatus(HttpStatus.CREATED)
    public CycleResponse createCycle(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody CreateCycleRequest request) {
        permission.requireAdministration(PrimeHrAction.ADD, token);
        return service.createCycle(agency(authentication), request, correlationId);
    }

    @PutMapping("/assessment-cycles/{cycleId}")
    public CycleResponse updateCycle(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("cycleId") String cycleId, @Valid @RequestBody UpdateCycleRequest request) {
        permission.requireAdministration(PrimeHrAction.EDIT, token);
        return service.updateCycle(agency(authentication), cycleId, request, correlationId);
    }

    @PostMapping("/assessment-cycles/{cycleId}/archive")
    public CycleResponse archiveCycle(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("cycleId") String cycleId, @Valid @RequestBody TransitionRequest request) {
        permission.requireAdministration(PrimeHrAction.ARCHIVE, token);
        return service.archiveCycle(agency(authentication), cycleId, request, correlationId);
    }

    @PostMapping("/assessment-cycles/{cycleId}/open")
    public CycleResponse openCycle(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("cycleId") String cycleId, @Valid @RequestBody TransitionRequest request) {
        permission.requireAdministration(PrimeHrAction.PUBLISH, token);
        return service.openCycle(agency(authentication), cycleId, request, correlationId);
    }

    @PostMapping("/assessment-cycles/{cycleId}/close")
    public CycleResponse closeCycle(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("cycleId") String cycleId, @Valid @RequestBody TransitionRequest request) {
        permission.requireAdministration(PrimeHrAction.FINALIZE, token);
        return service.closeCycle(agency(authentication), cycleId, request, correlationId);
    }

    @GetMapping("/assessment-cycles/{cycleId}/tools")
    public List<ToolResponse> listTools(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable("cycleId") String cycleId) {
        permission.requireAdministration(PrimeHrAction.ACCESS, token);
        return service.listTools(agency(authentication), cycleId);
    }

    @PostMapping("/assessment-cycles/{cycleId}/tools") @ResponseStatus(HttpStatus.CREATED)
    public ToolResponse createTool(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("cycleId") String cycleId, @Valid @RequestBody CreateToolRequest request) {
        permission.requireAdministration(PrimeHrAction.ADD, token);
        return service.createTool(agency(authentication), cycleId, request, correlationId);
    }

    @GetMapping("/assessment-tools/{toolId}")
    public ToolResponse getTool(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable("toolId") String toolId) {
        permission.requireAdministration(PrimeHrAction.ACCESS, token);
        return service.getTool(agency(authentication), toolId);
    }

    @PutMapping("/assessment-tools/{toolId}")
    public ToolResponse updateTool(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("toolId") String toolId, @Valid @RequestBody UpdateToolRequest request) {
        permission.requireAdministration(PrimeHrAction.EDIT, token);
        return service.updateTool(agency(authentication), toolId, request, correlationId);
    }

    @PostMapping("/assessment-tools/{toolId}/archive")
    public ToolResponse archiveTool(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("toolId") String toolId, @Valid @RequestBody TransitionRequest request) {
        permission.requireAdministration(PrimeHrAction.ARCHIVE, token);
        return service.archiveTool(agency(authentication), toolId, request, correlationId);
    }

    @PostMapping("/assessment-tools/{toolId}/publish")
    public ToolResponse publishTool(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("toolId") String toolId, @Valid @RequestBody TransitionRequest request) {
        permission.requireAdministration(PrimeHrAction.PUBLISH, token);
        return service.publishTool(agency(authentication), toolId, request, correlationId);
    }

    @GetMapping("/assessment-tools/{toolId}/subjects")
    public PageResponse<CaseResponse> listCases(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable("toolId") String toolId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.requireAdministration(PrimeHrAction.ACCESS, token);
        return service.listCases(agency(authentication), toolId, page, size);
    }

    @PostMapping("/assessment-tools/{toolId}/subjects") @ResponseStatus(HttpStatus.CREATED)
    public CaseResponse addSubject(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("toolId") String toolId, @Valid @RequestBody AddSubjectRequest request) {
        permission.requireAdministration(PrimeHrAction.ADD, token);
        return service.addSubject(agency(authentication), toolId, request, token, correlationId);
    }

    @GetMapping("/assessment-cases/{caseId}")
    public CaseResponse getCase(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable("caseId") String caseId) {
        permission.requireAdministration(PrimeHrAction.ACCESS, token);
        return service.getCase(agency(authentication), caseId);
    }

    @PostMapping("/assessment-cases/{caseId}/archive")
    public CaseResponse archiveCase(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("caseId") String caseId, @Valid @RequestBody TransitionRequest request) {
        permission.requireAdministration(PrimeHrAction.ARCHIVE, token);
        return service.archiveCase(agency(authentication), caseId, request, correlationId);
    }

    @PostMapping("/assessment-cases/{caseId}/assessors") @ResponseStatus(HttpStatus.CREATED)
    public CaseResponse addAssessor(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("caseId") String caseId, @Valid @RequestBody AddAssessorRequest request) {
        permission.requireAdministration(PrimeHrAction.ADD, token);
        return service.addAssessor(agency(authentication), caseId, request, token, correlationId);
    }

    @PutMapping("/assessment-cases/{caseId}/assessors/{assignmentId}")
    public CaseResponse updateAssessor(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("caseId") String caseId,
            @PathVariable("assignmentId") String assignmentId,
            @Valid @RequestBody UpdateAssessorRequest request) {
        permission.requireAdministration(PrimeHrAction.EDIT, token);
        return service.updateAssessor(agency(authentication), caseId, assignmentId, request, correlationId);
    }

    @PostMapping("/assessment-cases/{caseId}/assessors/{assignmentId}/archive")
    public CaseResponse archiveAssessor(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("caseId") String caseId,
            @PathVariable("assignmentId") String assignmentId,
            @Valid @RequestBody TransitionRequest request) {
        permission.requireAdministration(PrimeHrAction.ARCHIVE, token);
        return service.archiveAssessor(agency(authentication), caseId, assignmentId, request, correlationId);
    }

    private String agency(Authentication authentication) { return agencyScope.resolveAgencyId(authentication); }
}
