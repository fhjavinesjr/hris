package com.payroll.dtos;

import java.time.LocalDate;

public record AnnualizationAdjustmentPeriodDTO(String salaryPeriodKey, LocalDate salaryDate) {
}
