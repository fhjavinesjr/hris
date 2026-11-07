package com.payroll.impl;

import com.payroll.dtos.PayrollAdjustmentDTO;
import com.payroll.dtos.PayrollAdjustmentLineDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnualizationAdjustmentEvidenceTest {
    @Test
    void requiresMatchingPostedAnnualizationLine() {
        PayrollAdjustmentDTO header = new PayrollAdjustmentDTO();
        header.setId(200L);
        header.setStatus("POSTED");
        header.setLines(List.of(new PayrollAdjustmentLineDTO(
                "DEDUCTION", "WTX_ANNUALIZATION", "Annual withholding tax adjustment",
                -1000.00, null, false)));

        assertFalse(AnnualizationAdjustmentEvidence.matches(
                200L, new BigDecimal("-1456.80"), header));
        assertTrue(AnnualizationAdjustmentEvidence.matches(
                200L, new BigDecimal("-1000.00"), header));
        header.setStatus("PENDING");
        assertFalse(AnnualizationAdjustmentEvidence.matches(
                200L, new BigDecimal("-1000.00"), header));
    }
}
