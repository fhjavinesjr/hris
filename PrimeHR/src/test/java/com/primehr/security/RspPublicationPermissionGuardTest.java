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

class RspPublicationPermissionGuardTest {
    private final AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
    private final RspPublicationPermissionGuard guard = new RspPublicationPermissionGuard(client);

    @Test
    void publishIsIndependentFromApproveAndRequiresAgencyWideScope() {
        String token = "Bearer token";
        when(client.resolve(RspPublicationPermissionGuard.FEATURE, token)).thenReturn(permission(
                true, true, false, true, PermissionDataScope.AGENCY_WIDE));

        assertThatCode(() -> guard.require(PrimeHrAction.APPROVE, token)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(PrimeHrAction.PUBLISH, token))
                .isInstanceOf(AccessDeniedException.class);

        when(client.resolve(RspPublicationPermissionGuard.FEATURE, token)).thenReturn(permission(
                true, false, true, false, PermissionDataScope.OWN_RECORDS));
        assertThatThrownBy(() -> guard.require(PrimeHrAction.PUBLISH, token))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void administratorRetainsPublishPermissionWithoutADataScope() {
        String token = "Bearer token";
        when(client.resolve(RspPublicationPermissionGuard.FEATURE, token)).thenReturn(
                new EffectiveFeaturePermission(RspPublicationPermissionGuard.FEATURE, true,
                        false, false, false, false, false, false, false,
                        false, false, false, PermissionDataScope.NONE));
        assertThatCode(() -> guard.require(PrimeHrAction.PUBLISH, token)).doesNotThrowAnyException();
    }

    private static EffectiveFeaturePermission permission(boolean access, boolean approve,
                                                         boolean publish, boolean submit,
                                                         PermissionDataScope scope) {
        return new EffectiveFeaturePermission(RspPublicationPermissionGuard.FEATURE, false,
                access, false, false, false, publish, submit, approve,
                false, false, false, scope);
    }
}
