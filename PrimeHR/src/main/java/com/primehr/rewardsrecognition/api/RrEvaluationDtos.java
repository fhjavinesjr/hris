package com.primehr.rewardsrecognition.api;
import com.primehr.rewardsrecognition.domain.*;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.math.BigDecimal;import java.time.Instant;import java.util.List;
public final class RrEvaluationDtos{private RrEvaluationDtos(){}
 public record InitializeInput(@NotBlank String charterVersionId,@NotNull Long nominationRecordVersion){}
 public record FindingInput(@NotBlank@Size(max=100)String ruleCode,@NotBlank@Size(max=300)String ruleLabel,@NotNull RrEligibilityFinding.Outcome outcome,@NotBlank@Size(max=3000)String rationale){}
 public record EligibilityInput(@NotNull Long recordVersion,@NotNull RrEvaluationCase.Status decision,@NotBlank@Size(max=4000)String summary,@NotEmpty List<@Valid FindingInput>findings){}
 public record ConflictInput(@NotNull Long recordVersion,@NotBlank@Size(max=2000)String reason){}
 public record ReplacementInput(@NotNull Long recusedRecordVersion,@NotBlank String replacementAssignmentId){}
 public record RatingInput(@NotBlank String criterionId,@NotNull@DecimalMin("0")BigDecimal score,@NotBlank@Size(max=3000)String comments){}
 public record ScoreInput(@NotNull Long recordVersion,boolean attending,@NotEmpty List<@Valid RatingInput>ratings){}
 public record RecommendationInput(@NotNull Long recordVersion,@NotNull RrDecisionEvent.Outcome outcome,@NotBlank@Size(max=4000)String reason){}
 public record DecisionInput(@NotNull Long recordVersion,@NotNull RrDecisionEvent.Outcome outcome,@NotBlank@Size(max=4000)String reason){}
 public record IssueInput(@NotNull Long recordVersion,@NotBlank@Size(max=4000)String grounds){}
 public record ResolveIssueInput(@NotNull Long caseRecordVersion,@NotNull Long issueRecordVersion,@NotNull RrDecisionEvent.Outcome outcome,@NotBlank@Size(max=4000)String reason,boolean dismiss){}
 public record FindingResponse(String id,String ruleCode,String ruleLabel,String outcome,String rationale){}
 public record RatingResponse(String id,String criterionId,String criterionCode,BigDecimal weightPercent,BigDecimal maximumScore,BigDecimal rawScore,BigDecimal weightedScore,String comments){}
 public record AssignmentResponse(String id,long recordVersion,String employeeNo,String displayName,String positionTitle,String memberRole,boolean votingMember,String status,boolean conflictDeclared,String conflictReason,String replacementAssignmentId,boolean attending,BigDecimal totalScore,Instant submittedAt,List<RatingResponse>ratings){}
 public record EventResponse(String id,String type,String outcome,BigDecimal calculatedScore,Integer eligibleVoters,Integer attendingVoters,Boolean quorumMet,String reason,String decidedBy,Instant decidedAt){}
 public record IssueResponse(String id,long recordVersion,String grounds,String status,String filedBy,Instant filedAt,String resolutionReason,String resolvedBy,Instant resolvedAt){}
 public record CaseResponse(String id,long recordVersion,String nominationId,String cycleId,String categoryVersionId,String charterVersionId,int charterDefinitionVersion,String charterFingerprint,String status,String eligibilitySummary,String eligibilityDecidedBy,Instant eligibilityDecidedAt,BigDecimal calculatedScore,Integer eligibleVoters,Integer attendingVoters,Boolean quorumMet,List<FindingResponse>findings,List<AssignmentResponse>assignments,List<EventResponse>events,List<IssueResponse>issues){}
}
