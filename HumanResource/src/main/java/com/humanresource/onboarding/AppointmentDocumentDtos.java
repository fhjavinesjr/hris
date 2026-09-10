package com.humanresource.onboarding;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.time.LocalDate;

public final class AppointmentDocumentDtos {
    private AppointmentDocumentDtos() {}

    public enum DocumentKind { OATH_OF_OFFICE, ASSUMPTION_TO_DUTY }

    public record CreateDocumentCommand(
            @NotNull DocumentKind kind,
            @NotNull LocalDate issueDate,
            LocalDate oathDate,
            LocalDate assumptionDate,
            @NotBlank String venue,
            Long administeringEmployeeId,
            Long certifyingEmployeeId,
            Long attestingEmployeeId,
            String supersedesDocumentId,
            String reason) {}

    public record UpdateDocumentCommand(
            @NotNull LocalDate issueDate,
            LocalDate oathDate,
            LocalDate assumptionDate,
            @NotBlank String venue,
            Long administeringEmployeeId,
            Long certifyingEmployeeId,
            Long attestingEmployeeId,
            @PositiveOrZero long recordVersion) {}

    public record FinalizeDocumentCommand(
            @PositiveOrZero long recordVersion,
            @NotBlank String reason) {}

    public record DocumentResponse(
            String id,
            Long appointmentId,
            String onboardingCaseId,
            DocumentKind kind,
            String status,
            String officialTemplateCode,
            String officialTemplateVersion,
            LocalDate issueDate,
            LocalDate oathDate,
            LocalDate assumptionDate,
            String venue,
            Long administeringEmployeeId,
            Long certifyingEmployeeId,
            Long attestingEmployeeId,
            String supersedesDocumentId,
            String sourceFingerprint,
            long recordVersion,
            String finalizedBy,
            Instant finalizedAt) {}
}
