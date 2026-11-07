package com.payroll.dtos;

import java.time.LocalDate;

public record AnnualizationRefundVoucherRequest(
        LocalDate paidDate, String voucherReference, Boolean confirmedPaid) {
}
