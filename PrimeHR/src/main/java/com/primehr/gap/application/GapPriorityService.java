package com.primehr.gap.application;

import com.primehr.gap.api.GapPriorityDtos.*;
import com.primehr.gap.domain.GapPrioritySchemeStatus;
import com.primehr.shared.api.PageResponse;

public interface GapPriorityService {
    PageResponse<SchemeSummaryResponse> list(String agencyId, GapPrioritySchemeStatus status, int page, int size);
    SchemeResponse get(String agencyId, String id);
    SchemeResponse create(String agencyId, CreateSchemeRequest request, String correlationId);
    SchemeResponse update(String agencyId, String id, UpdateSchemeRequest request, String correlationId);
    SchemeResponse archive(String agencyId, String id, TransitionRequest request, String correlationId);
    SchemeResponse createSuccessor(String agencyId, String id, TransitionRequest request, String correlationId);
    SchemeResponse publish(String agencyId, String id, PublishRequest request, String correlationId);
    SchemeResponse addLevel(String agencyId, String schemeId, CreateLevelRequest request, String correlationId);
    SchemeResponse updateLevel(String agencyId, String schemeId, String levelId, UpdateLevelRequest request,
                               String correlationId);
    SchemeResponse archiveLevel(String agencyId, String schemeId, String levelId, TransitionRequest request,
                                String correlationId);
    SchemeResponse addRule(String agencyId, String schemeId, CreateRuleRequest request, String correlationId);
    SchemeResponse updateRule(String agencyId, String schemeId, String ruleId, UpdateRuleRequest request,
                              String correlationId);
    SchemeResponse archiveRule(String agencyId, String schemeId, String ruleId, TransitionRequest request,
                               String correlationId);
}
