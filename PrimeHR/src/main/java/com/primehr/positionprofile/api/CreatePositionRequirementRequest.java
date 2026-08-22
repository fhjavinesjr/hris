package com.primehr.positionprofile.api;

import com.primehr.positionprofile.domain.RequirementClassification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreatePositionRequirementRequest(
        @NotBlank @Size(max = 36) String competencyVersionId,
        @NotBlank @Size(max = 36) String requiredProficiencyLevelId,
        @NotNull RequirementClassification classification,
        @Size(max = 50) String criticalityCode,
        @Size(max = 2000) String remarks,
        @PositiveOrZero int displayOrder,
        @NotNull @PositiveOrZero Long profileRecordVersion
) {
}
