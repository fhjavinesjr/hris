package com.administrative.impl;

import com.administrative.dtos.MinimumWageRateDTO;
import com.administrative.entitymodels.MinimumWageRate;
import com.administrative.repositories.MinimumWageRateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MinimumWageRateServiceImplTest {
    @Test
    void createsAValidatedEffectiveDatedRate() {
        MinimumWageRateRepository repository = mock(MinimumWageRateRepository.class);
        when(repository.findByRegionCodeIgnoreCaseOrderByEffectiveFromAsc("NCR"))
                .thenReturn(List.of());
        when(repository.save(any())).thenAnswer(invocation -> {
            MinimumWageRate value = invocation.getArgument(0);
            value.setId(1L);
            return value;
        });
        MinimumWageRateServiceImpl service = new MinimumWageRateServiceImpl(repository);

        MinimumWageRateDTO result = service.create(request(
                LocalDate.of(2026, 1, 1), null));

        assertEquals(new BigDecimal("700.00"), result.dailyRate());
        assertEquals("NCR", result.regionCode());
    }

    @Test
    void rejectsOverlappingRatesForTheSameRegion() {
        MinimumWageRateRepository repository = mock(MinimumWageRateRepository.class);
        MinimumWageRate existing = new MinimumWageRate();
        existing.setId(2L); existing.setRegionCode("NCR");
        existing.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        existing.setEffectiveTo(LocalDate.of(2026, 6, 30));
        when(repository.findByRegionCodeIgnoreCaseOrderByEffectiveFromAsc("NCR"))
                .thenReturn(List.of(existing));
        MinimumWageRateServiceImpl service = new MinimumWageRateServiceImpl(repository);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.create(request(LocalDate.of(2026, 6, 1), null)));

        assertEquals(409, error.getStatusCode().value());
    }

    private MinimumWageRateDTO request(LocalDate from, LocalDate to) {
        return new MinimumWageRateDTO(null, "ncr", "National Capital Region", from, to,
                new BigDecimal("700"), new BigDecimal("18200"), "Wage Order", null);
    }
}
