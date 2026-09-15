package com.humanresource.impl;

import com.humanresource.dtos.EmployeeAppointmentDTO;
import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.repositories.EmployeeAppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeAppointmentImplTest {

    @Mock
    private EmployeeAppointmentRepository repository;

    private EmployeeAppointmentImpl service;

    @BeforeEach
    void setUp() {
        service = new EmployeeAppointmentImpl(repository);
    }

    @Test
    void creatingLaterActiveAppointmentDeactivatesAndFlushesPriorAppointmentFirst() throws Exception {
        EmployeeAppointment prior = appointment(34L, 201, LocalDateTime.of(2025, 4, 9, 0, 0));
        EmployeeAppointmentDTO request = request(34L, 202, LocalDateTime.of(2026, 4, 9, 0, 0), true);
        when(repository.findActiveByPlantillaForUpdate(202)).thenReturn(List.of());
        when(repository.findActiveByEmployeeForUpdate(34L)).thenReturn(List.of(prior));
        when(repository.saveAndFlush(any(EmployeeAppointment.class))).thenAnswer(invocation -> {
            EmployeeAppointment saved = invocation.getArgument(0);
            saved.setEmployeeAppointmentId(99L);
            return saved;
        });

        EmployeeAppointmentDTO result = service.createEmployeeAppointment(request);

        assertFalse(prior.getActiveAppointment());
        assertEquals(99L, result.getEmployeeAppointmentId());
        InOrder order = inOrder(repository);
        order.verify(repository).saveAllAndFlush(List.of(prior));
        order.verify(repository).saveAndFlush(any(EmployeeAppointment.class));
    }

    @Test
    void occupiedPlantillaIsRejectedBeforeCurrentAppointmentIsChanged() {
        EmployeeAppointment occupant = appointment(88L, 202, LocalDateTime.of(2025, 1, 1, 0, 0));
        EmployeeAppointmentDTO request = request(34L, 202, LocalDateTime.of(2026, 4, 9, 0, 0), true);
        when(repository.findActiveByPlantillaForUpdate(202)).thenReturn(List.of(occupant));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createEmployeeAppointment(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Plantilla is already occupied", exception.getReason());
        verify(repository, never()).findActiveByEmployeeForUpdate(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void appointmentNotLaterThanCurrentActiveAppointmentIsRejected() {
        LocalDateTime existingDate = LocalDateTime.of(2026, 4, 9, 0, 0);
        EmployeeAppointment prior = appointment(34L, 201, existingDate);
        EmployeeAppointmentDTO request = request(34L, 202, existingDate, true);
        when(repository.findActiveByPlantillaForUpdate(202)).thenReturn(List.of());
        when(repository.findActiveByEmployeeForUpdate(34L)).thenReturn(List.of(prior));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createEmployeeAppointment(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("New appointment must be later than the active appointment", exception.getReason());
        verify(repository, never()).saveAllAndFlush(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void inactiveHistoricalAppointmentDoesNotDeactivateCurrentAppointment() throws Exception {
        EmployeeAppointmentDTO request = request(
                34L, 201, LocalDateTime.of(2024, 4, 9, 0, 0), false);
        when(repository.saveAndFlush(any(EmployeeAppointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createEmployeeAppointment(request);

        verify(repository, never()).findActiveByPlantillaForUpdate(any());
        verify(repository, never()).findActiveByEmployeeForUpdate(any());
        verify(repository, never()).saveAllAndFlush(any());
        verify(repository).saveAndFlush(any(EmployeeAppointment.class));
    }

    @Test
    void editingExistingActiveAppointmentMayChangeItsDates() throws Exception {
        EmployeeAppointment existing = appointment(
                34L, 201, LocalDateTime.of(2026, 4, 14, 0, 0));
        existing.setEmployeeAppointmentId(6L);
        EmployeeAppointmentDTO request = request(
                34L, 201, LocalDateTime.of(2026, 4, 9, 0, 0), true);
        when(repository.findByIdForUpdate(6L)).thenReturn(Optional.of(existing));
        when(repository.findActiveByPlantillaForUpdate(201)).thenReturn(List.of(existing));
        when(repository.findActiveByEmployeeForUpdate(34L)).thenReturn(List.of(existing));
        when(repository.findByEmployeeId(34L)).thenReturn(List.of(existing));
        when(repository.saveAndFlush(existing)).thenReturn(existing);

        EmployeeAppointmentDTO result = service.updateEmployeeAppointment(6L, request);

        assertEquals(LocalDateTime.of(2026, 4, 9, 0, 0), existing.getAssumptionToDutyDate());
        assertEquals(6L, result.getEmployeeAppointmentId());
        verify(repository).saveAndFlush(existing);
    }

    @Test
    void editingAppointmentCannotTakeAnotherEmployeesPlantilla() {
        EmployeeAppointment existing = appointment(
                34L, 201, LocalDateTime.of(2026, 4, 14, 0, 0));
        existing.setEmployeeAppointmentId(6L);
        EmployeeAppointment occupant = appointment(
                88L, 202, LocalDateTime.of(2025, 1, 1, 0, 0));
        occupant.setEmployeeAppointmentId(9L);
        EmployeeAppointmentDTO request = request(
                34L, 202, LocalDateTime.of(2026, 4, 14, 0, 0), true);
        when(repository.findByIdForUpdate(6L)).thenReturn(Optional.of(existing));
        when(repository.findActiveByPlantillaForUpdate(202)).thenReturn(List.of(occupant));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateEmployeeAppointment(6L, request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Plantilla is already occupied", exception.getReason());
        verify(repository, never()).saveAndFlush(existing);
    }

    private static EmployeeAppointment appointment(
            Long employeeId,
            Integer plantillaId,
            LocalDateTime assumptionDate) {
        return new EmployeeAppointment(
                employeeId,
                assumptionDate.minusMonths(1),
                assumptionDate,
                1,
                plantillaId,
                10,
                7,
                1,
                new BigDecimal("306000"),
                new BigDecimal("25500"),
                new BigDecimal("1159.09"),
                "Appointment",
                true);
    }

    private static EmployeeAppointmentDTO request(
            Long employeeId,
            Integer plantillaId,
            LocalDateTime assumptionDate,
            boolean active) {
        return new EmployeeAppointmentDTO(
                employeeId,
                assumptionDate.minusMonths(1),
                assumptionDate,
                1,
                plantillaId,
                10,
                7,
                1,
                new BigDecimal("306000"),
                new BigDecimal("25500"),
                new BigDecimal("1159.09"),
                "Appointment",
                active);
    }
}
