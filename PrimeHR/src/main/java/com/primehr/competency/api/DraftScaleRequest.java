package com.primehr.competency.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DraftScaleRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description,
        @PositiveOrZero int displayOrder,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        @PositiveOrZero Long recordVersion
) {
}
