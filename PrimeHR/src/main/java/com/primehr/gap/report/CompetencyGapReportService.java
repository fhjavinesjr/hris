package com.primehr.gap.report;

import com.primehr.gap.api.CompetencyGapDtos.AnalysisResponse;

public interface CompetencyGapReportService {
    byte[] generate(AnalysisResponse analysis);
}
