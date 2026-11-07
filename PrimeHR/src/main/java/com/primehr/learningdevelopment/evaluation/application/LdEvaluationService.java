package com.primehr.learningdevelopment.evaluation.application;
import com.primehr.integration.administrative.PermissionDataScope;import com.primehr.learningdevelopment.evaluation.api.LdEvaluationDtos.*;import java.time.Instant;import java.util.List;
public interface LdEvaluationService {
 List<TaskResponse> tasks(String agency,String actor,PermissionDataScope scope);List<TaskResponse> mine(String agency,String actor);List<TaskResponse> assigned(String agency,String actor);
 List<TaskResponse> initialize(String agency,String completionId,InitializeInput input,String actor,PermissionDataScope scope,String correlation);
 ResultResponse submitResult(String agency,String taskId,ResultInput input,String actor,String correlation);TaskResponse decideResult(String agency,String taskId,String resultId,DecisionInput input,String actor,PermissionDataScope scope,String correlation);
 List<LapResponse> actionPlans(String agency,String actor,PermissionDataScope scope);List<LapResponse> myActionPlans(String agency,String actor);List<LapResponse> assignedActionPlans(String agency,String actor);
 LapResponse createActionPlan(String agency,String completionId,LapInput input,String actor,PermissionDataScope scope,String correlation);LapResponse updateActionPlan(String agency,String id,LapUpdate input,String actor,PermissionDataScope scope,String correlation);LapResponse submitActionPlan(String agency,String id,Transition input,String actor,String correlation);LapResponse followUp(String agency,String id,FollowUpInput input,String actor,PermissionDataScope scope,String correlation);
 EffectivenessResponse effectiveness(String agency,String actor,String correlation);CompletionFeedPage completionFeed(String agency,Instant after,Instant through,int page,int size,String actor,String correlation);
 HrmReviewPacket hrmReviewPacket(String agency,String completionId,String actor,PermissionDataScope scope,String correlation);
}
