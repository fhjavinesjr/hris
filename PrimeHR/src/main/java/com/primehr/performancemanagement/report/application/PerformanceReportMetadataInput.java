package com.primehr.performancemanagement.report.application;

import com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot.ReportKind;
import com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot.SourceType;

import java.time.LocalDate;

public record PerformanceReportMetadataInput(
        ReportKind reportKind, SourceType sourceType, String sourceId, int sourceBusinessRevision,
        long sourceRecordVersion, String sourceFingerprint, String templateCode, int templateVersion,
        Long employeeId, String employeeNo, String employeeName, String positionName, String plantillaName,
        Long areaId, String areaName, Long businessUnitId, String businessUnitName,
        String supervisorEmployeeNo, String supervisorName, String supervisorPositionName,
        String approverEmployeeNo, String approverName, String approverPositionName,
        LocalDate periodStart, LocalDate periodEnd, String agencyName, String agencyAddress,
        String brandingFingerprint) { }
