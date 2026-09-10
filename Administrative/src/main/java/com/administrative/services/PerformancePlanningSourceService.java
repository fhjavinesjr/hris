package com.administrative.services;

import com.administrative.dtos.PerformancePlanningSourceDtos.*;

public interface PerformancePlanningSourceService {
    OrganizationPage organizations(OrganizationType type,String search,int page,int size);
    OrganizationTarget organization(OrganizationType type,Long id);
    PersonnelMembership membership(Long employeeId);
    ApprovalRoute approvalRoute(Long businessUnitId,String requestCode);
}
