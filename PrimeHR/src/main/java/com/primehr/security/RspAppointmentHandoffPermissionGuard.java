package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import org.springframework.stereotype.Component;

@Component
public class RspAppointmentHandoffPermissionGuard {
    public static final String FEATURE = "primehr.rsp-appointment-handoff";
    private final AdministrativeAuthorizationClient client;

    public RspAppointmentHandoffPermissionGuard(AdministrativeAuthorizationClient client) {
        this.client = client;
    }

    public EffectiveFeaturePermission require(PrimeHrAction action, String token) {
        return RspEvaluationPolicyPermissionGuard.requireAgencyWide(client, FEATURE, action, token);
    }
}
