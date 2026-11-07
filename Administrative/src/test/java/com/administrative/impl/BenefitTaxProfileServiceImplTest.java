package com.administrative.impl;

import com.administrative.dtos.BenefitTaxProfileDTO;
import com.administrative.entitymodels.BenefitTaxProfile;
import com.administrative.repositories.BenefitTaxProfileRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BenefitTaxProfileServiceImplTest {
    private final BenefitTaxProfileRepository repository = Mockito.mock(BenefitTaxProfileRepository.class);
    private final BenefitTaxProfileServiceImpl service = new BenefitTaxProfileServiceImpl(repository);

    @Test
    void savesTaxYearGuidanceWithoutAllocatingEmployeeAmounts() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        BenefitTaxProfileDTO saved = service.save(null, profile(new BigDecimal("8000.00"), "35", null));
        assertEquals("CLOTHING", saved.profileCode());
        assertEquals(2026, saved.taxYear());
        assertEquals(new BigDecimal("8000.00"), saved.guidanceCeiling());
    }

    @Test
    void rejectsWrongTaxableItemAndNegativeCeiling() {
        assertThrows(ResponseStatusException.class,
                () -> service.save(null, profile(new BigDecimal("8000.00"), "48", null)));
        assertThrows(ResponseStatusException.class,
                () -> service.save(null, profile(new BigDecimal("-1.00"), "35", null)));
    }

    private BenefitTaxProfileDTO profile(BigDecimal ceiling, String exemptItem, String taxableItem) {
        return new BenefitTaxProfileDTO(null, "CLOTHING", 2026, "Clothing allowance", ceiling,
                "ANNUAL", null, exemptItem, taxableItem,
                "BIR RR 29-2025", "https://bir-cdn.bir.gov.ph/BIR/pdf/RR%20No.%2029-2025.pdf");
    }
}
