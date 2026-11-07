package com.primehr.rewardsrecognition;

import com.primehr.rewardsrecognition.api.RrFoundationController;
import com.primehr.rewardsrecognition.api.RrFoundationDtos.CommitteeRosterInput;
import com.primehr.rewardsrecognition.api.RrFoundationDtos.DefinitionTransition;
import com.primehr.rewardsrecognition.application.RrFoundationService;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.PrimeHrAction;
import com.primehr.security.RrFoundationPermissionGuard;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RrFoundationControllerTest {
    private final RrFoundationService service = mock(RrFoundationService.class);
    private final RrFoundationPermissionGuard permission = mock(RrFoundationPermissionGuard.class);
    private final AgencyScopeResolver agency = mock(AgencyScopeResolver.class);
    private final RrFoundationController controller = new RrFoundationController(service, permission, agency);
    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("EMP-00012", null, List.of());

    @Test
    void policyRetirementRequiresIndependentPublishPermissionAndAgencyScope() {
        when(agency.resolveAgencyId(authentication)).thenReturn("AGENCY-1");
        DefinitionTransition request = new DefinitionTransition(3L, "Superseded");

        controller.retirePolicy(authentication, "Bearer token", "corr-1", "policy-1", request);

        verify(permission).require(
                RrFoundationPermissionGuard.GOVERNANCE, PrimeHrAction.PUBLISH, "Bearer token");
        verify(service).retirePolicy("AGENCY-1", "policy-1", request, "corr-1");
    }

    @Test
    void committeeRosterReplacementUsesCommitteeEditPermission() {
        when(agency.resolveAgencyId(authentication)).thenReturn("AGENCY-1");
        CommitteeRosterInput request = new CommitteeRosterInput(2L, List.of());

        controller.replaceRoster(authentication, "Bearer token", "corr-2", "charter-1", request);

        verify(permission).require(
                RrFoundationPermissionGuard.COMMITTEE, PrimeHrAction.EDIT, "Bearer token");
        verify(service).replaceRoster("AGENCY-1", "charter-1", request, "corr-2");
    }
}
