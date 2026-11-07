package com.primehr.performancemanagement.report;

import com.primehr.performancemanagement.report.api.PerformanceReportingController;
import com.primehr.performancemanagement.report.application.PerformanceReportingData.*;
import com.primehr.performancemanagement.report.application.PerformanceReportingService;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.PerformanceReportingPermissionGuard;
import com.primehr.shared.audit.PrimeHrAuditService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PerformanceReportingControllerTest {
    @Test
    void routesEachReadSurfaceThroughItsIndependentAgencyWidePermission() {
        PerformanceReportingService service = mock(PerformanceReportingService.class);
        PerformanceReportingPermissionGuard permission = mock(PerformanceReportingPermissionGuard.class);
        AgencyScopeResolver agencies = mock(AgencyScopeResolver.class);
        PrimeHrAuditService audit = mock(PrimeHrAuditService.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");
        when(agencies.resolveAgencyId(authentication)).thenReturn("A");
        when(service.register(eq("A"), any(), eq("admin"), eq("corr"))).thenReturn(
                new RegisterPage(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), false, 0, 25,
                        0, 0, List.of()));
        when(service.analytics(eq("A"), any(), eq("admin"), eq("corr"))).thenReturn(
                new Analytics(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 5, 0, true,
                        "suppressed", List.of(), List.of()));
        when(service.outcomes(eq("A"), any(), eq("admin"), eq("corr"))).thenReturn(
                new OutcomePage("1.0", Instant.EPOCH, Instant.parse("2026-01-01T00:00:00Z"), Instant.now(),
                        0, 100, 0, 0, List.of()));
        PerformanceReportingController controller = new PerformanceReportingController(service, permission,
                agencies, audit);

        controller.register(authentication, "Bearer token", "corr", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), false, 0, 25, null, null, null, null, null);
        controller.analytics(authentication, "Bearer token", "corr", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), false, null, null, null, null, null);
        controller.outcomes(authentication, "Bearer token", "corr", Instant.EPOCH,
                Instant.parse("2026-01-01T00:00:00Z"), 0, 100);

        verify(permission).require(PerformanceReportingPermissionGuard.REGISTER, "Bearer token");
        verify(permission).require(PerformanceReportingPermissionGuard.ANALYTICS, "Bearer token");
        verify(permission).require(PerformanceReportingPermissionGuard.OUTCOME_FEED, "Bearer token");
    }
}
