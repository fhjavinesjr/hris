package com.primehr.positionprofile.api;

import com.primehr.positionprofile.domain.PositionProfileStatus;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

public record PositionProfileResponse(
        String id,
        String name,
        String description,
        PositionProfileStatus status,
        int definitionVersion,
        String supersedesId,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        long recordVersion,
        long contentRevision,
        String submittedBy,
        Instant submittedAt,
        String approvedBy,
        Instant approvedAt,
        PositionTargetSnapshotResponse targetSnapshot,
        List<PositionRequirementResponse> requirements
) {
}
