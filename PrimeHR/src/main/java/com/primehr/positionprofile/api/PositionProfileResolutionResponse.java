package com.primehr.positionprofile.api;

import com.primehr.positionprofile.domain.PositionTargetType;

import java.time.LocalDate;

public record PositionProfileResolutionResponse(
        LocalDate asOf,
        PositionTargetType resolvedBy,
        PositionProfileResponse profile
) {
}
