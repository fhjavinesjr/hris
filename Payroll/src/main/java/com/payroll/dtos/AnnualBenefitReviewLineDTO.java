package com.payroll.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AnnualBenefitReviewLineDTO(String sourceType, Long sourceId,
        String earningCode, String earningName, String salaryPeriodKey, LocalDate salaryDate,
        BigDecimal sourceAmount, String currentBirItem, Boolean currentTaxable,
        String suggestedProfileCode, String reviewedProfileCode,
        BigDecimal exemptAmount, String exemptBirItem,
        BigDecimal otherExemptAmount, String otherExemptBirItem,
        BigDecimal taxableAmount, String taxableBirItem,
        String reason, String reviewedBy, LocalDateTime reviewedAt, boolean stale) {}
