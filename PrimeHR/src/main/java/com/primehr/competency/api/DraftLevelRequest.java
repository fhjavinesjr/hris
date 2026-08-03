package com.primehr.competency.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DraftLevelRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String label,
        @Min(1) int levelOrder,
        @Size(max = 1000) String description,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        @PositiveOrZero Long recordVersion
) {
}
