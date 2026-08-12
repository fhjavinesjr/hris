package com.primehr.competency.application;

import com.primehr.competency.api.*;
import com.primehr.competency.domain.DefinitionStatus;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.AuditEventResponse;
import java.time.LocalDate;

public interface CompetencyAdminService {
    PageResponse<AdminCategoryResponse> listCategories(String agency, DefinitionStatus status, String search,
                                                       LocalDate asOf, int page, int size);
    AdminCategoryResponse createCategory(String agency, DraftCategoryRequest request, String correlationId);
    AdminCategoryResponse updateCategory(String agency, String id, DraftCategoryRequest request, String correlationId);
    AdminCategoryResponse versionCategory(String agency, String id, DraftTransitionRequest request, String correlationId);
    AdminCategoryResponse archiveCategory(String agency, String id, DraftTransitionRequest request, String correlationId);
    AdminCategoryResponse publishCategory(String agency, String id, PublishDefinitionRequest request, String correlationId);

    PageResponse<AdminScaleResponse> listScales(String agency, DefinitionStatus status, String search,
                                                LocalDate asOf, int page, int size);
    AdminScaleResponse createScale(String agency, DraftScaleRequest request, String correlationId);
    AdminScaleResponse updateScale(String agency, String id, DraftScaleRequest request, String correlationId);
    AdminScaleResponse versionScale(String agency, String id, DraftTransitionRequest request, String correlationId);
    AdminScaleResponse archiveScale(String agency, String id, DraftTransitionRequest request, String correlationId);
    AdminScaleResponse publishScale(String agency, String id, PublishDefinitionRequest request, String correlationId);
    AdminLevelResponse createLevel(String agency, String scaleId, DraftLevelRequest request, String correlationId);
    AdminLevelResponse updateLevel(String agency, String scaleId, String levelId, DraftLevelRequest request, String correlationId);
    AdminLevelResponse archiveLevel(String agency, String scaleId, String levelId, DraftTransitionRequest request, String correlationId);

    PageResponse<AdminCompetencyResponse> listCompetencies(String agency, DefinitionStatus status, String categoryId,
                                                           String search, LocalDate asOf, int page, int size);
    AdminCompetencyResponse createCompetency(String agency, DraftCompetencyRequest request, String correlationId);
    AdminCompetencyResponse updateCompetency(String agency, String id, DraftCompetencyRequest request, String correlationId);
    AdminCompetencyResponse versionCompetency(String agency, String id, DraftTransitionRequest request, String correlationId);
    AdminCompetencyResponse archiveCompetency(String agency, String id, DraftTransitionRequest request, String correlationId);
    AdminCompetencyResponse publishCompetency(String agency, String id, PublishDefinitionRequest request,
                                              String correlationId);
    AdminIndicatorResponse createIndicator(String agency, String competencyId, DraftIndicatorRequest request, String correlationId);
    AdminIndicatorResponse updateIndicator(String agency, String competencyId, String indicatorId,
                                            DraftIndicatorRequest request, String correlationId);
    AdminIndicatorResponse archiveIndicator(String agency, String competencyId, String indicatorId,
                                             DraftTransitionRequest request, String correlationId);

    PageResponse<AuditEventResponse> listAuditEvents(String agency, String aggregateType, String aggregateId,
                                                     int page, int size);
}
