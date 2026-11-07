package com.primehr.security;

import com.primehr.integration.administrative.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class LdParticipationPermissionGuard {
    public static final String NOMINATION="primehr.ld-nomination";
    public static final String DELIVERY="primehr.ld-delivery";
    private final AdministrativeAuthorizationClient client;
    public LdParticipationPermissionGuard(AdministrativeAuthorizationClient client){this.client=client;}
    public PermissionDataScope require(String feature,PrimeHrAction action,String token){
        EffectiveFeaturePermission p=client.resolve(feature,token);
        boolean allowed=p.administrator()||p.canAccess()&&switch(action){
            case ACCESS->true;case ADD->p.canAdd();case EDIT->p.canEdit();case ARCHIVE->p.canDelete();
            case SUBMIT->p.canSubmit();case APPROVE->p.canApprove();case FINALIZE->p.canFinalize();default->false;};
        if(!allowed)throw new AccessDeniedException("L&D nomination/delivery permission is required");
        PermissionDataScope scope=p.administrator()?PermissionDataScope.AGENCY_WIDE:p.dataScope();
        if(feature.equals(DELIVERY)&&scope==PermissionDataScope.OWN_RECORDS)throw new AccessDeniedException("L&D delivery scope must be Assigned Records or Agency Wide");
        return scope;
    }
}
