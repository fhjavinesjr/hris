package com.primehr.security;

import com.primehr.integration.administrative.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LdPlanningPermissionGuardTest {
    @Test
    void exactActionFlagsAndSupportedScopesAreEnforced() {
        AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
        LdPlanningPermissionGuard guard = new LdPlanningPermissionGuard(client);
        when(client.resolve(LdPlanningPermissionGuard.NEEDS, "Bearer token"))
                .thenReturn(permission(true, true, false, false, false, true, PermissionDataScope.AGENCY_WIDE));
        when(client.resolve(LdPlanningPermissionGuard.IDP, "Bearer token"))
                .thenReturn(permission(true, false, true, true, true, false, PermissionDataScope.ASSIGNED_RECORDS));

        assertThat(guard.require(LdPlanningPermissionGuard.NEEDS, PrimeHrAction.ADD, "Bearer token"))
                .isEqualTo(PermissionDataScope.AGENCY_WIDE);
        assertThatThrownBy(() -> guard.require(LdPlanningPermissionGuard.NEEDS, PrimeHrAction.EDIT, "Bearer token"))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(guard.require(LdPlanningPermissionGuard.IDP, PrimeHrAction.SUBMIT, "Bearer token"))
                .isEqualTo(PermissionDataScope.ASSIGNED_RECORDS);
        assertThat(guard.require(LdPlanningPermissionGuard.IDP, PrimeHrAction.FINALIZE, "Bearer token"))
                .isEqualTo(PermissionDataScope.ASSIGNED_RECORDS);
        assertThatThrownBy(() -> guard.require(LdPlanningPermissionGuard.IDP, PrimeHrAction.ARCHIVE, "Bearer token"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private EffectiveFeaturePermission permission(boolean access, boolean add, boolean edit, boolean submit,
            boolean finalizeAction, boolean delete, PermissionDataScope scope) {
        return new EffectiveFeaturePermission("feature", false, access, add, edit, delete,
                false, submit, false, false, false, finalizeAction, scope);
    }
}
