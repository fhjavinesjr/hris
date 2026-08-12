package com.primehr.competency.api;

import java.time.LocalDate;
import java.time.Instant;

public record AdminCategoryResponse(
        String id, String code, String name, String description, String status,
        int definitionVersion, String supersedesId, int displayOrder,
        LocalDate effectiveFrom, LocalDate effectiveTo, long recordVersion,
        Instant publishedAt, String publishedBy
) {
}
