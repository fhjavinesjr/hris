package com.primehr.integration.administrative;

import com.primehr.positionprofile.domain.PositionTargetType;

import java.time.Instant;

public record AdministrativePositionTarget(
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
