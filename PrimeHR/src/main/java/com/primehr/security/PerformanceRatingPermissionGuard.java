package com.primehr.security;

import com.primehr.integration.administrative.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class PerformanceRatingPermissionGuard {
    public static final String FEATURE="primehr.performance-rating";
    private final AdministrativeAuthorizationClient client;
    public PerformanceRatingPermissionGuard(AdministrativeAuthorizationClient client){this.client=client;}
    public PermissionDataScope require(PrimeHrAction action,String token){
        EffectiveFeaturePermission p=client.resolve(FEATURE,token);
        boolean allowed=p.administrator()||p.canAccess()&&switch(action){case ACCESS->true;case ADD->p.canAdd();case EDIT->p.canEdit();case SUBMIT->p.canSubmit();case APPROVE->p.canApprove();default->false;};
        if(!allowed)throw new AccessDeniedException("Performance rating permission is required");
        PermissionDataScope scope=p.administrator()?PermissionDataScope.AGENCY_WIDE:p.dataScope();
        if(scope!=PermissionDataScope.OWN_RECORDS&&scope!=PermissionDataScope.ASSIGNED_RECORDS&&scope!=PermissionDataScope.AGENCY_WIDE)throw new AccessDeniedException("Performance rating scope is not permitted");
        return scope;
    }
}
