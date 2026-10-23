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

class PerformanceMonitoringPermissionGuardTest {

    private final AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
    private final PerformanceMonitoringPermissionGuard guard = new PerformanceMonitoringPermissionGuard(client);

    @Test
    void monitoringActionsRemainIndependentAndPreserveTheirConfiguredScope() {
        when(client.resolve(PerformanceMonitoringPermissionGuard.FEATURE, "token"))
                .thenReturn(permission(true, true, true, true, false, PermissionDataScope.OWN_RECORDS));

        assertThat(guard.require(PrimeHrAction.ADD, "token")).isEqualTo(PermissionDataScope.OWN_RECORDS);
        assertThat(guard.require(PrimeHrAction.EDIT, "token")).isEqualTo(PermissionDataScope.OWN_RECORDS);
        assertThat(guard.require(PrimeHrAction.SUBMIT, "token")).isEqualTo(PermissionDataScope.OWN_RECORDS);
        assertThatThrownBy(() -> guard.require(PrimeHrAction.APPROVE, "token"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void supervisorApprovalDoesNotGrantEmployeeMutationAndUnsupportedScopesAreRejected() {
        when(client.resolve(PerformanceMonitoringPermissionGuard.FEATURE, "token"))
                .thenReturn(permission(true, false, false, false, true, PermissionDataScope.ASSIGNED_RECORDS));

        assertThat(guard.require(PrimeHrAction.APPROVE, "token"))
                .isEqualTo(PermissionDataScope.ASSIGNED_RECORDS);
        assertThatThrownBy(() -> guard.require(PrimeHrAction.EDIT, "token"))
                .isInstanceOf(AccessDeniedException.class);

        when(client.resolve(PerformanceMonitoringPermissionGuard.FEATURE, "token"))
                .thenReturn(permission(true, true, true, true, true, PermissionDataScope.NONE));
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ACCESS, "token"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void administratorRetainsAuthorityWithAgencyWideEffectiveScope() {
        when(client.resolve(PerformanceMonitoringPermissionGuard.FEATURE, "admin"))
                .thenReturn(new EffectiveFeaturePermission(PerformanceMonitoringPermissionGuard.FEATURE,
                        true, false, false, false, false, false, false, false, false, false, false,
                        PermissionDataScope.NONE));

        assertThat(guard.require(PrimeHrAction.APPROVE, "admin"))
                .isEqualTo(PermissionDataScope.AGENCY_WIDE);
    }

    private static EffectiveFeaturePermission permission(boolean access, boolean add, boolean edit,
                                                         boolean submit, boolean approve,
                                                         PermissionDataScope scope) {
        return new EffectiveFeaturePermission(PerformanceMonitoringPermissionGuard.FEATURE,
                false, access, add, edit, false, false, submit, approve, false, false, false, scope);
    }
}
