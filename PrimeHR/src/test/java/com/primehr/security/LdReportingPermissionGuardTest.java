package com.primehr.security;

import com.primehr.integration.administrative.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LdReportingPermissionGuardTest {
    @Test void reportAcceptsExplicitScopedAccessWhileAnalyticsRequiresAgencyWide() {
        AdministrativeAuthorizationClient client=mock(AdministrativeAuthorizationClient.class);
        LdReportingPermissionGuard guard=new LdReportingPermissionGuard(client);
        when(client.resolve(LdReportingPermissionGuard.REPORT,"Bearer token"))
                .thenReturn(permission(LdReportingPermissionGuard.REPORT,true,PermissionDataScope.OWN_RECORDS));
        when(client.resolve(LdReportingPermissionGuard.ANALYTICS,"Bearer token"))
                .thenReturn(permission(LdReportingPermissionGuard.ANALYTICS,true,PermissionDataScope.ASSIGNED_RECORDS));

        assertThat(guard.requireReport("Bearer token")).isEqualTo(PermissionDataScope.OWN_RECORDS);
        assertThatThrownBy(()->guard.requireAnalytics("Bearer token")).isInstanceOf(AccessDeniedException.class);

        when(client.resolve(LdReportingPermissionGuard.ANALYTICS,"Bearer token"))
                .thenReturn(permission(LdReportingPermissionGuard.ANALYTICS,true,PermissionDataScope.AGENCY_WIDE));
        assertThatCode(()->guard.requireAnalytics("Bearer token")).doesNotThrowAnyException();
        assertThatThrownBy(()->guard.requireReport(null)).isInstanceOf(AccessDeniedException.class);
    }

    private static EffectiveFeaturePermission permission(String feature,boolean access,PermissionDataScope scope){
        return new EffectiveFeaturePermission(feature,false,access,false,false,false,false,false,false,false,false,false,scope);
    }
}
