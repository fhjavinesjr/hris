package com.administrative.impl;

import com.administrative.dtos.NatureOfSeparationDTO;
import com.administrative.entitymodels.NatureOfSeparation;
import com.administrative.repositories.NatureOfSeparationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NatureOfSeparationImplTest {
    @Mock private NatureOfSeparationRepository repository;
    private NatureOfSeparationImpl service;

    @BeforeEach
    void setUp() {
        service = new NatureOfSeparationImpl(repository);
    }

    @Test
    void persistsOfficialBirReasonSeparatelyFromAgencyCode() throws Exception {
        NatureOfSeparationDTO request = new NatureOfSeparationDTO(null, "RES", "Resigned");
        request.setBirSeparationReasonCode(" t ");
        service.createNatureOfSeparation(request);
        ArgumentCaptor<NatureOfSeparation> saved = ArgumentCaptor.forClass(NatureOfSeparation.class);
        verify(repository).save(saved.capture());
        assertEquals("RES", saved.getValue().getCode());
        assertEquals("T", saved.getValue().getBirSeparationReasonCode());
        assertEquals("T", request.getBirSeparationReasonCode());
    }

    @Test
    void rejectsUnknownReasonInsteadOfGuessing() {
        NatureOfSeparationDTO request = new NatureOfSeparationDTO(null, "RES", "Resigned");
        request.setBirSeparationReasonCode("RES");
        assertThrows(IllegalArgumentException.class, () -> service.createNatureOfSeparation(request));
    }

    @Test
    void exposesStoredMapping() throws Exception {
        NatureOfSeparation source = new NatureOfSeparation(9L, "RES", "Resigned");
        source.setBirSeparationReasonCode("T");
        when(repository.findAll()).thenReturn(List.of(source));
        assertEquals("T", service.getAllNatureOfSeparation().get(0).getBirSeparationReasonCode());
    }
}
