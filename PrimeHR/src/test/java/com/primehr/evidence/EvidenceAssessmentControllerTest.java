package com.primehr.evidence;

import com.primehr.evidence.api.EvidenceAssessmentController;
import com.primehr.evidence.application.EvidenceAssessmentService;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.EvidenceWorkflowPermissionGuard;
import com.primehr.security.PrimeHrAction;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EvidenceAssessmentControllerTest {
    private final EvidenceAssessmentService service = mock(EvidenceAssessmentService.class);
    private final EvidenceWorkflowPermissionGuard permission = mock(EvidenceWorkflowPermissionGuard.class);
    private final AgencyScopeResolver agencies = mock(AgencyScopeResolver.class);
    private final EvidenceAssessmentController controller =
            new EvidenceAssessmentController(service, permission, agencies);
    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("VALIDATOR-1", null, List.of());

    @Test
    void validationInboxUsesValidationAccessAndItsReturnedScope() {
        when(agencies.resolveAgencyId(authentication)).thenReturn("AGENCY-1");
        when(permission.require(EvidenceWorkflowPermissionGuard.VALIDATION,
                PrimeHrAction.ACCESS, "Bearer token")).thenReturn(PermissionDataScope.ASSIGNED_RECORDS);

        controller.validationCases(authentication, "Bearer token");

        verify(service).cases("AGENCY-1", "VALIDATOR-1", PermissionDataScope.ASSIGNED_RECORDS);
    }

    @Test
    void actionInboxUsesActionAccessAndItsReturnedScope() {
        when(agencies.resolveAgencyId(authentication)).thenReturn("AGENCY-1");
        when(permission.require(EvidenceWorkflowPermissionGuard.ACTION,
                PrimeHrAction.ACCESS, "Bearer token")).thenReturn(PermissionDataScope.OWN_RECORDS);

        controller.actionCases(authentication, "Bearer token");

        verify(service).cases("AGENCY-1", "VALIDATOR-1", PermissionDataScope.OWN_RECORDS);
    }
}
