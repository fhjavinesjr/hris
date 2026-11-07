package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.PermissionDataScope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class LdReportingPermissionGuard {
    public static final String REPORT = "primehr.ld-report";
    public static final String ANALYTICS = "primehr.ld-analytics";
    private final AdministrativeAuthorizationClient authorization;

    public LdReportingPermissionGuard(AdministrativeAuthorizationClient authorization) {
        this.authorization = authorization;
    }

    public PermissionDataScope requireReport(String token) {
        if (token == null || !token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new AccessDeniedException("A bearer token is required");
        }
        var permission = authorization.resolve(REPORT, token);
        if (!permission.administrator() && !permission.canAccess()) {
            throw new AccessDeniedException("L&D report permission is required");
        }
        PermissionDataScope scope = permission.administrator()
                ? PermissionDataScope.AGENCY_WIDE : permission.dataScope();
        if (scope == null || scope == PermissionDataScope.NONE) {
            throw new AccessDeniedException("L&D report data scope is required");
        }
        return scope;
    }

    public void requireAnalytics(String token) {
        var permission = authorization.resolve(ANALYTICS, token);
        PermissionDataScope scope = permission.administrator()
                ? PermissionDataScope.AGENCY_WIDE : permission.dataScope();
        if ((!permission.administrator() && !permission.canAccess()) || scope != PermissionDataScope.AGENCY_WIDE) {
            throw new AccessDeniedException("L&D analytics requires Agency Wide permission");
        }
    }
}
