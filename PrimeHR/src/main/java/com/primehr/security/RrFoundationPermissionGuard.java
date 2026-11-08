package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.integration.administrative.PermissionDataScope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class RrFoundationPermissionGuard {
    public static final String GOVERNANCE = "primehr.rr-governance";
    public static final String COMMITTEE = "primehr.rr-committee";

    private final AdministrativeAuthorizationClient client;

    public RrFoundationPermissionGuard(AdministrativeAuthorizationClient client) {
        this.client = client;
    }

    public void require(String feature, PrimeHrAction action, String token) {
        EffectiveFeaturePermission permission = client.resolve(feature, token);
        boolean allowed = permission.administrator() || permission.canAccess() && switch (action) {
            case ACCESS -> true;
            case ADD -> permission.canAdd();
            case EDIT -> permission.canEdit();
            case PUBLISH -> permission.canPublish();
            default -> false;
        };
        if (!allowed || !permission.administrator() && permission.dataScope() != PermissionDataScope.AGENCY_WIDE) {
            throw new AccessDeniedException("Rewards and Recognition foundation requires agency-wide "
                    + feature + " permission");
        }
    }
}
