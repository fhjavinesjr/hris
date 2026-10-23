package com.administrative.controllers;

import com.administrative.dtos.EffectiveFeaturePermissionResponse;
import com.administrative.dtos.MinimumWageRateDTO;
import com.administrative.impl.EffectiveAuthorizationServiceImpl;
import com.administrative.services.EffectiveAuthorizationService;
import com.administrative.services.MinimumWageRateService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/minimum-wage-rates")
public class MinimumWageRateController {
    private final MinimumWageRateService service;
    private final EffectiveAuthorizationService authorization;

    public MinimumWageRateController(MinimumWageRateService service,
                                     EffectiveAuthorizationService authorization) {
        this.service = service;
        this.authorization = authorization;
    }

    @GetMapping
    public List<MinimumWageRateDTO> getAll(Authentication authentication) {
        require(authentication, Action.ACCESS);
        return service.getAll();
    }

    @PostMapping
    public MinimumWageRateDTO create(Authentication authentication,
                                     @RequestBody MinimumWageRateDTO request) {
        require(authentication, Action.ADD);
        return service.create(request);
    }

    @PutMapping("/{id}")
    public MinimumWageRateDTO update(Authentication authentication, @PathVariable Long id,
                                     @RequestBody MinimumWageRateDTO request) {
        require(authentication, Action.EDIT);
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long id) {
        require(authentication, Action.DELETE);
        service.delete(id);
    }

    private void require(Authentication authentication, Action action) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        String role = authentication.getAuthorities().stream().findFirst()
                .map(value -> value.getAuthority()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.FORBIDDEN, "An assigned role is required"));
        EffectiveFeaturePermissionResponse permission = authorization.resolve(
                authentication.getName(), role, EffectiveAuthorizationServiceImpl.PAYROLL_ANNUALIZATION);
        boolean allowed = permission.administrator() || switch (action) {
            case ACCESS -> permission.canAccess();
            case ADD -> permission.canAccess() && permission.canAdd();
            case EDIT -> permission.canAccess() && permission.canEdit();
            case DELETE -> permission.canAccess() && permission.canDelete();
        };
        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Annualization minimum-wage configuration is not permitted");
    }

    private enum Action { ACCESS, ADD, EDIT, DELETE }
}
