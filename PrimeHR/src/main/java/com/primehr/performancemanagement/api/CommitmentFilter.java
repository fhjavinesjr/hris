package com.primehr.performancemanagement.api;

import jakarta.validation.constraints.NotBlank;

public record CommitmentFilter(
        @NotBlank String formType,
        String cycleId,
        String subjectType,
        Long subjectId,
        String ownerEmployeeNo,
        String status,
        Boolean assignedTask) {
}
