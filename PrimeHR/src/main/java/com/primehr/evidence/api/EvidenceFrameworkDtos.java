package com.primehr.evidence.api;
import com.primehr.evidence.domain.EvidenceRequirement.EvidenceType;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.time.LocalDate;import java.util.List;
public final class EvidenceFrameworkDtos{private EvidenceFrameworkDtos(){}
 public record FrameworkInput(@NotBlank@Size(max=80)String code,@NotBlank@Size(max=200)String title,@Size(max=2000)String description,@NotBlank@Size(max=250)String issuingBody,@NotBlank@Size(max=2000)String legalBasis,@NotBlank@Size(max=250)String authorityReference,@NotBlank@Size(max=2000)String internalUseDisclaimer,Long recordVersion){}
 public record PillarInput(@NotBlank@Size(max=80)String code,@NotBlank@Size(max=200)String title,@Size(max=2000)String description,@Min(1)int displayOrder){}
 public record LevelInput(@NotBlank@Size(max=80)String code,@NotBlank@Size(max=200)String label,@NotBlank@Size(max=2000)String description,@Min(1)int displayOrder){}
 public record IndicatorInput(@NotBlank@Size(max=80)String pillarCode,@NotBlank@Size(max=80)String code,@NotBlank@Size(max=300)String title,@Size(max=2000)String description,@NotBlank@Size(max=4000)String evaluationGuidance,@Min(1)int displayOrder){}
 public record RequirementInput(@NotBlank@Size(max=80)String indicatorCode,@NotBlank@Size(max=80)String code,@NotBlank@Size(max=300)String title,@NotBlank@Size(max=4000)String evidenceGuidance,@NotNull EvidenceType evidenceType,@Min(1)Integer recurrenceMonths,@Min(1)Integer freshnessDays,boolean required,boolean allowsNotApplicable,@Min(1)int displayOrder){}
 public record ContentInput(@NotNull Long recordVersion,@NotEmpty List<@Valid PillarInput> pillars,@NotEmpty List<@Valid LevelInput> levels,@NotEmpty List<@Valid IndicatorInput> indicators,@NotEmpty List<@Valid RequirementInput> requirements){}
 public record Transition(@NotNull Long recordVersion,@NotBlank@Size(max=1000)String reason){}
 public record Publish(@NotNull Long recordVersion,@NotNull LocalDate effectiveFrom,LocalDate effectiveTo,@NotBlank@Size(max=1000)String reason){}
 public record PillarResponse(String id,String code,String title,String description,int displayOrder){}
 public record LevelResponse(String id,String code,String label,String description,boolean referenceOnly,int displayOrder){}
 public record IndicatorResponse(String id,String pillarCode,String code,String title,String description,String evaluationGuidance,int displayOrder){}
 public record RequirementResponse(String id,String indicatorCode,String code,String title,String evidenceGuidance,String evidenceType,Integer recurrenceMonths,Integer freshnessDays,boolean required,boolean allowsNotApplicable,int displayOrder){}
 public record FrameworkResponse(String id,String frameworkId,String code,String title,String description,String issuingBody,String legalBasis,String authorityReference,String internalUseDisclaimer,int contentRevision,int definitionVersion,String supersedesId,String status,LocalDate effectiveFrom,LocalDate effectiveTo,long recordVersion,List<PillarResponse>pillars,List<LevelResponse>levels,List<IndicatorResponse>indicators,List<RequirementResponse>requirements){}
}
