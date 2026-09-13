package com.humanresource.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hris.common.utilities.JwtUtil;
import com.humanresource.dtos.EmployeeDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.repositories.EmployeeRepository;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmployeeServiceImplTest {

    private EmployeeRepository employeeRepository;
    private EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        service = new EmployeeServiceImpl(
                employeeRepository,
                new ObjectMapper(),
                mock(PasswordEncoder.class),
                mock(JwtUtil.class),
                mock(JdbcTemplate.class)
        );
    }

    @Test
    void updateEmployeePersistsANewTrimmedEmployeeNumber() throws Exception {
        Employee employee = employee(7L, "OLD-007");
        when(employeeRepository.findById(7L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findByEmployeeNoIgnoreCase("NEW-007")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeDTO result = service.updateEmployee(7L, Map.of("employeeNo", "  NEW-007  "));

        assertEquals("NEW-007", result.getEmployeeNo());
        ArgumentCaptor<Employee> savedEmployee = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(savedEmployee.capture());
        assertEquals("NEW-007", savedEmployee.getValue().getEmployeeNo());
    }

    @Test
    void updateEmployeeRejectsAnEmployeeNumberAssignedToAnotherEmployee() {
        Employee employee = employee(7L, "OLD-007");
        when(employeeRepository.findById(7L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findByEmployeeNoIgnoreCase("EXISTING-008"))
                .thenReturn(Optional.of(employee(8L, "EXISTING-008")));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.updateEmployee(7L, Map.of("employeeNo", "EXISTING-008")));

        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void updateEmployeeRejectsABlankEmployeeNumber() {
        when(employeeRepository.findById(7L)).thenReturn(Optional.of(employee(7L, "OLD-007")));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.updateEmployee(7L, Map.of("employeeNo", "   ")));

        assertEquals(400, exception.getStatusCode().value());
    }

    private static Employee employee(Long id, String employeeNo) {
        Employee employee = new Employee(
                employeeNo,
                "encoded-password",
                "BIO-" + id,
                "2",
                "Test",
                "Employee",
                "",
                "employee" + id + "@example.test",
                "",
                "",
                LocalDateTime.of(2026, 1, 1, 8, 0),
                LocalDateTime.of(2026, 1, 1, 8, 0)
        );
        employee.setEmployeeId(id);
        return employee;
    }
}
