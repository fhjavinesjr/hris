package com.primehr.operations.api;
import jakarta.validation.constraints.*;import java.time.*;import java.util.List;
public final class OperationsDtos {private OperationsDtos(){}
 public record OperationsStatus(Instant checkedAt,String applicationStatus,List<DependencyStatus>dependencies){}
 public record DependencyStatus(String dependency,String status,Integer responseStatus,long durationMillis){}
 public record RunResponse(String id,String triggerType,String status,String startedBy,Instant startedAt,Instant completedAt,int checkedCount,int findingCount,String failureSummary,long recordVersion,List<FindingResponse>findings){}
 public record FindingResponse(String id,String runId,String kind,String status,String recordType,String recordId,String sourceId,Long storedVersion,String storedFingerprint,Long observedVersion,String observedFingerprint,String summary,String acknowledgedBy,Instant acknowledgedAt,String resolvedBy,Instant resolvedAt,String resolutionNote,long recordVersion){}
 public record FindingTransition(@NotNull Long recordVersion,@NotBlank @Size(max=2000)String note){}
 public record PolicyInput(@NotBlank @Size(max=100)String code,@Min(1)int policyVersion,@NotBlank @Size(max=100)String retentionTag,@Min(1)int retentionDays,@NotNull LocalDate effectiveFrom,LocalDate effectiveTo,@NotBlank @Size(max=1000)String basis,boolean published){}
 public record PolicyResponse(String id,String code,int policyVersion,String retentionTag,int retentionDays,LocalDate effectiveFrom,LocalDate effectiveTo,String basis,boolean published,long recordVersion){}
 public record HoldInput(@NotBlank @Size(max=80)String recordType,@NotBlank @Size(max=100)String recordId,@NotBlank @Size(max=2000)String reason,@NotBlank @Size(max=200)String referenceNo){}
 public record HoldResponse(String id,String recordType,String recordId,String reason,String referenceNo,String placedBy,Instant placedAt,boolean active,String releasedBy,Instant releasedAt,String releaseReason,long recordVersion){}
 public record RetentionEligibility(String recordType,String recordId,String retentionTag,LocalDate recordDate,LocalDate eligibleOn,boolean policyFound,boolean legalHold,boolean eligible,String explanation){}
}
