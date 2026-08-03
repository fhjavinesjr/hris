package com.primehr.competency.api;

import java.time.LocalDate;

public record CompetencySummaryResponse(
        String id,
        String agencyId,
        String code,
        String name,
        String definition,
        String status,
        String categoryId,
        String categoryCode,
        String categoryName,
        String proficiencyScaleId,
        String proficiencyScaleCode,
        String proficiencyScaleName,
        boolean active,
        boolean effective,
        int displayOrder,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        long version
) {
}
