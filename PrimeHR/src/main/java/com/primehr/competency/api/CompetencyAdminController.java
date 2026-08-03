package com.primehr.competency.api;

import com.primehr.competency.application.CompetencyAdminService;
import com.primehr.competency.domain.DefinitionStatus;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.PrimeHrAction;
import com.primehr.security.PrimeHrPermissionGuard;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.AuditEventResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/primehr/v1/admin")
public class CompetencyAdminController {
    private final CompetencyAdminService service;
    private final AgencyScopeResolver agencyScope;
    private final PrimeHrPermissionGuard permission;

    public CompetencyAdminController(CompetencyAdminService service, AgencyScopeResolver agencyScope,
                                     PrimeHrPermissionGuard permission) {
        this.service = service;
        this.agencyScope = agencyScope;
        this.permission = permission;
    }

    @GetMapping("/competency-categories")
    public PageResponse<AdminCategoryResponse> categories(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "status", required = false) DefinitionStatus status,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.listCategories(agency(authentication), status, search, asOf, page, size);
    }

    @PostMapping("/competency-categories")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCategoryResponse createCategory(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody DraftCategoryRequest request) {
        permission.require(PrimeHrAction.ADD, token);
        return service.createCategory(agency(authentication), request, correlationId);
    }

    @PutMapping("/competency-categories/{id}")
    public AdminCategoryResponse updateCategory(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody DraftCategoryRequest request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.updateCategory(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/competency-categories/{id}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCategoryResponse versionCategory(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody DraftTransitionRequest request) {
        permission.require(PrimeHrAction.ADD, token);
        return service.versionCategory(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/competency-categories/{id}/archive")
    public AdminCategoryResponse archiveCategory(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody DraftTransitionRequest request) {
        permission.require(PrimeHrAction.ARCHIVE, token);
        return service.archiveCategory(agency(authentication), id, request, correlationId);
    }

    @GetMapping("/proficiency-scales")
    public PageResponse<AdminScaleResponse> scales(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "status", required = false) DefinitionStatus status,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.listScales(agency(authentication), status, search, asOf, page, size);
    }

    @PostMapping("/proficiency-scales")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminScaleResponse createScale(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody DraftScaleRequest request) {
        permission.require(PrimeHrAction.ADD, token);
        return service.createScale(agency(authentication), request, correlationId);
    }

    @PutMapping("/proficiency-scales/{id}")
    public AdminScaleResponse updateScale(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody DraftScaleRequest request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.updateScale(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/proficiency-scales/{id}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminScaleResponse versionScale(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody DraftTransitionRequest request) {
        permission.require(PrimeHrAction.ADD, token);
        return service.versionScale(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/proficiency-scales/{id}/archive")
    public AdminScaleResponse archiveScale(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody DraftTransitionRequest request) {
        permission.require(PrimeHrAction.ARCHIVE, token);
        return service.archiveScale(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/proficiency-scales/{scaleId}/levels")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminLevelResponse createLevel(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("scaleId") String scaleId, @Valid @RequestBody DraftLevelRequest request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.createLevel(agency(authentication), scaleId, request, correlationId);
    }

    @PutMapping("/proficiency-scales/{scaleId}/levels/{levelId}")
    public AdminLevelResponse updateLevel(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("scaleId") String scaleId, @PathVariable("levelId") String levelId,
            @Valid @RequestBody DraftLevelRequest request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.updateLevel(agency(authentication), scaleId, levelId, request, correlationId);
    }

    @PostMapping("/proficiency-scales/{scaleId}/levels/{levelId}/archive")
    public AdminLevelResponse archiveLevel(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("scaleId") String scaleId, @PathVariable("levelId") String levelId,
            @Valid @RequestBody DraftTransitionRequest request) {
        permission.require(PrimeHrAction.ARCHIVE, token);
        return service.archiveLevel(agency(authentication), scaleId, levelId, request, correlationId);
    }

    @GetMapping("/competencies")
    public PageResponse<AdminCompetencyResponse> competencies(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "status", required = false) DefinitionStatus status,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.listCompetencies(agency(authentication), status, categoryId, search, asOf, page, size);
    }

    @PostMapping("/competencies")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCompetencyResponse createCompetency(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody DraftCompetencyRequest request) {
        permission.require(PrimeHrAction.ADD, token);
        return service.createCompetency(agency(authentication), request, correlationId);
    }

    @PutMapping("/competencies/{id}")
    public AdminCompetencyResponse updateCompetency(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody DraftCompetencyRequest request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.updateCompetency(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/competencies/{id}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCompetencyResponse versionCompetency(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody DraftTransitionRequest request) {
        permission.require(PrimeHrAction.ADD, token);
        return service.versionCompetency(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/competencies/{id}/archive")
    public AdminCompetencyResponse archiveCompetency(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("id") String id, @Valid @RequestBody DraftTransitionRequest request) {
        permission.require(PrimeHrAction.ARCHIVE, token);
        return service.archiveCompetency(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/competencies/{competencyId}/indicators")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminIndicatorResponse createIndicator(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("competencyId") String competencyId, @Valid @RequestBody DraftIndicatorRequest request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.createIndicator(agency(authentication), competencyId, request, correlationId);
    }

    @PutMapping("/competencies/{competencyId}/indicators/{indicatorId}")
    public AdminIndicatorResponse updateIndicator(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("competencyId") String competencyId, @PathVariable("indicatorId") String indicatorId,
            @Valid @RequestBody DraftIndicatorRequest request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.updateIndicator(agency(authentication), competencyId, indicatorId, request, correlationId);
    }

    @PostMapping("/competencies/{competencyId}/indicators/{indicatorId}/archive")
    public AdminIndicatorResponse archiveIndicator(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable("competencyId") String competencyId, @PathVariable("indicatorId") String indicatorId,
            @Valid @RequestBody DraftTransitionRequest request) {
        permission.require(PrimeHrAction.ARCHIVE, token);
        return service.archiveIndicator(agency(authentication), competencyId, indicatorId, request, correlationId);
    }

    @GetMapping("/audit-events")
    public PageResponse<AuditEventResponse> auditEvents(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam("aggregateType") String aggregateType,
            @RequestParam("aggregateId") String aggregateId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.listAuditEvents(agency(authentication), aggregateType, aggregateId, page, size);
    }

    private String agency(Authentication authentication) {
        return agencyScope.resolveAgencyId(authentication);
    }
}
