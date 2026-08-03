package com.primehr.competency.api;

import java.time.LocalDate;

public record AdminLevelResponse(
        String id, String code, String label, int levelOrder, String description, boolean active,
        LocalDate effectiveFrom, LocalDate effectiveTo, long recordVersion
) {
}
