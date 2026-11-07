package com.payroll.dtos;

import java.math.BigDecimal;
import java.util.List;

public record AnnualizationAlphalistPreviewDTO(
        int taxYear,
        String specificationReference,
        boolean exportEnabled,
        String employerName,
        String employerTin,
        String employerBirBranchCode,
        String employerRdoCode,
        int employees,
        int validEmployees,
        int employeesWithIssues,
        int substitutedFilingQualified,
        int substitutedFilingNotQualified,
        int substitutedFilingNotAssessed,
        BigDecimal totalGrossCompensation,
        BigDecimal totalTaxableCompensation,
        BigDecimal totalTaxDue,
        BigDecimal totalTaxesWithheld,
        List<String> datasetIssues,
        List<String> limitations,
        List<AnnualizationAlphalistEmployeeDTO> records) {
}
