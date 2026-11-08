package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.PermissionDataScope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class RrAwardPermissionGuard {
    public static final String AWARD = "primehr.rr-award";
    public static final String FULFILLMENT = "primehr.rr-fulfillment";
    public static final String REPORT = "primehr.rr-report";
    public static final String ANALYTICS = "primehr.rr-analytics";
    private final AdministrativeAuthorizationClient client;
    public RrAwardPermissionGuard(AdministrativeAuthorizationClient client) { this.client = client; }
    public PermissionDataScope require(String feature, PrimeHrAction action, String token) {
        var permission = client.resolve(feature, token);
        boolean allowed = permission.administrator() || permission.canAccess() && switch (action) {
            case ACCESS -> true; case ADD -> permission.canAdd(); case EDIT -> permission.canEdit();
            case SUBMIT -> permission.canSubmit(); case APPROVE -> permission.canApprove();
            case FINALIZE -> permission.canFinalize(); case PUBLISH -> permission.canPublish(); default -> false;
        };
        if (!allowed) throw new AccessDeniedException("Rewards and Recognition award permission is required");
        var scope = permission.administrator() ? PermissionDataScope.AGENCY_WIDE : permission.dataScope();
        if (scope != PermissionDataScope.OWN_RECORDS && scope != PermissionDataScope.ASSIGNED_RECORDS && scope != PermissionDataScope.AGENCY_WIDE)
            throw new AccessDeniedException("Rewards and Recognition data scope is required");
        return scope;
    }
    public PermissionDataScope requireAgency(String feature, PrimeHrAction action, String token) {
        var scope = require(feature, action, token);
        if (scope != PermissionDataScope.AGENCY_WIDE) throw new AccessDeniedException("This action requires Agency Wide scope");
        return scope;
    }
}
