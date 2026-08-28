package com.humanresource.integration.primehr;

import java.time.Instant;
import java.time.LocalDateTime;

public record AssessmentSubjectResponse(Long employeeId, String employeeNo, String displayName,
        boolean eligible, Long appointmentId, LocalDateTime assumptionToDutyDate, Long jobPositionId,
        Long plantillaId, String sourceFingerprint, LocalDateTime sourceUpdatedAt, Instant fetchedAt) {
}
