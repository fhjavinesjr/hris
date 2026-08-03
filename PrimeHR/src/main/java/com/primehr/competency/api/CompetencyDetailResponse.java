package com.primehr.competency.api;

import java.util.List;

public record CompetencyDetailResponse(
        CompetencySummaryResponse competency,
        ProficiencyScaleResponse proficiencyScale,
        List<BehavioralIndicatorResponse> behavioralIndicators
) {
    public CompetencyDetailResponse {
        behavioralIndicators = List.copyOf(behavioralIndicators);
    }
}
