package com.humanresource.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.PersonalData;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.PersonalDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersonalDataImplPasswordTest {

    private PersonalDataRepository personalDataRepository;
    private EmployeeRepository employeeRepository;
    private PasswordEncoder passwordEncoder;
    private ObjectMapper objectMapper;
    private PersonalDataImpl service;

    @BeforeEach
    void setUp() {
        personalDataRepository = mock(PersonalDataRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        objectMapper = mock(ObjectMapper.class);
        service = new PersonalDataImpl(
                personalDataRepository,
                objectMapper,
                employeeRepository,
                passwordEncoder
        );
    }

    @Test
    void dobChangeRotatesPasswordWhenEmployeeStillUsesPreviousDob() throws Exception {
        LocalDateTime previousDob = LocalDateTime.of(1979, 12, 25, 0, 0);
        LocalDateTime newDob = LocalDateTime.of(1980, 1, 2, 0, 0);
        PersonalData personalData = personalData(7L, previousDob);
        Employee employee = mock(Employee.class);
        when(employee.getEmployeePassword()).thenReturn("encoded-old");
        when(personalDataRepository.findByEmployeeId(7L)).thenReturn(personalData);
        when(employeeRepository.findById(7L)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("12/25/1979", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("01/02/1980")).thenReturn("encoded-new");
        doAnswer(invocation -> {
            personalData.setDob(newDob);
            return personalData;
        }).when(objectMapper).updateValue(eq(personalData), any());

        assertTrue(service.updatePersonalData(7L, Map.of("dob", newDob)));

        verify(employee).setEmployeePassword("encoded-new");
        verify(employeeRepository).save(employee);
    }

    @Test
    void dobChangeDoesNotRotateACustomPassword() throws Exception {
        LocalDateTime previousDob = LocalDateTime.of(1979, 12, 25, 0, 0);
        LocalDateTime newDob = LocalDateTime.of(1980, 1, 2, 0, 0);
        PersonalData personalData = personalData(7L, previousDob);
        Employee employee = mock(Employee.class);
        when(employee.getEmployeePassword()).thenReturn("encoded-custom");
        when(personalDataRepository.findByEmployeeId(7L)).thenReturn(personalData);
        when(employeeRepository.findById(7L)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("12/25/1979", "encoded-custom")).thenReturn(false);
        doAnswer(invocation -> {
            personalData.setDob(newDob);
            return personalData;
        }).when(objectMapper).updateValue(eq(personalData), any());

        assertTrue(service.updatePersonalData(7L, Map.of("dob", newDob)));

        verify(passwordEncoder, never()).encode("01/02/1980");
        verify(employeeRepository, never()).save(employee);
    }

    private static PersonalData personalData(Long employeeId, LocalDateTime dob) {
        PersonalData personalData = new PersonalData();
        personalData.setEmployeeId(employeeId);
        personalData.setDob(dob);
        return personalData;
    }
}
