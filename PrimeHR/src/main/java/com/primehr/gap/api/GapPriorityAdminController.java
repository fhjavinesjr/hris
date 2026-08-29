package com.primehr.gap.api;

import com.primehr.gap.api.GapPriorityDtos.*;
import com.primehr.gap.application.GapPriorityService;
import com.primehr.gap.domain.GapPrioritySchemeStatus;
import com.primehr.security.*;
import com.primehr.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/primehr/v1/admin/gap-priority-schemes")
public class GapPriorityAdminController {
    private final GapPriorityService service;
    private final GapPermissionGuard permission;
    private final AgencyScopeResolver agencyScope;

    public GapPriorityAdminController(GapPriorityService service, GapPermissionGuard permission,
                                      AgencyScopeResolver agencyScope) {
        this.service = service;
        this.permission = permission;
        this.agencyScope = agencyScope;
    }

    @GetMapping
    public PageResponse<SchemeSummaryResponse> list(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "status", required = false) GapPrioritySchemeStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.ACCESS, token);
        return service.list(agencyScope.resolveAgencyId(auth), status, page, size);
    }

    @GetMapping("/{id}")
    public SchemeResponse get(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                              @PathVariable("id") String id) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.ACCESS, token);
        return service.get(agencyScope.resolveAgencyId(auth), id);
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public SchemeResponse create(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody CreateSchemeRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.ADD, token);
        return service.create(agencyScope.resolveAgencyId(auth), request, correlationId);
    }

    @PutMapping("/{id}")
    public SchemeResponse update(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody UpdateSchemeRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.EDIT, token);
        return service.update(agencyScope.resolveAgencyId(auth), id, request, correlationId);
    }

    @PostMapping("/{id}/archive")
    public SchemeResponse archive(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody TransitionRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.ARCHIVE, token);
        return service.archive(agencyScope.resolveAgencyId(auth), id, request, correlationId);
    }

    @PostMapping("/{id}/versions") @ResponseStatus(HttpStatus.CREATED)
    public SchemeResponse successor(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody TransitionRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.ADD, token);
        return service.createSuccessor(agencyScope.resolveAgencyId(auth), id, request, correlationId);
    }

    @PostMapping("/{id}/publish")
    public SchemeResponse publish(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody PublishRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.PUBLISH, token);
        return service.publish(agencyScope.resolveAgencyId(auth), id, request, correlationId);
    }

    @PostMapping("/{id}/levels") @ResponseStatus(HttpStatus.CREATED)
    public SchemeResponse addLevel(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody CreateLevelRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.EDIT, token);
        return service.addLevel(agencyScope.resolveAgencyId(auth), id, request, correlationId);
    }

    @PutMapping("/{id}/levels/{levelId}")
    public SchemeResponse updateLevel(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @PathVariable("levelId") String levelId,
            @Valid @RequestBody UpdateLevelRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.EDIT, token);
        return service.updateLevel(agencyScope.resolveAgencyId(auth), id, levelId, request, correlationId);
    }

    @PostMapping("/{id}/levels/{levelId}/archive")
    public SchemeResponse archiveLevel(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @PathVariable("levelId") String levelId,
            @Valid @RequestBody TransitionRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.ARCHIVE, token);
        return service.archiveLevel(agencyScope.resolveAgencyId(auth), id, levelId, request, correlationId);
    }

    @PostMapping("/{id}/rules") @ResponseStatus(HttpStatus.CREATED)
    public SchemeResponse addRule(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody CreateRuleRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.EDIT, token);
        return service.addRule(agencyScope.resolveAgencyId(auth), id, request, correlationId);
    }

    @PutMapping("/{id}/rules/{ruleId}")
    public SchemeResponse updateRule(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @PathVariable("ruleId") String ruleId,
            @Valid @RequestBody UpdateRuleRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.EDIT, token);
        return service.updateRule(agencyScope.resolveAgencyId(auth), id, ruleId, request, correlationId);
    }

    @PostMapping("/{id}/rules/{ruleId}/archive")
    public SchemeResponse archiveRule(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @PathVariable("ruleId") String ruleId,
            @Valid @RequestBody TransitionRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.CONFIGURATION, PrimeHrAction.ARCHIVE, token);
        return service.archiveRule(agencyScope.resolveAgencyId(auth), id, ruleId, request, correlationId);
    }
}
