package com.primehr.gap.application;

import com.primehr.gap.api.CompetencyGapDtos.*;
import com.primehr.gap.domain.GapClassification;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.shared.api.PageResponse;

public interface CompetencyGapService {
    AnalysisResponse generate(String agencyId, GenerateRequest request, String authorizationHeader,
                              String actor, String correlationId);
    PageResponse<AnalysisSummaryResponse> list(String agencyId, String employeeNo,
                                               GapClassification classification, String priorityCode,
                                               int page, int size, String actor, PermissionDataScope scope);
    AnalysisResponse get(String agencyId, String id, String actor, PermissionDataScope scope);
    AnalysisResponse latest(String agencyId, String employeeNo, String actor, PermissionDataScope scope);
    PageResponse<AnalysisSummaryResponse> history(String agencyId, String employeeNo, int page, int size,
                                                  String actor, PermissionDataScope scope);
}
