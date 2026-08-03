package com.primehr.competency.api;

import java.time.LocalDate;

public record CompetencyCategoryResponse(
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
        long version
) {
}
