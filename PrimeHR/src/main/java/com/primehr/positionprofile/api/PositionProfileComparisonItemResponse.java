package com.primehr.positionprofile.api;

import com.primehr.positionprofile.domain.RequirementClassification;

public record PositionProfileComparisonItemResponse(
        PositionProfileComparisonChange change,
        String competencyVersionId,
        String competencyCode,
        String competencyName,
        String leftLevelId,
        String leftLevelLabel,
        RequirementClassification leftClassification,
        String leftCriticalityCode,
        String rightLevelId,
        String rightLevelLabel,
        RequirementClassification rightClassification,
        String rightCriticalityCode
) {
}
