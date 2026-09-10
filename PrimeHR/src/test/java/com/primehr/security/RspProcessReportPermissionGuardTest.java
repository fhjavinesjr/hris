package com.primehr.security;

import com.primehr.integration.administrative.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RspProcessReportPermissionGuardTest {
    @Test void registerAndAnalyticsAreIndependentAgencyWidePermissions(){AdministrativeAuthorizationClient client=mock(AdministrativeAuthorizationClient.class);RspProcessReportPermissionGuard guard=new RspProcessReportPermissionGuard(client);
        when(client.resolve(eq(RspProcessReportPermissionGuard.REGISTER),anyString())).thenReturn(permission(RspProcessReportPermissionGuard.REGISTER,true,PermissionDataScope.AGENCY_WIDE));
        when(client.resolve(eq(RspProcessReportPermissionGuard.ANALYTICS),anyString())).thenReturn(permission(RspProcessReportPermissionGuard.ANALYTICS,true,PermissionDataScope.OWN_RECORDS));
        assertThatCode(()->guard.require(RspProcessReportPermissionGuard.REGISTER,"Bearer token")).doesNotThrowAnyException();assertThatThrownBy(()->guard.require(RspProcessReportPermissionGuard.ANALYTICS,"Bearer token")).isInstanceOf(AccessDeniedException.class);}
    private static EffectiveFeaturePermission permission(String feature,boolean access,PermissionDataScope scope){return new EffectiveFeaturePermission(feature,false,access,false,false,false,false,false,false,false,false,false,scope);}
}
