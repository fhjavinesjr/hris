package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.AuthorizationDependencyException;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrimeHrPermissionGuardTest {
    private final AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
    private final PrimeHrPermissionGuard guard = new PrimeHrPermissionGuard(client);

    @Test
    void exactPersistedActionFlagIsRequired() {
        String token = "Bearer signed-token";
        when(client.resolve(token)).thenReturn(new EffectiveFeaturePermission(
                "primehr.competency", false, true, false, true, false, false));

        assertThatCode(() -> guard.require(PrimeHrAction.ACCESS, token)).doesNotThrowAnyException();
        assertThatCode(() -> guard.require(PrimeHrAction.EDIT, token)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ADD, token))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ARCHIVE, token))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(PrimeHrAction.PUBLISH, token))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void administratorBypassAndDependencyFailureAreHandledSafely() {
        String token = "Bearer signed-token";
        when(client.resolve(token)).thenReturn(new EffectiveFeaturePermission(
                "primehr.competency", true, false, false, false, false, false));
        assertThatCode(() -> guard.require(PrimeHrAction.ARCHIVE, token)).doesNotThrowAnyException();
        assertThatCode(() -> guard.require(PrimeHrAction.PUBLISH, token)).doesNotThrowAnyException();

        when(client.resolve(token)).thenThrow(new AuthorizationDependencyException("offline", null));
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ACCESS, token))
                .isInstanceOf(AuthorizationDependencyException.class);
        assertThatThrownBy(() -> guard.require(PrimeHrAction.ACCESS, "not-bearer"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void publishIsIndependentFromCrudPermissions() {
        String token = "Bearer publisher-token";
        when(client.resolve(token)).thenReturn(new EffectiveFeaturePermission(
                "primehr.competency", false, true, false, false, false, true));

        assertThatCode(() -> guard.require(PrimeHrAction.PUBLISH, token)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(PrimeHrAction.EDIT, token))
                .isInstanceOf(AccessDeniedException.class);
    }
}
