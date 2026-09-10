package com.humanresource.integration.primehr;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class AppointmentHandoffDtos {
    private AppointmentHandoffDtos() {
    }

    public record Request(
            @Min(1) int schemaVersion,
            @NotBlank String handoffId,
            @NotBlank String agencyId,
            @NotBlank String selectionId,
            @NotBlank String applicationId,
            @NotBlank String applicantId,
            @NotBlank String sourceFingerprint,
            @NotNull JsonNode payloadSnapshot,
            String correlationId,
            @NotBlank String sourceActor) {
    }

    public record Receipt(
            String receiptId,
            String handoffId,
            String agencyId,
            int schemaVersion,
            String sourceFingerprint,
            String selectionId,
            String applicationId,
            String applicantId,
            String state,
            JsonNode payloadSnapshot,
            Instant receivedAt,
            String correlationId,
            String sourceActor,
            long recordVersion,
            Instant updatedAt) {
    }
}
