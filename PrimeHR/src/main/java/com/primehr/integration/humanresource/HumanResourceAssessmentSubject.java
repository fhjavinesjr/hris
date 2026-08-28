package com.primehr.integration.humanresource;

import java.time.Instant;
import java.time.LocalDateTime;

public record HumanResourceAssessmentSubject(Long employeeId, String employeeNo, String displayName,
        boolean eligible, Long appointmentId, LocalDateTime assumptionToDutyDate, Long jobPositionId,
        Long plantillaId, String sourceFingerprint, LocalDateTime sourceUpdatedAt, Instant fetchedAt) {
}
