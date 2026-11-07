package com.payroll.impl;

import com.hris.common.config.SystemConfigRuntimeResolver;
import com.payroll.dtos.PayrollAdjustmentDTO;
import com.payroll.dtos.PayrollAdjustmentLineDTO;
import com.payroll.entitymodels.PayrollAdjustmentHeader;
import com.payroll.entitymodels.PayrollAdjustmentLine;
import com.payroll.entitymodels.PayrollDetail;
import com.payroll.entitymodels.PayrollDetailEarning;
import com.payroll.repositories.PayrollAdjustmentHeaderRepository;
import com.payroll.repositories.PayrollDetailRepository;
import com.payroll.services.PayrollPeriodLockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollAdjustmentServiceImplTest {
    @Mock private PayrollAdjustmentHeaderRepository headerRepository;
    @Mock private PayrollDetailRepository detailRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private PayrollPeriodLockService lockService;
    @Mock private SystemConfigRuntimeResolver systemConfigRuntimeResolver;

    @Test
    void createsPendingAnnualizationDeductionWithRefundSignPreserved() {
        PayrollAdjustmentServiceImpl service = service();
        PayrollDetail detail = new PayrollDetail();
        detail.setEmployeeNo("EMP-001");
        detail.setEmployeeName("Employee One");
        detail.setSalaryPeriodKey("2026-12-2");
        when(detailRepository.findByEmployeeNoAndSalaryPeriodKey("EMP-001", "2026-12-2"))
                .thenReturn(Optional.of(detail));
        when(headerRepository.findActiveWithLinesByEmployeeNoAndSalaryPeriodKey(
                "EMP-001", "2026-12-2")).thenReturn(Optional.empty());
        when(headerRepository.findByEmployeeNoAndSalaryPeriodKeyAndStatusNot(
                "EMP-001", "2026-12-2", "SUPERSEDED")).thenReturn(Optional.empty());
        when(headerRepository.save(any(PayrollAdjustmentHeader.class))).thenAnswer(invocation -> {
            PayrollAdjustmentHeader saved = invocation.getArgument(0);
            saved.setId(41L);
            return saved;
        });

        PayrollAdjustmentDTO result = service.createAnnualizationAdjustment(
                "EMP-001", "Employee One", "2026-12-2", new BigDecimal("-125.50"),
                2026, 1, "officer", "Bearer token");

        assertEquals(41L, result.getId());
        assertEquals("PENDING", result.getStatus());
        assertEquals("WTX_ANNUALIZATION", result.getLines().get(0).getCode());
        assertEquals(-125.50, result.getLines().get(0).getAmount());
    }

    @Test
    void recalculatedPendingFinalPayReplacesOldTaxLineWithoutDuplicatingIt() {
        PayrollAdjustmentServiceImpl service = service();
        PayrollDetail detail = new PayrollDetail();
        detail.setEmployeeNo("EMP-001");
        detail.setEmployeeName("Employee One");
        detail.setSalaryPeriodKey("2026-6-1");
        when(detailRepository.findByEmployeeNoAndSalaryPeriodKey("EMP-001", "2026-6-1"))
                .thenReturn(Optional.of(detail));
        PayrollAdjustmentHeader pending = new PayrollAdjustmentHeader();
        pending.setId(10L);
        pending.setEmployeeNo("EMP-001");
        pending.setEmployeeName("Employee One");
        pending.setSalaryPeriodKey("2026-6-1");
        pending.setStatus("PENDING");
        PayrollAdjustmentLine oldTax = new PayrollAdjustmentLine();
        oldTax.setHeader(pending);
        oldTax.setType("DEDUCTION");
        oldTax.setCode("WTX_ANNUALIZATION");
        oldTax.setName("Annual withholding tax adjustment");
        oldTax.setAmount(100.0);
        pending.getLines().add(oldTax);
        when(headerRepository.findActiveWithLinesByEmployeeNoAndSalaryPeriodKey(
                "EMP-001", "2026-6-1")).thenReturn(Optional.of(pending));
        when(headerRepository.findByEmployeeNoAndSalaryPeriodKeyAndStatusNot(
                "EMP-001", "2026-6-1", "SUPERSEDED")).thenReturn(Optional.of(pending));
        when(headerRepository.save(any(PayrollAdjustmentHeader.class))).thenAnswer(invocation -> {
            PayrollAdjustmentHeader saved = invocation.getArgument(0);
            if (saved != pending) saved.setId(11L);
            return saved;
        });

        PayrollAdjustmentDTO result = service.createAnnualizationAdjustment(
                "EMP-001", "Employee One", "2026-6-1", new BigDecimal("200.00"),
                2026, 0, "officer", "Bearer token");

        assertEquals(2, result.getVersion());
        assertEquals(1, result.getLines().size());
        assertEquals(200.0, result.getLines().get(0).getAmount());
        assertEquals("SUPERSEDED", pending.getStatus());
    }

    @Test
    void refusesToSupersedeExistingPostedAdjustment() {
        PayrollAdjustmentServiceImpl service = service();
        PayrollAdjustmentHeader posted = new PayrollAdjustmentHeader();
        posted.setEmployeeNo("EMP-001");
        posted.setEmployeeName("Employee One");
        posted.setSalaryPeriodKey("2026-12-2");
        posted.setStatus("POSTED");
        when(headerRepository.findActiveWithLinesByEmployeeNoAndSalaryPeriodKey(
                "EMP-001", "2026-12-2")).thenReturn(Optional.of(posted));

        assertThrows(IllegalStateException.class, () -> service.createAnnualizationAdjustment(
                "EMP-001", "Employee One", "2026-12-2", new BigDecimal("100.00"),
                2026, 1, "officer", "Bearer token"));
    }

    @Test
    void snapshotsClassificationFromExistingPayrollEarning() {
        PayrollAdjustmentServiceImpl service = service();
        PayrollDetail detail = new PayrollDetail();
        detail.setEmployeeNo("EMP-001");
        detail.setEmployeeName("Employee One");
        detail.setSalaryPeriodKey("2026-12-2");
        PayrollDetailEarning earning = new PayrollDetailEarning(
                detail, "PERA", "PERA", 2_000.0, false, 0);
        earning.setBir2316ItemNo("37");
        detail.getEarnings().add(earning);

        PayrollAdjustmentLineDTO line = new PayrollAdjustmentLineDTO(
                "EARNING", "PERA", "PERA adjustment", 100.0, true, false);
        PayrollAdjustmentDTO request = new PayrollAdjustmentDTO();
        request.setEmployeeNo("EMP-001");
        request.setEmployeeName("Employee One");
        request.setSalaryPeriodKey("2026-12-2");
        request.setStatus("PENDING");
        request.setLines(List.of(line));

        when(detailRepository.findByEmployeeNoAndSalaryPeriodKey("EMP-001", "2026-12-2"))
                .thenReturn(Optional.of(detail));
        when(headerRepository.findByEmployeeNoAndSalaryPeriodKeyAndStatusNot(
                "EMP-001", "2026-12-2", "SUPERSEDED")).thenReturn(Optional.empty());
        when(headerRepository.save(any(PayrollAdjustmentHeader.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PayrollAdjustmentDTO result = service.saveAdjustment(request, "Bearer token");

        assertEquals(false, result.getLines().get(0).getIsTaxable());
        assertEquals("37", result.getLines().get(0).getBir2316ItemNo());
    }

    @Test
    void refusesToPostUnclassifiedEarningAdjustment() {
        PayrollAdjustmentServiceImpl service = service();
        PayrollAdjustmentHeader pending = new PayrollAdjustmentHeader();
        pending.setId(9L);
        pending.setEmployeeNo("EMP-001");
        pending.setSalaryPeriodKey("2026-12-2");
        pending.setStatus("PENDING");
        var line = new com.payroll.entitymodels.PayrollAdjustmentLine();
        line.setHeader(pending);
        line.setType("EARNING");
        line.setCode("PERA");
        line.setName("PERA adjustment");
        pending.getLines().add(line);
        when(headerRepository.findById(9L)).thenReturn(Optional.of(pending));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.postAdjustment(9L, "officer"));

        assertEquals(true, exception.getMessage().contains(
                "BIR 2316 classification is missing for PERA adjustment"));
    }

    @Test
    void resolvesAdditionalEarningFromAdministrativeConfiguration() {
        PayrollAdjustmentServiceImpl service = service();
        PayrollDetail detail = new PayrollDetail();
        detail.setEmployeeNo("EMP-001");
        detail.setEmployeeName("Employee One");
        detail.setSalaryPeriodKey("2026-12-2");

        PayrollAdjustmentLineDTO line = new PayrollAdjustmentLineDTO(
                "EARNING", "RATA", "RATA", 100.0, true, false);
        PayrollAdjustmentDTO request = new PayrollAdjustmentDTO();
        request.setEmployeeNo("EMP-001");
        request.setSalaryPeriodKey("2026-12-2");
        request.setStatus("PENDING");
        request.setLines(List.of(line));

        when(detailRepository.findByEmployeeNoAndSalaryPeriodKey("EMP-001", "2026-12-2"))
                .thenReturn(Optional.of(detail));
        when(headerRepository.findByEmployeeNoAndSalaryPeriodKeyAndStatusNot(
                "EMP-001", "2026-12-2", "SUPERSEDED")).thenReturn(Optional.empty());
        when(systemConfigRuntimeResolver.resolveApiUrl(any(), any()))
                .thenReturn("http://administrative.test");
        when(restTemplate.exchange(
                contains("/api/earningType/get-all"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                org.mockito.ArgumentMatchers
                        .<ParameterizedTypeReference<List<PayrollAdjustmentServiceImpl.EarningTypeDefinition>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(
                        new PayrollAdjustmentServiceImpl.EarningTypeDefinition(
                                1L, "RATA", "RATA", false, "37"))));
        when(headerRepository.save(any(PayrollAdjustmentHeader.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PayrollAdjustmentDTO result = service.saveAdjustment(request, "Bearer token");

        assertEquals(false, result.getLines().get(0).getIsTaxable());
        assertEquals("37", result.getLines().get(0).getBir2316ItemNo());
    }

    private PayrollAdjustmentServiceImpl service() {
        return new PayrollAdjustmentServiceImpl(
                headerRepository, detailRepository, restTemplate, lockService,
                systemConfigRuntimeResolver);
    }
}
