package com.primehr.rsp.api;

import com.primehr.rsp.domain.VacancyPublicationStatus;
import com.primehr.rsp.domain.VacancyVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class RspPublicationDtos {
    private RspPublicationDtos() {
    }

    public record ChannelInput(
            @NotBlank @Size(max = 200) String channelName,
            @NotNull LocalDate publicationDate,
            @Size(max = 1000) String reference) {
    }

    public record CreatePublication(
            @NotBlank String vacancyRequestId,
            @NotNull VacancyVisibility visibility,
            @NotNull LocalDate openingDate,
            @NotNull LocalDate closingDate,
            @NotBlank @Size(max = 4000) String instructions,
            @NotBlank @Size(max = 2000) String contactGuidance,
            @NotBlank @Size(max = 4000) String noticeText,
            @Valid List<ChannelInput> channels,
            Long recordVersion) {
    }

    public record UpdatePublication(
            @NotNull VacancyVisibility visibility,
            @NotNull LocalDate openingDate,
            @NotNull LocalDate closingDate,
            @NotBlank @Size(max = 4000) String instructions,
            @NotBlank @Size(max = 2000) String contactGuidance,
            @NotBlank @Size(max = 4000) String noticeText,
            @Valid List<ChannelInput> channels,
            @NotNull Long recordVersion) {
    }

    public record PublicationTransition(
            @NotNull Long recordVersion,
            @Size(max = 1000) String reason) {
    }

    public record ChannelResponse(
            String id,
            String channelName,
            LocalDate publicationDate,
            String reference,
            boolean active,
            long recordVersion) {
    }

    public record RequirementSnapshotResponse(
            String id,
            String competencyVersionId,
            String competencyCode,
            String competencyName,
            int competencyDefinitionVersion,
            String requiredLevelId,
            String requiredLevelCode,
            String requiredLevelLabel,
            String classification,
            String criticalityCode,
            String remarks,
            int displayOrder) {
    }

    public record PublicationResponse(
            String id,
            String vacancyRequestId,
            String recruitmentPlanId,
            VacancyPublicationStatus status,
            VacancyVisibility visibility,
            LocalDate openingDate,
            LocalDate closingDate,
            String instructions,
            String placeOfAssignment,
            String contactGuidance,
            String noticeText,
            Long plantillaId,
            String plantillaName,
            Long jobPositionId,
            String jobPositionName,
            Long salaryGrade,
            Long salaryStep,
            Long businessUnitId,
            String businessUnitCode,
            String businessUnitName,
            Long qualificationStandardId,
            int qualificationStandardVersion,
            String educationRequirement,
            String trainingRequirement,
            String experienceRequirement,
            String eligibilityRequirement,
            String licenseRequirement,
            String qualificationSourceBasis,
            String positionProfileId,
            int positionProfileDefinitionVersion,
            long positionProfileRecordRevision,
            String administrativeFingerprint,
            String hrmFingerprint,
            Instant sourceSnapshotAt,
            String submittedBy,
            Instant submittedAt,
            String approvedBy,
            Instant approvedAt,
            String publishedBy,
            Instant publishedAt,
            String closedBy,
            Instant closedAt,
            String cancelledBy,
            Instant cancelledAt,
            long recordVersion,
            List<ChannelResponse> channels,
            List<RequirementSnapshotResponse> requirements) {
    }
}
