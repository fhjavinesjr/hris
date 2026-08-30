package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.integration.administrative.PermissionDataScope;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RspPlanningPermissionGuardTest {
    private final AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
    private final RspPlanningPermissionGuard guard = new RspPlanningPermissionGuard(client);

    @Test
    void accessAddAndEditAreIndependentAndRequireAgencyWideScope() {
        String token = "Bearer token";
        when(client.resolve(RspPlanningPermissionGuard.FEATURE, token)).thenReturn(permission(
                true, true, false, false, PermissionDataScope.AGENCY_WIDE));

        assertThatCode(() -> guard.require(PrimeHrAction.ACCESS, token)).doesNotThrowAnyException();
        assertThatCode(() -> guard.require(PrimeHrAction.ADD, token)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(PrimeHrAction.EDIT, token))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void missingAccessLegacyScopeAndMalformedBearerFailClosed() {
        String token = "Bearer token";
        when(client.resolve(RspPlanningPermissionGuard.FEATURE, token)).thenReturn(permission(
                false, true, true, true, PermissionDataScope.AGENCY_WIDE));
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ADD, token))
                .isInstanceOf(AccessDeniedException.class);

        when(client.resolve(RspPlanningPermissionGuard.FEATURE, token)).thenReturn(permission(
                true, true, true, true, PermissionDataScope.OWN_RECORDS));
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ACCESS, token))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ACCESS, "token"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void administratorRetainsSupportedActions() {
        String token = "Bearer token";
        when(client.resolve(RspPlanningPermissionGuard.FEATURE, token)).thenReturn(
                new EffectiveFeaturePermission(RspPlanningPermissionGuard.FEATURE, true,
                        false, false, false, false, false, false, false, false, false, false,
                        PermissionDataScope.NONE));
        assertThatCode(() -> guard.require(PrimeHrAction.ARCHIVE, token)).doesNotThrowAnyException();
    }

    private static EffectiveFeaturePermission permission(boolean access, boolean add, boolean edit,
                                                          boolean delete, PermissionDataScope scope) {
        return new EffectiveFeaturePermission(RspPlanningPermissionGuard.FEATURE, false,
                access, add, edit, delete, false, false, false, false, false, false, scope);
    }
}
