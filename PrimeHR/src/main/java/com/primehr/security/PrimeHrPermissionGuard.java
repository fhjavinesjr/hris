package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class PrimeHrPermissionGuard {
    private final AdministrativeAuthorizationClient client;

    public PrimeHrPermissionGuard(AdministrativeAuthorizationClient client) {
        this.client = client;
    }

    public void require(PrimeHrAction action, String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new AccessDeniedException("A bearer token is required");
        }
        EffectiveFeaturePermission permission = client.resolve(authorizationHeader);
        boolean allowed = permission.administrator() || switch (action) {
            case ACCESS -> permission.canAccess();
            case ADD -> permission.canAdd();
            case EDIT -> permission.canEdit();
            case ARCHIVE -> permission.canDelete();
            case PUBLISH -> permission.canPublish();
            case SUBMIT, APPROVE -> false;
        };
        if (!allowed) throw new AccessDeniedException("The required PrimeHR action is not permitted");
    }
}
