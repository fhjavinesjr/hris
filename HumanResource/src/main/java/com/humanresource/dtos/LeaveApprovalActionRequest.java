package com.humanresource.dtos;

import jakarta.validation.constraints.NotNull;

public record LeaveApprovalActionRequest(
        @NotNull(message = "approvedById is mandatory") Long approvedById,
        String remarks) {
}
