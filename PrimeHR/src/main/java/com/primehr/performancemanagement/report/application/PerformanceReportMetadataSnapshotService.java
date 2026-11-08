package com.primehr.performancemanagement.report.application;

import com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot;

public interface PerformanceReportMetadataSnapshotService {
    PerformanceReportMetadataSnapshot capture(String agencyId, PerformanceReportMetadataInput input, String actor);
}
