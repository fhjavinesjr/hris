package com.primehr.succession.report;

import com.primehr.integration.administrative.PermissionDataScope;

public interface SuccessionReportService {
    byte[] readiness(String agency, String id, String actor, PermissionDataScope scope);
    byte[] development(String agency, String id, String actor, PermissionDataScope scope);
    byte[] continuity(String agency, String id, String actor, PermissionDataScope scope);
    byte[] referral(String agency, String id, String actor, PermissionDataScope scope);
    byte[] coverageRegister(String agency, String actor, PermissionDataScope scope);
    SuccessionReportData.Analytics analytics(String agency, String actor);
}
