package com.humanresource.integration.primehr;

import java.time.Instant;

public record AppointmentHandoffReceiptRecord(
        String id,
        String agencyId,
        String handoffId,
        int schemaVersion,
        String sourceFingerprint,
        String selectionId,
        String applicationId,
        String applicantId,
        String state,
        String payloadSnapshot,
        Instant receivedAt,
        String correlationId,
        String sourceActor,
        long recordVersion,
        Instant updatedAt) {
}
