package com.primehr.positionprofile.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SubmitPositionProfileRequest(
        @NotNull @PositiveOrZero Long recordVersion
) {
}
