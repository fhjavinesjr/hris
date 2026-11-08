package com.primehr.learningdevelopment.report.application;

import com.primehr.integration.administrative.PermissionDataScope;

public interface LdFormalReportService {
    byte[] individualPlan(String agency,String id,String actor,PermissionDataScope scope,String correlation);
    byte[] trainingOrder(String agency,String offeringId,String actor,PermissionDataScope scope,String correlation);
    byte[] completionPacket(String agency,String completionId,String actor,PermissionDataScope scope,String correlation);
    byte[] evaluationSummary(String agency,String completionId,String actor,PermissionDataScope scope,String correlation);
}
