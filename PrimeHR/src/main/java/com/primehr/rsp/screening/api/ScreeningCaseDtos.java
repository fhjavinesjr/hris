package com.primehr.rsp.screening.api;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.time.Instant; import java.util.List;
public final class ScreeningCaseDtos {private ScreeningCaseDtos(){}
 public record OpenCase(@Min(0)long applicationRecordVersion,@NotBlank String screenerEmployeeNo,@NotBlank String validatorEmployeeNo){}
 public record Successor(@Min(0)long recordVersion,@NotBlank String screenerEmployeeNo,@NotBlank String validatorEmployeeNo,@NotBlank String reason){}
 public record Assignments(@Min(0)long recordVersion,@NotBlank String screenerEmployeeNo,@NotBlank String validatorEmployeeNo){}
 public record Evidence(@NotBlank String type,@NotBlank String referenceId,@NotBlank String label,String staffDeclaration){}
 public record SaveFinding(@Min(0)long caseRecordVersion,@Min(0)long findingRecordVersion,@NotBlank String result,String remarks,@NotNull List<@Valid Evidence> evidence){}
 public record Submit(@Min(0)long recordVersion,@NotBlank String recommendation,String reasonCodeId,String internalExplanation,@NotBlank String applicantSafeReason){}
 public record Transition(@Min(0)long recordVersion,@NotBlank String reason){}
 public record Finalize(@Min(0)long recordVersion){}
 public record AdminOverride(@Min(0)long recordVersion,@NotBlank String outcome,String reasonCodeId,String internalExplanation,@NotBlank String applicantSafeReason,@NotBlank String overrideReason){}
 public record Assignment(String employeeNo,String role,boolean active){}
 public record EvidenceResponse(String id,String type,String referenceId,String label,String staffDeclaration){}
 public record Finding(String id,String criterionId,String code,String label,boolean mandatory,boolean disqualifying,boolean allowsNotApplicable,boolean requiresRemarks,boolean requiresEvidence,int displayOrder,String result,String remarks,boolean humanConfirmed,long recordVersion,List<EvidenceResponse> evidence){}
 public record Decision(String outcome,String reasonCode,String internalExplanation,String applicantSafeReason,String recommendedBy,String validatedBy,Instant decidedAt,boolean administratorOverride,String overrideReason){}
 public record CaseResponse(String id,String applicationId,String applicantId,String publicationId,String policyId,int policyDefinitionVersion,int caseRevision,String supersedesId,boolean current,String status,String recommendation,long recordVersion,String applicationSnapshot,String policySnapshot,List<Assignment> assignments,List<Finding> findings,Decision decision){}
}
