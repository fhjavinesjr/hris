package com.administrative.controllers;

import com.administrative.dtos.BenefitTaxProfileDTO;
import com.administrative.dtos.EffectiveFeaturePermissionResponse;
import com.administrative.impl.EffectiveAuthorizationServiceImpl;
import com.administrative.services.BenefitTaxProfileService;
import com.administrative.services.EffectiveAuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/benefit-tax-profiles")
public class BenefitTaxProfileController {
    private final BenefitTaxProfileService service;
    private final EffectiveAuthorizationService authorization;

    public BenefitTaxProfileController(BenefitTaxProfileService service, EffectiveAuthorizationService authorization) {
        this.service = service;
        this.authorization = authorization;
    }

    @GetMapping
    public List<BenefitTaxProfileDTO> list(@RequestParam int year, Authentication authentication) {
        require(authentication, false, false);
        return service.list(year);
    }

    @PostMapping
    public BenefitTaxProfileDTO create(@RequestBody BenefitTaxProfileDTO request, Authentication authentication) {
        require(authentication, true, false);
        return service.save(null, request);
    }

    @PutMapping("/{id}")
    public BenefitTaxProfileDTO update(@PathVariable Long id, @RequestBody BenefitTaxProfileDTO request,
                                       Authentication authentication) {
        require(authentication, false, true);
        return service.save(id, request);
    }

    private void require(Authentication authentication, boolean add, boolean edit) {
        if (authentication == null || !authentication.isAuthenticated())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        String role = authentication.getAuthorities().stream().findFirst()
                .map(value -> value.getAuthority()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.FORBIDDEN, "An assigned role is required"));
        EffectiveFeaturePermissionResponse earningTypes = authorization.resolve(authentication.getName(), role,
                EffectiveAuthorizationServiceImpl.ADMIN_EARNING_TYPES);
        EffectiveFeaturePermissionResponse annualization = authorization.resolve(authentication.getName(), role,
                EffectiveAuthorizationServiceImpl.PAYROLL_ANNUALIZATION);
        boolean allowed = earningTypes.administrator() || annualization.administrator()
                || (add ? earningTypes.canAccess() && earningTypes.canAdd()
                    : edit ? earningTypes.canAccess() && earningTypes.canEdit()
                    : earningTypes.canAccess() || annualization.canAccess());
        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Benefit tax profile access is not permitted");
    }
}
