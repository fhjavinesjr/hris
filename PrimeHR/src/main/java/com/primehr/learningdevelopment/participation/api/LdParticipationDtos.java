package com.primehr.learningdevelopment.participation.api;

import com.primehr.learningdevelopment.participation.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public final class LdParticipationDtos {
    private LdParticipationDtos() {}

    public record Transition(@NotNull Long recordVersion, @Size(max=2000) String reason) {}
    public record NominationCreate(@NotBlank String offeringId, @NotNull @Positive Long employeeId,
            @NotNull LdNomination.SourceType sourceType, String sourceId,
            @NotBlank @Size(max=3000) String justification,
            @NotNull @DecimalMin("0.00") BigDecimal estimatedCost) {}
    public record NominationUpdate(@NotNull Long recordVersion,
            @NotBlank @Size(max=3000) String justification,
            @NotNull @DecimalMin("0.00") BigDecimal estimatedCost) {}
    public record RouteStepResponse(int level,String action,String employeeNo,String employeeName,String status,
            String decidedBy,Instant decidedAt,String reason) {}
    public record NominationResponse(String id,long recordVersion,String offeringId,int offeringVersion,
            String offeringTitle,OffsetDateTime scheduleStart,OffsetDateTime scheduleEnd,String sourceType,
            String sourceId,String justification,Long employeeId,String employeeNo,String employeeName,
            Long appointmentId,Long businessUnitId,String businessUnitName,BigDecimal estimatedCost,
            String initiatorType,String initiatedBy,String status,int contentRevision,String submittedBy,
            Instant submittedAt,String approvedBy,Instant approvedAt,String returnReason,List<RouteStepResponse> route) {}
    public record EnrollmentResponse(String id,long recordVersion,String nominationId,String offeringId,
            int offeringVersion,Long employeeId,String employeeNo,String employeeName,Long appointmentId,
            String status,Instant confirmedAt,Instant cancelledAt,String statusReason,
            String replacesEnrollmentId,String replacedByEnrollmentId) {}
    public record SubstitutionInput(@NotNull Long recordVersion,@NotBlank String replacementEnrollmentId,
            @NotNull Long replacementRecordVersion,@NotBlank @Size(max=2000) String reason) {}
    public record TrainingOrderInput(@NotBlank @Size(max=100) String orderNumber,
            @NotBlank @Size(max=1000) String subject,
            @NotBlank @Size(max=3000) String attendanceDirective,
            @Size(max=1000) String dressCode,@Size(max=2000) String notes) {}
    public record TrainingOrderParticipantResponse(String enrollmentId,long enrollmentVersion,Long employeeId,
            String employeeNo,String employeeName,Long appointmentId,String enrollmentStatus,int displayOrder) {}
    public record TrainingOrderResponse(String id,long recordVersion,String offeringId,int offeringVersion,
            String orderNumber,String subject,String attendanceDirective,String dressCode,String notes,
            String approvedBy,Instant approvedAt,String snapshotFingerprint,
            List<TrainingOrderParticipantResponse> participants) {}
}
