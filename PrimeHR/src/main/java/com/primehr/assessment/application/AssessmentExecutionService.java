package com.primehr.assessment.application;

import com.primehr.assessment.api.AssessmentExecutionDtos.*;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.shared.api.PageResponse;

public interface AssessmentExecutionService {
    PageResponse<InboxItemResponse> mine(String agencyId, String actor, int page, int size);
    AssessmentWorkResponse getAssignedWork(String agencyId, String caseId, String actor,
                                             PermissionDataScope scope);
    AssessmentWorkResponse saveRating(String agencyId, String caseId, String assignmentId,
            String competencyId, SaveRatingRequest request, String actor, PermissionDataScope scope,
            String correlationId);
    AssessmentWorkResponse createEvidence(String agencyId, String caseId, String assignmentId,
            CreateEvidenceRequest request, String actor, PermissionDataScope scope, String correlationId);
    AssessmentWorkResponse updateEvidence(String agencyId, String caseId, String assignmentId,
            String evidenceId, UpdateEvidenceRequest request, String actor, PermissionDataScope scope,
            String correlationId);
    AssessmentWorkResponse archiveEvidence(String agencyId, String caseId, String assignmentId,
            String evidenceId, WorkTransitionRequest request, String actor, PermissionDataScope scope,
            String correlationId);
    AssessmentWorkResponse submit(String agencyId, String caseId, String assignmentId,
            WorkTransitionRequest request, String actor, PermissionDataScope scope, String correlationId);
    ReturnCaseResponse returnCase(String agencyId, String caseId, ReturnCaseRequest request,
                                  String actor, String correlationId);
}
