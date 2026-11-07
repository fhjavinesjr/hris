package com.payroll.dtos;

import java.util.List;

public record AnnualizationYearDTO(
        Integer taxYear,
        boolean annualTaxTableConfigured,
        int employees,
        int computed,
        int finalized,
        int forReview,
        List<AnnualizationEmployeeDTO> records) {
}
