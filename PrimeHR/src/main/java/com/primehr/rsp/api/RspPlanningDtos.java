package com.primehr.rsp.api;

import com.primehr.rsp.domain.RecruitmentPlanStatus;
import com.primehr.rsp.domain.VacancyRequestStatus;
import com.primehr.rsp.domain.VacancyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class RspPlanningDtos {
    private RspPlanningDtos() {
    }

    public record CreatePlan(
            @NotBlank @Size(max = 100) String code,
            @NotBlank @Size(max = 200) String title,
            @NotNull LocalDate periodStart,
            @NotNull LocalDate periodEnd,
            @Size(max = 4000) String description) {
    }

    public record UpdatePlan(
            @NotBlank @Size(max = 200) String title,
            @NotNull LocalDate periodStart,
            @NotNull LocalDate periodEnd,
            @Size(max = 4000) String description,
            @NotNull Long recordVersion) {
    }

    public record Transition(
            @NotNull Long recordVersion,
            @Size(max = 1000) String reason) {
    }

    public record SaveVacancy(
            @NotNull VacancyType vacancyType,
            @NotNull Long plantillaId,
            @NotNull Long businessUnitId,
            LocalDate anticipatedVacancyDate,
            @Size(max = 80) String anticipatedReasonCode,
            @Size(max = 2000) String anticipatedExplanation,
            @Size(max = 500) String authorityReference,
            @NotBlank @Size(max = 80) String recruitmentPriority,
            LocalDate targetFillDate,
            @NotBlank @Size(max = 4000) String justification,
            Long recordVersion) {
    }

    public record VacancyResponse(
            String id,
            String planId,
            VacancyRequestStatus status,
            boolean active,
            VacancyType vacancyType,
            LocalDate anticipatedVacancyDate,
            String anticipatedReasonCode,
            String anticipatedExplanation,
            String authorityReference,
            String recruitmentPriority,
            LocalDate targetFillDate,
            String justification,
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
            Instant administrativeFetchedAt,
            boolean occupied,
            Long activeAppointmentId,
            LocalDateTime occupancyAssumptionDate,
            String hrmFingerprint,
            Instant hrmFetchedAt,
            String submittedBy,
            Instant submittedAt,
            String decidedBy,
            Instant decidedAt,
            long recordVersion) {
    }

    public record PlanResponse(
            String id,
            String code,
            String title,
            LocalDate periodStart,
            LocalDate periodEnd,
            String description,
            RecruitmentPlanStatus status,
            String submittedBy,
            Instant submittedAt,
            String approvedBy,
            Instant approvedAt,
            long recordVersion,
            List<VacancyResponse> vacancies) {
    }

    public record Readiness(
            Long plantillaId,
            boolean occupied,
            String administrativeFingerprint,
            String hrmFingerprint,
            String positionProfileId,
            int positionProfileVersion,
            boolean ready,
            List<String> blockers,
            Instant checkedAt) {
    }
}
