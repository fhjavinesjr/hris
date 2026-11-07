package com.primehr.rewardsrecognition.api;

import com.primehr.rewardsrecognition.api.RrFoundationDtos.*;
import com.primehr.rewardsrecognition.application.RrFoundationService;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.PrimeHrAction;
import com.primehr.security.RrFoundationPermissionGuard;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/primehr/v1/rewards-recognition")
public class RrFoundationController {
    private final RrFoundationService service;
    private final RrFoundationPermissionGuard permission;
    private final AgencyScopeResolver agency;

    public RrFoundationController(RrFoundationService service, RrFoundationPermissionGuard permission,
                                  AgencyScopeResolver agency) {
        this.service = service;
        this.permission = permission;
        this.agency = agency;
    }

    @GetMapping("/policies")
    public List<PolicyResponse> policies(Authentication authentication,
                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.ACCESS, token);
        return service.policies(agency(authentication));
    }

    @GetMapping("/policies/effective")
    public PolicyResponse effectivePolicy(Authentication authentication,
                                          @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                          @RequestParam LocalDate on) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.ACCESS, token);
        return service.effectivePolicy(agency(authentication), on);
    }

    @GetMapping("/policies/{id}")
    public PolicyResponse policy(Authentication authentication,
                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                 @PathVariable String id) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.ACCESS, token);
        return service.policy(agency(authentication), id);
    }

    @PostMapping("/policies") @ResponseStatus(HttpStatus.CREATED)
    public PolicyResponse createPolicy(Authentication authentication,
                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                       @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                       @Valid @RequestBody PolicyInput input) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.ADD, token);
        return service.createPolicy(agency(authentication), input, correlation);
    }

    @PutMapping("/policies/{id}")
    public PolicyResponse updatePolicy(Authentication authentication,
                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                       @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                       @PathVariable String id, @Valid @RequestBody PolicyInput input) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.EDIT, token);
        return service.updatePolicy(agency(authentication), id, input, correlation);
    }

    @PostMapping("/policies/{id}/revisions") @ResponseStatus(HttpStatus.CREATED)
    public PolicyResponse revisePolicy(Authentication authentication,
                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                       @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                       @PathVariable String id, @Valid @RequestBody DefinitionTransition input) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.ADD, token);
        return service.revisePolicy(agency(authentication), id, input, correlation);
    }

    @PostMapping("/policies/{id}/publish")
    public PolicyResponse publishPolicy(Authentication authentication,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                        @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                        @PathVariable String id, @Valid @RequestBody PublishTransition input) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.PUBLISH, token);
        return service.publishPolicy(agency(authentication), id, input, correlation);
    }

    @PostMapping("/policies/{id}/retire")
    public PolicyResponse retirePolicy(Authentication authentication,
                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                       @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                       @PathVariable String id, @Valid @RequestBody DefinitionTransition input) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.PUBLISH, token);
        return service.retirePolicy(agency(authentication), id, input, correlation);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories(Authentication authentication,
                                             @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.ACCESS, token);
        return service.categories(agency(authentication));
    }

    @GetMapping("/categories/{id}")
    public CategoryResponse category(Authentication authentication,
                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                     @PathVariable String id) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.ACCESS, token);
        return service.category(agency(authentication), id);
    }

    @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(Authentication authentication,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                           @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                           @Valid @RequestBody CategoryInput input) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.ADD, token);
        return service.createCategory(agency(authentication), input, correlation);
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(Authentication authentication,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                           @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                           @PathVariable String id, @Valid @RequestBody CategoryInput input) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.EDIT, token);
        return service.updateCategory(agency(authentication), id, input, correlation);
    }

    @PutMapping("/categories/{id}/criteria")
    public CategoryResponse replaceCriteria(Authentication authentication,
                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                            @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                            @PathVariable String id, @Valid @RequestBody CriteriaInput input) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.EDIT, token);
        return service.replaceCriteria(agency(authentication), id, input, correlation);
    }

    @PostMapping("/categories/{id}/revisions") @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse reviseCategory(Authentication authentication,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                           @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                           @PathVariable String id, @Valid @RequestBody DefinitionTransition input) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.ADD, token);
        return service.reviseCategory(agency(authentication), id, input, correlation);
    }

    @PostMapping("/categories/{id}/publish")
    public CategoryResponse publishCategory(Authentication authentication,
                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                            @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                            @PathVariable String id, @Valid @RequestBody PublishTransition input) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.PUBLISH, token);
        return service.publishCategory(agency(authentication), id, input, correlation);
    }

    @PostMapping("/categories/{id}/retire")
    public CategoryResponse retireCategory(Authentication authentication,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                           @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                           @PathVariable String id, @Valid @RequestBody DefinitionTransition input) {
        require(RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.PUBLISH, token);
        return service.retireCategory(agency(authentication), id, input, correlation);
    }

    @GetMapping("/committee-charters")
    public List<CommitteeCharterResponse> charters(Authentication authentication,
                                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        require(RrFoundationPermissionGuard.COMMITTEE, PrimeHrAction.ACCESS, token);
        return service.charters(agency(authentication));
    }

    @GetMapping("/committee-charters/{id}")
    public CommitteeCharterResponse charter(Authentication authentication,
                                             @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                             @PathVariable String id) {
        require(RrFoundationPermissionGuard.COMMITTEE, PrimeHrAction.ACCESS, token);
        return service.charter(agency(authentication), id);
    }

    @PostMapping("/committee-charters") @ResponseStatus(HttpStatus.CREATED)
    public CommitteeCharterResponse createCharter(Authentication authentication,
                                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                   @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                                   @Valid @RequestBody CommitteeCharterInput input) {
        require(RrFoundationPermissionGuard.COMMITTEE, PrimeHrAction.ADD, token);
        return service.createCharter(agency(authentication), input, correlation);
    }

    @PutMapping("/committee-charters/{id}")
    public CommitteeCharterResponse updateCharter(Authentication authentication,
                                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                   @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                                   @PathVariable String id,
                                                   @Valid @RequestBody CommitteeCharterInput input) {
        require(RrFoundationPermissionGuard.COMMITTEE, PrimeHrAction.EDIT, token);
        return service.updateCharter(agency(authentication), id, input, correlation);
    }

    @PutMapping("/committee-charters/{id}/roster")
    public CommitteeCharterResponse replaceRoster(Authentication authentication,
                                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                   @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                                   @PathVariable String id,
                                                   @Valid @RequestBody CommitteeRosterInput input) {
        require(RrFoundationPermissionGuard.COMMITTEE, PrimeHrAction.EDIT, token);
        return service.replaceRoster(agency(authentication), id, input, correlation);
    }

    @PostMapping("/committee-charters/{id}/revisions") @ResponseStatus(HttpStatus.CREATED)
    public CommitteeCharterResponse reviseCharter(Authentication authentication,
                                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                   @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                                   @PathVariable String id,
                                                   @Valid @RequestBody DefinitionTransition input) {
        require(RrFoundationPermissionGuard.COMMITTEE, PrimeHrAction.ADD, token);
        return service.reviseCharter(agency(authentication), id, input, correlation);
    }

    @PostMapping("/committee-charters/{id}/publish")
    public CommitteeCharterResponse publishCharter(Authentication authentication,
                                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                    @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                                    @PathVariable String id,
                                                    @Valid @RequestBody PublishTransition input) {
        require(RrFoundationPermissionGuard.COMMITTEE, PrimeHrAction.PUBLISH, token);
        return service.publishCharter(agency(authentication), id, input, correlation);
    }

    @PostMapping("/committee-charters/{id}/retire")
    public CommitteeCharterResponse retireCharter(Authentication authentication,
                                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                   @RequestHeader(value = "X-Correlation-Id", required = false) String correlation,
                                                   @PathVariable String id,
                                                   @Valid @RequestBody DefinitionTransition input) {
        require(RrFoundationPermissionGuard.COMMITTEE, PrimeHrAction.PUBLISH, token);
        return service.retireCharter(agency(authentication), id, input, correlation);
    }

    private String agency(Authentication authentication) {
        return agency.resolveAgencyId(authentication);
    }

    private void require(String feature, PrimeHrAction action, String token) {
        permission.require(feature, action, token);
    }
}
