package com.primehr.performancemanagement.api;

import com.primehr.performancemanagement.domain.PerformanceRatingScaleVersion.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.List;

public final class PerformanceRatingScaleDtos { private PerformanceRatingScaleDtos() {}
 public record ScaleInput(@NotBlank @Size(max=80)String code,@NotBlank @Size(max=200)String title,
   @Size(max=2000)String description,@NotBlank @Size(max=1000)String legalBasis,
   @NotBlank @Size(max=36)String policyVersionId,@NotNull BigDecimal minimumScore,@NotNull BigDecimal maximumScore,
   @Min(0)@Max(6)int roundingScale,@NotNull RoundingMode roundingMode,@NotNull MeasureType measureType,
   @NotNull Direction direction,@NotNull DimensionScoreSource dimensionScoreSource,Long recordVersion){}
 public record BandInput(@NotBlank @Size(max=80)String code,@NotNull BigDecimal numericScore,
   @NotBlank @Size(max=200)String label,@NotNull BigDecimal lowerBound,@NotNull BigDecimal upperBound,
   @Min(1)int displayOrder,@Size(max=2000)String guidance){}
 public record BandsInput(@NotNull Long recordVersion,@NotEmpty List<@Valid BandInput> bands){}
 public record Transition(@NotNull Long recordVersion,@NotBlank @Size(max=1000)String reason){}
 public record PublishTransition(@NotNull Long recordVersion,@NotNull LocalDate effectiveFrom,LocalDate effectiveTo,
   @NotBlank @Size(max=1000)String reason){}
 public record PreviewInput(@NotNull BigDecimal sampleValue){}
 public record BandResponse(String id,String code,BigDecimal numericScore,String label,BigDecimal lowerBound,
   BigDecimal upperBound,int displayOrder,String guidance){}
 public record ScaleResponse(String id,String ratingScaleId,String code,String title,String description,String legalBasis,
   String policyVersionId,int definitionVersion,String supersedesId,String status,BigDecimal minimumScore,
   BigDecimal maximumScore,int roundingScale,String roundingMode,String measureType,String direction,String aggregation,
   String missingValuePolicy,String dimensionScoreSource,LocalDate effectiveFrom,LocalDate effectiveTo,String publishedBy,
   Instant publishedAt,String retiredBy,Instant retiredAt,String retirementReason,long recordVersion,List<BandResponse> bands){}
 public record PreviewResponse(BigDecimal inputValue,BigDecimal roundedScore,BigDecimal roundingUnit,
   String matchedBandCode,String matchedBandLabel,BigDecimal numericScore,String calculation){}
}
