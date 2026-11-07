package com.primehr.rewardsrecognition;

import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.rewardsrecognition.api.RrNominationController;
import com.primehr.rewardsrecognition.api.RrNominationDtos.Transition;
import com.primehr.rewardsrecognition.application.RrNominationService;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.PrimeHrAction;
import com.primehr.security.RrNominationPermissionGuard;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RrNominationControllerTest {
    private final RrNominationService service = mock(RrNominationService.class);
    private final RrNominationPermissionGuard permission = mock(RrNominationPermissionGuard.class);
    private final AgencyScopeResolver agency = mock(AgencyScopeResolver.class);
    private final RrNominationController controller = new RrNominationController(service, permission, agency);
    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("EMP-1", null, List.of());

    @Test
    void nominationSubmissionUsesAuthenticatedActorAndSubmitPermissionScope() {
        when(agency.resolveAgencyId(authentication)).thenReturn("AGENCY-1");
        when(permission.require(RrNominationPermissionGuard.NOMINATION, PrimeHrAction.SUBMIT, "Bearer token"))
                .thenReturn(PermissionDataScope.OWN_RECORDS);
        Transition request = new Transition(3L, "Ready for PRAISE review");

        controller.submit(authentication, "Bearer token", "corr-1", "nom-1", request);

        verify(service).submit("AGENCY-1", "nom-1", request, "EMP-1",
                PermissionDataScope.OWN_RECORDS, "corr-1");
    }

    @Test
    void cycleOpeningUsesCycleSubmitPermission() {
        when(agency.resolveAgencyId(authentication)).thenReturn("AGENCY-1");
        Transition request = new Transition(1L, "Approved call for nominations");

        controller.transitionCycle(authentication, "Bearer token", "corr-2", "cycle-1", "open", request);

        verify(permission).require(RrNominationPermissionGuard.CYCLE, PrimeHrAction.SUBMIT, "Bearer token");
        verify(service).transitionCycle("AGENCY-1", "cycle-1", request, "open", "corr-2");
    }
}
