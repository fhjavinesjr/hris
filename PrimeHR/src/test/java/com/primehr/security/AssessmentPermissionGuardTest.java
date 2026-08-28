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

class AssessmentPermissionGuardTest {
    private final AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
    private final AssessmentPermissionGuard guard = new AssessmentPermissionGuard(client);

    @Test
    void assessmentActionsAreIndependent() {
        String token = "Bearer token";
        when(client.resolve(AssessmentPermissionGuard.ASSESSMENT, token)).thenReturn(new EffectiveFeaturePermission(
                AssessmentPermissionGuard.ASSESSMENT, false, true, false, false, false,
                false, true, false, true, false, false, PermissionDataScope.ASSIGNED_RECORDS));

        assertThatCode(() -> guard.require(AssessmentPermissionGuard.ASSESSMENT, PrimeHrAction.ASSESS, token))
                .doesNotThrowAnyException();
        assertThatCode(() -> guard.require(AssessmentPermissionGuard.ASSESSMENT, PrimeHrAction.SUBMIT, token))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.require(AssessmentPermissionGuard.ASSESSMENT,
                PrimeHrAction.VALIDATE, token)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void administrationRequiresAgencyWideScope() {
        String token = "Bearer token";
        when(client.resolve(AssessmentPermissionGuard.ADMINISTRATION, token)).thenReturn(
                new EffectiveFeaturePermission(AssessmentPermissionGuard.ADMINISTRATION, false, true,
                        true, true, true, true, false, false, false, false, true,
                        PermissionDataScope.ASSIGNED_RECORDS));

        assertThatThrownBy(() -> guard.requireAdministration(PrimeHrAction.ADD, token))
                .isInstanceOf(AccessDeniedException.class);

        when(client.resolve(AssessmentPermissionGuard.ADMINISTRATION, token)).thenReturn(
                new EffectiveFeaturePermission(AssessmentPermissionGuard.ADMINISTRATION, false, true,
                        true, true, true, true, false, false, false, false, true,
                        PermissionDataScope.AGENCY_WIDE));
        assertThatCode(() -> guard.requireAdministration(PrimeHrAction.ADD, token)).doesNotThrowAnyException();
    }

    @Test
    void returnValidationRequiresValidatePermissionAndAgencyWideScope() {
        String token = "Bearer token";
        when(client.resolve(AssessmentPermissionGuard.VALIDATION, token)).thenReturn(
                new EffectiveFeaturePermission(AssessmentPermissionGuard.VALIDATION, false, true,
                        false, false, false, false, false, false, false, true, false,
                        PermissionDataScope.ASSIGNED_RECORDS));
        assertThatThrownBy(() -> guard.requireAgencyWide(AssessmentPermissionGuard.VALIDATION,
                PrimeHrAction.VALIDATE, token)).isInstanceOf(AccessDeniedException.class);

        when(client.resolve(AssessmentPermissionGuard.VALIDATION, token)).thenReturn(
                new EffectiveFeaturePermission(AssessmentPermissionGuard.VALIDATION, false, true,
                        false, false, false, false, false, false, false, true, false,
                        PermissionDataScope.AGENCY_WIDE));
        assertThatCode(() -> guard.requireAgencyWide(AssessmentPermissionGuard.VALIDATION,
                PrimeHrAction.VALIDATE, token)).doesNotThrowAnyException();
    }

    @Test
    void missingAccessAndMalformedBearerFailClosed() {
        String token = "Bearer token";
        when(client.resolve(AssessmentPermissionGuard.VALIDATION, token)).thenReturn(
                new EffectiveFeaturePermission(AssessmentPermissionGuard.VALIDATION, false, false,
                        false, false, false, false, false, false, false, true, false,
                        PermissionDataScope.AGENCY_WIDE));

        assertThatThrownBy(() -> guard.require(AssessmentPermissionGuard.VALIDATION,
                PrimeHrAction.VALIDATE, token)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(AssessmentPermissionGuard.ASSESSMENT,
                PrimeHrAction.ACCESS, "token")).isInstanceOf(AccessDeniedException.class);
    }
}
