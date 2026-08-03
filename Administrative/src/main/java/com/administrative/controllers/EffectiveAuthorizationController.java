package com.administrative.controllers;

import com.administrative.dtos.EffectiveFeaturePermissionResponse;
import com.administrative.services.EffectiveAuthorizationService;
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
}
