package com.primehr.assessment.api;

import com.primehr.assessment.api.AssessmentExecutionDtos.*;
import com.primehr.assessment.application.AssessmentExecutionService;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.security.*;
import com.primehr.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/primehr/v1/assessments")
public class AssessmentExecutionController {
    private final AssessmentExecutionService service;
    private final AssessmentPermissionGuard permission;
    private final AgencyScopeResolver agencyScope;

    public AssessmentExecutionController(AssessmentExecutionService service,
            AssessmentPermissionGuard permission, AgencyScopeResolver agencyScope) {
        this.service = service; this.permission = permission; this.agencyScope = agencyScope;
    }

    @GetMapping("/mine")
    public PageResponse<InboxItemResponse> mine(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.require(AssessmentPermissionGuard.ASSESSMENT, PrimeHrAction.ACCESS, token);
        return service.mine(agency(authentication), authentication.getName(), page, size);
    }

    @GetMapping("/{caseId}")
    public AssessmentWorkResponse get(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable("caseId") String caseId) {
        EffectiveFeaturePermission allowed = permission.require(
                AssessmentPermissionGuard.ASSESSMENT, PrimeHrAction.ACCESS, token);
        return service.getAssignedWork(agency(authentication), caseId, authentication.getName(),
                allowed.dataScope());
    }

    @PutMapping("/{caseId}/assignments/{assignmentId}/ratings/{competencyVersionId}")
    public AssessmentWorkResponse saveRating(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("caseId") String caseId,
            @PathVariable("assignmentId") String assignmentId,
            @PathVariable("competencyVersionId") String competencyVersionId,
            @Valid @RequestBody SaveRatingRequest request) {
        EffectiveFeaturePermission allowed = permission.require(
                AssessmentPermissionGuard.ASSESSMENT, PrimeHrAction.ASSESS, token);
        return service.saveRating(agency(authentication), caseId, assignmentId, competencyVersionId,
                request, authentication.getName(), allowed.dataScope(), correlationId);
    }

    @PostMapping("/{caseId}/assignments/{assignmentId}/evidence")
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentWorkResponse createEvidence(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("caseId") String caseId,
            @PathVariable("assignmentId") String assignmentId,
            @Valid @RequestBody CreateEvidenceRequest request) {
        EffectiveFeaturePermission allowed = permission.require(
                AssessmentPermissionGuard.ASSESSMENT, PrimeHrAction.ASSESS, token);
        return service.createEvidence(agency(authentication), caseId, assignmentId, request,
                authentication.getName(), allowed.dataScope(), correlationId);
    }

    @PutMapping("/{caseId}/assignments/{assignmentId}/evidence/{evidenceId}")
    public AssessmentWorkResponse updateEvidence(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("caseId") String caseId,
            @PathVariable("assignmentId") String assignmentId,
            @PathVariable("evidenceId") String evidenceId,
            @Valid @RequestBody UpdateEvidenceRequest request) {
        EffectiveFeaturePermission allowed = permission.require(
                AssessmentPermissionGuard.ASSESSMENT, PrimeHrAction.ASSESS, token);
        return service.updateEvidence(agency(authentication), caseId, assignmentId, evidenceId, request,
                authentication.getName(), allowed.dataScope(), correlationId);
    }

    @PostMapping("/{caseId}/assignments/{assignmentId}/evidence/{evidenceId}/archive")
    public AssessmentWorkResponse archiveEvidence(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("caseId") String caseId,
            @PathVariable("assignmentId") String assignmentId,
            @PathVariable("evidenceId") String evidenceId,
            @Valid @RequestBody WorkTransitionRequest request) {
        EffectiveFeaturePermission allowed = permission.require(
                AssessmentPermissionGuard.ASSESSMENT, PrimeHrAction.ASSESS, token);
        return service.archiveEvidence(agency(authentication), caseId, assignmentId, evidenceId, request,
                authentication.getName(), allowed.dataScope(), correlationId);
    }

    @PostMapping("/{caseId}/assignments/{assignmentId}/submit")
    public AssessmentWorkResponse submit(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("caseId") String caseId,
            @PathVariable("assignmentId") String assignmentId,
            @Valid @RequestBody WorkTransitionRequest request) {
        EffectiveFeaturePermission allowed = permission.require(
                AssessmentPermissionGuard.ASSESSMENT, PrimeHrAction.SUBMIT, token);
        return service.submit(agency(authentication), caseId, assignmentId, request,
                authentication.getName(), allowed.dataScope(), correlationId);
    }

    private String agency(Authentication authentication) { return agencyScope.resolveAgencyId(authentication); }
}
