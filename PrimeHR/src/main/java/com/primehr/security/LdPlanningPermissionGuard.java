package com.primehr.security;
import com.primehr.integration.administrative.*;import org.springframework.security.access.AccessDeniedException;import org.springframework.stereotype.Component;
@Component public class LdPlanningPermissionGuard{
 public static final String NEEDS="primehr.ld-needs",IDP="primehr.ld-individual-plan";private final AdministrativeAuthorizationClient client;
 public LdPlanningPermissionGuard(AdministrativeAuthorizationClient client){this.client=client;}
 public PermissionDataScope require(String feature,PrimeHrAction action,String token){EffectiveFeaturePermission p=client.resolve(feature,token);boolean allowed=p.administrator()||p.canAccess()&&switch(action){case ACCESS->true;case ADD->p.canAdd();case EDIT->p.canEdit();case SUBMIT->p.canSubmit();case APPROVE->p.canApprove();case FINALIZE->p.canFinalize();case ARCHIVE->p.canDelete();default->false;};if(!allowed)throw new AccessDeniedException("L&D planning permission is required");PermissionDataScope scope=p.administrator()?PermissionDataScope.AGENCY_WIDE:p.dataScope();if(scope!=PermissionDataScope.OWN_RECORDS&&scope!=PermissionDataScope.ASSIGNED_RECORDS&&scope!=PermissionDataScope.AGENCY_WIDE)throw new AccessDeniedException("L&D planning scope is not permitted");return scope;}
}
