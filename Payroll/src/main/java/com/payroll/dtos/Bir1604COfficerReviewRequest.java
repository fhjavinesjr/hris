package com.payroll.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Bir1604COfficerReviewRequest(
        String contactNumber, String emailAddress, Integer sheetsAttached,
        String refundsReleased, LocalDate refundDate, BigDecimal overremittanceAmount,
        Integer firstCreditingMonth, String evidenceReference,
        String signatoryName, String signatoryTitle, String signatoryTin,
        String remarks, String status) {}
