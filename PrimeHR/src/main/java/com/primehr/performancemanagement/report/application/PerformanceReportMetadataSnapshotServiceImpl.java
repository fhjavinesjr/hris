package com.primehr.performancemanagement.report.application;

import com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot;
import com.primehr.performancemanagement.report.infrastructure.PerformanceReportMetadataSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
@Transactional
public class PerformanceReportMetadataSnapshotServiceImpl implements PerformanceReportMetadataSnapshotService {
    private final PerformanceReportMetadataSnapshotRepository repository;

    public PerformanceReportMetadataSnapshotServiceImpl(PerformanceReportMetadataSnapshotRepository repository) {
        this.repository = repository;
    }

    @Override
    public PerformanceReportMetadataSnapshot capture(String agencyId, PerformanceReportMetadataInput input, String actor) {
        if (input == null) throw new IllegalArgumentException("report metadata is required");
        String metadataFingerprint = hash(input);
        var existing = repository.findByAgencyIdAndReportKindAndSourceTypeAndSourceIdAndSourceBusinessRevision(
                agencyId, input.reportKind(), input.sourceType(), input.sourceId(), input.sourceBusinessRevision());
        if (existing.isPresent()) {
            if (!existing.get().getMetadataFingerprint().equals(metadataFingerprint)) {
                throw new IllegalStateException("Immutable report metadata already exists with different authoritative values");
            }
            return existing.get();
        }
        return repository.saveAndFlush(new PerformanceReportMetadataSnapshot(agencyId, input.reportKind(),
                input.sourceType(), input.sourceId(), input.sourceBusinessRevision(), input.sourceRecordVersion(),
                input.sourceFingerprint(), input.templateCode(), input.templateVersion(), input.employeeId(),
                input.employeeNo(), input.employeeName(), input.positionName(), input.plantillaName(), input.areaId(),
                input.areaName(), input.businessUnitId(), input.businessUnitName(), input.supervisorEmployeeNo(),
                input.supervisorName(), input.supervisorPositionName(), input.approverEmployeeNo(), input.approverName(),
                input.approverPositionName(), input.periodStart(), input.periodEnd(), input.agencyName(),
                input.agencyAddress(), input.brandingFingerprint(), metadataFingerprint, actor, Instant.now()));
    }

    private static String hash(PerformanceReportMetadataInput value) {
        List<Object> fields = java.util.Arrays.asList(value.reportKind(), value.sourceType(), value.sourceId(),
                value.sourceBusinessRevision(), value.sourceRecordVersion(), value.sourceFingerprint(),
                value.templateCode(), value.templateVersion(), value.employeeId(), value.employeeNo(),
                value.employeeName(), value.positionName(), nullable(value.plantillaName()), nullable(value.areaId()),
                nullable(value.areaName()), value.businessUnitId(), value.businessUnitName(),
                nullable(value.supervisorEmployeeNo()), nullable(value.supervisorName()), nullable(value.supervisorPositionName()),
                nullable(value.approverEmployeeNo()), nullable(value.approverName()), nullable(value.approverPositionName()),
                value.periodStart(), value.periodEnd(), value.agencyName(), nullable(value.agencyAddress()),
                value.brandingFingerprint());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (Object field : fields) {
                digest.update(String.valueOf(field).getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static Object nullable(Object value) { return value == null ? "<null>" : value; }
}
