package com.administrative.controllers;

import com.administrative.dtos.EffectiveFeaturePermissionResponse;
import com.administrative.dtos.PositionTargetPageResponse;
import com.administrative.dtos.PositionTargetResponse;
import com.administrative.dtos.PositionTargetType;
import com.administrative.impl.EffectiveAuthorizationServiceImpl;
import com.administrative.services.EffectiveAuthorizationService;
import com.administrative.services.PositionTargetService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/integration/v1/primehr/position-targets")
public class PositionTargetIntegrationController {
    private final PositionTargetService targets;
    private final EffectiveAuthorizationService authorization;

    public PositionTargetIntegrationController(PositionTargetService targets,
                                               EffectiveAuthorizationService authorization) {
        this.targets = targets;
        this.authorization = authorization;
    }

    @GetMapping
    public PositionTargetPageResponse list(Authentication authentication,
                                           @RequestParam("type") PositionTargetType type,
                                           @RequestParam(name = "search", required = false) String search,
                                           @RequestParam(name = "page", defaultValue = "0") int page,
                                           @RequestParam(name = "size", defaultValue = "20") int size) {
        requireAccess(authentication);
        return targets.list(type, search, page, size);
    }

    @GetMapping("/{type}/{id}")
    public PositionTargetResponse get(Authentication authentication,
                                      @PathVariable("type") PositionTargetType type,
                                      @PathVariable("id") Long id) {
        requireAccess(authentication);
        return targets.get(type, id);
    }

    private void requireAccess(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        String role = authentication.getAuthorities().stream().findFirst()
                .map(authority -> authority.getAuthority())
                .orElseThrow(() -> new AccessDeniedException("The authenticated account has no assigned role"));
        EffectiveFeaturePermissionResponse permission = authorization.resolve(authentication.getName(), role,
                EffectiveAuthorizationServiceImpl.PRIMEHR_POSITION_PROFILE);
        if (!permission.administrator() && !permission.canAccess()) {
            throw new AccessDeniedException("Position competency profile access is not permitted");
        }
    }
}
