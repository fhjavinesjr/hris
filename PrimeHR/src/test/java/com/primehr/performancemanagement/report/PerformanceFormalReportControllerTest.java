package com.primehr.performancemanagement.report;

import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.performancemanagement.report.api.PerformanceFormalReportController;
import com.primehr.performancemanagement.report.application.PerformanceFormalReportService;
import com.primehr.security.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PerformanceFormalReportControllerTest {
    @Test void allFormalReportEndpointsReturnNoStoreInlinePdf() {
        PerformanceFormalReportService service = mock(PerformanceFormalReportService.class);
        AgencyScopeResolver agencies = mock(AgencyScopeResolver.class);
        PerformanceCommitmentPermissionGuard commitments = mock(PerformanceCommitmentPermissionGuard.class);
        PerformanceRatingPermissionGuard ratings = mock(PerformanceRatingPermissionGuard.class);
        PerformanceMonitoringPermissionGuard monitoring = mock(PerformanceMonitoringPermissionGuard.class);
        PerformanceCoachingPermissionGuard coaching = mock(PerformanceCoachingPermissionGuard.class);
        PerformanceInterventionPermissionGuard interventions = mock(PerformanceInterventionPermissionGuard.class);
        PerformanceReportingPermissionGuard reports = mock(PerformanceReportingPermissionGuard.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("E-1"); when(agencies.resolveAgencyId(auth)).thenReturn("A");
        when(service.formTypeForCommitment("A", "c")).thenReturn("IPCR");
        when(commitments.require("IPCR", PrimeHrAction.ACCESS, "Bearer token")).thenReturn(PermissionDataScope.OWN_RECORDS);
        when(ratings.require(PrimeHrAction.ACCESS, "Bearer token")).thenReturn(PermissionDataScope.OWN_RECORDS);
        when(monitoring.require(PrimeHrAction.ACCESS, "Bearer token")).thenReturn(PermissionDataScope.OWN_RECORDS);
        when(coaching.require(PrimeHrAction.ACCESS, "Bearer token")).thenReturn(PermissionDataScope.OWN_RECORDS);
        when(interventions.require(PrimeHrAction.ACCESS, "Bearer token")).thenReturn(PermissionDataScope.OWN_RECORDS);
        when(reports.requireScoped(anyString(), eq("Bearer token"))).thenReturn(PermissionDataScope.OWN_RECORDS);
        byte[] pdf = "%PDF-test".getBytes();
        when(service.planningForm("A", "c", "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS, "x")).thenReturn(pdf);
        when(service.ratedForm("A", "r", "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS, "x")).thenReturn(pdf);
        when(service.monitoring("A", "m", "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS, "x")).thenReturn(pdf);
        when(service.coaching("A", "s", "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS, "x")).thenReturn(pdf);
        when(service.intervention("A", "i", false, "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS, "x")).thenReturn(pdf);
        when(service.intervention("A", "i", true, "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS, "x")).thenReturn(pdf);
        PerformanceFormalReportController controller = new PerformanceFormalReportController(service, agencies, commitments,
                ratings, monitoring, coaching, interventions, reports);
        assertPdf(controller.planning(auth, "Bearer token", "x", "c"));
        assertPdf(controller.rated(auth, "Bearer token", "x", "r"));
        assertPdf(controller.monitoring(auth, "Bearer token", "x", "m"));
        assertPdf(controller.coaching(auth, "Bearer token", "x", "s"));
        assertPdf(controller.intervention(auth, "Bearer token", "x", "i"));
        assertPdf(controller.development(auth, "Bearer token", "x", "i"));
        verify(reports, times(2)).requireScoped(PerformanceReportingPermissionGuard.FORM, "Bearer token");
        verify(reports, times(4)).requireScoped(PerformanceReportingPermissionGuard.MONITORING, "Bearer token");
    }

    private static void assertPdf(ResponseEntity<byte[]> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).startsWith("inline");
        assertThat(response.getBody()).startsWith("%PDF".getBytes());
    }
}
