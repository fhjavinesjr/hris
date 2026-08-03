package com.primehr.competency.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DraftIndicatorRequest(
        @NotBlank String proficiencyLevelId,
        @NotBlank @Size(max = 2000) String behaviorDescription,
        @Size(max = 2000) String evidenceGuidance,
        @PositiveOrZero int displayOrder,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        @PositiveOrZero Long recordVersion
) {
}
