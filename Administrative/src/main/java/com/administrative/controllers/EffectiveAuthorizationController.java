package com.administrative.controllers;

import com.administrative.dtos.EffectiveFeaturePermissionResponse;
import com.administrative.services.EffectiveAuthorizationService;
import com.administrative.impl.EffectiveAuthorizationServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/authorization")
public class EffectiveAuthorizationController {
    private final EffectiveAuthorizationService service;

    public EffectiveAuthorizationController(EffectiveAuthorizationService service) {
        this.service = service;
    }

    @GetMapping("/effective")
    public EffectiveFeaturePermissionResponse effective(Authentication authentication,
                                                         @RequestParam("featureKey") String featureKey) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        String role = authentication.getAuthorities().stream().findFirst()
                .map(authority -> authority.getAuthority())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "The authenticated account has no assigned role"));
        return service.resolve(authentication.getName(), role, featureKey);
    }

    @GetMapping("/effective/employee")
    public EffectiveFeaturePermissionResponse effectiveEmployee(Authentication authentication,
                                                                 @RequestParam String employeeNo,
                                                                 @RequestParam String employeeRole,
                                                                 @RequestParam String featureKey) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        String callerRole = authentication.getAuthorities().stream().findFirst()
                .map(authority -> authority.getAuthority())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "The authenticated account has no assigned role"));
        if (!EffectiveAuthorizationServiceImpl.PRIMEHR_RSP_APPLICATION_SCREENING.equals(featureKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only application-screening assignment permission may be resolved");
        }
        EffectiveFeaturePermissionResponse caller = service.resolve(authentication.getName(), callerRole,
                EffectiveAuthorizationServiceImpl.PRIMEHR_RSP_APPLICATION_SCREENING);
        if (!caller.administrator() && (!caller.canAccess() || !caller.canAdd()
                || caller.dataScope() != com.administrative.dtos.PermissionDataScope.AGENCY_WIDE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Application-screening assignment permission is required");
        }
        return service.resolve(employeeNo, employeeRole, featureKey);
    }
}
