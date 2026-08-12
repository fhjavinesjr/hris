package com.primehr.competency.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record PublishDefinitionRequest(
        @PositiveOrZero long recordVersion,
        @NotBlank @Size(max = 1000) String reason
) {
}
