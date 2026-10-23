package com.administrative.impl;

import com.administrative.dtos.EarningTypeDTO;
import com.administrative.entitymodels.EarningType;
import com.administrative.repositories.EarningTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EarningTypeImplTest {
    @Mock private EarningTypeRepository repository;

    @Test
    void storesAnAuthoritativeBir2316Classification() throws Exception {
        EarningTypeImpl service = new EarningTypeImpl(repository);
        EarningTypeDTO request = request(true, "40");
        when(repository.save(any(EarningType.class))).thenAnswer(invocation -> {
            EarningType entity = invocation.getArgument(0);
            entity.setEarningTypeId(9L);
            return entity;
        });

        EarningTypeDTO result = service.createEarningType(request);

        assertEquals(9L, result.getEarningTypeId());
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(
                entity -> "40".equals(entity.getBir2316ItemNo())));
    }

    @Test
    void rejectsAClassificationThatConflictsWithTaxability() {
        EarningTypeImpl service = new EarningTypeImpl(repository);

        assertThrows(ResponseStatusException.class,
                () -> service.createEarningType(request(false, "40")));
        verify(repository, never()).save(any());
    }

    private EarningTypeDTO request(boolean taxable, String item) {
        EarningTypeDTO dto = new EarningTypeDTO();
        dto.setAccountingCode("RATA");
        dto.setName("Representation Allowance");
        dto.setTaxable(taxable);
        dto.setBir2316ItemNo(item);
        return dto;
    }
}
