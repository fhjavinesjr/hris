package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.integration.administrative.PermissionDataScope;
import org.springframework.stereotype.Component;

@Component
public class PerformanceReportingPermissionGuard {
    public static final String FORM = "primehr.performance-form-report";
    public static final String MONITORING = "primehr.performance-monitoring-report";
    public static final String REGISTER = "primehr.performance-register-report";
    public static final String ANALYTICS = "primehr.performance-analytics";
    public static final String OUTCOME_FEED = "primehr.performance-outcome-feed";

    private final AdministrativeAuthorizationClient authorization;

    public PerformanceReportingPermissionGuard(AdministrativeAuthorizationClient authorization) {
        this.authorization = authorization;
    }

    public EffectiveFeaturePermission require(String featureKey, String token) {
        return RspEvaluationPolicyPermissionGuard.requireAgencyWide(
                authorization, featureKey, PrimeHrAction.ACCESS, token);
    }

    public PermissionDataScope requireScoped(String featureKey, String token) {
        if (token == null || !token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new org.springframework.security.access.AccessDeniedException("A bearer token is required");
        }
        EffectiveFeaturePermission permission = authorization.resolve(featureKey, token);
        if (!permission.administrator() && !permission.canAccess()) {
            throw new org.springframework.security.access.AccessDeniedException("Performance report permission is required");
        }
        PermissionDataScope scope = permission.administrator()
                ? PermissionDataScope.AGENCY_WIDE : permission.dataScope();
        if (scope != PermissionDataScope.OWN_RECORDS && scope != PermissionDataScope.ASSIGNED_RECORDS
                && scope != PermissionDataScope.AGENCY_WIDE) {
            throw new org.springframework.security.access.AccessDeniedException("Performance report data scope is required");
        }
        return scope;
    }
}
