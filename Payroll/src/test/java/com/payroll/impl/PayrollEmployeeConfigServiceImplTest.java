package com.payroll.impl;

import com.hris.common.config.SystemConfigRuntimeResolver;
import com.payroll.dtos.EmployeePayrollInfoDTO;
import com.payroll.dtos.PayrollEmployeeConfigDTO;
import com.payroll.repositories.PayrollEmployeeConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollEmployeeConfigServiceImplTest {

    @Mock
    private PayrollEmployeeConfigRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SystemConfigRuntimeResolver systemConfigResolver;

    private PayrollEmployeeConfigServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PayrollEmployeeConfigServiceImpl(repository, restTemplate, systemConfigResolver);
        ReflectionTestUtils.setField(service, "hrServiceUrl", "http://localhost:8085");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void usesTheCurrentCentralizedHrmUrlForEmployeeSetup() {
        when(repository.findBySalaryPeriodKey(any())).thenReturn(Collections.emptyList());
        when(systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_HRM, "http://localhost:8085"))
                .thenReturn("http://combined-app");

        EmployeePayrollInfoDTO employee = new EmployeePayrollInfoDTO();
        employee.setEmployeeNo("001");
        employee.setFullName("FERDINAND JAVINES");
        employee.setIsExcludedFromPayroll(false);
        when(restTemplate.exchange(
                eq("http://combined-app/api/employee/payroll-info/bulk"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of(employee)));

        List<PayrollEmployeeConfigDTO> result =
                service.getConfigForSetup("2026-8-1", "regular", "Bearer test-token");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployeeNo()).isEqualTo("001");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void reportsBadGatewayInsteadOfReturningAnEmptyListWhenEveryHrmUrlFails() {
        when(systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_HRM, "http://localhost:8085"))
                .thenReturn("http://combined-app");
        when(restTemplate.exchange(
                eq("http://combined-app/api/employee/payroll-info/bulk"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("HRM unavailable"));

        assertThatThrownBy(() -> service.getConfigForSetup("2026-8-1", "regular", null))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
    }
}
