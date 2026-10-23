package com.humanresource.impl;

import com.humanresource.dtos.EmployeeMweStatusDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.EmployeeMweStatus;
import com.humanresource.repositories.EmployeeMweStatusRepository;
import com.humanresource.repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmployeeMweStatusServiceImplTest {
    @Test
    void createsAnExplicitAuditedMweStatus() {
        EmployeeMweStatusRepository statuses = mock(EmployeeMweStatusRepository.class);
        EmployeeRepository employees = mock(EmployeeRepository.class);
        Employee employee = employee();
        when(employees.findByEmployeeNo("EMP-001")).thenReturn(Optional.of(employee));
        when(statuses.findByEmployeeIdOrderByEffectiveFromDesc(1L)).thenReturn(List.of());
        when(statuses.save(any())).thenAnswer(invocation -> {
            EmployeeMweStatus value = invocation.getArgument(0); value.setId(3L); return value;
        });
        EmployeeMweStatusServiceImpl service = new EmployeeMweStatusServiceImpl(statuses, employees);

        EmployeeMweStatusDTO result = service.create(request(
                LocalDate.of(2026, 1, 1), null), "payroll.officer");

        assertEquals(Boolean.TRUE, result.minimumWageEarner());
        assertEquals("NCR", result.regionCode());
        assertEquals("payroll.officer", result.createdBy());
    }

    @Test
    void rejectsAnOverlappingEmployeeClassification() {
        EmployeeMweStatusRepository statuses = mock(EmployeeMweStatusRepository.class);
        EmployeeRepository employees = mock(EmployeeRepository.class);
        when(employees.findByEmployeeNo("EMP-001")).thenReturn(Optional.of(employee()));
        EmployeeMweStatus existing = new EmployeeMweStatus();
        existing.setId(2L); existing.setEmployeeId(1L);
        existing.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        existing.setEffectiveTo(LocalDate.of(2026, 6, 30));
        when(statuses.findByEmployeeIdOrderByEffectiveFromDesc(1L)).thenReturn(List.of(existing));
        EmployeeMweStatusServiceImpl service = new EmployeeMweStatusServiceImpl(statuses, employees);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.create(request(LocalDate.of(2026, 6, 1), null), "officer"));

        assertEquals(409, error.getStatusCode().value());
    }

    private EmployeeMweStatusDTO request(LocalDate from, LocalDate to) {
        return new EmployeeMweStatusDTO(null, "EMP-001", from, to, true, "ncr", null,
                null, null, null, null);
    }

    private Employee employee() {
        Employee value = new Employee("EMP-001", "password", "1", "2", "First", "Last", "",
                "employee@example.test", "", "", LocalDateTime.now(), LocalDateTime.now());
        value.setEmployeeId(1L);
        return value;
    }
}
