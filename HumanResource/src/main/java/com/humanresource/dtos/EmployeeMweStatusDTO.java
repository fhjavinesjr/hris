package com.humanresource.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeMweStatusDTO(
        Long id,
        String employeeNo,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Boolean minimumWageEarner,
        String regionCode,
        String remarks,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt) {
}
