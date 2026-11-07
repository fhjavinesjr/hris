package com.payroll.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SeparationFinalPayReviewDTO(
        String salaryPeriodKey,
        LocalDate salaryDate,
        BigDecimal netPayBeforeAdjustment,
        AnnualizationEmployeeDTO provisionalAnnualization,
        boolean canApply,
        String actionRequired) {
}
