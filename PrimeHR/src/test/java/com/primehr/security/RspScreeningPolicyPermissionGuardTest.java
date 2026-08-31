package com.primehr.security;

import com.primehr.integration.administrative.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RspScreeningPolicyPermissionGuardTest {
    private final AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
    private final RspScreeningPolicyPermissionGuard guard = new RspScreeningPolicyPermissionGuard(client);

    @Test
    void actionsAreIndependentAndAgencyWide() {
        String token = "Bearer token";
        when(client.resolve(RspScreeningPolicyPermissionGuard.FEATURE, token)).thenReturn(permission(
                true, true, false, false, PermissionDataScope.AGENCY_WIDE));
        assertThatCode(() -> guard.require(PrimeHrAction.ACCESS, token)).doesNotThrowAnyException();
        assertThatCode(() -> guard.require(PrimeHrAction.ADD, token)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(PrimeHrAction.EDIT, token)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(PrimeHrAction.PUBLISH, token)).isInstanceOf(AccessDeniedException.class);

        when(client.resolve(RspScreeningPolicyPermissionGuard.FEATURE, token)).thenReturn(permission(
                true, true, true, true, PermissionDataScope.OWN_RECORDS));
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ACCESS, token)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void bearerTokenIsMandatoryAndAdministratorCompatibilityIsPreserved() {
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ACCESS, "token"))
                .isInstanceOf(AccessDeniedException.class);
        String token = "Bearer token";
        when(client.resolve(RspScreeningPolicyPermissionGuard.FEATURE, token)).thenReturn(
                new EffectiveFeaturePermission(RspScreeningPolicyPermissionGuard.FEATURE, true,
                        false, false, false, false, false, false, false,
                        false, false, false, PermissionDataScope.NONE));
        assertThatCode(() -> guard.require(PrimeHrAction.PUBLISH, token)).doesNotThrowAnyException();
    }

    private static EffectiveFeaturePermission permission(boolean access, boolean add, boolean edit,
                                                         boolean publish, PermissionDataScope scope) {
        return new EffectiveFeaturePermission(RspScreeningPolicyPermissionGuard.FEATURE, false,
                access, add, edit, false, publish, false, false,
                false, false, false, scope);
    }
}
