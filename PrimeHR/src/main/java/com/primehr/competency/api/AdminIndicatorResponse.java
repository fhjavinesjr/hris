package com.primehr.competency.api;

import java.time.LocalDate;

public record AdminIndicatorResponse(
        String id, String proficiencyLevelId, String proficiencyLevelCode,
        String behaviorDescription, String evidenceGuidance, boolean active, int displayOrder,
        LocalDate effectiveFrom, LocalDate effectiveTo, long recordVersion
) {
}
