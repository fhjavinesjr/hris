package com.payroll.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record Bir1604COfficerReviewDTO(
        int taxYear, boolean alphalistReady,
        BigDecimal confirmed1601CWithheld, BigDecimal adjustedAlphalistWithheld,
        BigDecimal withholdingGap, BigDecimal recordedEmployeeRefunds,
        String contactNumber, String emailAddress, Integer sheetsAttached,
        String refundsReleased, LocalDate refundDate, BigDecimal overremittanceAmount,
        Integer firstCreditingMonth, String evidenceReference,
        String signatoryName, String signatoryTitle, String signatoryTin, String remarks,
        String status, String updatedBy, LocalDateTime updatedAt, List<String> issues) {}
