package com.administrative.dtos;

import java.time.Instant;

public record PositionTargetResponse(
        PositionTargetType type,
        Long targetId,
        Long jobPositionId,
        String jobPositionName,
        Long salaryGrade,
        Long salaryStep,
        Long plantillaId,
        String plantillaName,
        String sourceFingerprint,
        Instant fetchedAt
) {
}
