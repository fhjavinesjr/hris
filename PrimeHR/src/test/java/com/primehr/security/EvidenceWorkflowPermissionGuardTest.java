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

class EvidenceWorkflowPermissionGuardTest {

    @Test
    void exactActionsAndAssignedOrAgencyScopeAreRequired() {
        AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
        EvidenceWorkflowPermissionGuard guard = new EvidenceWorkflowPermissionGuard(client);
        when(client.resolve(EvidenceWorkflowPermissionGuard.ASSESSMENT, "assigned"))
                .thenReturn(permission(true, true, true, false, true, false, false,
                        PermissionDataScope.ASSIGNED_RECORDS));
        when(client.resolve(EvidenceWorkflowPermissionGuard.VALIDATION, "agency"))
                .thenReturn(permission(true, false, true, false, false, true, true,
                        PermissionDataScope.AGENCY_WIDE));
        when(client.resolve(EvidenceWorkflowPermissionGuard.ACTION, "own"))
                .thenReturn(permission(true, true, true, false, true, false, false,
                        PermissionDataScope.OWN_RECORDS));

        assertThat(guard.require(EvidenceWorkflowPermissionGuard.ASSESSMENT, PrimeHrAction.SUBMIT, "assigned"))
                .isEqualTo(PermissionDataScope.ASSIGNED_RECORDS);
        assertThat(guard.require(EvidenceWorkflowPermissionGuard.VALIDATION, PrimeHrAction.FINALIZE, "agency"))
                .isEqualTo(PermissionDataScope.AGENCY_WIDE);
        assertThatThrownBy(() -> guard.require(EvidenceWorkflowPermissionGuard.ASSESSMENT,
                PrimeHrAction.ARCHIVE, "assigned")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require(EvidenceWorkflowPermissionGuard.ACTION,
                PrimeHrAction.ACCESS, "own")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.require("primehr.evidence-unknown", PrimeHrAction.ACCESS, "assigned"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private EffectiveFeaturePermission permission(boolean access, boolean add, boolean edit, boolean delete,
                                                  boolean submit, boolean approve, boolean finalize,
                                                  PermissionDataScope scope) {
        return new EffectiveFeaturePermission("feature", false, access, add, edit, delete,
                false, submit, approve, false, false, finalize, scope);
    }
}
