package com.payroll.impl;

import com.payroll.dtos.PayrollAdjustmentDTO;
import com.payroll.dtos.PayrollAdjustmentLineDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

final class AnnualizationAdjustmentEvidence {
    private AnnualizationAdjustmentEvidence() {}

    static boolean matches(Long headerId, BigDecimal expectedAmount, PayrollAdjustmentDTO header) {
        if (header == null || headerId == null || !headerId.equals(header.getId())
                || !"POSTED".equalsIgnoreCase(header.getStatus()) || expectedAmount == null) {
            return false;
        }
        List<PayrollAdjustmentLineDTO> lines = header.getLines();
        if (lines == null) return false;
        List<PayrollAdjustmentLineDTO> annualizationLines = lines.stream()
                .filter(line -> "DEDUCTION".equalsIgnoreCase(line.getType()))
                .filter(line -> "WTX_ANNUALIZATION".equalsIgnoreCase(line.getCode()))
                .filter(line -> !Boolean.TRUE.equals(line.getIsAutoComputed()))
                .toList();
        if (annualizationLines.size() != 1) return false;
        Double value = annualizationLines.get(0).getAmount();
        return value != null && Double.isFinite(value)
                && BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP)
                        .compareTo(expectedAmount.setScale(2, RoundingMode.HALF_UP)) == 0;
    }
}
