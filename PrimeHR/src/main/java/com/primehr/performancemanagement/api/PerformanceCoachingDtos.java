package com.primehr.performancemanagement.api;
import jakarta.validation.constraints.*;import java.time.*;import java.util.*;
public final class PerformanceCoachingDtos{private PerformanceCoachingDtos(){}
 public record SessionInput(@NotNull Instant sessionAt,@NotBlank String agenda,@NotBlank String goal,@NotBlank String observedIssue,@NotBlank String agreedAction,String resourcesSupport,LocalDate actionDueDate,Instant nextMeetingAt,String employeeVisibleFeedback,String privateNotes,List<String>commitmentItemIds,@NotBlank String idempotencyKey){}
 public record SessionEdit(@NotNull Instant sessionAt,@NotBlank String agenda,@NotBlank String goal,@NotBlank String observedIssue,@NotBlank String agreedAction,String resourcesSupport,LocalDate actionDueDate,Instant nextMeetingAt,String employeeVisibleFeedback,String privateNotes,@NotNull Long recordVersion){}
 public record SessionAction(@NotNull Long recordVersion,@NotBlank String idempotencyKey,String reason,String employeeResponse){}
 public record SessionResponse(String id,String monitoringCaseId,String rootSessionId,int revisionNo,String supersedesId,Instant sessionAt,String agenda,String goal,String observedIssue,String agreedAction,String resourcesSupport,LocalDate actionDueDate,Instant nextMeetingAt,String employeeVisibleFeedback,String privateNotes,String employeeResponse,String status,String issuedBy,Instant issuedAt,String acknowledgedBy,Instant acknowledgedAt,String voidReason,long recordVersion,List<String>commitmentItemIds,List<ActionItemResponse>actionItems){}
 public record ActionItemInput(@NotBlank String description,@NotBlank String accountableEmployeeNo,@NotNull LocalDate dueDate,@NotBlank String idempotencyKey){}
 public record ActionItemEdit(@NotBlank String progressNote,boolean completed,@NotNull Long recordVersion,@NotBlank String idempotencyKey){}
 public record ActionItemResponse(String id,String coachingSessionId,String description,String accountableEmployeeNo,LocalDate dueDate,String status,String progressNote,Instant completedAt,String verifiedBy,Instant verifiedAt,String reopenReason,long recordVersion){}
 public record ReviewInput(@NotBlank String supervisorNarrative,String employeeNarrative,boolean amendmentRecommended,String amendmentReason,@NotBlank String idempotencyKey){}
 public record ReviewEdit(@NotBlank String supervisorNarrative,String employeeNarrative,boolean amendmentRecommended,String amendmentReason,@NotNull Long recordVersion){}
 public record ReviewAction(@NotNull Long recordVersion,@NotBlank String idempotencyKey,String reason,String employeeNarrative){}
 public record ReviewResponse(String id,String monitoringCaseId,int snapshotUpdateCount,int snapshotOpenActionCount,int snapshotMissingEvidenceCount,String supervisorNarrative,String employeeNarrative,boolean amendmentRecommended,String amendmentReason,String status,String submittedBy,Instant submittedAt,String acknowledgedBy,Instant acknowledgedAt,String closureReason,long recordVersion){}
}
