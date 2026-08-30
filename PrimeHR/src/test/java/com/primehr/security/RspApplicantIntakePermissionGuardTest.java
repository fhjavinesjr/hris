package com.primehr.security;

import com.primehr.integration.administrative.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RspApplicantIntakePermissionGuardTest {
    private final AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
    private final RspApplicantIntakePermissionGuard guard = new RspApplicantIntakePermissionGuard(client);

    @Test
    void accessAndMessagingAreIndependentAndAgencyWide() {
        String token = "Bearer token";
        when(client.resolve(RspApplicantIntakePermissionGuard.FEATURE, token)).thenReturn(permission(
                true, false, PermissionDataScope.AGENCY_WIDE));
        assertThatCode(() -> guard.require(PrimeHrAction.ACCESS, token)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ADD, token)).isInstanceOf(AccessDeniedException.class);

        when(client.resolve(RspApplicantIntakePermissionGuard.FEATURE, token)).thenReturn(permission(
                true, true, PermissionDataScope.OWN_RECORDS));
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ACCESS, token)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void administratorCompatibilityDoesNotRequireADataScope() {
        String token = "Bearer token";
        when(client.resolve(RspApplicantIntakePermissionGuard.FEATURE, token)).thenReturn(
                new EffectiveFeaturePermission(RspApplicantIntakePermissionGuard.FEATURE, true,
                        false, false, false, false, false, false, false,
                        false, false, false, PermissionDataScope.NONE));
        assertThatCode(() -> guard.require(PrimeHrAction.ACCESS, token)).doesNotThrowAnyException();
        assertThatCode(() -> guard.require(PrimeHrAction.ADD, token)).doesNotThrowAnyException();
    }

    private static EffectiveFeaturePermission permission(boolean access, boolean add, PermissionDataScope scope) {
        return new EffectiveFeaturePermission(RspApplicantIntakePermissionGuard.FEATURE, false,
                access, add, false, false, false, false, false,
                false, false, false, scope);
    }
}
