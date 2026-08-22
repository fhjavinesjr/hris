package com.primehr.positionprofile.api;

import com.primehr.positionprofile.application.PositionProfileAdminService;
import com.primehr.positionprofile.domain.PositionProfileStatus;
import com.primehr.positionprofile.domain.PositionTargetType;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.PositionProfilePermissionGuard;
import com.primehr.security.PrimeHrAction;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import com.primehr.shared.audit.AuditEventResponse;

@RestController
@RequestMapping("/api/primehr/v1/admin/position-profiles")
public class PositionProfileAdminController {
    private final PositionProfileAdminService service;
    private final PositionProfilePermissionGuard permission;
    private final AgencyScopeResolver agencyScope;

    public PositionProfileAdminController(PositionProfileAdminService service,
                                          PositionProfilePermissionGuard permission,
                                          AgencyScopeResolver agencyScope) {
        this.service = service;
        this.permission = permission;
        this.agencyScope = agencyScope;
    }

    @GetMapping
    public PageResponse<PositionProfileSummaryResponse> list(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "status", required = false) PositionProfileStatus status,
            @RequestParam(name = "targetType", required = false) PositionTargetType targetType,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.list(agency(authentication), status, targetType, search, page, size);
    }

    @GetMapping("/{id}")
    public PositionProfileResponse get(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token, @PathVariable("id") String id) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.get(agency(authentication), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PositionProfileResponse create(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody CreatePositionProfileRequest request) {
        permission.require(PrimeHrAction.ADD, token);
        return service.create(agency(authentication), request, token, correlationId);
    }

    @PutMapping("/{id}")
    public PositionProfileResponse update(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody UpdatePositionProfileRequest request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.update(agency(authentication), id, request, token, correlationId);
    }

    @PostMapping("/{id}/archive")
    public PositionProfileResponse archive(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody PositionProfileTransitionRequest request) {
        permission.require(PrimeHrAction.ARCHIVE, token);
        return service.archive(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/{id}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    public PositionProfileResponse createSuccessor(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody PositionProfileTransitionRequest request) {
        permission.require(PrimeHrAction.ADD, token);
        return service.createSuccessor(agency(authentication), id, request, token, correlationId);
    }

    @PostMapping("/{id}/requirements")
    @ResponseStatus(HttpStatus.CREATED)
    public PositionProfileResponse addRequirement(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody CreatePositionRequirementRequest request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.addRequirement(agency(authentication), id, request, correlationId);
    }

    @PutMapping("/{id}/requirements/{requirementId}")
    public PositionProfileResponse updateRequirement(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @PathVariable("requirementId") String requirementId,
            @Valid @RequestBody UpdatePositionRequirementRequest request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.updateRequirement(agency(authentication), id, requirementId, request, correlationId);
    }

    @PostMapping("/{id}/requirements/{requirementId}/archive")
    public PositionProfileResponse archiveRequirement(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @PathVariable("requirementId") String requirementId,
            @Valid @RequestBody PositionRequirementTransitionRequest request) {
        permission.require(PrimeHrAction.ARCHIVE, token);
        return service.archiveRequirement(agency(authentication), id, requirementId, request, correlationId);
    }

    @PostMapping("/{id}/submit")
    public PositionProfileResponse submit(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody SubmitPositionProfileRequest request) {
        permission.require(PrimeHrAction.SUBMIT, token);
        return service.submit(agency(authentication), id, request, token, correlationId);
    }

    @PostMapping("/{id}/return")
    public PositionProfileResponse returnSubmission(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody PositionProfileTransitionRequest request) {
        permission.require(PrimeHrAction.APPROVE, token);
        return service.returnSubmission(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/{id}/approve")
    public PositionProfileResponse approve(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody ApprovePositionProfileRequest request) {
        EffectiveFeaturePermission effective = permission.require(PrimeHrAction.APPROVE, token);
        return service.approve(agency(authentication), id, request, token, effective.administrator(), correlationId);
    }

    @GetMapping("/resolve")
    public PositionProfileResolutionResponse resolve(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam("jobPositionId") Long jobPositionId,
            @RequestParam(name = "plantillaId", required = false) Long plantillaId,
            @RequestParam("asOf") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.resolve(agency(authentication), jobPositionId, plantillaId, asOf);
    }

    @GetMapping("/compare")
    public PositionProfileComparisonResponse compare(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam("leftProfileId") String leftProfileId,
            @RequestParam("rightProfileId") String rightProfileId) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.compare(agency(authentication), leftProfileId, rightProfileId);
    }

    @GetMapping("/{id}/audit-events")
    public PageResponse<AuditEventResponse> auditEvents(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable("id") String id,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.auditEvents(agency(authentication), id, page, size);
    }

    private String agency(Authentication authentication) {
        return agencyScope.resolveAgencyId(authentication);
    }
}
