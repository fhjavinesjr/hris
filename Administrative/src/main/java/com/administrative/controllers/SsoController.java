package com.administrative.controllers;

import com.administrative.dtos.*;
import com.administrative.services.SsoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sso")
public class SsoController {
    private final SsoService ssoService;

    public SsoController(SsoService ssoService) { this.ssoService = ssoService; }

    @PostMapping("/launch")
    public ResponseEntity<SsoLaunchResponse> launch(Authentication authentication,
                                                     @Valid @RequestBody SsoLaunchRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        String role = authentication.getAuthorities().stream().findFirst()
                .map(authority -> authority.getAuthority())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "The authenticated account has no assigned role"));
        return ResponseEntity.ok(ssoService.launch(authentication.getName(), role, request.getTarget()));
    }

    @PostMapping("/exchange")
    public ResponseEntity<SsoExchangeResponse> exchange(@Valid @RequestBody SsoExchangeRequest request)
            throws Exception {
        return ResponseEntity.ok(ssoService.exchange(request.getCode(), request.getTarget()));
    }
}
