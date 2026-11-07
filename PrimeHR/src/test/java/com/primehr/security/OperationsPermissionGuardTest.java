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

class OperationsPermissionGuardTest {
    @Test
    void exactActionsAndAgencyWideScopeAreRequired() {
        var client = mock(AdministrativeAuthorizationClient.class);
        var guard = new OperationsPermissionGuard(client);
        when(client.resolve(OperationsPermissionGuard.FEATURE, "operator"))
                .thenReturn(permission(true, true, true, false, PermissionDataScope.AGENCY_WIDE));
        assertThatCode(() -> guard.require(PrimeHrAction.ADD, "operator")).doesNotThrowAnyException();
        assertThatCode(() -> guard.require(PrimeHrAction.EDIT, "operator")).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ARCHIVE, "operator"))
                .isInstanceOf(AccessDeniedException.class);

        when(client.resolve(OperationsPermissionGuard.FEATURE, "assigned"))
                .thenReturn(permission(true, true, true, true, PermissionDataScope.ASSIGNED_RECORDS));
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ACCESS, "assigned"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private EffectiveFeaturePermission permission(boolean access, boolean add, boolean edit, boolean delete,
                                                   PermissionDataScope scope) {
        return new EffectiveFeaturePermission(OperationsPermissionGuard.FEATURE, false, access, add, edit,
                delete, false, false, false, false, false, false, scope);
    }
}
