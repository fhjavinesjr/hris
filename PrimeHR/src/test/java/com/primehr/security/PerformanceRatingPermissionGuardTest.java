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

class PerformanceRatingPermissionGuardTest {
    private final AdministrativeAuthorizationClient client = mock(AdministrativeAuthorizationClient.class);
    private final PerformanceRatingPermissionGuard guard = new PerformanceRatingPermissionGuard(client);

    @Test void actionsRemainIndependentAndPreserveScope(){
        when(client.resolve(PerformanceRatingPermissionGuard.FEATURE,"token")).thenReturn(permission(true,true,true,true,false,PermissionDataScope.OWN_RECORDS));
        assertThat(guard.require(PrimeHrAction.ACCESS,"token")).isEqualTo(PermissionDataScope.OWN_RECORDS);
        assertThat(guard.require(PrimeHrAction.ADD,"token")).isEqualTo(PermissionDataScope.OWN_RECORDS);
        assertThat(guard.require(PrimeHrAction.EDIT,"token")).isEqualTo(PermissionDataScope.OWN_RECORDS);
        assertThat(guard.require(PrimeHrAction.SUBMIT,"token")).isEqualTo(PermissionDataScope.OWN_RECORDS);
        assertThatThrownBy(()->guard.require(PrimeHrAction.APPROVE,"token")).isInstanceOf(AccessDeniedException.class);
    }

    @Test void overrideApprovalDoesNotGrantDraftMutationAndInvalidScopeIsRejected(){
        when(client.resolve(PerformanceRatingPermissionGuard.FEATURE,"token")).thenReturn(permission(true,false,false,false,true,PermissionDataScope.ASSIGNED_RECORDS));
        assertThat(guard.require(PrimeHrAction.APPROVE,"token")).isEqualTo(PermissionDataScope.ASSIGNED_RECORDS);
        assertThatThrownBy(()->guard.require(PrimeHrAction.ADD,"token")).isInstanceOf(AccessDeniedException.class);
        when(client.resolve(PerformanceRatingPermissionGuard.FEATURE,"token")).thenReturn(permission(true,true,true,true,true,PermissionDataScope.NONE));
        assertThatThrownBy(()->guard.require(PrimeHrAction.ACCESS,"token")).isInstanceOf(AccessDeniedException.class);
    }

    @Test void administratorReceivesAgencyWideScope(){
        when(client.resolve(PerformanceRatingPermissionGuard.FEATURE,"admin")).thenReturn(new EffectiveFeaturePermission(PerformanceRatingPermissionGuard.FEATURE,true,false,false,false,false,false,false,false,false,false,false,PermissionDataScope.NONE));
        assertThat(guard.require(PrimeHrAction.APPROVE,"admin")).isEqualTo(PermissionDataScope.AGENCY_WIDE);
    }

    private static EffectiveFeaturePermission permission(boolean access,boolean add,boolean edit,boolean submit,boolean approve,PermissionDataScope scope){
        return new EffectiveFeaturePermission(PerformanceRatingPermissionGuard.FEATURE,false,access,add,edit,false,false,submit,approve,false,false,false,scope);
    }
}
