package com.primehr.assessment.application;

import com.primehr.assessment.api.AssessmentDtos.*;
import com.primehr.assessment.domain.AssessmentCycleStatus;
import com.primehr.shared.api.PageResponse;

import java.util.List;

public interface AssessmentAdministrationService {
    PageResponse<CycleResponse> listCycles(String agencyId, AssessmentCycleStatus status, int page, int size);
    CycleResponse getCycle(String agencyId, String cycleId);
    CycleResponse createCycle(String agencyId, CreateCycleRequest request, String correlationId);
    CycleResponse updateCycle(String agencyId, String cycleId, UpdateCycleRequest request, String correlationId);
    CycleResponse archiveCycle(String agencyId, String cycleId, TransitionRequest request, String correlationId);
    CycleResponse openCycle(String agencyId, String cycleId, TransitionRequest request, String correlationId);
    CycleResponse closeCycle(String agencyId, String cycleId, TransitionRequest request, String correlationId);
    List<ToolResponse> listTools(String agencyId, String cycleId);
    ToolResponse getTool(String agencyId, String toolId);
    ToolResponse createTool(String agencyId, String cycleId, CreateToolRequest request, String correlationId);
    ToolResponse updateTool(String agencyId, String toolId, UpdateToolRequest request, String correlationId);
    ToolResponse archiveTool(String agencyId, String toolId, TransitionRequest request, String correlationId);
    ToolResponse publishTool(String agencyId, String toolId, TransitionRequest request, String correlationId);
    PageResponse<CaseResponse> listCases(String agencyId, String toolId, int page, int size);
    CaseResponse getCase(String agencyId, String caseId);
    CaseResponse addSubject(String agencyId, String toolId, AddSubjectRequest request,
                            String authorizationHeader, String correlationId);
    CaseResponse archiveCase(String agencyId, String caseId, TransitionRequest request, String correlationId);
    CaseResponse addAssessor(String agencyId, String caseId, AddAssessorRequest request,
                             String authorizationHeader, String correlationId);
    CaseResponse updateAssessor(String agencyId, String caseId, String assignmentId,
                                UpdateAssessorRequest request, String correlationId);
    CaseResponse archiveAssessor(String agencyId, String caseId, String assignmentId,
                                 TransitionRequest request, String correlationId);
}
