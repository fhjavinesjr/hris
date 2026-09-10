package com.primehr.security;
import com.primehr.integration.administrative.*; import org.springframework.stereotype.*;
@Component public class HrmpsbGovernancePermissionGuard {public static final String FEATURE="primehr.hrmpsb-governance";private final AdministrativeAuthorizationClient client;public HrmpsbGovernancePermissionGuard(AdministrativeAuthorizationClient c){client=c;}public EffectiveFeaturePermission require(PrimeHrAction action,String token){return RspEvaluationPolicyPermissionGuard.requireAgencyWide(client,FEATURE,action,token);}}
