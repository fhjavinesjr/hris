package com.administrative.controllers;

import com.administrative.dtos.EffectiveFeaturePermissionResponse;
import com.administrative.dtos.PerformancePlanningSourceDtos.*;
import com.administrative.impl.EffectiveAuthorizationServiceImpl;
import com.administrative.services.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integration/v1/primehr/performance")
public class PerformancePlanningSourceIntegrationController {
    private static final String[] FEATURES={"primehr.performance-objective","primehr.performance-plan-assignment"};
    private final PerformancePlanningSourceService sources;private final EffectiveAuthorizationService authorization;
    public PerformancePlanningSourceIntegrationController(PerformancePlanningSourceService s,EffectiveAuthorizationService a){sources=s;authorization=a;}
    @GetMapping("/organization-targets") public OrganizationPage list(Authentication a,@RequestParam OrganizationType type,@RequestParam(required=false)String search,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){require(a);return sources.organizations(type,search,page,size);}
    @GetMapping("/organization-targets/{type}/{id}") public OrganizationTarget get(Authentication a,@PathVariable OrganizationType type,@PathVariable Long id){require(a);return sources.organization(type,id);}
    @GetMapping("/personnel-membership/{employeeId}") public PersonnelMembership membership(Authentication a,@PathVariable Long employeeId){require(a);return sources.membership(employeeId);}
    @GetMapping("/approval-routes") public ApprovalRoute route(Authentication a,@RequestParam Long businessUnitId,@RequestParam String requestCode){require(a);return sources.approvalRoute(businessUnitId,requestCode);}
    private void require(Authentication a){if(a==null||!a.isAuthenticated())throw new AccessDeniedException("Authentication is required");String role=a.getAuthorities().stream().findFirst().map(v->v.getAuthority()).orElseThrow(()->new AccessDeniedException("No role"));for(String key:FEATURES){EffectiveFeaturePermissionResponse p=authorization.resolve(a.getName(),role,key);if(p.administrator()||p.canAccess()&&"AGENCY_WIDE".equals(p.dataScope()))return;}throw new AccessDeniedException("Performance planning source access is not permitted");}
}
