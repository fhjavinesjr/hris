package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.integration.administrative.PermissionDataScope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class RspApplicantIntakePermissionGuard {
    public static final String FEATURE = "primehr.rsp-applicant-intake";

    private final AdministrativeAuthorizationClient client;

    public RspApplicantIntakePermissionGuard(AdministrativeAuthorizationClient client) {
        this.client = client;
    }

    public EffectiveFeaturePermission require(PrimeHrAction action, String token) {
        if (token == null || !token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new AccessDeniedException("A bearer token is required");
        }
        EffectiveFeaturePermission permission = client.resolve(FEATURE, token);
        boolean allowed = permission.administrator() || permission.canAccess() && switch (action) {
            case ACCESS -> true;
            case ADD -> permission.canAdd();
            default -> false;
        };
        if (!allowed || !permission.administrator()
                && permission.dataScope() != PermissionDataScope.AGENCY_WIDE) {
            throw new AccessDeniedException("RSP applicant intake requires agency-wide permission");
        }
        return permission;
    }
}
