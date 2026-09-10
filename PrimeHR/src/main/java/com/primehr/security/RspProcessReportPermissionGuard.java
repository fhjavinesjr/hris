package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import org.springframework.stereotype.Component;

@Component
public class RspProcessReportPermissionGuard {
    public static final String REGISTER="primehr.rsp-register-report";
    public static final String ANALYTICS="primehr.rsp-process-analytics";
    private final AdministrativeAuthorizationClient client;
    public RspProcessReportPermissionGuard(AdministrativeAuthorizationClient client){this.client=client;}
    public EffectiveFeaturePermission require(String feature,String token){
        return RspEvaluationPolicyPermissionGuard.requireAgencyWide(client,feature,PrimeHrAction.ACCESS,token);
    }
}
