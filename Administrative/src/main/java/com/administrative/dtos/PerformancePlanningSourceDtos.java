package com.administrative.dtos;

import java.time.Instant;
import java.util.List;

public final class PerformancePlanningSourceDtos {
    private PerformancePlanningSourceDtos() {}
    public enum OrganizationType { AREA, BUSINESS_UNIT }
    public record OrganizationTarget(OrganizationType type,Long id,String code,String name,
            Long areaId,String areaName,String sourceFingerprint,Instant fetchedAt) {}
    public record OrganizationPage(List<OrganizationTarget> content,int page,int size,long totalElements,
            int totalPages,boolean first,boolean last) {}
    public record PersonnelMembership(Long employeeId,Long businessUnitId,String businessUnitCode,
            String businessUnitName,Long areaId,String areaName,boolean head,boolean coApprover,
            String base,String sourceFingerprint,Instant fetchedAt) {}
    public record ApprovalRouteStep(Long workflowId,int level,Long employeeId) {}
    public record ApprovalRoute(Long employeeRequestId,String requestCode,int configuredMaximum,
            Long businessUnitId,Long areaId,List<ApprovalRouteStep> steps,String sourceFingerprint,
            Instant fetchedAt) {}
}
