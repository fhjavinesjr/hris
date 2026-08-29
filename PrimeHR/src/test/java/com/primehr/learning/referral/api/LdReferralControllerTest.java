package com.primehr.learning.referral.api;

import com.primehr.learning.referral.api.LdReferralDtos.*;
import com.primehr.learning.referral.application.LdReferralService;
import com.primehr.security.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.List;
import static org.mockito.Mockito.*;

class LdReferralControllerTest {
    private final LdReferralService service=mock(LdReferralService.class);
    private final GapPermissionGuard permission=mock(GapPermissionGuard.class);
    private final AgencyScopeResolver agency=mock(AgencyScopeResolver.class);
    private final LdReferralController controller=new LdReferralController(service,permission,agency);
    private final UsernamePasswordAuthenticationToken auth=
            new UsernamePasswordAuthenticationToken("001",null,List.of());

    @Test void createUsesIndependentAddPermissionAndAgencyScope() {
        when(agency.resolveAgencyId(auth)).thenReturn("AGENCY");
        CreateRequest request=new CreateRequest("analysis-1","Need","Coaching",null,null,null);
        controller.create(auth,"Bearer token","corr",request);
        verify(permission).requireAgencyWide(GapPermissionGuard.REFERRAL,PrimeHrAction.ADD,"Bearer token");
        verify(service).create("AGENCY",request,"corr");
    }

    @Test void itemArchiveUsesDeleteArchivePermissionRatherThanEdit() {
        when(agency.resolveAgencyId(auth)).thenReturn("AGENCY");
        ItemTransitionRequest request=new ItemTransitionRequest(0L);
        controller.archiveItem(auth,"Bearer token","corr","referral-1","item-1",request);
        verify(permission).requireAgencyWide(GapPermissionGuard.REFERRAL,PrimeHrAction.ARCHIVE,"Bearer token");
        verify(permission,never()).requireAgencyWide(GapPermissionGuard.REFERRAL,PrimeHrAction.EDIT,"Bearer token");
        verify(service).archiveItem("AGENCY","referral-1","item-1",request,"corr");
    }

    @Test void submitUsesIndependentSubmitPermission() {
        when(agency.resolveAgencyId(auth)).thenReturn("AGENCY");
        TransitionRequest request=new TransitionRequest(0L,"Manual referral");
        controller.submit(auth,"Bearer token","corr","referral-1",request);
        verify(permission).requireAgencyWide(GapPermissionGuard.REFERRAL,PrimeHrAction.SUBMIT,"Bearer token");
        verify(service).submit("AGENCY","referral-1",request,"corr");
    }
}
