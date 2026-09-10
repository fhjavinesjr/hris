package com.primehr.performancemanagement.api;

import com.primehr.performancemanagement.domain.PerformanceTemplateSection;
import com.primehr.performancemanagement.domain.PerformanceTemplateVersion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class PerformanceTemplateDtos {
    private PerformanceTemplateDtos() {}
    public record TemplateInput(@NotBlank @Size(max=80) String code,@NotBlank @Size(max=200) String title,
        @Size(max=2000) String description,@NotNull PerformanceTemplateVersion.FormType formType,
        @NotBlank @Size(max=36) String policyVersionId,@NotBlank @Size(max=36) String ratingScaleVersionId,
        @NotBlank @Size(max=200) String formLabel,@NotBlank @Size(max=1000) String legalBasis,Long recordVersion) {
        public TemplateInput(String code,String title,String description,PerformanceTemplateVersion.FormType formType,
            String policyVersionId,String ratingScaleVersionId,Long recordVersion){
            this(code,title,description,formType,policyVersionId,ratingScaleVersionId,title,"Not specified",recordVersion);
        }
    }
    public record ItemInput(@NotBlank @Size(max=36) String indicatorVersionId,@Size(max=200) String labelOverride,
        @NotNull @DecimalMin("0.0001") BigDecimal weightPercent,boolean required,
        @Size(max=2000) String evidenceOverride,@Min(1) int displayOrder) {
        public ItemInput(String indicatorVersionId,BigDecimal weightPercent,int displayOrder){
            this(indicatorVersionId,null,weightPercent,true,null,displayOrder);
        }
    }
    public record SectionInput(@NotNull PerformanceTemplateSection.Type type,@NotBlank @Size(max=80) String code,
        @NotBlank @Size(max=200) String title,@Size(max=2000) String description,
        @NotNull @DecimalMin("0.0001") BigDecimal weightPercent,@Min(1) int displayOrder,
        @NotEmpty List<@Valid ItemInput> items) {
        public SectionInput(PerformanceTemplateSection.Type type,String title,BigDecimal weightPercent,
            int displayOrder,List<ItemInput> items){
            this(type,type==null?null:type.name(),title,null,weightPercent,displayOrder,items);
        }
    }
    public record StructureInput(@NotNull Long recordVersion,@NotEmpty List<@Valid SectionInput> sections) {}
    public record Transition(@NotNull Long recordVersion,@NotBlank @Size(max=1000) String reason,
        LocalDate effectiveFrom,LocalDate effectiveTo) {
        public Transition(Long recordVersion,String reason){this(recordVersion,reason,null,null);}
    }
    public record ItemResponse(String id,String indicatorVersionId,String labelOverride,BigDecimal weightPercent,
        boolean required,String evidenceOverride,int displayOrder) {}
    public record SectionResponse(String id,String type,String code,String title,String description,
        BigDecimal weightPercent,int displayOrder,List<ItemResponse> items) {}
    public record TemplateResponse(String id,String templateId,String code,String title,String description,
        String formType,String formLabel,String legalBasis,String policyVersionId,String ratingScaleVersionId,
        String coverageScope,String aggregation,String missingValuePolicy,int definitionVersion,String supersedesId,
        String status,LocalDate effectiveFrom,LocalDate effectiveTo,long recordVersion,List<SectionResponse> sections) {}
    public record ReadinessResponse(boolean ready,List<String> errors) {}
    public record ItemScore(@NotBlank String itemId,@NotNull BigDecimal indicatorScore) {}
    public record PreviewInput(@NotEmpty List<@Valid ItemScore> scores) {}
    public record SectionScore(String sectionId,BigDecimal score,BigDecimal contribution) {}
    public record PreviewResponse(BigDecimal overallScore,String ratingBandId,String ratingLabel,
        List<SectionScore> sections,String formula) {}
}
