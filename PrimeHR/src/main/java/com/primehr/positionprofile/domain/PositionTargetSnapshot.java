package com.primehr.positionprofile.domain;

import com.primehr.integration.administrative.AdministrativePositionTarget;

import java.time.Instant;

public record PositionTargetSnapshot(
        PositionTargetType type,
        Long targetId,
        Long jobPositionId,
        String jobPositionName,
        Long salaryGrade,
        Long salaryStep,
        Long plantillaId,
        String plantillaName,
        String fingerprint,
        Instant capturedAt
) {
    public static PositionTargetSnapshot from(AdministrativePositionTarget source) {
        return new PositionTargetSnapshot(source.type(), source.targetId(), source.jobPositionId(),
                source.jobPositionName(), source.salaryGrade(), source.salaryStep(), source.plantillaId(),
                source.plantillaName(), source.sourceFingerprint(), source.fetchedAt());
    }

    public PositionTargetSnapshot {
        if (type == null || targetId == null || targetId < 1 || jobPositionId == null || jobPositionId < 1) {
            throw new IllegalArgumentException("A valid position target is required");
        }
        jobPositionName = requireText(jobPositionName, "jobPositionName");
        fingerprint = requireText(fingerprint, "fingerprint");
        if (capturedAt == null) throw new IllegalArgumentException("capturedAt is required");
        if (type == PositionTargetType.JOB_POSITION) {
            if (!targetId.equals(jobPositionId) || plantillaId != null || plantillaName != null) {
                throw new IllegalArgumentException("A Job Position target cannot contain Plantilla identity");
            }
        } else if (plantillaId == null || !targetId.equals(plantillaId)) {
            throw new IllegalArgumentException("A Plantilla target requires its exact Plantilla identity");
        } else {
            plantillaName = requireText(plantillaName, "plantillaName");
        }
    }

    public String targetKey() {
        return type.name() + ":" + targetId;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}
