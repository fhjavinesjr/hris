package com.primehr.positionprofile.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ApprovePositionProfileRequest(
        @NotNull @PositiveOrZero Long recordVersion,
        @Size(max = 1000) String reason
) {
}
