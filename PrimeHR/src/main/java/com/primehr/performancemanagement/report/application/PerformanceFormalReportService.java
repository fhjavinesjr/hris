package com.primehr.performancemanagement.report.application;

import com.primehr.integration.administrative.PermissionDataScope;

public interface PerformanceFormalReportService {
    String formTypeForCommitment(String agencyId,String commitmentVersionId);
    String formTypeForFinalRating(String agencyId,String finalRatingId);
    byte[] planningForm(String agencyId,String commitmentVersionId,String authorization,String actor,PermissionDataScope scope,String correlationId);
    byte[] ratedForm(String agencyId,String finalRatingId,String authorization,String actor,PermissionDataScope scope,String correlationId);
    byte[] monitoring(String agencyId,String monitoringCaseId,String authorization,String actor,PermissionDataScope scope,String correlationId);
    byte[] coaching(String agencyId,String coachingSessionId,String authorization,String actor,PermissionDataScope scope,String correlationId);
    byte[] intervention(String agencyId,String interventionId,boolean developmentPlan,String authorization,String actor,PermissionDataScope scope,String correlationId);
}
