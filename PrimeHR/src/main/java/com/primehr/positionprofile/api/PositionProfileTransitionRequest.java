package com.primehr.positionprofile.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record PositionProfileTransitionRequest(
        @NotNull @PositiveOrZero Long recordVersion,
        @NotBlank @Size(max = 1000) String reason
) {
}
