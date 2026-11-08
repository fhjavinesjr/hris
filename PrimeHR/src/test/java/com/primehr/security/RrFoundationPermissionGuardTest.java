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

class RrFoundationPermissionGuardTest {
    @Test
    void actionAndAgencyScopeAreEnforcedIndependently() {
        AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
        RrFoundationPermissionGuard guard = new RrFoundationPermissionGuard(client);
        when(client.resolve(RrFoundationPermissionGuard.GOVERNANCE, "Bearer token"))
                .thenReturn(permission(true, true, false, true, PermissionDataScope.AGENCY_WIDE));
        when(client.resolve(RrFoundationPermissionGuard.COMMITTEE, "Bearer token"))
                .thenReturn(permission(true, true, true, true, PermissionDataScope.ASSIGNED_RECORDS));

        assertThatCode(() -> guard.require(RrFoundationPermissionGuard.GOVERNANCE,
                PrimeHrAction.PUBLISH, "Bearer token")).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(RrFoundationPermissionGuard.GOVERNANCE,
                PrimeHrAction.EDIT, "Bearer token")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(RrFoundationPermissionGuard.COMMITTEE,
                PrimeHrAction.EDIT, "Bearer token")).isInstanceOf(AccessDeniedException.class);
    }

    private EffectiveFeaturePermission permission(boolean access, boolean add, boolean edit, boolean publish,
                                                  PermissionDataScope scope) {
        return new EffectiveFeaturePermission("feature", false, access, add, edit, false, publish,
                false, false, false, false, false, scope);
    }
}
