package com.primehr.assessment.api;

import com.primehr.assessment.api.AssessmentExecutionDtos.*;
import com.primehr.assessment.api.AssessmentValidationDtos.*;
import com.primehr.assessment.application.AssessmentExecutionService;
import com.primehr.assessment.application.AssessmentValidationService;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.shared.api.PageResponse;
import com.primehr.security.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/primehr/v1/validation/assessment-cases")
public class AssessmentValidationController {
    private final AssessmentExecutionService service;
    private final AssessmentValidationService validation;
    private final AssessmentPermissionGuard permission;
    private final AgencyScopeResolver agencyScope;

    public AssessmentValidationController(AssessmentExecutionService service, AssessmentValidationService validation,
            AssessmentPermissionGuard permission, AgencyScopeResolver agencyScope) {
        this.service = service; this.validation = validation;
        this.permission = permission;
        this.agencyScope = agencyScope;
    }

    @PostMapping("/{caseId}/return")
    public ReturnCaseResponse returnForCorrection(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("caseId") String caseId, @Valid @RequestBody ReturnCaseRequest request) {
        permission.requireAgencyWide(AssessmentPermissionGuard.VALIDATION, PrimeHrAction.VALIDATE, token);
        return service.returnCase(agencyScope.resolveAgencyId(authentication), caseId, request,
                authentication.getName(), correlationId);
    }

    @GetMapping
    public PageResponse<ValidationListItem> pending(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.requireAgencyWide(AssessmentPermissionGuard.VALIDATION, PrimeHrAction.VALIDATE, token);
        return validation.pending(agencyScope.resolveAgencyId(authentication), page, size);
    }

    @GetMapping("/{caseId}")
    public ValidationCaseResponse get(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable("caseId") String caseId) {
        permission.requireAgencyWide(AssessmentPermissionGuard.VALIDATION, PrimeHrAction.VALIDATE, token);
        return validation.get(agencyScope.resolveAgencyId(authentication), caseId);
    }

    @PostMapping("/{caseId}/validate")
    public ValidationResultResponse validate(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("caseId") String caseId, @Valid @RequestBody ValidateCaseRequest request) {
        EffectiveFeaturePermission effective = permission.requireAgencyWide(
                AssessmentPermissionGuard.VALIDATION, PrimeHrAction.VALIDATE, token);
        return validation.validate(agencyScope.resolveAgencyId(authentication), caseId, request,
                authentication.getName(), effective.administrator(), correlationId);
    }
}
