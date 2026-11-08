package com.primehr.learningdevelopment.completion.api;

import com.primehr.learningdevelopment.completion.domain.*;
import jakarta.validation.constraints.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public final class LdCompletionDtos {
    private LdCompletionDtos() {}
    public record OpenCaseInput(@NotNull Long enrollmentRecordVersion) {}
    public record AttendanceInput(@NotNull Long recordVersion,@NotNull LdSessionAttendance.Status status,
            @NotNull @DecimalMin("0.00") BigDecimal attendedHours,
            @NotNull @DecimalMin("0.00") BigDecimal creditedHours,@Size(max=2000)String remarks) {}
    public record OutputInput(@NotNull Long caseRecordVersion,@NotBlank @Size(max=80)String code,
            @NotBlank @Size(max=300)String title,@NotBlank @Size(max=3000)String description) {}
    public record OutputDecision(@NotNull Long caseRecordVersion,@NotNull Long outputRecordVersion,
            @Size(max=2000)String reason) {}
    public record CaseTransition(@NotNull Long recordVersion,@Size(max=3000)String reason) {}
    public record CompletionDecision(@NotNull Long recordVersion,@NotNull LdCompletionCase.Decision decision,
            @NotNull LocalDate completionDate,@NotBlank @Size(max=3000)String reason) {}
    public record AttendanceResponse(String id,long recordVersion,String sessionId,String sessionTitle,
            OffsetDateTime startsAt,OffsetDateTime endsAt,BigDecimal plannedHours,String status,
            BigDecimal attendedHours,BigDecimal creditedHours,String remarks,String recordedBy,Instant recordedAt) {}
    public record OutputResponse(String id,long recordVersion,String code,String title,String description,String status,
            String submittedBy,Instant submittedAt,String verifiedBy,Instant verifiedAt,String decisionReason) {}
    public record EvidenceResponse(String id,long recordVersion,String outputId,String evidenceType,String filename,
            String mediaType,long byteSize,String checksum,String certificateNumber,String certificateIssuer,
            LocalDate certificateIssueDate,String uploadedBy,Instant uploadedAt) {}
    public record EvidenceContent(String filename,String mediaType,long size,InputStream stream) {}
    public record CompletionCaseResponse(String id,long recordVersion,String enrollmentId,String trainingOrderId,
            String offeringId,int offeringVersion,Long employeeId,String employeeNo,String employeeName,
            Long appointmentId,BigDecimal plannedHours,BigDecimal creditedHours,String status,int contentRevision,
            String submittedBy,Instant submittedAt,String verifiedBy,Instant verifiedAt,LocalDate completionDate,
            String decisionReason,String completionFingerprint,List<AttendanceResponse> attendance,
            List<OutputResponse> outputs,List<EvidenceResponse> evidence) {}
}
