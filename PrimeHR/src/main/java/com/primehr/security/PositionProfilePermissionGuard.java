package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class PositionProfilePermissionGuard {
    public static final String FEATURE_KEY = "primehr.position-profile";

    private final AdministrativeAuthorizationClient client;

    public PositionProfilePermissionGuard(AdministrativeAuthorizationClient client) {
        this.client = client;
    }

    public EffectiveFeaturePermission require(PrimeHrAction action, String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new AccessDeniedException("A bearer token is required");
        }
        if (action == PrimeHrAction.PUBLISH) {
            throw new AccessDeniedException("Position profiles use the dedicated submit and approve actions");
        }
        EffectiveFeaturePermission permission = client.resolve(FEATURE_KEY, authorizationHeader);
        boolean allowed = permission.administrator() || switch (action) {
            case ACCESS -> permission.canAccess();
            case ADD -> permission.canAccess() && permission.canAdd();
            case EDIT -> permission.canAccess() && permission.canEdit();
            case ARCHIVE -> permission.canAccess() && permission.canDelete();
            case PUBLISH -> false;
            case SUBMIT -> permission.canAccess() && permission.canSubmit();
            case APPROVE -> permission.canAccess() && permission.canApprove();
        };
        if (!allowed) throw new AccessDeniedException("The required position profile action is not permitted");
        return permission;
    }
}
