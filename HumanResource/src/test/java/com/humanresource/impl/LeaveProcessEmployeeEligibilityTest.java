package com.humanresource.impl;

import com.humanresource.dtos.LeaveProcessRequestDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.services.EmployeeService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeaveProcessEmployeeEligibilityTest {

    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final EmployeeService employeeService = mock(EmployeeService.class);
    private final LeaveProcessServiceImpl service = new LeaveProcessServiceImpl(
            employeeRepository, employeeService, null, null, null, null, null,
            null, null, null, null, null, null
    );

    @Test
    void allScopeUsesRegularPayrollPopulationAndStillExcludesSystemAccounts() {
        Employee regular = employee(10L, "EMP-10", "2");
        Employee regularAdministrator = employee(12L, "EMP-12", "ADMIN");
        Employee systemAdmin = employee(11L, "admin", "1");
        when(employeeService.getRegularPayrollEmployeeIds()).thenReturn(Set.of(10L, 11L, 12L));
        when(employeeRepository.findAllById(Set.of(10L, 11L, 12L)))
                .thenReturn(List.of(regular, regularAdministrator, systemAdmin));

        LeaveProcessRequestDTO request = new LeaveProcessRequestDTO();
        request.setScope("ALL");

        List<Employee> resolved = service.resolveEmployeesForRequest(request);

        assertEquals(List.of(regular, regularAdministrator), resolved);
        verify(employeeRepository, never()).findAll();
    }

    @Test
    void selectedEmployeesAreIntersectedWithRegularPayrollPopulation() {
        Employee regular = employee(20L, "EMP-20", "2");
        when(employeeService.getRegularPayrollEmployeeIds()).thenReturn(Set.of(20L));
        when(employeeRepository.findAllById(List.of(20L))).thenReturn(List.of(regular));

        LeaveProcessRequestDTO request = new LeaveProcessRequestDTO();
        request.setScope("ALL");
        request.setSelectedEmployeeIds(List.of(20L, 99L));

        assertEquals(List.of(regular), service.resolveEmployeesForRequest(request));
    }

    @Test
    void specificEmployeeOutsideRegularPayrollPopulationIsRejected() {
        when(employeeService.getRegularPayrollEmployeeIds()).thenReturn(Set.of(30L));

        LeaveProcessRequestDTO request = new LeaveProcessRequestDTO();
        request.setScope("EMPLOYEE");
        request.setEmployeeId(99L);

        assertTrue(service.resolveEmployeesForRequest(request).isEmpty());
        verify(employeeRepository, never()).findById(99L);
    }

    @Test
    void specificRegularEmployeeIsResolved() {
        Employee regular = employee(30L, "EMP-30", "2");
        when(employeeService.getRegularPayrollEmployeeIds()).thenReturn(Set.of(30L));
        when(employeeRepository.findById(30L)).thenReturn(Optional.of(regular));

        LeaveProcessRequestDTO request = new LeaveProcessRequestDTO();
        request.setScope("EMPLOYEE");
        request.setEmployeeId(30L);

        assertEquals(List.of(regular), service.resolveEmployeesForRequest(request));
    }

    private Employee employee(Long id, String employeeNo, String role) {
        Employee employee = new Employee();
        employee.setEmployeeId(id);
        employee.setEmployeeNo(employeeNo);
        employee.setRole(role);
        return employee;
    }
}
