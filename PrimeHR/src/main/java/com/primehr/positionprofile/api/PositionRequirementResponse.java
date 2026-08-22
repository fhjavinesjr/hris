package com.primehr.positionprofile.api;

import com.primehr.positionprofile.domain.RequirementClassification;

public record PositionRequirementResponse(
        String id,
        String competencyVersionId,
        String competencyCode,
        String competencyName,
        int competencyDefinitionVersion,
        String requiredProficiencyLevelId,
        String requiredProficiencyLevelCode,
        String requiredProficiencyLevelLabel,
        RequirementClassification classification,
        String criticalityCode,
        String remarks,
        boolean active,
        int displayOrder,
        long recordVersion
) {
}
