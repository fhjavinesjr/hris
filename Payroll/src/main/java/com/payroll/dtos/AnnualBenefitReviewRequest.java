package com.payroll.dtos;

import java.math.BigDecimal;

public record AnnualBenefitReviewRequest(String profileCode, BigDecimal exemptAmount,
        String exemptBirItem, BigDecimal otherExemptAmount, String otherExemptBirItem,
        BigDecimal taxableAmount, String taxableBirItem, String reason) {}
