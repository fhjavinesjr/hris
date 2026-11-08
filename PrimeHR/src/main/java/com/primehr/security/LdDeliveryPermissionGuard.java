package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.integration.administrative.PermissionDataScope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class LdDeliveryPermissionGuard {
    public static final String PLAN = "primehr.ld-agency-plan";
    public static final String PROGRAM = "primehr.ld-program";

    private final AdministrativeAuthorizationClient client;

    public LdDeliveryPermissionGuard(AdministrativeAuthorizationClient client) {
        this.client = client;
    }

    public PermissionDataScope require(String feature, PrimeHrAction action, String token) {
        EffectiveFeaturePermission permission = client.resolve(feature, token);
        boolean allowed = permission.administrator() || permission.canAccess() && switch (action) {
            case ACCESS -> true;
            case ADD -> permission.canAdd();
            case EDIT -> permission.canEdit();
            case ARCHIVE -> permission.canDelete();
            case SUBMIT -> permission.canSubmit();
            case APPROVE -> permission.canApprove();
            case PUBLISH -> permission.canPublish();
            default -> false;
        };
        if (!allowed) {
            throw new AccessDeniedException("L&D plan/program permission is required");
        }
        PermissionDataScope scope = permission.administrator()
                ? PermissionDataScope.AGENCY_WIDE
                : permission.dataScope();
        if (scope != PermissionDataScope.ASSIGNED_RECORDS && scope != PermissionDataScope.AGENCY_WIDE) {
            throw new AccessDeniedException("L&D plan/program scope must be Assigned Records or Agency Wide");
        }
        return scope;
    }
}
