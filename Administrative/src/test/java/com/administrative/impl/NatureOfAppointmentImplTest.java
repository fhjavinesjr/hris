package com.administrative.impl;

import com.administrative.dtos.NatureOfAppointmentDTO;
import com.administrative.entitymodels.NatureOfAppointment;
import com.administrative.repositories.NatureOfAppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NatureOfAppointmentImplTest {
    @Mock private NatureOfAppointmentRepository repository;
    private NatureOfAppointmentImpl service;

    @BeforeEach
    void setUp() {
        service = new NatureOfAppointmentImpl(repository);
    }

    @Test
    void normalizesSupportedBirEmploymentStatusCode() throws Exception {
        NatureOfAppointmentDTO request = new NatureOfAppointmentDTO(null, "PERM", "Permanent");
        request.setBirEmploymentStatusCode(" r ");

        NatureOfAppointmentDTO result = service.createNatureOfAppointment(request);

        ArgumentCaptor<NatureOfAppointment> saved = ArgumentCaptor.forClass(NatureOfAppointment.class);
        verify(repository).save(saved.capture());
        assertEquals("R", saved.getValue().getBirEmploymentStatusCode());
        assertEquals("R", result.getBirEmploymentStatusCode());
    }

    @Test
    void rejectsUnsupportedBirEmploymentStatusCode() throws Exception {
        NatureOfAppointmentDTO request = new NatureOfAppointmentDTO(null, "PERM", "Permanent");
        request.setBirEmploymentStatusCode("PERM");

        assertNull(service.createNatureOfAppointment(request));
    }

    @Test
    void exposesBirEmploymentStatusMapping() throws Exception {
        NatureOfAppointment source = new NatureOfAppointment(7L, "COS", "Contract of Service");
        source.setBirEmploymentStatusCode("CP");
        when(repository.findAll()).thenReturn(List.of(source));

        List<NatureOfAppointmentDTO> result = service.getAllNatureOfAppointment();

        assertEquals("CP", result.get(0).getBirEmploymentStatusCode());
    }
}
