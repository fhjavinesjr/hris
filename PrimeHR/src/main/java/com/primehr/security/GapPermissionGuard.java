package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.integration.administrative.PermissionDataScope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class GapPermissionGuard {
    public static final String CONFIGURATION = "primehr.gap-configuration";
    public static final String GAP = "primehr.competency-gap";
    public static final String REFERRAL = "primehr.ld-referral";
    private static final Set<String> FEATURES = Set.of(CONFIGURATION, GAP, REFERRAL);

    private final AdministrativeAuthorizationClient client;

    public GapPermissionGuard(AdministrativeAuthorizationClient client) {
        this.client = client;
    }

    public EffectiveFeaturePermission require(String featureKey, PrimeHrAction action, String authorizationHeader) {
        requireBearer(authorizationHeader);
        if (!FEATURES.contains(featureKey)) {
            throw new IllegalArgumentException("Unsupported competency-gap feature key");
        }
        EffectiveFeaturePermission permission = client.resolve(featureKey, authorizationHeader);
        boolean allowed = permission.administrator() || permission.canAccess() && switch (action) {
            case ACCESS -> true;
            case ADD -> permission.canAdd();
            case EDIT -> permission.canEdit();
            case ARCHIVE -> permission.canDelete();
            case PUBLISH -> permission.canPublish();
            case SUBMIT -> permission.canSubmit();
            case APPROVE -> permission.canApprove();
            case ASSESS -> permission.canAssess();
            case VALIDATE -> permission.canValidate();
            case FINALIZE -> permission.canFinalize();
        };
        if (!allowed) {
            throw new AccessDeniedException("The required competency-gap action is not permitted");
        }
        return permission;
    }

    public EffectiveFeaturePermission requireAgencyWide(String featureKey, PrimeHrAction action,
                                                        String authorizationHeader) {
        EffectiveFeaturePermission permission = require(featureKey, action, authorizationHeader);
        if (!permission.administrator() && permission.dataScope() != PermissionDataScope.AGENCY_WIDE) {
            throw new AccessDeniedException("This competency-gap action requires agency-wide data scope");
        }
        return permission;
    }

    private static void requireBearer(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new AccessDeniedException("A bearer token is required");
        }
    }
}
