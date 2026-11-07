package com.payroll.dtos;

import java.math.BigDecimal;
import java.util.List;

public record TaxRemittance1601CReviewDTO(
        int taxYear,
        BigDecimal expectedPayrollWithholding,
        BigDecimal confirmedTaxesWithheld,
        BigDecimal confirmedAmountRemitted,
        boolean reconciled,
        List<String> issues,
        List<TaxRemittance1601CMonthDTO> months) {}
