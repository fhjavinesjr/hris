package com.primehr.security;

import com.primehr.integration.administrative.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LdDeliveryPermissionGuardTest {
    @Test
    void exactActionsAndAssignedOrAgencyScopesAreEnforced() {
        AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
        LdDeliveryPermissionGuard guard = new LdDeliveryPermissionGuard(client);
        when(client.resolve(LdDeliveryPermissionGuard.PLAN, "Bearer token"))
                .thenReturn(permission(true, true, false, true, true, false, false, PermissionDataScope.ASSIGNED_RECORDS));
        when(client.resolve(LdDeliveryPermissionGuard.PROGRAM, "Bearer token"))
                .thenReturn(permission(true, false, true, true, false, true, true, PermissionDataScope.AGENCY_WIDE));

        assertThat(guard.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.ADD, "Bearer token"))
                .isEqualTo(PermissionDataScope.ASSIGNED_RECORDS);
        assertThat(guard.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.SUBMIT, "Bearer token"))
                .isEqualTo(PermissionDataScope.ASSIGNED_RECORDS);
        assertThatThrownBy(() -> guard.require(LdDeliveryPermissionGuard.PLAN, PrimeHrAction.EDIT, "Bearer token"))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(guard.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.PUBLISH, "Bearer token"))
                .isEqualTo(PermissionDataScope.AGENCY_WIDE);
        assertThat(guard.require(LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.ARCHIVE, "Bearer token"))
                .isEqualTo(PermissionDataScope.AGENCY_WIDE);
    }

    @Test
    void ownScopeFailsClosedForConsolidatedRecords() {
        AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
        when(client.resolve(anyString(), anyString())).thenReturn(
                permission(true, true, true, true, true, true, true, PermissionDataScope.OWN_RECORDS));
        assertThatThrownBy(() -> new LdDeliveryPermissionGuard(client).require(
                LdDeliveryPermissionGuard.PROGRAM, PrimeHrAction.ACCESS, "token"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private EffectiveFeaturePermission permission(boolean access, boolean add, boolean edit, boolean submit,
            boolean approve, boolean publish, boolean delete, PermissionDataScope scope) {
        return new EffectiveFeaturePermission("feature", false, access, add, edit, delete,
                publish, submit, approve, false, false, false, scope);
    }
}
