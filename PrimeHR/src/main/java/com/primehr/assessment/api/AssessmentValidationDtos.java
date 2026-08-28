package com.primehr.assessment.api;
import com.primehr.assessment.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.List;
public final class AssessmentValidationDtos {
 private AssessmentValidationDtos(){}
 public record ExpectedContribution(@NotBlank String assignmentId,@NotNull @PositiveOrZero Long recordVersion){}
 public record FinalDecision(@NotBlank String competencyVersionId,@NotBlank String finalLevelId,
   @Size(max=2000) String remarks,@NotEmpty List<@NotBlank String> contributingAssignmentIds){}
 public record ValidateCaseRequest(@NotNull @PositiveOrZero Long caseRecordVersion,@NotNull LocalDate validFrom,
   LocalDate reassessmentDate,@Size(max=4000) String validationRemarks,boolean administratorOverride,
   @Size(max=1000) String overrideReason,@NotEmpty List<@Valid ExpectedContribution> expectedContributions,
   @NotEmpty List<@Valid FinalDecision> decisions){}
 public record ValidationListItem(String caseId,String cycleName,String toolName,String subjectEmployeeNo,
   String subjectDisplayName,Instant submittedAt,long caseRecordVersion){}
 public record ValidationPage(List<ValidationListItem> content,int page,int size,long totalElements,int totalPages){}
 public record ValidationCaseResponse(String caseId,AssessmentCaseStatus status,String subjectEmployeeNo,
   String subjectDisplayName,long caseRecordVersion,List<AssessmentExecutionDtos.RequirementResponse> requirements,
   List<AssessmentExecutionDtos.ContributionResponse> contributions){}
 public record ValidatedDecisionResponse(String competencyId,String competencyCode,String finalLevelId,
   String finalLevelCode,String remarks,List<String> contributingAssignmentIds){}
 public record ValidationResultResponse(String validationId,String caseId,String personProfileVersionId,
   int profileVersion,AssessmentCaseStatus status,Instant validatedAt,long caseRecordVersion,
   List<ValidatedDecisionResponse> decisions){}
 public record PersonResultResponse(String competencyId,String competencyCode,String competencyName,
   String attainedLevelId,String attainedLevelCode,String attainedLevelLabel,String validationRemarks){}
 public record PersonProfileResponse(String id,String subjectEmployeeNo,String subjectDisplayName,int profileVersion,
   LocalDate validFrom,LocalDate validTo,LocalDate reassessmentDate,String status,Instant validatedAt,
   String predecessorId,String cycleId,String toolId,String positionProfileId,int positionProfileDefinitionVersion,
   long positionProfileContentRevision,List<PersonResultResponse> results){}
}
