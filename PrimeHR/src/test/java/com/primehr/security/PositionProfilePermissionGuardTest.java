package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PositionProfilePermissionGuardTest {
    private final AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
    private final PositionProfilePermissionGuard guard = new PositionProfilePermissionGuard(client);

    @Test
    void accessIsRequiredByEveryDraftAction() {
        String token = "Bearer signed-token";
        when(client.resolve(PositionProfilePermissionGuard.FEATURE_KEY, token)).thenReturn(
                new EffectiveFeaturePermission(PositionProfilePermissionGuard.FEATURE_KEY,
                        false, false, true, true, true, false, true, true));

        assertThatThrownBy(() -> guard.require(PrimeHrAction.ADD, token))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(PrimeHrAction.EDIT, token))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ARCHIVE, token))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(PrimeHrAction.SUBMIT, token))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(PrimeHrAction.APPROVE, token))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void draftCrudAndLifecycleFlagsAreIndependent() {
        String token = "Bearer signed-token";
        when(client.resolve(PositionProfilePermissionGuard.FEATURE_KEY, token)).thenReturn(
                new EffectiveFeaturePermission(PositionProfilePermissionGuard.FEATURE_KEY,
                        false, true, true, false, false, true, true, false));

        assertThatCode(() -> guard.require(PrimeHrAction.ACCESS, token)).doesNotThrowAnyException();
        assertThatCode(() -> guard.require(PrimeHrAction.ADD, token)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(PrimeHrAction.EDIT, token))
                .isInstanceOf(AccessDeniedException.class);
        assertThatCode(() -> guard.require(PrimeHrAction.SUBMIT, token)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(PrimeHrAction.APPROVE, token))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(PrimeHrAction.PUBLISH, token))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void administratorRetainsAllProfileActionsButCannotUseCompetencyPublishAction() {
        String token = "Bearer signed-token";
        when(client.resolve(PositionProfilePermissionGuard.FEATURE_KEY, token)).thenReturn(
                new EffectiveFeaturePermission(PositionProfilePermissionGuard.FEATURE_KEY,
                        true, false, false, false, false, false, false, false));

        assertThatCode(() -> guard.require(PrimeHrAction.ARCHIVE, token)).doesNotThrowAnyException();
        assertThatCode(() -> guard.require(PrimeHrAction.SUBMIT, token)).doesNotThrowAnyException();
        assertThatCode(() -> guard.require(PrimeHrAction.APPROVE, token)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(PrimeHrAction.PUBLISH, token))
                .isInstanceOf(AccessDeniedException.class);
    }
}
