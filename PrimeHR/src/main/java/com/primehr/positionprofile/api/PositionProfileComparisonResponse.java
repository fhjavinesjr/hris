package com.primehr.positionprofile.api;

import java.util.List;

public record PositionProfileComparisonResponse(
        String leftProfileId,
        int leftDefinitionVersion,
        String rightProfileId,
        int rightDefinitionVersion,
        int added,
        int removed,
        int changed,
        int unchanged,
        List<PositionProfileComparisonItemResponse> items
) {
}
