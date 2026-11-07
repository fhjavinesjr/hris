package com.primehr.security;

import com.primehr.integration.administrative.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerformanceReportingPermissionGuardTest {
    @Test
    void eachReportingSurfaceRequiresItsOwnAccessPermissionAndAgencyWideScope() {
        AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
        PerformanceReportingPermissionGuard guard = new PerformanceReportingPermissionGuard(client);
        when(client.resolve(PerformanceReportingPermissionGuard.REGISTER, "Bearer token"))
                .thenReturn(permission(PerformanceReportingPermissionGuard.REGISTER, true,
                        PermissionDataScope.AGENCY_WIDE));
        when(client.resolve(PerformanceReportingPermissionGuard.ANALYTICS, "Bearer token"))
                .thenReturn(permission(PerformanceReportingPermissionGuard.ANALYTICS, true,
                        PermissionDataScope.OWN_RECORDS));
        when(client.resolve(PerformanceReportingPermissionGuard.OUTCOME_FEED, "Bearer token"))
                .thenReturn(permission(PerformanceReportingPermissionGuard.OUTCOME_FEED, false,
                        PermissionDataScope.AGENCY_WIDE));

        assertThatCode(() -> guard.require(PerformanceReportingPermissionGuard.REGISTER, "Bearer token"))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(PerformanceReportingPermissionGuard.ANALYTICS, "Bearer token"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(PerformanceReportingPermissionGuard.OUTCOME_FEED, "Bearer token"))
                .isInstanceOf(AccessDeniedException.class);

        when(client.resolve(PerformanceReportingPermissionGuard.FORM, "Bearer token"))
                .thenReturn(permission(PerformanceReportingPermissionGuard.FORM, true,
                        PermissionDataScope.OWN_RECORDS));
        when(client.resolve(PerformanceReportingPermissionGuard.MONITORING, "Bearer token"))
                .thenReturn(permission(PerformanceReportingPermissionGuard.MONITORING, true,
                        PermissionDataScope.ASSIGNED_RECORDS));
        assertThat(guard.requireScoped(PerformanceReportingPermissionGuard.FORM, "Bearer token"))
                .isEqualTo(PermissionDataScope.OWN_RECORDS);
        assertThat(guard.requireScoped(PerformanceReportingPermissionGuard.MONITORING, "Bearer token"))
                .isEqualTo(PermissionDataScope.ASSIGNED_RECORDS);
    }

    private static EffectiveFeaturePermission permission(String feature, boolean access, PermissionDataScope scope) {
        return new EffectiveFeaturePermission(feature, false, access, false, false, false, false, false, false,
                false, false, false, scope);
    }
}
