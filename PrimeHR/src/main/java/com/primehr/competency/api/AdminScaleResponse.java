package com.primehr.competency.api;

import java.time.LocalDate;
import java.util.List;

public record AdminScaleResponse(
        String id, String code, String name, String description, String status,
        int definitionVersion, String supersedesId, int displayOrder,
        LocalDate effectiveFrom, LocalDate effectiveTo, long recordVersion,
        List<AdminLevelResponse> levels
) {
    public AdminScaleResponse { levels = List.copyOf(levels); }
}
