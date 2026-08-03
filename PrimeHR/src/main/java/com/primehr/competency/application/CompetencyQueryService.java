package com.primehr.competency.application;

import com.primehr.competency.api.CompetencyCategoryResponse;
import com.primehr.competency.api.CompetencyDetailResponse;
import com.primehr.competency.api.CompetencySummaryResponse;
import com.primehr.competency.api.ProficiencyScaleResponse;
import com.primehr.shared.api.PageResponse;

import java.time.LocalDate;
import java.util.List;

public interface CompetencyQueryService {

    List<CompetencyCategoryResponse> listCategories(String agencyId, Boolean active, LocalDate asOf);

    PageResponse<CompetencySummaryResponse> listCompetencies(String agencyId, String categoryId,
                                                              Boolean active, String search, LocalDate asOf,
                                                              int page, int size);

    CompetencyDetailResponse getCompetency(String agencyId, String competencyId,
                                            boolean includeInactive, LocalDate asOf);

    List<ProficiencyScaleResponse> listScales(String agencyId, Boolean active, LocalDate asOf);
}
