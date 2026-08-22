package com.primehr.positionprofile.application;

import com.primehr.positionprofile.api.*;
import com.primehr.positionprofile.domain.PositionProfileStatus;
import com.primehr.positionprofile.domain.PositionTargetType;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.AuditEventResponse;

import java.time.LocalDate;

public interface PositionProfileAdminService {
    PageResponse<PositionProfileSummaryResponse> list(String agencyId, PositionProfileStatus status,
                                                      PositionTargetType targetType, String search,
                                                      int page, int size);
    PositionProfileResponse get(String agencyId, String id);
    PositionProfileResponse create(String agencyId, CreatePositionProfileRequest request,
                                   String authorizationHeader, String correlationId);
    PositionProfileResponse update(String agencyId, String id, UpdatePositionProfileRequest request,
                                   String authorizationHeader, String correlationId);
    PositionProfileResponse archive(String agencyId, String id, PositionProfileTransitionRequest request,
                                    String correlationId);
    PositionProfileResponse createSuccessor(String agencyId, String id, PositionProfileTransitionRequest request,
                                            String authorizationHeader, String correlationId);
    PositionProfileResponse addRequirement(String agencyId, String profileId,
                                           CreatePositionRequirementRequest request, String correlationId);
    PositionProfileResponse updateRequirement(String agencyId, String profileId, String requirementId,
                                              UpdatePositionRequirementRequest request, String correlationId);
    PositionProfileResponse archiveRequirement(String agencyId, String profileId, String requirementId,
                                               PositionRequirementTransitionRequest request, String correlationId);
    PositionProfileResponse submit(String agencyId, String id, SubmitPositionProfileRequest request,
                                   String authorizationHeader, String correlationId);
    PositionProfileResponse returnSubmission(String agencyId, String id, PositionProfileTransitionRequest request,
                                             String correlationId);
    PositionProfileResponse approve(String agencyId, String id, ApprovePositionProfileRequest request,
                                    String authorizationHeader, boolean administrator, String correlationId);
    PositionProfileResolutionResponse resolve(String agencyId, Long jobPositionId, Long plantillaId,
                                              LocalDate asOf);
    PositionProfileComparisonResponse compare(String agencyId, String leftProfileId, String rightProfileId);
    PageResponse<AuditEventResponse> auditEvents(String agencyId, String profileId, int page, int size);
}
