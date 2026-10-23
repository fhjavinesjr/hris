package com.humanresource.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hris.common.utilities.JwtUtil;
import com.humanresource.dtos.EmployeeDTO;
import com.humanresource.dtos.EmployeeAnnualizationIdentityDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.entitymodels.PersonalData;
import com.humanresource.entitymodels.Separation;
import com.humanresource.repositories.EmployeeAppointmentRepository;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.PersonalDataRepository;
import com.humanresource.repositories.SeparationRepository;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
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
    private EmployeeAppointmentRepository employeeAppointmentRepository;
    private SeparationRepository separationRepository;
    private EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        personalDataRepository = mock(PersonalDataRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtUtil = mock(JwtUtil.class);
        employeeAppointmentRepository = mock(EmployeeAppointmentRepository.class);
        separationRepository = mock(SeparationRepository.class);
        service = new EmployeeServiceImpl(
                employeeRepository,
                new ObjectMapper(),
                passwordEncoder,
                jwtUtil,
                mock(JdbcTemplate.class),
                personalDataRepository,
                employeeAppointmentRepository,
                separationRepository
        );
    }

    @Test
    void annualizationIdentityUsesPersonalDataAndActiveAppointment() {
        Employee employee = employee(7L, "EMP-007");
        PersonalData personalData = mock(PersonalData.class);
        when(personalData.getEmployeeId()).thenReturn(7L);
        when(personalData.getTinNo()).thenReturn("000-000-007");
        when(personalData.getRdoCode()).thenReturn("123");
        when(personalData.getPermAddress()).thenReturn("Permanent Address");
        when(personalData.getPermZip()).thenReturn("1000");
        when(personalData.getResAddress()).thenReturn("Residential Address");
        when(personalData.getResZip()).thenReturn("1001");
        when(personalData.getDob()).thenReturn(LocalDateTime.of(1990, 5, 10, 0, 0));
        when(personalData.getTelNo()).thenReturn("");
        when(personalData.getMobileNo()).thenReturn("09171234567");
        when(personalData.getSurname()).thenReturn("Employee");
        when(personalData.getFirstname()).thenReturn("Test");
        when(personalData.getMiddlename()).thenReturn("Middle");
        when(personalData.getExtname()).thenReturn("Jr.");
        when(personalData.getCitizenship()).thenReturn("Filipino");
        EmployeeAppointment appointment = new EmployeeAppointment();
        appointment.setEmployeeId(7L);
        appointment.setJobPositionId(12);
        appointment.setNatureOfAppointmentId(4);
        appointment.setAssumptionToDutyDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        Separation separation = new Separation();
        separation.setEmployeeId(7L);
        separation.setSeparationDate(LocalDateTime.of(2026, 9, 30, 0, 0));
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(personalDataRepository.findAll()).thenReturn(List.of(personalData));
        when(employeeAppointmentRepository.findAll())
                .thenReturn(List.of(appointment));
        when(separationRepository.findAll()).thenReturn(List.of(separation));

        EmployeeAnnualizationIdentityDTO result =
                service.getAnnualizationIdentities(2026).get(0);

        assertEquals("Permanent Address", result.address());
        assertEquals("Residential Address", result.localAddress());
        assertEquals(LocalDate.of(1990, 5, 10), result.birthDate());
        assertEquals("09171234567", result.contactNo());
        assertEquals(12L, result.jobPositionId());
        assertEquals("Employee", result.lastName());
        assertEquals("Test", result.firstName());
        assertEquals("Middle", result.middleName());
        assertEquals("Jr.", result.extensionName());
        assertEquals("Filipino", result.nationality());
        assertEquals(4L, result.natureOfAppointmentId());
        assertEquals(LocalDate.of(2026, 1, 1), result.employmentFrom());
        assertEquals(LocalDate.of(2026, 9, 30), result.employmentTo());
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
