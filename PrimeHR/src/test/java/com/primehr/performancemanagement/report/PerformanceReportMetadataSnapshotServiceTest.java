package com.primehr.performancemanagement.report;

import com.primehr.performancemanagement.report.application.*;
import com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot;
import com.primehr.performancemanagement.report.infrastructure.PerformanceReportMetadataSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot.ReportKind.IPCR;
import static com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot.SourceType.FINAL_RATING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceReportMetadataSnapshotServiceTest {
    private static final String HASH_A = "a".repeat(64);
    private static final String HASH_B = "b".repeat(64);
    @Mock private PerformanceReportMetadataSnapshotRepository repository;

    @Test
    void capturesAuthoritativeMetadataOnce() {
        when(repository.findByAgencyIdAndReportKindAndSourceTypeAndSourceIdAndSourceBusinessRevision(
                "agency", IPCR, FINAL_RATING, "final-1", 1)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PerformanceReportMetadataSnapshot value = service().capture("agency", input("Employee One"), "admin");

        assertThat(value.getEmployeeName()).isEqualTo("Employee One");
        assertThat(value.getPositionName()).isEqualTo("Administrative Officer V");
        assertThat(value.getMetadataFingerprint()).hasSize(64);
        verify(repository).saveAndFlush(any(PerformanceReportMetadataSnapshot.class));
    }

    @Test
    void returnsAnIdenticalExistingSnapshotWithoutMutation() {
        PerformanceReportMetadataSnapshotServiceImpl service = service();
        PerformanceReportMetadataInput input = input("Employee One");
        when(repository.findByAgencyIdAndReportKindAndSourceTypeAndSourceIdAndSourceBusinessRevision(
                "agency", IPCR, FINAL_RATING, "final-1", 1)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        PerformanceReportMetadataSnapshot first = service.capture("agency", input, "admin");
        when(repository.findByAgencyIdAndReportKindAndSourceTypeAndSourceIdAndSourceBusinessRevision(
                "agency", IPCR, FINAL_RATING, "final-1", 1)).thenReturn(Optional.of(first));

        assertThat(service.capture("agency", input, "another-user")).isSameAs(first);
        verify(repository, times(1)).saveAndFlush(any());
    }

    @Test
    void rejectsDifferentValuesForTheSameImmutableSourceRevision() {
        PerformanceReportMetadataSnapshotServiceImpl service = service();
        PerformanceReportMetadataInput original = input("Employee One");
        when(repository.findByAgencyIdAndReportKindAndSourceTypeAndSourceIdAndSourceBusinessRevision(
                "agency", IPCR, FINAL_RATING, "final-1", 1)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        PerformanceReportMetadataSnapshot first = service.capture("agency", original, "admin");
        when(repository.findByAgencyIdAndReportKindAndSourceTypeAndSourceIdAndSourceBusinessRevision(
                "agency", IPCR, FINAL_RATING, "final-1", 1)).thenReturn(Optional.of(first));

        assertThatThrownBy(() -> service.capture("agency", input("Changed Name"), "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Immutable report metadata");
    }

    private PerformanceReportMetadataSnapshotServiceImpl service() {
        return new PerformanceReportMetadataSnapshotServiceImpl(repository);
    }

    private static PerformanceReportMetadataInput input(String employeeName) {
        return new PerformanceReportMetadataInput(IPCR, FINAL_RATING, "final-1", 1, 0, HASH_A,
                "CSC-SPMS-IPCR", 1, 12L, "EMP-00012", employeeName, "Administrative Officer V", "ITEM-12",
                1L, "Hospital Operations", 2L, "Human Resource Management", "EMP-00002", "Supervisor",
                "Division Chief", "EMP-00001", "Agency Head", "Medical Center Chief",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), "Quezon City General Hospital",
                "Quezon City", HASH_B);
    }
}
