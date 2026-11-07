package com.payroll.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AnnualizationRefundVoucherDTO(
        Long id, BigDecimal amount, LocalDate paidDate, String voucherReference,
        String recordedBy, LocalDateTime recordedAt) {
}
