package com.primehr.positionprofile.api;

import com.primehr.positionprofile.domain.PositionTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePositionProfileRequest(
        @NotNull PositionTargetType targetType,
        @NotNull @Positive Long targetId,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Long recordVersion
) {
}
