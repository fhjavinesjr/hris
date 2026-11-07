package com.primehr.learningdevelopment.plan.api;

import com.primehr.learningdevelopment.plan.domain.*;
import jakarta.validation.constraints.*;
import java.time.*;import java.util.List;

public final class LdPlanDtos {
 private LdPlanDtos(){}
 public record NeedInput(@NotNull Long employeeId,@NotNull LearningNeedSourceType sourceType,String sourceReferenceId,Long sourceVersion,String sourceFingerprint,String targetCompetencyId,String targetCompetencyCode,String targetCompetencyName,@NotBlank String developmentObjective,@NotBlank String currentState,@NotBlank String targetState,@NotBlank String successMeasure,LocalDate targetDate,@NotBlank String priority,Long recordVersion){}
 public record NeedResponse(String id,Long employeeId,String employeeNo,String employeeName,Long appointmentId,String employeeFingerprint,String sourceType,String sourceReferenceId,Long sourceVersion,String sourceFingerprint,String targetCompetencyId,String targetCompetencyCode,String targetCompetencyName,String developmentObjective,String currentState,String targetState,String successMeasure,LocalDate targetDate,String priority,String status,long recordVersion){}
 public record ReferralDispositionInput(@NotNull Long referralVersion,@NotNull LdReferralDisposition.Decision decision,@NotBlank String reason,String currentState,String targetState,String successMeasure,LocalDate targetDate,@Pattern(regexp="LOW|MEDIUM|HIGH|CRITICAL") String priority,String linkedIdpItemId){}
 public record ReferralDispositionResponse(String id,String referralId,long referralVersion,String referralFingerprint,String employeeNo,String decision,String learningNeedId,String linkedIdpItemId,String decidedBy,Instant decidedAt,String reason){}
 public record IdpCreate(@NotNull Long employeeId,@Min(2000)@Max(2200)int planningYear,@NotBlank String overallGoal){}
 public record IdpGoalUpdate(@NotNull Long recordVersion,@NotBlank String overallGoal){}
 public record IdpItemInput(@NotNull Long recordVersion,@NotBlank String learningNeedId,@NotBlank String interventionCategory,@NotBlank String employeeCommitment,@NotBlank String supervisorCommitment,@Min(1)int displayOrder){}
 public record IdpItemUpdate(@NotNull Long planRecordVersion,@NotNull Long itemRecordVersion,@NotBlank String interventionCategory,@NotBlank String employeeCommitment,@NotBlank String supervisorCommitment,@Min(1)int displayOrder){}
 public record IdpTransition(@NotNull Long recordVersion,String reason){}
 public record IdpItemResponse(String id,String learningNeedId,String sourceType,String sourceReferenceId,Long sourceVersion,String sourceFingerprint,String targetCompetencyId,String targetCompetencyCode,String targetCompetencyName,String developmentObjective,String currentState,String targetState,String interventionCategory,String successMeasure,LocalDate targetDate,String employeeCommitment,String supervisorCommitment,String priority,int displayOrder,long recordVersion){}
 public record RouteStepResponse(int level,String action,String employeeNo,String employeeName,String status,String decidedBy,Instant decidedAt,String decisionReason){}
 public record IdpResponse(String id,String seriesId,int planVersion,String supersedesId,Long employeeId,String employeeNo,String employeeName,Long appointmentId,Long jobPositionId,Long plantillaId,String employeeFingerprint,Long businessUnitId,String businessUnitName,Long areaId,String areaName,String organizationFingerprint,int planningYear,String overallGoal,String status,int contentRevision,long recordVersion,String submittedBy,Instant submittedAt,String approvedBy,Instant approvedAt,String returnReason,List<IdpItemResponse>items,List<RouteStepResponse>route){}
}
