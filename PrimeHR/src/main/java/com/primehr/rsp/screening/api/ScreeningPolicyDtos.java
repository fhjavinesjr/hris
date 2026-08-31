package com.primehr.rsp.screening.api;
import com.primehr.rsp.screening.domain.*; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.*; import java.util.*;
public final class ScreeningPolicyDtos { private ScreeningPolicyDtos(){}
 public record CriterionInput(@NotBlank @Size(max=80) String code,@NotBlank @Size(max=300) String label,@Size(max=2000) String internalInstructions,@Size(max=1000) String publicGuidance,@NotNull ScreeningCriterion.Category category,@NotNull ScreeningCriterion.EvaluationMode evaluationMode,@Size(max=100) String sourceKey,BigDecimal thresholdValue,@Size(max=30) String thresholdUnit,boolean mandatory,boolean disqualifying,boolean allowsNotApplicable,boolean requiresRemarks,boolean requiresEvidence,@PositiveOrZero int displayOrder){}
 public record ReasonInput(@NotBlank @Size(max=80) String code,@NotBlank @Size(max=300) String label,@NotBlank @Size(max=1000) String publicSafeText,@NotNull ScreeningReasonCode.Outcome outcomeCompatibility,boolean remarksRequired,@PositiveOrZero int displayOrder){}
 public record SavePolicy(@NotBlank @Size(max=80) String code,@NotBlank @Size(max=200) String name,@Size(max=2000) String description,LocalDate effectiveFrom,LocalDate effectiveTo,@NotEmpty List<@Valid CriterionInput> criteria,@NotEmpty List<@Valid ReasonInput> reasonCodes,Long recordVersion){}
 public record Publish(@NotNull Long recordVersion,@NotNull LocalDate effectiveFrom,LocalDate effectiveTo){}
 public record Transition(@NotNull Long recordVersion){}
 public record CriterionResponse(String id,String code,String label,String internalInstructions,String publicGuidance,String category,String evaluationMode,String sourceKey,BigDecimal thresholdValue,String thresholdUnit,boolean mandatory,boolean disqualifying,boolean allowsNotApplicable,boolean requiresRemarks,boolean requiresEvidence,int displayOrder){}
 public record ReasonResponse(String id,String code,String label,String publicSafeText,String outcomeCompatibility,boolean remarksRequired,int displayOrder){}
 public record PolicyResponse(String id,String code,String name,String description,int definitionVersion,String supersedesId,String status,LocalDate effectiveFrom,LocalDate effectiveTo,String publishedBy,Instant publishedAt,long recordVersion,List<CriterionResponse> criteria,List<ReasonResponse> reasonCodes){}
 public record BindPolicy(@NotBlank String policyId,@NotNull Long policyRecordVersion,@NotNull Long publicationRecordVersion){}
 public record BindingResponse(String id,String publicationId,String policyId,String policyCode,int policyDefinitionVersion,String policyFingerprint,String boundBy,Instant boundAt,long recordVersion){}
 public record EvidenceFacts(Set<String> presentKeys,Map<String,BigDecimal> numericValues,Map<String,Long> durationDays,Set<String> declarations){}
 public record Evaluation(String criterionCode,String result,String explanation,boolean humanConfirmationRequired){}
}
