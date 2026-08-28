package com.primehr.assessment.domain;

import java.time.Instant;
import java.time.LocalDateTime;

public record AssessmentSubjectSnapshot(Long employeeId, String employeeNo, String displayName, Long appointmentId,
        LocalDateTime assumptionToDutyDate, Long jobPositionId, Long plantillaId, String sourceFingerprint,
        LocalDateTime sourceUpdatedAt, Instant capturedAt) {
    public AssessmentSubjectSnapshot {
        if (employeeId == null || employeeId < 1) throw new IllegalArgumentException("employeeId is required");
        if (appointmentId == null || appointmentId < 1) throw new IllegalArgumentException("appointmentId is required");
        if (jobPositionId == null || jobPositionId < 1) throw new IllegalArgumentException("jobPositionId is required");
        employeeNo = require(employeeNo, "employeeNo");
        displayName = require(displayName, "displayName");
        sourceFingerprint = require(sourceFingerprint, "sourceFingerprint");
        if (assumptionToDutyDate == null || capturedAt == null) {
            throw new IllegalArgumentException("appointment and snapshot timestamps are required");
        }
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}
