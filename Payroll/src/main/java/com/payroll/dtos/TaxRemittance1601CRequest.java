package com.payroll.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TaxRemittance1601CRequest(
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
        String status) {}
