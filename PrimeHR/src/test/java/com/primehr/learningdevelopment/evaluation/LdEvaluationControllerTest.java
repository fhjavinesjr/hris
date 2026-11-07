package com.primehr.learningdevelopment.evaluation;

import com.primehr.learningdevelopment.evaluation.api.LdEvaluationController;
import com.primehr.learningdevelopment.evaluation.api.LdEvaluationDtos.EffectivenessResponse;
import com.primehr.learningdevelopment.evaluation.application.LdEvaluationService;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.LdEvaluationPermissionGuard;
import com.primehr.security.LdReportingPermissionGuard;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LdEvaluationControllerTest {

    @Test
    void effectivenessRequiresTheIndependentAnalyticsPermission() {
        LdEvaluationService service = mock(LdEvaluationService.class);
        LdEvaluationPermissionGuard evaluation = mock(LdEvaluationPermissionGuard.class);
        LdReportingPermissionGuard reporting = mock(LdReportingPermissionGuard.class);
        AgencyScopeResolver agencies = mock(AgencyScopeResolver.class);
        Authentication authentication = mock(Authentication.class);
        EffectivenessResponse expected = new EffectivenessResponse(Instant.EPOCH, 5, 0, List.of());

        when(authentication.getName()).thenReturn("EMP-00012");
        when(agencies.resolveAgencyId(authentication)).thenReturn("AGENCY-1");
        when(service.effectiveness("AGENCY-1", "EMP-00012", "correlation-1")).thenReturn(expected);

        LdEvaluationController controller =
                new LdEvaluationController(service, evaluation, reporting, agencies);

        EffectivenessResponse actual = controller.effectiveness(
                authentication,
                "Bearer token",
                "correlation-1");

        assertSame(expected, actual);
        verify(reporting).requireAnalytics("Bearer token");
        verify(service).effectiveness("AGENCY-1", "EMP-00012", "correlation-1");
    }
}
