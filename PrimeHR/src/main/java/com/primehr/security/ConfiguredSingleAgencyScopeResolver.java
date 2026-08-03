package com.primehr.security;

import com.primehr.config.PrimeHrProperties;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ConfiguredSingleAgencyScopeResolver implements AgencyScopeResolver {

    private final String agencyId;

    public ConfiguredSingleAgencyScopeResolver(PrimeHrProperties properties) {
        this.agencyId = properties.agency().id();
        if (agencyId.isBlank()) {
            throw new IllegalStateException("PRIMEHR_AGENCY_ID must be configured");
        }
    }

    @Override
    public String resolveAgencyId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("An authenticated agency context is required");
        }
        return agencyId;
    }
}
