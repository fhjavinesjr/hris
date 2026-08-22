package com.primehr.positionprofile.api;

import com.primehr.positionprofile.domain.PositionProfileStatus;
import com.primehr.positionprofile.domain.PositionTargetType;

import java.time.LocalDate;

public record PositionProfileSummaryResponse(
        String id,
        String name,
        PositionTargetType targetType,
        Long jobPositionId,
        Long plantillaId,
        String targetDisplayName,
        PositionProfileStatus status,
        int definitionVersion,
        String supersedesId,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        long recordVersion
) {
}
