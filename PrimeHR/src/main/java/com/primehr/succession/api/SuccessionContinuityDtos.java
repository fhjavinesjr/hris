package com.primehr.succession.api;

import com.primehr.succession.domain.SuccessionRspReferralPacket;
import jakarta.validation.constraints.*;

import java.time.*;
import java.util.List;

public final class SuccessionContinuityDtos {
    private SuccessionContinuityDtos() { }
    public record PlanInput(@NotNull Long recordVersion,@NotBlank @Size(max=4000)String criticalFunctions,@NotBlank @Size(max=4000)String interimArrangement,@NotBlank @Size(max=4000)String continuityActions,@NotBlank @Size(max=4000)String activationTriggers,@Min(0)int targetRecoveryDays,@NotNull LocalDate reviewOn){}
    public record VersionInput(@NotNull Long recordVersion){}
    public record DecisionInput(@NotNull Long recordVersion,@NotBlank @Size(max=4000)String reason){}
    public record BenchResponse(String id,String candidateId,String poolDecisionId,String membershipEventId,String employeeNo,String employeeName,String readinessLevelCode,LocalDate membershipEffectiveOn,LocalDate membershipExpiresOn,String developmentPlanId,String developmentStatus,Instant snapshotAt){}
    public record PlanResponse(String id,long recordVersion,String riskReviewId,String cycleId,String designationId,String targetType,Long targetId,Long jobPositionId,String positionName,Long plantillaId,String plantillaName,String riskLevel,String riskReasonSnapshot,String criticalFunctions,String interimArrangement,String continuityActions,String activationTriggers,int targetRecoveryDays,LocalDate reviewOn,String status,String coverageStatus,int benchCount,String submittedBy,Instant submittedAt,String approvedBy,Instant approvedAt,String approvalReason,String finalizedBy,Instant finalizedAt,List<BenchResponse>benchCoverage){}
    public record ReferralCreate(@NotNull Long planRecordVersion,@NotBlank String benchEntryId,@NotBlank @Size(max=4000)String referralReason,@NotBlank @Size(max=4000)String employeeVisibleNotice,@NotBlank @Size(max=4000)String restrictedRationale,String vacancyPublicationId){}
    public record ReferralUpdate(@NotNull Long recordVersion,@NotBlank @Size(max=4000)String referralReason,@NotBlank @Size(max=4000)String employeeVisibleNotice,@NotBlank @Size(max=4000)String restrictedRationale){}
    public record VacancyLink(@NotNull Long recordVersion,@NotBlank String vacancyPublicationId){}
    public record ReferralTransition(@NotNull Long recordVersion,@NotBlank @Size(max=4000)String employeeVisibleReason,@NotBlank @Size(max=4000)String restrictedReason){}
    public record ReferralStatus(@NotNull Long recordVersion,@NotNull SuccessionRspReferralPacket.Status status,@NotBlank @Size(max=4000)String employeeVisibleReason,@NotBlank @Size(max=4000)String restrictedReason){}
    public record ReferralEventResponse(String id,String previousEventId,int sequence,String eventType,String resultingStatus,String employeeVisibleReason,String restrictedReason,String recordedBy,Instant recordedAt){}
    public record ReferralResponse(String id,long recordVersion,String referralCode,String continuityPlanId,String benchEntryId,String candidateId,String employeeNo,String employeeName,String readinessLevelCode,Long targetJobPositionId,String targetPositionName,Long targetPlantillaId,String targetPlantillaName,LocalDate membershipExpiresOn,String referralReason,String employeeVisibleNotice,String restrictedRationale,String vacancyPublicationId,String vacancyRequestId,String vacancyStatus,Long vacancyRecordVersion,String vacancyFingerprint,Instant vacancyFetchedAt,String status,String createdBy,String authorizedBy,Instant authorizedAt,String sentBy,Instant sentAt,String receivedBy,Instant receivedAt,String statusReason,List<ReferralEventResponse>events){}
}
