package com.primehr.performancemanagement.api;

import com.primehr.performancemanagement.domain.PerformanceIndicatorDimension;
import com.primehr.performancemanagement.domain.PerformanceIndicatorDimensionLevel;
import com.primehr.performancemanagement.domain.PerformanceRatingScaleVersion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class PerformanceSuccessIndicatorDtos {
    private PerformanceSuccessIndicatorDtos() {}
    public record IndicatorInput(@NotBlank @Size(max=80) String code,@NotBlank @Size(max=200) String title,
        @NotBlank @Size(max=2000) String outputDescription,@NotBlank @Size(max=500) String keyResultArea,
        @NotBlank @Size(max=1000) String performanceIndicator,@NotBlank @Size(max=2000) String successStatement,
        @NotBlank @Size(max=36) String policyVersionId,@NotBlank @Size(max=36) String ratingScaleVersionId,
        @NotNull PerformanceRatingScaleVersion.MeasureType measureType,@NotBlank @Size(max=100) String unitOfMeasure,
        @NotNull PerformanceRatingScaleVersion.Direction direction,BigDecimal targetValue,BigDecimal targetFrom,
        BigDecimal targetTo,@Size(max=2000) String evidenceRequirements,@Size(max=2000) String description,
        boolean requiresEvidence,Long recordVersion) {
        public IndicatorInput(String code,String title,String outputDescription,String keyResultArea,
            String performanceIndicator,String successStatement,String policyVersionId,String ratingScaleVersionId,
            PerformanceRatingScaleVersion.MeasureType measureType,String unitOfMeasure,
            PerformanceRatingScaleVersion.Direction direction,BigDecimal targetValue,BigDecimal targetFrom,
            BigDecimal targetTo,String evidenceRequirements,Long recordVersion) {
            this(code,title,outputDescription,keyResultArea,performanceIndicator,successStatement,policyVersionId,
                ratingScaleVersionId,measureType,unitOfMeasure,direction,targetValue,targetFrom,targetTo,
                evidenceRequirements,null,true,recordVersion);
        }
    }
    public record LevelInput(@NotBlank @Size(max=36) String ratingBandId,
        @NotNull PerformanceIndicatorDimensionLevel.Operator operator,BigDecimal thresholdFrom,
        BigDecimal thresholdTo,@Size(max=2000) String manualCriteria) {}
    public record DimensionInput(@NotNull PerformanceIndicatorDimension.Type type,@NotBlank @Size(max=80) String code,
        @NotBlank @Size(max=200) String label,@NotNull @DecimalMin("0.0001") BigDecimal weightPercent,
        @Min(1) int displayOrder,@NotEmpty List<@Valid LevelInput> levels) {
        public DimensionInput(PerformanceIndicatorDimension.Type type,String label,BigDecimal weightPercent,
            int displayOrder,List<LevelInput> levels) {
            this(type,type==null?null:type.name(),label,weightPercent,displayOrder,levels);
        }
    }
    public record DimensionsInput(@NotNull Long recordVersion,@NotEmpty List<@Valid DimensionInput> dimensions) {}
    public record Transition(@NotNull Long recordVersion,@NotBlank @Size(max=1000) String reason,
        LocalDate effectiveFrom,LocalDate effectiveTo) {
        public Transition(Long recordVersion,String reason){this(recordVersion,reason,null,null);}
    }
    public record DimensionActual(@NotBlank String dimensionCode,BigDecimal actualValue,
        @Size(max=36) String manualBandId) {
        public DimensionActual(PerformanceIndicatorDimension.Type type,BigDecimal actualValue,String manualBandId){
            this(type==null?null:type.name(),actualValue,manualBandId);
        }
    }
    public record PreviewInput(@NotEmpty List<@Valid DimensionActual> actuals) {}
    public record LevelResponse(String id,String ratingBandId,String operator,BigDecimal thresholdFrom,
        BigDecimal thresholdTo,String manualCriteria) {}
    public record DimensionResponse(String id,String type,String code,String label,BigDecimal weightPercent,
        int displayOrder,List<LevelResponse> levels) {}
    public record IndicatorResponse(String id,String indicatorId,String code,String title,String description,
        String outputDescription,String keyResultArea,String performanceIndicator,String successStatement,
        String policyVersionId,String ratingScaleVersionId,String measureType,String unitOfMeasure,String direction,
        BigDecimal targetValue,BigDecimal targetFrom,BigDecimal targetTo,boolean requiresEvidence,
        String evidenceRequirements,int definitionVersion,String supersedesId,String status,
        LocalDate effectiveFrom,LocalDate effectiveTo,long recordVersion,List<DimensionResponse> dimensions) {}
    public record DimensionResult(String type,String code,BigDecimal score,BigDecimal weightPercent,
        BigDecimal contribution,String ratingBandId) {}
    public record PreviewResponse(BigDecimal indicatorScore,List<DimensionResult> dimensions,String formula) {}
}
