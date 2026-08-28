package com.primehr.assessment.api;

import com.primehr.assessment.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class AssessmentDtos {
    private AssessmentDtos() { }

    public record CreateCycleRequest(@NotBlank @Size(max = 100) String code,
            @NotBlank @Size(max = 200) String name, @Size(max = 2000) String description,
            LocalDate effectiveFrom, LocalDate effectiveTo) { }

    public record UpdateCycleRequest(@NotBlank @Size(max = 200) String name,
            @Size(max = 2000) String description, LocalDate effectiveFrom, LocalDate effectiveTo,
            @NotNull @PositiveOrZero Long recordVersion) { }

    public record TransitionRequest(@NotNull @PositiveOrZero Long recordVersion,
            @Size(max = 1000) String reason) { }

    public record MethodRequest(@NotNull AssessmentMethod method, boolean evidenceRequired) { }

    public record CreateToolRequest(@NotBlank String positionProfileId,
            @NotBlank @Size(max = 200) String name, @Size(max = 4000) String instructions,
            @NotEmpty List<@Valid MethodRequest> methods) { }

    public record UpdateToolRequest(@NotBlank @Size(max = 200) String name,
            @Size(max = 4000) String instructions, @NotEmpty List<@Valid MethodRequest> methods,
            @NotNull @PositiveOrZero Long recordVersion) { }

    public record AddSubjectRequest(@NotNull @Positive Long employeeId,
            @NotNull @PositiveOrZero Long toolRecordVersion) { }

    public record AddAssessorRequest(@NotNull AssessmentMethod method,
            @NotNull @Positive Long employeeId, @Size(max = 1000) String reason,
            @NotNull @PositiveOrZero Long caseRecordVersion) { }

    public record UpdateAssessorRequest(@Size(max = 1000) String reason,
            @NotNull @PositiveOrZero Long recordVersion,
            @NotNull @PositiveOrZero Long caseRecordVersion) { }

    public record CycleResponse(String id, String code, String name, String description,
            AssessmentCycleStatus status, LocalDate effectiveFrom, LocalDate effectiveTo,
            long recordVersion, String createdBy, Instant createdAt, String updatedBy, Instant updatedAt,
            String openedBy, Instant openedAt, String closedBy, Instant closedAt) { }

    public record MethodResponse(String id, AssessmentMethod method, boolean evidenceRequired,
            long recordVersion) { }

    public record ToolResponse(String id, String cycleId, String positionProfileId, String name,
            String instructions, AssessmentToolStatus status, int profileDefinitionVersion,
            long profileContentRevision, String profileTargetKey, String profileName,
            String profileSourceFingerprint, LocalDate effectiveFrom, LocalDate effectiveTo,
            long recordVersion, String publishedBy, Instant publishedAt, List<MethodResponse> methods) { }

    public record AssignmentResponse(String id, AssessmentMethod method, Long assessorEmployeeId,
            String assessorEmployeeNo, String assessorDisplayName, String reason,
            String assessorSourceFingerprint, Instant assessorSnapshotAt,
            AssessorAssignmentStatus status, boolean active, long recordVersion,
            String submittedBy, Instant submittedAt) { }

    public record CaseResponse(String id, String toolId, Long subjectEmployeeId, String subjectEmployeeNo,
            String subjectDisplayName, Long appointmentId, LocalDateTime assumptionToDutyDate,
            Long jobPositionId, Long plantillaId, String subjectSourceFingerprint,
            LocalDateTime subjectSourceUpdatedAt, Instant subjectSnapshotAt, AssessmentCaseStatus status,
            boolean active, long recordVersion, Instant forValidationAt, List<AssignmentResponse> assessors) { }
}
