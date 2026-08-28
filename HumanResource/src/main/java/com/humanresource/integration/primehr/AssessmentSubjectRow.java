package com.humanresource.integration.primehr;

import java.time.LocalDateTime;

public record AssessmentSubjectRow(Long employeeId, String employeeNo, String firstname, String lastname,
                                   String suffix, LocalDateTime employeeUpdatedAt, Long appointmentId,
                                   LocalDateTime assumptionToDutyDate, Integer jobPositionId, Integer plantillaId) {
}
