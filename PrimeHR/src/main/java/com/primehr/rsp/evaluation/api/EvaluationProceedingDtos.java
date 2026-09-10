package com.primehr.rsp.evaluation.api;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.time.*; import java.util.*;
public final class EvaluationProceedingDtos {private EvaluationProceedingDtos(){}
 public record CreateProceeding(@NotBlank String publicationId,@NotNull Long publicationRecordVersion,@NotBlank String committeeId,@NotNull Long committeeRecordVersion,@NotNull LocalDate plannedAsOfDate){}
 public record CandidateInput(@NotBlank String applicationId,@NotNull Long applicationRecordVersion,@NotNull Long screeningCaseRecordVersion){}
 public record AdmitCandidates(@NotNull Long proceedingRecordVersion,@NotEmpty List<@Valid CandidateInput> candidates){}
 public record Transition(@NotNull Long recordVersion,@Size(max=2000)String reason){}
 public record CandidateResponse(String id,String applicationId,String applicantId,String screeningCaseId,String status,int applicationVersion,int screeningCaseRevision,String applicationFingerprint,String screeningFingerprint,String admittedBy,Instant admittedAt,long recordVersion){}
 public record ProceedingResponse(String id,String publicationId,String evaluationPolicyId,int evaluationPolicyDefinitionVersion,String committeeId,int committeeDefinitionVersion,LocalDate plannedAsOfDate,String status,int candidateSetRevision,String vacancyFingerprint,String policyFingerprint,String committeeFingerprint,String vacancySnapshot,String policySnapshot,String committeeSnapshot,String openedBy,Instant openedAt,String cancelledBy,Instant cancelledAt,String cancellationReason,long recordVersion,List<CandidateResponse> candidates){}
}
