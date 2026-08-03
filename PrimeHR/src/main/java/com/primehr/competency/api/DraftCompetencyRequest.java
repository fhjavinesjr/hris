package com.primehr.competency.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DraftCompetencyRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 4000) String definition,
        @NotBlank String categoryId,
        @NotBlank String proficiencyScaleId,
        @PositiveOrZero int displayOrder,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        @PositiveOrZero Long recordVersion
) {
}
