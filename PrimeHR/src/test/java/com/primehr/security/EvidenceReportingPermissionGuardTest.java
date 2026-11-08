package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.integration.administrative.PermissionDataScope;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvidenceReportingPermissionGuardTest {
    private final AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
    private final EvidenceReportingPermissionGuard guard = new EvidenceReportingPermissionGuard(client);

    @Test
    void reportAcceptsAssignedScopeButAnalyticsRequiresAgencyWide() {
        when(client.resolve(EvidenceReportingPermissionGuard.REPORT, "Bearer token"))
                .thenReturn(permission(true, PermissionDataScope.ASSIGNED_RECORDS));
        when(client.resolve(EvidenceReportingPermissionGuard.ANALYTICS, "Bearer token"))
                .thenReturn(permission(true, PermissionDataScope.ASSIGNED_RECORDS));
        assertThat(guard.requireReport("Bearer token")).isEqualTo(PermissionDataScope.ASSIGNED_RECORDS);
        assertThatThrownBy(() -> guard.requireAnalytics("Bearer token"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void permissionsFailClosedIndependently() {
        when(client.resolve(EvidenceReportingPermissionGuard.REPORT, "Bearer token"))
                .thenReturn(permission(false, PermissionDataScope.AGENCY_WIDE));
        assertThatThrownBy(() -> guard.requireReport("Bearer token"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private static EffectiveFeaturePermission permission(boolean access, PermissionDataScope scope) {
        return new EffectiveFeaturePermission("feature", false, access, false, false, false,
                false, false, false, false, false, false, scope);
    }
}
