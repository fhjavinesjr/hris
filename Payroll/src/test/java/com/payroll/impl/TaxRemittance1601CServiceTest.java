package com.payroll.impl;

import com.hris.common.config.SystemConfigRuntimeResolver;
import com.payroll.dtos.TaxRemittance1601CRequest;
import com.payroll.dtos.TaxRemittance1601CVoidRequest;
import com.payroll.entitymodels.PayrollDetail;
import com.payroll.entitymodels.PayrollDetailDeduction;
import com.payroll.entitymodels.PayrollStatus;
import com.payroll.entitymodels.TaxRemittance1601CAudit;
import com.payroll.entitymodels.TaxRemittance1601C;
import com.payroll.repositories.PayrollAdjustmentLineRepository;
import com.payroll.repositories.PayrollDetailDeductionRepository;
import com.payroll.repositories.PayrollDetailRepository;
import com.payroll.repositories.TaxRemittance1601CAuditRepository;
import com.payroll.repositories.TaxRemittance1601CRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxRemittance1601CServiceTest {
    @Mock private TaxRemittance1601CRepository repository;
    @Mock private TaxRemittance1601CAuditRepository auditRepository;
    @Mock private PayrollDetailRepository detailRepository;
    @Mock private PayrollDetailDeductionRepository deductionRepository;
    @Mock private PayrollAdjustmentLineRepository adjustmentRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private SystemConfigRuntimeResolver configResolver;
    private TaxRemittance1601CService service;

    @BeforeEach
    void setUp() {
        service = new TaxRemittance1601CService(repository, auditRepository,
                detailRepository, deductionRepository, adjustmentRepository,
                restTemplate, configResolver);
    }

    @Test
    void confirmedRecordUsesLockedPayrollExpectationAndSavesAudit() {
        authorize(true);
        PayrollDetail detail = new PayrollDetail();
        detail.setSalaryPeriodKey("2026-10-1");
        detail.setSalaryDate(LocalDate.of(2026, 10, 30));
        PayrollDetailDeduction withholding = new PayrollDetailDeduction(
                detail, "WTX", "Withholding Tax", 1456.80, 0.0, 0);
        when(detailRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(detail));
        when(deductionRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(withholding));
        when(repository.findByTaxYearAndTaxMonth(2026, 10)).thenReturn(Optional.empty());
        when(repository.save(any(TaxRemittance1601C.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var saved = service.save(2026, 10, new TaxRemittance1601CRequest(
                new BigDecimal("1456.80"), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("1456.80"), LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1), "Test Bank", "SIM-1601C-001", "Test entry", null,
                "CONFIRMED"), "payroll.officer", "Bearer token");

        assertTrue(saved.withholdingMatchesPayroll());
        assertEquals(new BigDecimal("1456.80"), saved.expectedPayrollWithholding());
        ArgumentCaptor<TaxRemittance1601CAudit> audit = ArgumentCaptor.forClass(
                TaxRemittance1601CAudit.class);
        verify(auditRepository).save(audit.capture());
        assertEquals("CREATED", audit.getValue().getAction());
        assertEquals("payroll.officer", audit.getValue().getPerformedBy());
    }

    @Test
    void reviewFlagsUnconfirmedMonthsAndDoesNotTreatPayrollAsRemitted() {
        authorize(false);
        PayrollDetail detail = new PayrollDetail();
        detail.setSalaryPeriodKey("2026-10-1");
        detail.setSalaryDate(LocalDate.of(2026, 10, 30));
        when(detailRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(detail));
        when(deductionRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(new PayrollDetailDeduction(
                        detail, "WTX", "Withholding Tax", 1456.80, 0.0, 0)));

        var review = service.review(2026, "Bearer token");

        assertEquals(new BigDecimal("1456.80"), review.expectedPayrollWithholding());
        assertEquals(new BigDecimal("0.00"), review.confirmedAmountRemitted());
        assertEquals(false, review.reconciled());
        assertEquals("MISSING", review.months().get(9).status());
        assertTrue(review.issues().get(0).contains("Month 10"));
    }

    @Test
    void confirmedRecordRequiresEvidenceFieldsAndArithmetic() {
        authorize(true);
        assertThrows(IllegalArgumentException.class, () -> service.save(2026, 10,
                new TaxRemittance1601CRequest(new BigDecimal("1456.80"), BigDecimal.ZERO,
                        BigDecimal.ZERO, new BigDecimal("1456.80"), null, null,
                        null, null, null, null, "CONFIRMED"),
                "payroll.officer", "Bearer token"));
        assertThrows(IllegalArgumentException.class, () -> service.save(2026, 10,
                new TaxRemittance1601CRequest(new BigDecimal("1456.80"), BigDecimal.ZERO,
                        BigDecimal.ZERO, new BigDecimal("100.00"),
                        LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                        "Test Bank", "SIM", null, null, "CONFIRMED"),
                "payroll.officer", "Bearer token"));
    }

    @Test
    void refusesUnauthenticatedRead() {
        assertThrows(AccessDeniedException.class, () -> service.review(2026, null));
    }

    @Test
    void readPermissionDoesNotAllowOfficerToConfirm() {
        authorize(false);
        assertThrows(AccessDeniedException.class, () -> service.save(2026, 10,
                new TaxRemittance1601CRequest(new BigDecimal("1456.80"), BigDecimal.ZERO,
                        BigDecimal.ZERO, new BigDecimal("1456.80"),
                        LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                        "Test Bank", "SIM", null, null, "CONFIRMED"),
                "viewer", "Bearer token"));
    }

    @Test
    void correctionToConfirmedRecordRequiresRemarks() {
        authorize(true);
        TaxRemittance1601C previous = new TaxRemittance1601C();
        previous.setId(10L);
        previous.setStatus("CONFIRMED");
        when(repository.findByTaxYearAndTaxMonth(2026, 10)).thenReturn(Optional.of(previous));
        assertThrows(IllegalArgumentException.class, () -> service.save(2026, 10,
                new TaxRemittance1601CRequest(new BigDecimal("1456.80"), BigDecimal.ZERO,
                        BigDecimal.ZERO, new BigDecimal("1456.80"),
                        LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                        "Test Bank", "SIM", null, null, "CONFIRMED"),
                "payroll.officer", "Bearer token"));
    }

    @Test
    void zeroWithholdingNeedsNoInventedFiledReturn() {
        authorize(false);

        var review = service.review(2026, "Bearer token");

        assertTrue(review.reconciled());
        assertTrue(review.issues().isEmpty());
        assertEquals(new BigDecimal("0.00"), review.confirmedTaxesWithheld());
    }

    @Test
    void voidedDraftIsAuditedAndExcludedWhenPayrollWithholdingIsZero() {
        authorize(true);
        TaxRemittance1601C previous = new TaxRemittance1601C();
        previous.setId(10L);
        previous.setTaxYear(2026);
        previous.setTaxMonth(10);
        previous.setStatus("DRAFT");
        previous.setTaxesWithheld(BigDecimal.ZERO);
        previous.setAdjustment(BigDecimal.ZERO);
        previous.setPenalties(BigDecimal.ZERO);
        previous.setAmountRemitted(BigDecimal.ZERO);
        previous.setRemarks("Superseded October simulation");
        when(repository.findByTaxYearAndTaxMonth(2026, 10)).thenReturn(Optional.of(previous));
        when(repository.save(any(TaxRemittance1601C.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findByTaxYearOrderByTaxMonthAsc(2026)).thenReturn(List.of(previous));

        var result = service.voidRecord(2026, 10,
                new TaxRemittance1601CVoidRequest("Earlier payroll was reprocessed; no return filed.", true),
                "payroll.officer", "Bearer token");
        var review = service.review(2026, "Bearer token");

        assertEquals("VOIDED", result.status());
        assertTrue(review.reconciled());
        assertEquals("VOIDED", review.months().get(9).status());
        assertEquals(new BigDecimal("0.00"), review.confirmedTaxesWithheld());
        ArgumentCaptor<TaxRemittance1601CAudit> audit = ArgumentCaptor.forClass(
                TaxRemittance1601CAudit.class);
        verify(auditRepository).save(audit.capture());
        assertEquals("VOIDED", audit.getValue().getAction());
        assertTrue(audit.getValue().getPreviousValues().contains("status=DRAFT"));
        assertTrue(audit.getValue().getNewValues().contains("status=VOIDED"));
    }

    @Test
    void voidedRecordDoesNotHideOutstandingPayrollWithholding() {
        authorize(false);
        PayrollDetail detail = new PayrollDetail();
        detail.setSalaryPeriodKey("2026-10-1");
        detail.setSalaryDate(LocalDate.of(2026, 10, 30));
        when(detailRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(detail));
        when(deductionRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(new PayrollDetailDeduction(
                        detail, "WTX", "Withholding Tax", 1456.80, 0.0, 0)));
        TaxRemittance1601C voided = new TaxRemittance1601C();
        voided.setTaxMonth(10);
        voided.setStatus("VOIDED");
        voided.setTaxesWithheld(BigDecimal.ZERO);
        when(repository.findByTaxYearOrderByTaxMonthAsc(2026)).thenReturn(List.of(voided));

        var review = service.review(2026, "Bearer token");

        assertEquals(false, review.reconciled());
        assertTrue(review.issues().stream().anyMatch(issue -> issue.contains("Month 10")));
    }

    @Test
    void voidRequiresOfficerPermissionReasonAndNoFilingAttestation() {
        authorize(true);
        assertThrows(IllegalArgumentException.class, () -> service.voidRecord(2026, 10,
                new TaxRemittance1601CVoidRequest("", true), "payroll.officer", "Bearer token"));
        assertThrows(IllegalArgumentException.class, () -> service.voidRecord(2026, 10,
                new TaxRemittance1601CVoidRequest("Mistaken test entry", false),
                "payroll.officer", "Bearer token"));
    }

    private void authorize(boolean edit) {
        when(configResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_ADMINISTRATIVE, null))
                .thenReturn("http://administrative.test");
        when(restTemplate.exchange(
                eq("http://administrative.test/api/authorization/effective?featureKey=payroll.annualization"),
                eq(HttpMethod.GET), any(HttpEntity.class),
                eq(TaxRemittance1601CService.Permission.class)))
                .thenReturn(ResponseEntity.ok(new TaxRemittance1601CService.Permission(
                        false, true, true, edit, false, false)));
    }
}
