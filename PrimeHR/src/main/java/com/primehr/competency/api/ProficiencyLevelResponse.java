package com.primehr.competency.api;

import java.time.LocalDate;

public record ProficiencyLevelResponse(
        String id,
        String code,
        String label,
        int levelOrder,
        String description,
        boolean active,
        boolean effective,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        long version
) {
}
