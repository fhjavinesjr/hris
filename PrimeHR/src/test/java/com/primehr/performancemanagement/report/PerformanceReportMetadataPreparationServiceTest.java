package com.primehr.performancemanagement.report;

import com.primehr.integration.administrative.AdministrativePerformanceReportSourceClient;
import com.primehr.performancemanagement.domain.*;
import com.primehr.performancemanagement.infrastructure.*;
import com.primehr.performancemanagement.report.application.*;
import com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot;
import com.primehr.performancemanagement.report.infrastructure.PerformanceReportMetadataSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.*;

import static com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot.ReportKind.IPCR;
import static com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot.SourceType.COMMITMENT_VERSION;
import static com.primehr.integration.administrative.PermissionDataScope.OWN_RECORDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PerformanceReportMetadataPreparationServiceTest {
    @Test void reusesExactImmutableSnapshotWithoutCallingAdministrative() {
        PerformanceReportMetadataSnapshotRepository snapshots = mock(PerformanceReportMetadataSnapshotRepository.class);
        PerformanceReportMetadataSnapshotService capture = mock(PerformanceReportMetadataSnapshotService.class);
        AdministrativePerformanceReportSourceClient administrative = mock(AdministrativePerformanceReportSourceClient.class);
        PerformanceReportMetadataSnapshot existing = mock(PerformanceReportMetadataSnapshot.class);
        when(existing.getEmployeeNo()).thenReturn("E-1");
        PerformanceCommitmentVersion version = version();
        when(snapshots.findByAgencyIdAndReportKindAndSourceTypeAndSourceIdAndSourceBusinessRevision(
                "A", IPCR, COMMITMENT_VERSION, "commitment-1", 2)).thenReturn(Optional.of(existing));
        PerformanceReportMetadataPreparationService service = new PerformanceReportMetadataPreparationService(snapshots,
                capture, mock(PerformanceCommitmentRouteRepository.class), mock(PerformanceCommitmentRouteStepRepository.class), administrative);

        assertThat(service.commitment("A", IPCR, version, "Bearer token", "E-1", OWN_RECORDS)).isSameAs(existing);
        verifyNoInteractions(administrative, capture);
    }

    @Test void capturesCurrentAuthoritativeBrandingPositionsAndApprovalRoute() {
        PerformanceReportMetadataSnapshotRepository snapshots = mock(PerformanceReportMetadataSnapshotRepository.class);
        PerformanceReportMetadataSnapshotService capture = mock(PerformanceReportMetadataSnapshotService.class);
        PerformanceCommitmentRouteRepository routes = mock(PerformanceCommitmentRouteRepository.class);
        PerformanceCommitmentRouteStepRepository steps = mock(PerformanceCommitmentRouteStepRepository.class);
        AdministrativePerformanceReportSourceClient administrative = mock(AdministrativePerformanceReportSourceClient.class);
        PerformanceCommitmentVersion version = version();
        PerformanceCommitmentRoute route = mock(PerformanceCommitmentRoute.class); when(route.getId()).thenReturn("route-1");
        PerformanceCommitmentRouteStep supervisor = step("SUP-1", "Supervisor One", 21L);
        PerformanceCommitmentRouteStep approver = step("APP-1", "Approver One", 31L);
        when(routes.findFirstByAgencyIdAndCommitmentVersionIdOrderByRouteRevisionDesc("A", "commitment-1"))
                .thenReturn(Optional.of(route));
        when(steps.findByAgencyIdAndRouteIdOrderByLevelAsc("A", "route-1")).thenReturn(List.of(supervisor, approver));
        when(administrative.branding("Bearer token")).thenReturn(new AdministrativePerformanceReportSourceClient.Branding(
                "ISOFT Test Agency", "Main Road", "b".repeat(64)));
        when(administrative.position(11L, "Bearer token")).thenReturn(position(11L, "Owner Plantilla", "Owner Position"));
        when(administrative.position(21L, "Bearer token")).thenReturn(position(21L, "Supervisor Plantilla", "Supervisor Position"));
        when(administrative.position(31L, "Bearer token")).thenReturn(position(31L, "Approver Plantilla", "Approver Position"));
        PerformanceReportMetadataSnapshot saved = mock(PerformanceReportMetadataSnapshot.class);
        when(capture.capture(eq("A"), any(), eq("E-1"))).thenReturn(saved);
        PerformanceReportMetadataPreparationService service = new PerformanceReportMetadataPreparationService(
                snapshots, capture, routes, steps, administrative);

        assertThat(service.commitment("A", IPCR, version, "Bearer token", "E-1", OWN_RECORDS)).isSameAs(saved);
        ArgumentCaptor<PerformanceReportMetadataInput> input = ArgumentCaptor.forClass(PerformanceReportMetadataInput.class);
        verify(capture).capture(eq("A"), input.capture(), eq("E-1"));
        assertThat(input.getValue().employeeNo()).isEqualTo("E-1");
        assertThat(input.getValue().positionName()).isEqualTo("Owner Position");
        assertThat(input.getValue().supervisorPositionName()).isEqualTo("Supervisor Position");
        assertThat(input.getValue().approverPositionName()).isEqualTo("Approver Position");
        assertThat(input.getValue().agencyName()).isEqualTo("ISOFT Test Agency");
    }

    @Test void deniesWrongScopeBeforeAdministrativeLookupOrSnapshotCapture() {
        PerformanceReportMetadataSnapshotRepository snapshots = mock(PerformanceReportMetadataSnapshotRepository.class);
        PerformanceReportMetadataSnapshotService capture = mock(PerformanceReportMetadataSnapshotService.class);
        PerformanceCommitmentRouteRepository routes = mock(PerformanceCommitmentRouteRepository.class);
        PerformanceCommitmentRouteStepRepository steps = mock(PerformanceCommitmentRouteStepRepository.class);
        AdministrativePerformanceReportSourceClient administrative = mock(AdministrativePerformanceReportSourceClient.class);
        PerformanceCommitmentRoute route = mock(PerformanceCommitmentRoute.class);
        when(route.getId()).thenReturn("route-1");
        when(routes.findFirstByAgencyIdAndCommitmentVersionIdOrderByRouteRevisionDesc("A", "commitment-1"))
                .thenReturn(Optional.of(route));
        PerformanceCommitmentRouteStep supervisor = step("SUP-1", "Supervisor One", 21L);
        when(steps.findByAgencyIdAndRouteIdOrderByLevelAsc("A", "route-1"))
                .thenReturn(List.of(supervisor));
        PerformanceReportMetadataPreparationService service = new PerformanceReportMetadataPreparationService(
                snapshots, capture, routes, steps, administrative);

        assertThatThrownBy(() -> service.commitment("A", IPCR, version(), "Bearer token", "OTHER", OWN_RECORDS))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessage("Report is outside the permitted record scope");
        verifyNoInteractions(administrative, capture);
    }

    private static PerformanceCommitmentVersion version() {
        PerformanceCommitmentVersion value = mock(PerformanceCommitmentVersion.class);
        when(value.getId()).thenReturn("commitment-1"); when(value.getDefinitionVersion()).thenReturn(2);
        when(value.getVersion()).thenReturn(4L); when(value.getContentFingerprint()).thenReturn("a".repeat(64));
        when(value.getOwnerEmployeeId()).thenReturn(1L); when(value.getOwnerEmployeeNo()).thenReturn("E-1");
        when(value.getOwnerName()).thenReturn("Employee One"); when(value.getOwnerPlantillaId()).thenReturn(11L);
        when(value.getAreaId()).thenReturn(2L); when(value.getAreaName()).thenReturn("Operations");
        when(value.getBusinessUnitId()).thenReturn(3L); when(value.getBusinessUnitName()).thenReturn("HRM");
        when(value.getPeriodStart()).thenReturn(LocalDate.of(2026, 1, 1));
        when(value.getPeriodEnd()).thenReturn(LocalDate.of(2026, 12, 31)); return value;
    }

    private static PerformanceCommitmentRouteStep step(String employeeNo, String name, Long plantillaId) {
        PerformanceCommitmentRouteStep value = mock(PerformanceCommitmentRouteStep.class);
        when(value.getEmployeeNo()).thenReturn(employeeNo); when(value.getEmployeeName()).thenReturn(name);
        when(value.getPlantillaId()).thenReturn(plantillaId); return value;
    }

    private static AdministrativePerformanceReportSourceClient.Position position(Long id, String plantilla, String job) {
        return new AdministrativePerformanceReportSourceClient.Position(id, plantilla, id + 100, job, "c".repeat(64));
    }
}
