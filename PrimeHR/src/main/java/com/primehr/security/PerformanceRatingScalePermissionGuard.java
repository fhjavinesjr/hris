package com.primehr.security;
import com.primehr.integration.administrative.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
@Component public class PerformanceRatingScalePermissionGuard{
 public static final String FEATURE="primehr.performance-rating-scale";
 private final AdministrativeAuthorizationClient client;
 public PerformanceRatingScalePermissionGuard(AdministrativeAuthorizationClient client){this.client=client;}
 public void require(PrimeHrAction action,String token){EffectiveFeaturePermission p=client.resolve(FEATURE,token);boolean allowed=p.administrator()||p.canAccess()&&switch(action){case ACCESS->true;case ADD->p.canAdd();case EDIT->p.canEdit();case PUBLISH->p.canPublish();default->false;};if(!allowed||!p.administrator()&&p.dataScope()!=PermissionDataScope.AGENCY_WIDE)throw new AccessDeniedException("Performance rating scales require agency-wide permission");}
}
