package com.primehr.competency.api;

import java.time.LocalDate;
import java.util.List;

public record AdminCompetencyResponse(
        String id, String code, String name, String definition, String status,
        int definitionVersion, String supersedesId, String categoryId, String categoryName,
        String proficiencyScaleId, String proficiencyScaleName, int displayOrder,
        LocalDate effectiveFrom, LocalDate effectiveTo, long recordVersion,
        List<AdminIndicatorResponse> indicators
) {
    public AdminCompetencyResponse { indicators = List.copyOf(indicators); }
}
