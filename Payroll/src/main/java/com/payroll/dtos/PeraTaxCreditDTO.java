package com.payroll.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Verified Personal Equity and Retirement Account contribution evidence for one tax year. */
public record PeraTaxCreditDTO(
        Long id,
        String employeeNo,
        Integer taxYear,
        BigDecimal contributionAmount,
        BigDecimal certifiedTaxCredit,
        String certificateNo,
        String administratorName,
        LocalDate certificateIssuedDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy) {
}
