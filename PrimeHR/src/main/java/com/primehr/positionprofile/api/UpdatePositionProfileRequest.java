package com.primehr.positionprofile.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdatePositionProfileRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        @NotNull @PositiveOrZero Long recordVersion
) {
}
