package com.primehr.succession.api;
import com.primehr.succession.domain.*;import jakarta.validation.constraints.*;import java.time.*;import java.util.List;
public final class SuccessionDevelopmentDtos{private SuccessionDevelopmentDtos(){}
 public record CreatePlanInput(@NotNull Long decisionRecordVersion,@NotBlank @Size(max=4000)String overallGoal,@NotNull LocalDate startsOn,@NotNull LocalDate targetCompletionDate,@NotNull LocalDate nextReviewOn){}
 public record UpdatePlanInput(@NotNull Long recordVersion,@NotBlank @Size(max=4000)String overallGoal,@NotNull LocalDate startsOn,@NotNull LocalDate targetCompletionDate,@NotNull LocalDate nextReviewOn){}
 public record SourceInput(@NotNull Long recordVersion,@NotNull SuccessionDevelopmentSource.SourceType sourceType,@NotBlank String sourceId){}
 public record MilestoneInput(@NotNull Long recordVersion,@NotBlank @Size(max=250)String title,@NotBlank @Size(max=3000)String measurableOutcome,@NotNull LocalDate dueDate,@Min(1)int displayOrder){}
 public record MilestoneStatusInput(@NotNull Long recordVersion,@NotNull SuccessionDevelopmentMilestone.Status status,@Size(max=1000)String evidenceReference,@Size(max=3000)String employeeVisibleFeedback,@Size(max=3000)String restrictedFeedback){}
 public record VersionInput(@NotNull Long recordVersion){}
 public record AcknowledgmentInput(@NotNull Long recordVersion,@Size(max=4000)String response){}
 public record ReviewInput(@NotNull Long recordVersion,@NotNull LocalDate reviewDate,@NotNull SuccessionDevelopmentReview.Recommendation recommendation,@NotBlank @Size(max=4000)String employeeVisibleSummary,@NotBlank @Size(max=4000)String restrictedRationale,LocalDate nextReviewOn){}
 public record MembershipInput(@NotNull Long currentEventRecordVersion,@NotNull SuccessionPoolMembershipEvent.EventType eventType,@NotNull LocalDate effectiveOn,LocalDate expiresOn,@NotBlank @Size(max=4000)String employeeVisibleReason,@NotBlank @Size(max=4000)String restrictedRationale){}
 public record SourceResponse(String id,String sourceType,String sourceId,String sourceParentId,long sourceVersion,String sourceFingerprint,String sourceSummary,LocalDate sourceDate){}
 public record MilestoneResponse(String id,long recordVersion,String title,String measurableOutcome,LocalDate dueDate,int displayOrder,String status,String evidenceReference,String employeeVisibleFeedback,String restrictedFeedback,String updatedBy,Instant updatedAt){}
 public record ReviewResponse(String id,int sequence,LocalDate reviewDate,String recommendation,String employeeVisibleSummary,String restrictedRationale,String reviewedBy,Instant reviewedAt,LocalDate nextReviewOn){}
 public record MembershipResponse(String id,long recordVersion,String previousEventId,int sequence,String eventType,String resultingStatus,LocalDate effectiveOn,LocalDate expiresOn,String employeeVisibleReason,String restrictedRationale,String recordedBy,Instant recordedAt){}
 public record PlanResponse(String id,long recordVersion,String seriesId,int planVersion,String poolDecisionId,String assessmentId,String candidateId,String employeeNo,String employeeName,String targetPositionName,String readinessLevelCode,String overallGoal,LocalDate startsOn,LocalDate targetCompletionDate,LocalDate nextReviewOn,String status,String acknowledgmentStatus,String acknowledgmentResponse,Instant acknowledgedAt,String activatedBy,Instant activatedAt,String completedBy,Instant completedAt,List<SourceResponse>sources,List<MilestoneResponse>milestones,List<ReviewResponse>reviews,List<MembershipResponse>membershipHistory){}
}
