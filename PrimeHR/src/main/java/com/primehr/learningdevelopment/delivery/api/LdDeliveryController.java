package com.primehr.learningdevelopment.delivery.api;

import com.primehr.learningdevelopment.delivery.api.LdDeliveryDtos.*;
import com.primehr.learningdevelopment.delivery.application.LdDeliveryService;
import com.primehr.security.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/primehr/v1/learning-development")
public class LdDeliveryController {
    private final LdDeliveryService service;
    private final LdDeliveryPermissionGuard permission;
    private final AgencyScopeResolver agency;

    public LdDeliveryController(LdDeliveryService service, LdDeliveryPermissionGuard permission,
                                AgencyScopeResolver agency) {
        this.service = service;
        this.permission = permission;
        this.agency = agency;
    }

    private String agency(Authentication authentication) { return agency.resolveAgencyId(authentication); }
    private static String actor(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException("Authenticated employee identity is required");
        }
        return authentication.getName();
    }

    @GetMapping("/plans")
    public List<PlanResponse> plans(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return service.plans(agency(a), actor(a), permission.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.ACCESS, token));
    }

    @GetMapping("/plans/{id}")
    public PlanResponse plan(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                             @PathVariable String id) {
        return service.plan(agency(a), id, actor(a), permission.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.ACCESS, token));
    }

    @PostMapping("/plans") @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse createPlan(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                   @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                   @Valid @RequestBody PlanCreate input) {
        return service.createPlan(agency(a), input, token, actor(a), permission.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.ADD, token), correlation);
    }

    @PutMapping("/plans/{id}")
    public PlanResponse updatePlan(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                   @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                   @PathVariable String id, @Valid @RequestBody PlanUpdate input) {
        return service.updatePlan(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.EDIT, token), correlation);
    }

    @PostMapping("/plans/{id}/sources")
    public PlanResponse addPlanSource(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                      @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                      @PathVariable String id, @Valid @RequestBody PlanSourceInput input) {
        return service.addPlanSource(agency(a), id, input, token, actor(a), permission.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.EDIT, token), correlation);
    }

    @DeleteMapping("/plans/{id}/sources/{sourceId}")
    public PlanResponse removePlanSource(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                         @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                         @PathVariable String id, @PathVariable String sourceId,
                                         @Valid @RequestBody Transition input) {
        return service.removePlanSource(agency(a), id, sourceId, input, actor(a), permission.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.EDIT, token), correlation);
    }

    @PostMapping("/plans/{id}/{action:submit|approve|publish|cancel}")
    public PlanResponse transitionPlan(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                       @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                       @PathVariable String id, @PathVariable String action,
                                       @Valid @RequestBody Transition input) {
        return switch (action) {
            case "submit" -> service.submitPlan(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.SUBMIT, token), correlation);
            case "approve" -> service.approvePlan(agency(a), id, input, token, actor(a), permission.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.APPROVE, token), correlation);
            case "publish" -> service.publishPlan(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.PUBLISH, token), correlation);
            default -> service.cancelPlan(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.ARCHIVE, token), correlation);
        };
    }

    @PostMapping("/plans/{id}/revisions") @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse revisePlan(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                   @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                   @PathVariable String id, @Valid @RequestBody Transition input) {
        return service.revisePlan(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.EDIT, token), correlation);
    }

    @GetMapping("/programs")
    public List<ProgramResponse> programs(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return service.programs(agency(a), actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.ACCESS, token));
    }

    @GetMapping("/programs/{id}")
    public ProgramResponse program(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                   @PathVariable String id) {
        return service.program(agency(a), id, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.ACCESS, token));
    }

    @PostMapping("/programs") @ResponseStatus(HttpStatus.CREATED)
    public ProgramResponse createProgram(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                         @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                         @Valid @RequestBody ProgramCreate input) {
        return service.createProgram(agency(a), input, token, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.ADD, token), correlation);
    }

    @PutMapping("/programs/{id}")
    public ProgramResponse updateProgram(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                         @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                         @PathVariable String id, @Valid @RequestBody ProgramUpdate input) {
        return service.updateProgram(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.EDIT, token), correlation);
    }

    @PostMapping("/programs/{id}/sources")
    public ProgramResponse addProgramSource(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                            @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                            @PathVariable String id, @Valid @RequestBody ProgramSourceInput input) {
        return service.addProgramSource(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.EDIT, token), correlation);
    }

    @DeleteMapping("/programs/{id}/sources/{sourceId}")
    public ProgramResponse removeProgramSource(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                               @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                               @PathVariable String id, @PathVariable String sourceId,
                                               @Valid @RequestBody Transition input) {
        return service.removeProgramSource(agency(a), id, sourceId, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.EDIT, token), correlation);
    }

    @PostMapping("/programs/{id}/{action:submit|approve|cancel}")
    public ProgramResponse transitionProgram(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                             @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                             @PathVariable String id, @PathVariable String action,
                                             @Valid @RequestBody Transition input) {
        return switch (action) {
            case "submit" -> service.submitProgram(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.SUBMIT, token), correlation);
            case "approve" -> service.approveProgram(agency(a), id, input, token, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.APPROVE, token), correlation);
            default -> service.cancelProgram(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.ARCHIVE, token), correlation);
        };
    }

    @PostMapping("/programs/{id}/revisions") @ResponseStatus(HttpStatus.CREATED)
    public ProgramResponse reviseProgram(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                         @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                         @PathVariable String id, @Valid @RequestBody Transition input) {
        return service.reviseProgram(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.EDIT, token), correlation);
    }

    @GetMapping("/offerings")
    public List<OfferingResponse> offerings(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return service.offerings(agency(a), actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.ACCESS, token));
    }

    @GetMapping("/offerings/{id}")
    public OfferingResponse offering(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                     @PathVariable String id) {
        return service.offering(agency(a), id, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.ACCESS, token));
    }

    @PostMapping("/offerings") @ResponseStatus(HttpStatus.CREATED)
    public OfferingResponse createOffering(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                            @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                            @Valid @RequestBody OfferingCreate input) {
        return service.createOffering(agency(a), input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.ADD, token), correlation);
    }

    @PutMapping("/offerings/{id}")
    public OfferingResponse updateOffering(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                            @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                            @PathVariable String id, @Valid @RequestBody OfferingUpdate input) {
        return service.updateOffering(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.EDIT, token), correlation);
    }

    @PutMapping("/offerings/{id}/sessions")
    public OfferingResponse replaceSessions(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                             @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                             @PathVariable String id, @Valid @RequestBody SessionsInput input) {
        return service.replaceSessions(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.EDIT, token), correlation);
    }

    @PutMapping("/offerings/{id}/resources")
    public OfferingResponse replaceResources(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                              @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                              @PathVariable String id, @Valid @RequestBody ResourcesInput input) {
        return service.replaceResources(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.EDIT, token), correlation);
    }

    @PostMapping("/offerings/{id}/{action:submit|approve|publish|cancel}")
    public OfferingResponse transitionOffering(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                               @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                               @PathVariable String id, @PathVariable String action,
                                               @Valid @RequestBody Transition input) {
        return switch (action) {
            case "submit" -> service.submitOffering(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.SUBMIT, token), correlation);
            case "approve" -> service.approveOffering(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.APPROVE, token), correlation);
            case "publish" -> service.publishOffering(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.PUBLISH, token), correlation);
            default -> service.cancelOffering(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.ARCHIVE, token), correlation);
        };
    }

    @PostMapping("/offerings/{id}/revisions") @ResponseStatus(HttpStatus.CREATED)
    public OfferingResponse reviseOffering(Authentication a, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                           @RequestHeader(value="X-Correlation-Id", required=false) String correlation,
                                           @PathVariable String id, @Valid @RequestBody Transition input) {
        return service.reviseOffering(agency(a), id, input, actor(a), permission.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.EDIT, token), correlation);
    }
}
