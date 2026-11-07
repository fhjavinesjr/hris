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

class RrNominationPermissionGuardTest {
    @Test
    void cycleIsAgencyWideWhileNominationPreservesConfiguredScope() {
        AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
        RrNominationPermissionGuard guard = new RrNominationPermissionGuard(client);
        when(client.resolve(RrNominationPermissionGuard.CYCLE, "Bearer token"))
                .thenReturn(permission(true, true, true, true, true, PermissionDataScope.ASSIGNED_RECORDS));
        when(client.resolve(RrNominationPermissionGuard.NOMINATION, "Bearer token"))
                .thenReturn(permission(true, true, true, true, false, PermissionDataScope.OWN_RECORDS));

        assertThatThrownBy(() -> guard.require(RrNominationPermissionGuard.CYCLE,
                PrimeHrAction.ADD, "Bearer token")).isInstanceOf(AccessDeniedException.class);
        assertThat(guard.require(RrNominationPermissionGuard.NOMINATION,
                PrimeHrAction.SUBMIT, "Bearer token")).isEqualTo(PermissionDataScope.OWN_RECORDS);
        assertThatThrownBy(() -> guard.require(RrNominationPermissionGuard.NOMINATION,
                PrimeHrAction.FINALIZE, "Bearer token")).isInstanceOf(AccessDeniedException.class);
    }

    private EffectiveFeaturePermission permission(boolean access, boolean add, boolean edit,
                                                  boolean submit, boolean finalize,
                                                  PermissionDataScope scope) {
        return new EffectiveFeaturePermission("feature", false, access, add, edit, true, false,
                submit, false, false, false, finalize, scope);
    }
}
