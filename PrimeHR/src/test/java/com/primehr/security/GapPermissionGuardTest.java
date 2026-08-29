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

class GapPermissionGuardTest {
    private final AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
    private final GapPermissionGuard guard = new GapPermissionGuard(client);

    @Test
    void configurationActionsRemainIndependentAndAgencyWide() {
        String token = "Bearer token";
        when(client.resolve(GapPermissionGuard.CONFIGURATION, token)).thenReturn(permission(
                GapPermissionGuard.CONFIGURATION, true, true, false, false, true,
                false, PermissionDataScope.AGENCY_WIDE));

        assertThatCode(() -> guard.requireAgencyWide(GapPermissionGuard.CONFIGURATION,
                PrimeHrAction.ADD, token)).doesNotThrowAnyException();
        assertThatCode(() -> guard.requireAgencyWide(GapPermissionGuard.CONFIGURATION,
                PrimeHrAction.PUBLISH, token)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.requireAgencyWide(GapPermissionGuard.CONFIGURATION,
                PrimeHrAction.EDIT, token)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void gapReadMayBeOwnRecordButGenerationRequiresAgencyWide() {
        String token = "Bearer token";
        when(client.resolve(GapPermissionGuard.GAP, token)).thenReturn(permission(
                GapPermissionGuard.GAP, true, true, false, false, false,
                false, PermissionDataScope.OWN_RECORDS));

        assertThatCode(() -> guard.require(GapPermissionGuard.GAP, PrimeHrAction.ACCESS, token))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.requireAgencyWide(GapPermissionGuard.GAP, PrimeHrAction.ADD, token))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void missingAccessMalformedBearerAndUnknownFeatureFailClosed() {
        String token = "Bearer token";
        when(client.resolve(GapPermissionGuard.GAP, token)).thenReturn(permission(
                GapPermissionGuard.GAP, false, true, true, true, true,
                true, PermissionDataScope.AGENCY_WIDE));
        assertThatThrownBy(() -> guard.require(GapPermissionGuard.GAP, PrimeHrAction.ADD, token))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(GapPermissionGuard.GAP, PrimeHrAction.ACCESS, "token"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require("primehr.unknown", PrimeHrAction.ACCESS, token))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void administratorRetainsAllActionsAndAgencyWideCompatibility() {
        String token = "Bearer token";
        when(client.resolve(GapPermissionGuard.GAP, token)).thenReturn(new EffectiveFeaturePermission(
                GapPermissionGuard.GAP, true, false, false, false, false, false,
                false, false, false, false, false, PermissionDataScope.NONE));
        assertThatCode(() -> guard.requireAgencyWide(GapPermissionGuard.GAP, PrimeHrAction.ADD, token))
                .doesNotThrowAnyException();
    }

    @Test
    void referralSubmitAndArchiveRequireIndependentAgencyWideActions() {
        String token = "Bearer token";
        when(client.resolve(GapPermissionGuard.REFERRAL, token)).thenReturn(permission(
                GapPermissionGuard.REFERRAL, true, true, true, false, false,
                true, PermissionDataScope.AGENCY_WIDE));
        assertThatCode(() -> guard.requireAgencyWide(GapPermissionGuard.REFERRAL,
                PrimeHrAction.SUBMIT, token)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.requireAgencyWide(GapPermissionGuard.REFERRAL,
                PrimeHrAction.ARCHIVE, token)).isInstanceOf(AccessDeniedException.class);
    }

    private static EffectiveFeaturePermission permission(String key, boolean access, boolean add,
            boolean edit, boolean delete, boolean publish, boolean submit, PermissionDataScope scope) {
        return new EffectiveFeaturePermission(key, false, access, add, edit, delete, publish,
                submit, false, false, false, false, scope);
    }
}
