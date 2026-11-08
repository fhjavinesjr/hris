package com.primehr.succession.api;

import com.primehr.positionprofile.domain.PositionTargetType;
import com.primehr.succession.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.List;

public final class SuccessionPlanningDtos { private SuccessionPlanningDtos() { }
    public record PathLinkInput(@NotNull PositionTargetType sourceType,@NotNull Long sourceId,@NotNull PositionTargetType targetType,@NotNull Long targetId,@NotNull SuccessionCareerPathLink.RelationshipType relationshipType,@NotBlank @Size(max=4000) String developmentGuidance,@Min(1) int displayOrder) { }
    public record PathLinksInput(@NotNull Long recordVersion,@NotEmpty List<@Valid PathLinkInput> links) { }
    public record CareerFamilyInput(@NotBlank @Size(max=80) String code,@NotBlank @Size(max=200) String title,@Size(max=2000) String description,@NotBlank @Size(max=2000) String legalBasis,@NotBlank @Size(max=4000) String pathwayGuidance,Long recordVersion) { }
    public record PathLinkResponse(String id,String sourceType,Long sourceId,String sourcePositionName,Long sourcePlantillaId,String sourcePlantillaName,String targetType,Long targetId,String targetPositionName,Long targetPlantillaId,String targetPlantillaName,String relationshipType,String developmentGuidance,int displayOrder) { }
    public record CareerFamilyResponse(String id,String familyId,String code,String title,String description,String legalBasis,String pathwayGuidance,int linksRevision,int definitionVersion,String supersedesId,String status,LocalDate effectiveFrom,LocalDate effectiveTo,long recordVersion,List<PathLinkResponse> links) { }
    public record CriterionInput(@NotBlank @Size(max=80) String code,@NotBlank @Size(max=200) String label,@NotBlank @Size(max=2000) String description,@NotBlank @Size(max=4000) String evidenceGuidance,@Min(1) int displayOrder) { }
    public record CriteriaInput(@NotNull Long recordVersion,@NotEmpty List<@Valid CriterionInput> criteria) { }
    public record FrameworkInput(@NotBlank @Size(max=80) String code,@NotBlank @Size(max=200) String title,@Size(max=2000) String description,@NotBlank @Size(max=2000) String legalBasis,@NotBlank @Size(max=4000) String decisionGuidance,Long recordVersion) { }
    public record CriterionResponse(String id,String code,String label,String description,String evidenceGuidance,int displayOrder) { }
    public record FrameworkResponse(String id,String frameworkId,String code,String title,String description,String legalBasis,String decisionGuidance,int criteriaRevision,int definitionVersion,String supersedesId,String status,LocalDate effectiveFrom,LocalDate effectiveTo,long recordVersion,List<CriterionResponse> criteria) { }
    public record FindingInput(@NotBlank String criterionId,@NotNull SuccessionCriticalityFinding.Outcome outcome,@NotBlank @Size(max=2000) String reason,@Size(max=1000) String evidenceReference) { }
    public record DesignationInput(@NotNull PositionTargetType targetType,@NotNull Long targetId,@NotBlank String frameworkVersionId,@NotBlank String charterVersionId,@NotBlank @Size(max=200) String title,@NotBlank @Size(max=4000) String justification,@NotNull LocalDate assessmentDate,@NotNull LocalDate reviewDate,@NotEmpty List<@Valid FindingInput> findings,Long recordVersion) { }
    public record DesignationDecision(@NotNull Long recordVersion,@NotBlank @Size(max=2000) String reason,LocalDate effectiveFrom,LocalDate effectiveTo) { }
    public record FindingResponse(String id,String criterionId,String criterionCode,String criterionLabel,String outcome,String reason,String evidenceReference,int displayOrder) { }
    public record DesignationResponse(String id,String targetType,Long targetId,Long jobPositionId,String positionName,Long plantillaId,String plantillaName,String frameworkVersionId,int frameworkDefinitionVersion,String charterVersionId,String title,String justification,LocalDate assessmentDate,LocalDate effectiveFrom,LocalDate effectiveTo,LocalDate reviewDate,String status,String submittedBy,Instant submittedAt,String decisionBy,Instant decisionAt,String decisionReason,long recordVersion,List<FindingResponse> findings) { }
}
