package com.payroll.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PreviousEmployerTaxDataDTO(
        Long id,
        String employeeNo,
        Integer taxYear,
        String employerName,
        String employerTin,
        String employerAddress,
        String employerZipCode,
        LocalDate employmentFrom,
        LocalDate employmentTo,
        BigDecimal grossCompensation,
        BigDecimal nonTaxableCompensation,
        BigDecimal taxableCompensation,
        BigDecimal taxWithheld,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy) {
}
