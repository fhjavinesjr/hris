package com.primehr.assessment.api;

import com.primehr.assessment.domain.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.List;

public final class AssessmentExecutionDtos {
    private AssessmentExecutionDtos() { }

    public record InboxItemResponse(String caseId, String assignmentId, String cycleCode, String cycleName,
            String toolName, String subjectEmployeeNo, String subjectDisplayName, AssessmentMethod method,
            AssessmentCaseStatus caseStatus, AssessorAssignmentStatus assignmentStatus,
            long caseRecordVersion, long assignmentRecordVersion) { }

    public record LevelOptionResponse(String id, String code, String label, int levelOrder) { }
    public record RequirementResponse(String competencyId, String competencyCode, String competencyName,
            int competencyDefinitionVersion, String requiredLevelId, String requiredLevelCode,
            String classification, String criticality, List<LevelOptionResponse> levelOptions) { }

    public record EvidenceResponse(String id, String evidenceType, String titleReference,
            LocalDate evidenceDate, String description, String sourceSystem, String sourceReference,
            long recordVersion) { }

    public record RatingResponse(String id, String competencyId, String attainedLevelId,
            String attainedLevelCode, String remarks, String behavioralNotes, long recordVersion,
            List<EvidenceResponse> evidence) { }

    public record ContributionResponse(String assignmentId, AssessmentMethod method,
            String assessorEmployeeNo, String assessorDisplayName, AssessorAssignmentStatus status,
            long recordVersion, String submittedBy, Instant submittedAt, List<RatingResponse> ratings) { }

    public record AssessmentWorkResponse(String caseId, String cycleId, String cycleCode, String cycleName,
            String toolId, String toolName, String instructions, String subjectEmployeeNo,
            String subjectDisplayName, AssessmentCaseStatus caseStatus, long caseRecordVersion,
            Instant forValidationAt, List<RequirementResponse> requirements,
            List<ContributionResponse> contributions) { }

    public record SaveRatingRequest(@NotBlank String attainedLevelId,
            @Size(max = 2000) String remarks, @Size(max = 4000) String behavioralNotes,
            @PositiveOrZero Long recordVersion, @NotNull @PositiveOrZero Long assignmentRecordVersion,
            @NotNull @PositiveOrZero Long caseRecordVersion) { }

    public record CreateEvidenceRequest(@NotBlank String competencyVersionId,
            @NotBlank @Size(max = 100) String evidenceType,
            @NotBlank @Size(max = 500) String titleReference, @NotNull LocalDate evidenceDate,
            @Size(max = 4000) String description, @Size(max = 100) String sourceSystem,
            @Size(max = 500) String sourceReference,
            @NotNull @PositiveOrZero Long assignmentRecordVersion,
            @NotNull @PositiveOrZero Long caseRecordVersion) { }

    public record UpdateEvidenceRequest(@NotBlank @Size(max = 100) String evidenceType,
            @NotBlank @Size(max = 500) String titleReference, @NotNull LocalDate evidenceDate,
            @Size(max = 4000) String description, @Size(max = 100) String sourceSystem,
            @Size(max = 500) String sourceReference, @NotNull @PositiveOrZero Long recordVersion,
            @NotNull @PositiveOrZero Long assignmentRecordVersion,
            @NotNull @PositiveOrZero Long caseRecordVersion) { }

    public record WorkTransitionRequest(@NotNull @PositiveOrZero Long assignmentRecordVersion,
            @NotNull @PositiveOrZero Long caseRecordVersion, @PositiveOrZero Long recordVersion,
            @Size(max = 1000) String reason) { }

    public record ReturnCaseRequest(@NotNull @PositiveOrZero Long caseRecordVersion,
            @NotBlank @Size(max = 1000) String reason) { }

    public record ReturnCaseResponse(String caseId, AssessmentCaseStatus status,
            long caseRecordVersion, int returnedContributions) { }

}
