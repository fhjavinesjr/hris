package com.primehr.rsp.api;

import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.rsp.api.RspPlanningDtos.CreatePlan;
import com.primehr.rsp.api.RspPlanningDtos.PlanResponse;
import com.primehr.rsp.api.RspPlanningDtos.Readiness;
import com.primehr.rsp.api.RspPlanningDtos.SaveVacancy;
import com.primehr.rsp.api.RspPlanningDtos.Transition;
import com.primehr.rsp.api.RspPlanningDtos.UpdatePlan;
import com.primehr.rsp.application.RspPlanningService;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.PrimeHrAction;
import com.primehr.security.RspPlanningPermissionGuard;
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

import java.time.LocalDate;

@RestController
@RequestMapping("/api/primehr/v1/rsp")
public class RspPlanningController {
    private final RspPlanningService service;
    private final RspPlanningPermissionGuard permission;
    private final AgencyScopeResolver agencyScope;

    public RspPlanningController(RspPlanningService service,
                                 RspPlanningPermissionGuard permission,
                                 AgencyScopeResolver agencyScope) {
        this.service = service;
        this.permission = permission;
        this.agencyScope = agencyScope;
    }

    @GetMapping("/recruitment-plans")
    public PageResponse<PlanResponse> list(Authentication authentication,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                           @RequestParam(name = "page", defaultValue = "0") int page,
                                           @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.list(agency(authentication), page, size);
    }

    @GetMapping("/recruitment-plans/{id}")
    public PlanResponse get(Authentication authentication,
                            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                            @PathVariable("id") String id) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.get(agency(authentication), id);
    }

    @PostMapping("/recruitment-plans")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse create(Authentication authentication,
                               @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                               @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                               @Valid @RequestBody CreatePlan request) {
        permission.require(PrimeHrAction.ADD, token);
        return service.create(agency(authentication), request, correlationId);
    }

    @PutMapping("/recruitment-plans/{id}")
    public PlanResponse update(Authentication authentication,
                               @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                               @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                               @PathVariable("id") String id,
                               @Valid @RequestBody UpdatePlan request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.update(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/recruitment-plans/{id}/archive")
    public PlanResponse archive(Authentication authentication,
                                @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                @PathVariable("id") String id,
                                @Valid @RequestBody Transition request) {
        permission.require(PrimeHrAction.ARCHIVE, token);
        return service.archive(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/recruitment-plans/{id}/submit")
    public PlanResponse submitPlan(Authentication authentication,
                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                   @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                   @PathVariable("id") String id,
                                   @Valid @RequestBody Transition request) {
        permission.require(PrimeHrAction.SUBMIT, token);
        return service.submitPlan(agency(authentication), id, request, token, correlationId);
    }

    @PostMapping("/recruitment-plans/{id}/return")
    public PlanResponse returnPlan(Authentication authentication,
                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                   @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                   @PathVariable("id") String id,
                                   @Valid @RequestBody Transition request) {
        permission.require(PrimeHrAction.APPROVE, token);
        return service.returnPlan(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/recruitment-plans/{id}/approve")
    public PlanResponse approvePlan(Authentication authentication,
                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                    @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                    @PathVariable("id") String id,
                                    @Valid @RequestBody Transition request) {
        EffectiveFeaturePermission effective = permission.require(PrimeHrAction.APPROVE, token);
        return service.approvePlan(agency(authentication), id, request, token,
                effective.administrator(), correlationId);
    }

    @PostMapping("/recruitment-plans/{id}/vacancies")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse addVacancy(Authentication authentication,
                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                   @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                   @PathVariable("id") String id,
                                   @Valid @RequestBody SaveVacancy request) {
        permission.require(PrimeHrAction.ADD, token);
        return service.addVacancy(agency(authentication), id, request, token, correlationId);
    }

    @PutMapping("/vacancy-requests/{id}")
    public PlanResponse updateVacancy(Authentication authentication,
                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                      @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                      @PathVariable("id") String id,
                                      @Valid @RequestBody SaveVacancy request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.updateVacancy(agency(authentication), id, request, token, correlationId);
    }

    @PostMapping("/vacancy-requests/{id}/archive")
    public PlanResponse archiveVacancy(Authentication authentication,
                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                       @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                       @PathVariable("id") String id,
                                       @Valid @RequestBody Transition request) {
        permission.require(PrimeHrAction.ARCHIVE, token);
        return service.archiveVacancy(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/vacancy-requests/{id}/submit")
    public PlanResponse submitVacancy(Authentication authentication,
                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                      @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                      @PathVariable("id") String id,
                                      @Valid @RequestBody Transition request) {
        permission.require(PrimeHrAction.SUBMIT, token);
        return service.submitVacancy(agency(authentication), id, request, token, correlationId);
    }

    @PostMapping("/vacancy-requests/{id}/return")
    public PlanResponse returnVacancy(Authentication authentication,
                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                      @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                      @PathVariable("id") String id,
                                      @Valid @RequestBody Transition request) {
        permission.require(PrimeHrAction.APPROVE, token);
        return service.returnVacancy(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/vacancy-requests/{id}/authorize")
    public PlanResponse authorizeVacancy(Authentication authentication,
                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                         @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                         @PathVariable("id") String id,
                                         @Valid @RequestBody Transition request) {
        EffectiveFeaturePermission effective = permission.require(PrimeHrAction.APPROVE, token);
        return service.authorizeVacancy(agency(authentication), id, request, token,
                effective.administrator(), correlationId);
    }

    @PostMapping("/vacancy-requests/{id}/decline")
    public PlanResponse declineVacancy(Authentication authentication,
                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                       @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                       @PathVariable("id") String id,
                                       @Valid @RequestBody Transition request) {
        EffectiveFeaturePermission effective = permission.require(PrimeHrAction.APPROVE, token);
        return service.declineVacancy(agency(authentication), id, request,
                effective.administrator(), correlationId);
    }

    @PostMapping("/vacancy-requests/{id}/cancel")
    public PlanResponse cancelVacancy(Authentication authentication,
                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                      @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                      @PathVariable("id") String id,
                                      @Valid @RequestBody Transition request) {
        permission.require(PrimeHrAction.ARCHIVE, token);
        return service.cancelVacancy(agency(authentication), id, request, correlationId);
    }

    @GetMapping("/vacancy-requests/{id}/readiness")
    public Readiness readiness(Authentication authentication,
                               @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                               @PathVariable("id") String id) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.vacancyReadiness(agency(authentication), id, token);
    }

    @GetMapping("/vacancy-readiness")
    public Readiness sourceReadiness(Authentication authentication,
                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                     @RequestParam("plantillaId") Long plantillaId,
                                     @RequestParam("businessUnitId") Long businessUnitId,
                                     @RequestParam(name = "asOf", required = false) LocalDate asOf) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.readiness(agency(authentication), plantillaId, businessUnitId, asOf, token);
    }

    private String agency(Authentication authentication) {
        return agencyScope.resolveAgencyId(authentication);
    }
}
