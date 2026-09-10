package com.primehr.performancemanagement.api;
import com.primehr.performancemanagement.domain.*;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.time.*;import java.util.*;
public final class PerformanceManagementDtos {private PerformanceManagementDtos(){}
 public record PolicyInput(@NotBlank @Size(max=80)String code,@NotBlank @Size(max=200)String title,@Size(max=2000)String description,@NotBlank @Size(max=1000)String legalBasis,@NotBlank @Size(max=120)String displayLabel,@NotNull PerformancePolicyVersion.Frequency cycleFrequency,boolean requiresMidCycleReview,boolean requiresSelfAssessment,boolean requiresPmtCalibration,boolean requiresAcknowledgment,boolean requiresAppeal,Long recordVersion){}
 public record PolicyTransition(@NotNull Long recordVersion,@NotNull LocalDate effectiveFrom,LocalDate effectiveTo,@NotBlank @Size(max=1000)String reason){}
 public record VersionTransition(@NotNull Long recordVersion,@NotBlank @Size(max=1000)String reason){}
 public record PolicyResponse(String id,String policyId,String code,String title,String description,String legalBasis,String displayLabel,String cycleFrequency,int definitionVersion,String supersedesId,String status,LocalDate effectiveFrom,LocalDate effectiveTo,String publishedBy,Instant publishedAt,String retiredBy,Instant retiredAt,String retirementReason,boolean requiresMidCycleReview,boolean requiresSelfAssessment,boolean requiresPmtCalibration,boolean requiresAcknowledgment,boolean requiresAppeal,long recordVersion){}
 public record MilestoneInput(@NotNull PerformanceCycleMilestone.Type type,@NotBlank @Size(max=120)String key,@NotBlank @Size(max=200)String label,@NotNull Instant startsAt,Instant endsAt,boolean required,@Min(1)int displayOrder,@Size(max=2000)String instructions,boolean postCycleCloseout){}
 public record CycleInput(@NotBlank @Size(max=80)String code,@NotBlank @Size(max=200)String name,@NotNull LocalDate periodStart,@NotNull LocalDate periodEnd,@NotBlank @Size(max=36)String policyVersionId,@NotBlank @Size(max=60)String timezone,Long recordVersion){}
 public record MilestonesInput(@NotNull Long recordVersion,@NotEmpty List<@Valid MilestoneInput> milestones){}
 public record CycleTransition(@NotNull Long recordVersion,@NotBlank @Size(max=1000)String reason){}
 public record MilestoneResponse(String id,String type,String key,String label,Instant startsAt,Instant endsAt,boolean required,int displayOrder,String instructions,boolean postCycleCloseout){}
 public record CycleResponse(String id,String code,String name,LocalDate periodStart,LocalDate periodEnd,String policyVersionId,String timezone,String coverageScope,String status,int calendarRevision,String transitionReason,long recordVersion,List<MilestoneResponse> milestones){}
}
