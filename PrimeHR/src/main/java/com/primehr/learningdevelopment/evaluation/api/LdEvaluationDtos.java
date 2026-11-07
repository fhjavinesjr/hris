package com.primehr.learningdevelopment.evaluation.api;
import jakarta.validation.constraints.*;import java.math.BigDecimal;import java.time.*;import java.util.List;
public final class LdEvaluationDtos {private LdEvaluationDtos(){}
 public record InitializeInput(@NotNull Long completionRecordVersion,@Size(max=100)String supervisorEmployeeNo,@Size(max=100)String staffEvaluatorEmployeeNo){}
 public record ResultInput(@NotNull Long taskRecordVersion,@DecimalMin("0.000000")BigDecimal score,Boolean passed,@NotBlank @Size(max=4000)String narrative,@Size(max=500)String evidenceReference){}
 public record DecisionInput(@NotNull Long taskRecordVersion,@NotNull Long resultRecordVersion,@NotNull Boolean approve,@NotBlank @Size(max=2000)String reason){}
 public record LapInput(@NotNull Long completionRecordVersion,@NotBlank @Size(max=100)String supervisorEmployeeNo,@NotBlank @Size(max=3000)String applicationObjective,@NotBlank @Size(max=3000)String successMeasure,@NotNull LocalDate targetDate){}
 public record LapUpdate(@NotNull Long recordVersion,@NotBlank @Size(max=3000)String applicationObjective,@NotBlank @Size(max=3000)String successMeasure,@NotNull LocalDate targetDate){}
 public record Transition(@NotNull Long recordVersion,@Size(max=2000)String reason){}
 public record FollowUpInput(@NotNull Long recordVersion,@NotNull Boolean approve,@NotBlank @Size(max=4000)String outcome,@Size(max=500)String evidenceReference,@NotBlank @Size(max=2000)String reason){}
 public record ResultResponse(String id,long recordVersion,int resultRevision,String supersedesResultId,BigDecimal score,Boolean passed,String narrative,String evidenceReference,String status,String submittedBy,Instant submittedAt,String decidedBy,Instant decidedAt,String decisionReason,String resultFingerprint){}
 public record TaskResponse(String id,long recordVersion,String completionCaseId,String offeringId,String employeeNo,String frameworkVersionId,String frameworkFingerprint,String levelType,boolean required,String scoreMethod,BigDecimal passingScore,String evidenceRequirements,String instrumentGuidance,String evaluatorRole,String assignedEvaluatorNo,LocalDate dueDate,String status,String currentResultId,List<ResultResponse>results){}
 public record LapResponse(String id,long recordVersion,String completionCaseId,String employeeNo,String supervisorEmployeeNo,String applicationObjective,String successMeasure,LocalDate targetDate,String status,int contentRevision,String submittedBy,Instant submittedAt,String followUpOutcome,String evidenceReference,String decidedBy,Instant decidedAt,String decisionReason){}
 public record EffectivenessRow(String dimension,String sourceId,String label,long verifiedResults,long passedResults,BigDecimal averageScore){}
 public record EffectivenessResponse(Instant generatedAt,int minimumCohort,int suppressedGroups,List<EffectivenessRow>rows){}
 public record HrmReviewPacket(String completionId,String employeeNo,String employeeName,String offeringId,BigDecimal creditedHours,LocalDate completionDate,String completionFingerprint,List<TaskResponse>evaluations,LapResponse actionPlan){}
 public record CompletionFeedPage(String schemaVersion,Instant changedAfter,Instant changedThrough,Instant generatedAt,int page,int size,long totalElements,int totalPages,List<CompletionFeedItem>content){}
 public record CompletionFeedItem(String completionId,long recordVersion,String employeeNo,String offeringId,int offeringVersion,String offeringCode,String offeringTitle,String programId,int programVersion,String interventionVersionId,String providerId,String providerName,BigDecimal creditedHours,LocalDate completionDate,Instant verifiedAt,String completionFingerprint,Instant changedAt,String outcomeFingerprint){}
}
