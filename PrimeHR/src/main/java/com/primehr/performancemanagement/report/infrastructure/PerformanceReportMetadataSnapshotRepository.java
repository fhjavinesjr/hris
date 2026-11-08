package com.primehr.performancemanagement.report.infrastructure;

import com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerformanceReportMetadataSnapshotRepository extends JpaRepository<PerformanceReportMetadataSnapshot, String> {
    Optional<PerformanceReportMetadataSnapshot> findByAgencyIdAndReportKindAndSourceTypeAndSourceIdAndSourceBusinessRevision(
            String agencyId, PerformanceReportMetadataSnapshot.ReportKind reportKind,
            PerformanceReportMetadataSnapshot.SourceType sourceType, String sourceId, int sourceBusinessRevision);
}
