package com.humanresource.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hris.common.utilities.JwtUtil;
import com.humanresource.dtos.EmployeeDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.PersonalData;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.PersonalDataRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

class EmployeeServiceImplTest {

    private EmployeeRepository employeeRepository;
    private PersonalDataRepository personalDataRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        personalDataRepository = mock(PersonalDataRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtUtil = mock(JwtUtil.class);
        service = new EmployeeServiceImpl(
                employeeRepository,
                new ObjectMapper(),
                passwordEncoder,
                jwtUtil,
                mock(JdbcTemplate.class),
                personalDataRepository
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

    @Test
    void loginInitializesBlankPasswordFromBirthDate() {
        Employee employee = employee(7L, "EMP-007");
        employee.setEmployeePassword(null);
        PersonalData personalData = mock(PersonalData.class);
        when(personalData.getDob()).thenReturn(LocalDateTime.of(1979, 12, 25, 0, 0));
        when(employeeRepository.findByEmployeeNo("EMP-007")).thenReturn(Optional.of(employee));
        when(personalDataRepository.findByEmployeeId(7L)).thenReturn(personalData);
        when(passwordEncoder.encode("12/25/1979")).thenReturn("encoded-default");
        when(passwordEncoder.matches("12/25/1979", "encoded-default")).thenReturn(true);
        when(jwtUtil.generateToken("EMP-007", "2")).thenReturn("token");

        assertEquals("token", service.loginEmployee("EMP-007", "12/25/1979"));

        assertEquals("encoded-default", employee.getEmployeePassword());
        verify(employeeRepository).save(employee);
    }

    @Test
    void securityStatusDetectsDefaultPasswordWithoutChangingIt() {
        Employee employee = employee(7L, "EMP-007");
        PersonalData personalData = mock(PersonalData.class);
        when(personalData.getDob()).thenReturn(LocalDateTime.of(1979, 12, 25, 0, 0));
        when(employeeRepository.findByEmployeeNo("EMP-007")).thenReturn(Optional.of(employee));
        when(personalDataRepository.findByEmployeeId(7L)).thenReturn(personalData);
        when(passwordEncoder.matches("12/25/1979", "encoded-password")).thenReturn(true);

        assertTrue(service.getSecurityStatus("EMP-007").usingDefaultPassword());
        assertTrue(service.getSecurityStatus("EMP-007").roleAssigned());
        assertEquals("2", service.getSecurityStatus("EMP-007").role());
        verify(employeeRepository, never()).save(employee);
    }

    @Test
    void securityStatusDoesNotFlagCustomPassword() {
        Employee employee = employee(7L, "EMP-007");
        PersonalData personalData = mock(PersonalData.class);
        when(personalData.getDob()).thenReturn(LocalDateTime.of(1979, 12, 25, 0, 0));
        when(employeeRepository.findByEmployeeNo("EMP-007")).thenReturn(Optional.of(employee));
        when(personalDataRepository.findByEmployeeId(7L)).thenReturn(personalData);
        when(passwordEncoder.matches("12/25/1979", "encoded-password")).thenReturn(false);

        assertFalse(service.getSecurityStatus("EMP-007").usingDefaultPassword());
    }

    @Test
    void securityStatusReportsAnUnassignedBlankRole() {
        Employee employee = employee(7L, "EMP-007");
        employee.setRole("  ");
        PersonalData personalData = mock(PersonalData.class);
        when(personalData.getDob()).thenReturn(LocalDateTime.of(1979, 12, 25, 0, 0));
        when(employeeRepository.findByEmployeeNo("EMP-007")).thenReturn(Optional.of(employee));
        when(personalDataRepository.findByEmployeeId(7L)).thenReturn(personalData);

        assertFalse(service.getSecurityStatus("EMP-007").roleAssigned());
    }

    @Test
    void securityStatusReportsLegacyNullTextAsUnassigned() {
        Employee employee = employee(7L, "EMP-007");
        employee.setRole(" null ");
        when(employeeRepository.findByEmployeeNo("EMP-007")).thenReturn(Optional.of(employee));

        var status = service.getSecurityStatus("EMP-007");

        assertFalse(status.roleAssigned());
        assertEquals(null, status.role());
    }

    @Test
    void securityStatusReportsLegacyRoleNameAsUnassigned() {
        Employee employee = employee(7L, "EMP-007");
        employee.setRole("ADMIN");
        when(employeeRepository.findByEmployeeNo("EMP-007")).thenReturn(Optional.of(employee));

        var status = service.getSecurityStatus("EMP-007");

        assertFalse(status.roleAssigned());
        assertEquals(null, status.role());
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
