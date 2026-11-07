package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.PermissionDataScope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class EvidenceReportingPermissionGuard {
    public static final String REPORT = "primehr.evidence-report";
    public static final String ANALYTICS = "primehr.evidence-analytics";
    private final AdministrativeAuthorizationClient authorization;

    public EvidenceReportingPermissionGuard(AdministrativeAuthorizationClient authorization) {
        this.authorization = authorization;
    }

    public PermissionDataScope requireReport(String token) {
        var permission = authorization.resolve(REPORT, token);
        boolean allowed = permission.administrator() || permission.canAccess();
        PermissionDataScope scope = permission.administrator()
                ? PermissionDataScope.AGENCY_WIDE : permission.dataScope();
        if (!allowed || scope != PermissionDataScope.ASSIGNED_RECORDS
                && scope != PermissionDataScope.AGENCY_WIDE) {
            throw new AccessDeniedException("Evidence reporting requires Assigned Records or Agency Wide access");
        }
        return scope;
    }

    public void requireAnalytics(String token) {
        var permission = authorization.resolve(ANALYTICS, token);
        PermissionDataScope scope = permission.administrator()
                ? PermissionDataScope.AGENCY_WIDE : permission.dataScope();
        if ((!permission.administrator() && !permission.canAccess()) || scope != PermissionDataScope.AGENCY_WIDE) {
            throw new AccessDeniedException("Evidence analytics requires Agency Wide access");
        }
    }
}
