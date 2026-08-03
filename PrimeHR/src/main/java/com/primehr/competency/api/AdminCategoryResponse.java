package com.primehr.competency.api;

import java.time.LocalDate;

public record AdminCategoryResponse(
        String id, String code, String name, String description, String status,
        int definitionVersion, String supersedesId, int displayOrder,
        LocalDate effectiveFrom, LocalDate effectiveTo, long recordVersion
) {
}
