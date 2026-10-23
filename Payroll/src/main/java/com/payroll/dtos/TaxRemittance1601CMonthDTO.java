package com.payroll.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaxRemittance1601CMonthDTO(
        int taxYear,
        int taxMonth,
        BigDecimal expectedPayrollWithholding,
        BigDecimal taxesWithheld,
        BigDecimal adjustment,
        BigDecimal penalties,
        BigDecimal amountRemitted,
        LocalDate filingDate,
        LocalDate remittanceDate,
        String remittanceChannel,
        String referenceNo,
        String remarks,
        String evidenceReference,
        String status,
        String updatedBy,
        LocalDateTime updatedAt,
        boolean withholdingMatchesPayroll) {}
