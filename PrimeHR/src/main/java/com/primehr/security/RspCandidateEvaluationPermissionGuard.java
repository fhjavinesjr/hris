package com.primehr.security;
import com.primehr.integration.administrative.*; import org.springframework.stereotype.*;
@Component public class RspCandidateEvaluationPermissionGuard {public static final String FEATURE="primehr.rsp-candidate-evaluation";private final AdministrativeAuthorizationClient client;public RspCandidateEvaluationPermissionGuard(AdministrativeAuthorizationClient c){client=c;}public EffectiveFeaturePermission require(PrimeHrAction action,String token){return RspEvaluationPolicyPermissionGuard.requireAgencyWide(client,FEATURE,action,token);}}
