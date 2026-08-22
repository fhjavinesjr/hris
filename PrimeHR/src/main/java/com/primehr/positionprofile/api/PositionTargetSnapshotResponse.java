package com.primehr.positionprofile.api;

import com.primehr.positionprofile.domain.PositionTargetType;

import java.time.Instant;

public record PositionTargetSnapshotResponse(
        PositionTargetType type,
        Long targetId,
        Long jobPositionId,
        String jobPositionName,
        Long salaryGrade,
        Long salaryStep,
        Long plantillaId,
        String plantillaName,
        String sourceFingerprint,
        Instant capturedAt
) {
}
