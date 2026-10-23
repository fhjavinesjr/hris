package com.administrative.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MinimumWageRateDTO(
        Long id,
        String regionCode,
        String regionName,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        BigDecimal dailyRate,
        BigDecimal monthlyRate,
        String legalBasis,
        String remarks) {
}
