package com.administrative.controllers;

import com.administrative.dtos.PhilHealthContributionDTO;
import com.administrative.services.PhilHealthContributionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PhilHealthContributionControllerTest {

    @Test
    void highestFixedBracketIsOpenEndedDespiteHistoricalArtificialCeiling() throws Exception {
        PhilHealthContributionService service = mock(PhilHealthContributionService.class);
        when(service.getAllPhilHealthContribution()).thenReturn(List.of(
                bracket(1L, "0.00", "10,000.00", "250", "0"),
                bracket(2L, "10,000.01", "99,999.99", "250", "2500"),
                bracket(3L, "100,000.00", "9,999,999.00", "2500", "0")
        ));

        PhilHealthContributionController controller = new PhilHealthContributionController(service);
        ResponseEntity<List<Map<String, Object>>> response = controller.getPhilHealthBrackets();
        List<Map<String, Object>> brackets = response.getBody();

        assertEquals(Boolean.FALSE, brackets.get(0).get("isAndUp"));
        assertEquals(Boolean.FALSE, brackets.get(1).get("isAndUp"));
        assertEquals(Boolean.TRUE, brackets.get(2).get("isAndUp"));
    }

    private PhilHealthContributionDTO bracket(Long id, String salaryFrom, String salaryTo,
                                                String shareFrom, String shareTo) {
        return new PhilHealthContributionDTO(id, LocalDateTime.of(2025, 1, 1, 0, 0), "5",
                salaryFrom, salaryTo, shareFrom, shareTo, shareFrom, shareTo);
    }
}
