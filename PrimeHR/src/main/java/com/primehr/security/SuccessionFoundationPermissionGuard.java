package com.primehr.security;
import com.primehr.integration.administrative.*; import org.springframework.security.access.AccessDeniedException; import org.springframework.stereotype.Component;
@Component public class SuccessionFoundationPermissionGuard {
 public static final String GOVERNANCE="primehr.succession-governance",COMMITTEE="primehr.succession-committee"; private final AdministrativeAuthorizationClient client;
 public SuccessionFoundationPermissionGuard(AdministrativeAuthorizationClient client){this.client=client;}
 public void require(String feature,PrimeHrAction action,String token){var p=client.resolve(feature,token);boolean allowed=p.administrator()||p.canAccess()&&switch(action){case ACCESS->true;case ADD->p.canAdd();case EDIT->p.canEdit();case PUBLISH->p.canPublish();default->false;};if(!allowed||!p.administrator()&&p.dataScope()!=PermissionDataScope.AGENCY_WIDE)throw new AccessDeniedException("Succession governance requires agency-wide "+feature+" permission");}
}
