package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import org.springframework.stereotype.Component;

@Component
public class RspFormalReportPermissionGuard {
    public static final String COMPARATIVE="primehr.rsp-comparative-report";
    public static final String SELECTION="primehr.rsp-selection-report";
    public static final String EVIDENCE_INDEX="primehr.rsp-evidence-index-report";
    private final AdministrativeAuthorizationClient client;
    public RspFormalReportPermissionGuard(AdministrativeAuthorizationClient client){this.client=client;}
    public EffectiveFeaturePermission require(String feature,String token){return RspEvaluationPolicyPermissionGuard.requireAgencyWide(client,feature,PrimeHrAction.ACCESS,token);}
}
