package com.payroll.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnnualizationPeriodDTO(
        String salaryPeriodKey,
        LocalDate salaryDate,
        BigDecimal grossCompensation,
        BigDecimal nonTaxableCompensation,
        BigDecimal taxableCompensation,
        BigDecimal gsisContribution,
        BigDecimal philHealthContribution,
        BigDecimal pagIbigContribution,
        BigDecimal taxWithheld,
        Boolean minimumWageEarner,
        String minimumWageRegionCode,
        BigDecimal minimumWageDailyRate,
        BigDecimal minimumWageMonthlyRate,
        String minimumWageLegalBasis) {
}
