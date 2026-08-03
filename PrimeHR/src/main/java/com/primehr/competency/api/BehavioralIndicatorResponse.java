package com.primehr.competency.api;

import java.time.LocalDate;

public record BehavioralIndicatorResponse(
        String id,
        String proficiencyLevelId,
        String proficiencyLevelCode,
        String proficiencyLevelLabel,
        int proficiencyLevelOrder,
        String behaviorDescription,
        String evidenceGuidance,
        boolean active,
        boolean effective,
        int displayOrder,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        long version
) {
}
