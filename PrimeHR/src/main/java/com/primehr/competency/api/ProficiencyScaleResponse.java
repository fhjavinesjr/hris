package com.primehr.competency.api;

import java.time.LocalDate;
import java.util.List;

public record ProficiencyScaleResponse(
        String id,
        String agencyId,
        String code,
        String name,
        String description,
        boolean active,
        boolean effective,
        int displayOrder,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        long version,
        List<ProficiencyLevelResponse> levels
) {
    public ProficiencyScaleResponse {
        levels = List.copyOf(levels);
    }
}
