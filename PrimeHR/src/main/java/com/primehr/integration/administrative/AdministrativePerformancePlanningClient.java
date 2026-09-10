package com.primehr.integration.administrative;

import java.time.Instant;
import java.util.List;

public interface AdministrativePerformancePlanningClient {
    record OrganizationTarget(String type,Long id,String code,String name,Long areaId,String areaName,
            String sourceFingerprint,Instant fetchedAt) {}
    record PersonnelMembership(Long employeeId,Long businessUnitId,String businessUnitCode,String businessUnitName,
            Long areaId,String areaName,boolean head,boolean coApprover,String base,String sourceFingerprint,Instant fetchedAt) {}
    record ApprovalRouteStep(Long workflowId,int level,Long employeeId) {}
    record ApprovalRoute(Long employeeRequestId,String requestCode,int configuredMaximum,Long businessUnitId,
            Long areaId,List<ApprovalRouteStep> steps,String sourceFingerprint,Instant fetchedAt) {}
    OrganizationTarget organization(String type,Long id,String token);
    PersonnelMembership membership(Long employeeId,String token);
    ApprovalRoute approvalRoute(Long businessUnitId,String requestCode,String token);
}
