package com.payroll.impl;

import com.payroll.dtos.PayslipDTO;
import com.payroll.dtos.PayslipLineDTO;
import com.payroll.services.PayrollPeriodLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayslipServiceImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbc;
    @Mock
    private DataSource dataSource;
    @Mock
    private ResourceLoader resourceLoader;
    @Mock
    private PayrollPeriodLockService payrollPeriodLockService;

    private PayslipServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PayslipServiceImpl(
                jdbc,
                dataSource,
                resourceLoader,
                payrollPeriodLockService
        );
        lenient().when(jdbc.query(
                anyString(),
                anyMap(),
                org.mockito.ArgumentMatchers.<RowMapper<PayslipLineDTO>>any()
        )).thenReturn(List.of());
    }

    @Test
    void releasedOnlySelectsLatestLockedDetailWithoutBooleanSqlComparison() {
        when(payrollPeriodLockService.isPeriodLocked("2026-07")).thenReturn(false);
        when(jdbc.queryForList(anyString(), anyMap())).thenReturn(List.of(
                detailRow(2L, false),
                detailRow(1L, true)
        ));

        PayslipDTO result = service.getPayslip("001", "2026-07", true);

        assertThat(result.getPayrollDetailId()).isEqualTo(1L);
        assertThat(result.getLocked()).isTrue();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(jdbc).queryForList(sqlCaptor.capture(), anyMap());
        assertThat(sqlCaptor.getValue().toLowerCase())
                .doesNotContain("top ")
                .doesNotContain("islocked = 1");
    }

    @Test
    void lockedPeriodReleasesLatestDetailEvenWhenDetailFlagIsFalse() {
        when(payrollPeriodLockService.isPeriodLocked("2026-07")).thenReturn(true);
        when(jdbc.queryForList(anyString(), anyMap())).thenReturn(List.of(
                detailRow(2L, false),
                detailRow(1L, true)
        ));

        PayslipDTO result = service.getPayslip("001", "2026-07", true);

        assertThat(result.getPayrollDetailId()).isEqualTo(2L);
        assertThat(result.getLocked()).isTrue();
        assertThat(result.getStatus()).isEqualTo("FINAL / RELEASED");
    }

    @Test
    void releasedOnlyRejectsPeriodWithNoReleasedDetail() {
        when(payrollPeriodLockService.isPeriodLocked("2026-07")).thenReturn(false);
        when(jdbc.queryForList(anyString(), anyMap())).thenReturn(List.of(
                detailRow(2L, false)
        ));

        assertThatThrownBy(() -> service.getPayslip("001", "2026-07", true))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("No payroll detail found");
    }

    @Test
    void salaryPeriodQueryOmitsNullableParameterWhenListingAllEmployees() {
        service.getSalaryPeriods(null);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        org.mockito.Mockito.verify(jdbc).query(
                sqlCaptor.capture(),
                paramsCaptor.capture(),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any()
        );

        assertThat(sqlCaptor.getValue().toLowerCase())
                .doesNotContain(":employeeno is null")
                .doesNotContain("where employeeno");
        assertThat(paramsCaptor.getValue()).isEmpty();
    }

    @Test
    void salaryPeriodQueryUsesTypedParameterWhenEmployeeIsProvided() {
        service.getSalaryPeriods(" 001 ");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        org.mockito.Mockito.verify(jdbc).query(
                sqlCaptor.capture(),
                paramsCaptor.capture(),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any()
        );

        assertThat(sqlCaptor.getValue().toLowerCase())
                .contains("where employeeno = :employeeno")
                .doesNotContain(":employeeno is null");
        assertThat(paramsCaptor.getValue()).containsEntry("employeeNo", "001");
    }

    private Map<String, Object> detailRow(long id, boolean locked) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("employeeNo", "001");
        row.put("salaryPeriodKey", "2026-07");
        row.put("isLocked", locked);
        row.put("status", "COMPUTED");
        return row;
    }
}
