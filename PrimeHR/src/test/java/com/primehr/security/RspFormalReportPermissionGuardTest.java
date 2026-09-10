package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.integration.administrative.PermissionDataScope;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RspFormalReportPermissionGuardTest {
    @Test
    void reportPermissionsAreIndependentAgencyWideAndFailClosed() {
        AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
        RspFormalReportPermissionGuard guard = new RspFormalReportPermissionGuard(client);
        when(client.resolve(eq(RspFormalReportPermissionGuard.COMPARATIVE), anyString()))
                .thenReturn(permission(RspFormalReportPermissionGuard.COMPARATIVE, true,
                        PermissionDataScope.AGENCY_WIDE));
        when(client.resolve(eq(RspFormalReportPermissionGuard.SELECTION), anyString()))
                .thenReturn(permission(RspFormalReportPermissionGuard.SELECTION, false,
                        PermissionDataScope.AGENCY_WIDE));
        when(client.resolve(eq(RspFormalReportPermissionGuard.EVIDENCE_INDEX), anyString()))
                .thenReturn(permission(RspFormalReportPermissionGuard.EVIDENCE_INDEX, true,
                        PermissionDataScope.OWN_RECORDS));

        assertThatCode(() -> guard.require(RspFormalReportPermissionGuard.COMPARATIVE, "Bearer token"))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(RspFormalReportPermissionGuard.SELECTION, "Bearer token"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(RspFormalReportPermissionGuard.EVIDENCE_INDEX, "Bearer token"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(RspFormalReportPermissionGuard.COMPARATIVE, "invalid"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private static EffectiveFeaturePermission permission(String feature, boolean access,
                                                         PermissionDataScope scope) {
        return new EffectiveFeaturePermission(feature, false, access, false, false, false,
                false, false, false, false, false, false, scope);
    }
}
