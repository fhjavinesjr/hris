package com.administrative.controllers;

import com.administrative.dtos.WTAXContributionDTO;
import com.administrative.services.WTAXContributionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WTAXContributionControllerTest {

    @Mock private WTAXContributionService service;
    @InjectMocks private WTAXContributionController controller;

    @Test
    void returnsOnlyAnnualBracketsForRequestedTaxYearInIncomeOrder() throws Exception {
        when(service.getAllWTAXContribution()).thenReturn(List.of(
                bracket("Annual", 2026, "800000", null, "102500", "25"),
                bracket("Annual", 2025, "0", "250000", "0", "0"),
                bracket("Monthly", null, "0", "20833", "0", "0"),
                bracket("Annual", 2026, "0", "250000", "0", "0")));

        List<Map<String, Object>> result = controller.getAnnualTaxBrackets(2026).getBody();

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("0"), result.get(0).get("incomeFrom"));
        assertEquals(new BigDecimal("800000"), result.get(1).get("incomeFrom"));
        assertEquals(new BigDecimal("0.25"), result.get(1).get("excessRate"));
    }

    private WTAXContributionDTO bracket(
            String salaryType,
            Integer taxYear,
            String from,
            String to,
            String baseTax,
            String rate) {
        WTAXContributionDTO dto = new WTAXContributionDTO();
        dto.setSalaryType(salaryType);
        dto.setTaxYear(taxYear);
        dto.setIncomeFrom(from);
        dto.setIncomeTo(to);
        dto.setFixedAmount(baseTax);
        dto.setPercentageOverBase(rate);
        return dto;
    }
}
