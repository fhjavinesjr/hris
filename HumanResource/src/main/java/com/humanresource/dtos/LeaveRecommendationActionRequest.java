package com.humanresource.dtos;

import jakarta.validation.constraints.NotNull;

public record LeaveRecommendationActionRequest(
        @NotNull(message = "recommendedById is mandatory") Long recommendedById,
        String remarks) {
}
