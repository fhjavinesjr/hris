package com.primehr.performancemanagement.api;

import com.primehr.performancemanagement.api.PerformanceCommitmentDtos.CommitmentResponse;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;

public final class PerformanceCommitmentWorkflowDtos {
    private PerformanceCommitmentWorkflowDtos() {}
    public record Command(@NotNull Long recordVersion,@NotNull Integer contentRevision,@NotNull Integer lifecycleRevision,Integer routeRevision,@NotBlank @Size(max=100) String requestKey,@Size(max=2000) String reason,boolean overrideWindow,@Size(max=80) String overrideMilestone) {}
    public record AmendmentCommand(@NotNull Long recordVersion,@NotBlank @Size(max=100) String requestKey,@NotBlank @Size(max=2000) String reason) {}
    public record StepResponse(String id,int level,String actionType,Long employeeId,String employeeNo,String employeeName,Long appointmentId,Long jobPositionId,Long plantillaId,String participantFingerprint,String organizationFingerprint,String status,String decidedBy,Instant decidedAt,String decisionReason) {}
    public record RouteResponse(String id,int routeRevision,String requestCode,Long businessUnitId,String organizationFingerprint,String routeFingerprint,String submittedContentFingerprint,int submittedContentRevision,int currentLevel,String status,String submittedBy,Instant submittedAt,List<StepResponse> steps) {}
    public record ActionResponse(String id,String routeId,String routeStepId,String type,String requestKey,String actorEmployeeNo,String priorStatus,String resultStatus,int contentRevision,int lifecycleRevision,Integer routeRevision,String reason,String overrideMilestone,Instant actedAt) {}
    public record WorkflowResponse(CommitmentResponse commitment,List<RouteResponse> routes,List<ActionResponse> actions) {}
}
