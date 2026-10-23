package com.payroll.impl;

import com.hris.common.config.SystemConfigRuntimeResolver;
import com.payroll.dtos.AnnualizationEmployeeDTO;
import com.payroll.dtos.AnnualizationAlphalistEmployeeDTO;
import com.payroll.dtos.AnnualizationAlphalistPreviewDTO;
import com.payroll.dtos.AnnualizationYearDTO;
import com.payroll.dtos.PreviousEmployerTaxDataDTO;
import com.payroll.dtos.PeraTaxCreditDTO;
import com.payroll.dtos.PayrollAdjustmentDTO;
import com.payroll.dtos.PayrollAdjustmentLineDTO;
import com.payroll.dtos.AnnualizationRefundVoucherRequest;
import com.payroll.entitymodels.AnnualPeraTaxCredit;
import com.payroll.entitymodels.PayrollAdjustmentHeader;
import com.payroll.entitymodels.PayrollAdjustmentLine;
import com.payroll.entitymodels.PayrollDetail;
import com.payroll.entitymodels.PayrollDetailDeduction;
import com.payroll.entitymodels.PayrollDetailEarning;
import com.payroll.entitymodels.PayrollStatus;
import com.payroll.entitymodels.PreviousEmployerTaxData;
import com.payroll.entitymodels.AnnualizationSnapshot;
import com.payroll.entitymodels.AnnualizationSnapshotPeriod;
import com.payroll.entitymodels.AnnualizationRefundVoucher;
import com.payroll.repositories.PayrollAdjustmentLineRepository;
import com.payroll.repositories.PayrollDetailDeductionRepository;
import com.payroll.repositories.PayrollDetailEarningRepository;
import com.payroll.repositories.PayrollDetailRepository;
import com.payroll.repositories.PreviousEmployerTaxDataRepository;
import com.payroll.repositories.AnnualizationAuditLogRepository;
import com.payroll.repositories.AnnualizationSnapshotPeriodRepository;
import com.payroll.repositories.AnnualizationSnapshotPreviousEmployerRepository;
import com.payroll.repositories.AnnualizationSnapshotRepository;
import com.payroll.repositories.AnnualizationSnapshotItemRepository;
import com.payroll.repositories.PayrollPeriodLockRepository;
import com.payroll.repositories.AnnualPeraTaxCreditRepository;
import com.payroll.services.PayrollAdjustmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AnnualizationServiceImplTest {

    @Mock private PayrollDetailRepository detailRepository;
    @Mock private PayrollDetailEarningRepository earningRepository;
    @Mock private PayrollDetailDeductionRepository deductionRepository;
    @Mock private PayrollAdjustmentLineRepository adjustmentLineRepository;
    @Mock private PreviousEmployerTaxDataRepository previousEmployerRepository;
    @Mock private AnnualizationSnapshotRepository snapshotRepository;
    @Mock private AnnualizationSnapshotPeriodRepository snapshotPeriodRepository;
    @Mock private AnnualizationSnapshotPreviousEmployerRepository snapshotPreviousRepository;
    @Mock private AnnualizationSnapshotItemRepository snapshotItemRepository;
    @Mock private AnnualizationAuditLogRepository auditLogRepository;
    @Mock private AnnualPeraTaxCreditRepository peraTaxCreditRepository;
    @Mock private com.payroll.repositories.AnnualBenefitReviewRepository benefitReviewRepository;
    @Mock private com.payroll.repositories.AnnualizationRefundVoucherRepository refundVoucherRepository;
    @Mock private PayrollPeriodLockRepository periodLockRepository;
    @Mock private PayrollAdjustmentService payrollAdjustmentService;
    @Mock private SystemConfigRuntimeResolver systemConfigResolver;
    @Mock private RestTemplate restTemplate;

    private AnnualizationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AnnualizationServiceImpl(
                detailRepository,
                earningRepository,
                deductionRepository,
                adjustmentLineRepository,
                previousEmployerRepository,
                snapshotRepository,
                snapshotPeriodRepository,
                snapshotPreviousRepository,
                snapshotItemRepository,
                auditLogRepository,
                peraTaxCreditRepository,
                periodLockRepository,
                payrollAdjustmentService,
                restTemplate,
                systemConfigResolver,
                new Bir1604CDatWriter(), benefitReviewRepository, refundVoucherRepository);
        ReflectionTestUtils.setField(service, "administrativeUrl", "http://administrative.test");
        ReflectionTestUtils.setField(service, "humanResourceUrl", "http://hrm.test");
        when(systemConfigResolver.resolveApiUrl(
                eq(SystemConfigRuntimeResolver.API_ADMINISTRATIVE), any()))
                .thenReturn("http://administrative.test");
        org.mockito.Mockito.lenient().when(restTemplate.exchange(
                eq("http://administrative.test/api/benefit-tax-profiles?year=2026"),
                eq(HttpMethod.GET), any(HttpEntity.class),
                org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.BenefitTaxProfileSource>>>any()))
                .thenReturn(ResponseEntity.ok(List.of()));
        org.mockito.Mockito.lenient().when(restTemplate.exchange(
                eq("http://administrative.test/api/earningType/get-all"),
                eq(HttpMethod.GET), any(HttpEntity.class),
                org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.EarningTypeBenefitSource>>>any()))
                .thenReturn(ResponseEntity.ok(List.of()));
    }

    @Test
    void aggregatesOnlyPersistedLinesAndPostedAdjustmentsUsingAnnualTaxTable() {
        PayrollDetail detail = detail();
        PayrollDetailEarning basic = new PayrollDetailEarning(detail, "BASIC", "Basic Salary", 100_000.0, true, 0);
        PayrollDetailEarning allowance = new PayrollDetailEarning(detail, "PERA", "PERA", 10_000.0, false, 1);
        basic.setBir2316ItemNo("39");
        allowance.setBir2316ItemNo("37");

        List<PayrollDetailDeduction> deductions = List.of(
                new PayrollDetailDeduction(detail, "GSIS", "GSIS", 9_000.0, 0.0, 0),
                new PayrollDetailDeduction(detail, "PHIC", "PhilHealth", 1_000.0, 0.0, 1),
                new PayrollDetailDeduction(detail, "HDMF", "Pag-IBIG", 1_000.0, 0.0, 2),
                new PayrollDetailDeduction(detail, "WTX", "Withholding Tax", 10_000.0, 0.0, 3));

        PayrollAdjustmentHeader header = new PayrollAdjustmentHeader();
        header.setEmployeeNo("EMP-001");
        header.setSalaryPeriodKey("2026-1-1");
        header.setStatus("POSTED");
        PayrollAdjustmentLine earningAdjustment = adjustment(header, "EARNING", "BACK_PAY", 10_000.0, true);
        earningAdjustment.setBir2316ItemNo("39");
        PayrollAdjustmentLine taxAdjustment = adjustment(header, "DEDUCTION", "WTX_CASCADE", 1_000.0, null);

        when(detailRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(detail));
        when(earningRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(basic, allowance));
        when(deductionRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(deductions);
        when(adjustmentLineRepository.findPostedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(earningAdjustment, taxAdjustment));
        PreviousEmployerTaxData previous = new PreviousEmployerTaxData();
        previous.setId(1L);
        previous.setEmployeeNo("EMP-001");
        previous.setTaxYear(2026);
        previous.setEmployerName("Previous Agency");
        previous.setEmployerTin("000-000-000");
        previous.setEmployerAddress("Previous Employer Address");
        previous.setEmployerZipCode("1000");
        previous.setEmploymentFrom(LocalDate.of(2026, 1, 1));
        previous.setEmploymentTo(LocalDate.of(2026, 1, 10));
        previous.setGrossCompensation(new BigDecimal("50000.00"));
        previous.setNonTaxableCompensation(new BigDecimal("10000.00"));
        previous.setTaxableCompensation(new BigDecimal("40000.00"));
        previous.setTaxWithheld(new BigDecimal("3000.00"));
        when(previousEmployerRepository.findByTaxYearOrderByEmployeeNoAscEmploymentFromAsc(2026))
                .thenReturn(List.of(previous));

        expectPermission();
        expectTaxIdentities();
        expectMweSources(
                List.of(new AnnualizationServiceImpl.EmployeeMweStatusSource(
                        1L, "EMP-001", LocalDate.of(2026, 1, 1), null,
                        false, null, null, "hr.officer", null, "hr.officer", null)),
                List.of());
        when(restTemplate.exchange(
                contains("/api/wh-tax/annual-brackets?year=2026"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                org.mockito.ArgumentMatchers
                        .<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualTaxBracket>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.AnnualTaxBracket(
                        BigDecimal.ZERO,
                        new BigDecimal("1000000"),
                        BigDecimal.ZERO,
                        new BigDecimal("0.10")))));

        AnnualizationYearDTO result = service.getAnnualization(2026, "Bearer token");

        assertTrue(result.annualTaxTableConfigured());
        assertEquals(1, result.employees());
        assertEquals(1, result.computed());
        AnnualizationEmployeeDTO employee = result.records().get(0);
        assertMoney("120000.00", employee.presentGrossCompensation());
        assertMoney("50000.00", employee.previousGrossCompensation());
        assertMoney("170000.00", employee.grossCompensation());
        assertMoney("139000.00", employee.taxableCompensation());
        assertMoney("31000.00", employee.nonTaxableCompensation());
        assertMoney("14000.00", employee.taxWithheld());
        assertMoney("13900.00", employee.annualTaxDue());
        assertMoney("-100.00", employee.adjustmentAmount());
        assertEquals("REFUND", employee.adjustmentType());
        assertTrue(employee.readiness().ready());
        assertEquals("123", employee.taxIdentity().employeeRdoCode());
        assertEquals(LocalDate.of(1990, 5, 10), employee.taxIdentity().employeeBirthDate());
        assertEquals("09171234567", employee.taxIdentity().employeeContactNo());
        assertEquals("Accountant III", employee.taxIdentity().employeePosition());
        assertMoney("21000.00", employee.bir2316Items().item38TotalNonTaxable());
        assertMoney("99000.00", employee.bir2316Items().item52TotalTaxable());

        doReturn(ResponseEntity.ok(new AnnualizationServiceImpl.AnnualizationEmployerIdentity(
                "ISOFT Agency", "000-000-001", "0000", "001", "Agency Address", "HRIS Client")))
                .when(restTemplate).exchange(
                        contains("/api/settings/annualization-identity"), eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(AnnualizationServiceImpl.AnnualizationEmployerIdentity.class));
        AnnualizationEmployeeDTO invalidZip = service.getAnnualization(2026, "Bearer token")
                .records().get(0);
        assertTrue(invalidZip.readiness().issues().contains(
                "Employer ZIP code must be four digits in Administrative System Setup"));
        doReturn(ResponseEntity.ok(new AnnualizationServiceImpl.AnnualizationEmployerIdentity(
                "ISOFT Agency", "000-000-001", "0000", "001", "Agency Address", "1000")))
                .when(restTemplate).exchange(
                        contains("/api/settings/annualization-identity"), eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(AnnualizationServiceImpl.AnnualizationEmployerIdentity.class));

        allowance.setBir2316ItemNo(null);
        AnnualizationEmployeeDTO forReview = service.getAnnualization(2026, "Bearer token")
                .records().get(0);
        assertEquals("FOR_REVIEW", forReview.status());
        assertTrue(forReview.readiness().issues().contains(
                "BIR 2316 classification is missing for PERA"));
        ResponseStatusException notReady = assertThrows(ResponseStatusException.class,
                () -> service.finalizeAnnualization(
                        2026, "EMP-001", "Reviewed", false,
                        "Previous employer recorded", "payroll.officer", "Bearer token"));
        assertEquals(409, notReady.getStatusCode().value());
        verify(snapshotRepository, never()).save(any(AnnualizationSnapshot.class));
        allowance.setBir2316ItemNo("37");

        earningAdjustment.setBir2316ItemNo(null);
        AnnualizationEmployeeDTO adjustmentForReview = service.getAnnualization(2026, "Bearer token")
                .records().get(0);
        assertTrue(adjustmentForReview.readiness().issues().contains(
                "BIR 2316 classification is missing for BACK_PAY"));
        earningAdjustment.setBir2316ItemNo("39");

        IllegalArgumentException missingAssessment = assertThrows(IllegalArgumentException.class,
                () -> service.finalizeAnnualization(
                        2026, "EMP-001", "Reviewed", null, null,
                        "payroll.officer", "Bearer token"));
        assertTrue(missingAssessment.getMessage().contains("substituted-filing assessment"));

        ResponseStatusException incorrectlyQualified = assertThrows(ResponseStatusException.class,
                () -> service.finalizeAnnualization(
                        2026, "EMP-001", "Reviewed", true,
                        "Pure compensation income", "payroll.officer", "Bearer token"));
        assertEquals(409, incorrectlyQualified.getStatusCode().value());

        IllegalArgumentException longReason = assertThrows(IllegalArgumentException.class,
                () -> service.finalizeAnnualization(
                        2026, "EMP-001", "Reviewed", false,
                        "R".repeat(501), "payroll.officer", "Bearer token"));
        assertTrue(longReason.getMessage().contains("500 characters"));
        IllegalArgumentException longRemarks = assertThrows(IllegalArgumentException.class,
                () -> service.finalizeAnnualization(
                        2026, "EMP-001", "R".repeat(501), false,
                        "Previous employer compensation is recorded", "payroll.officer", "Bearer token"));
        assertTrue(longRemarks.getMessage().contains("500 characters"));

        AnnualizationSnapshot[] savedSnapshot = new AnnualizationSnapshot[1];
        when(snapshotRepository.save(any(AnnualizationSnapshot.class))).thenAnswer(invocation -> {
            AnnualizationSnapshot snapshot = invocation.getArgument(0);
            snapshot.setId(10L);
            savedSnapshot[0] = snapshot;
            return snapshot;
        });
        AnnualizationEmployeeDTO finalized = service.finalizeAnnualization(
                2026, "EMP-001", "Reviewed", false,
                "Previous employer compensation is recorded", "payroll.officer", "Bearer token");
        assertEquals("FINALIZED", finalized.status());
        assertEquals(1, finalized.snapshotVersion());
        assertEquals(false, finalized.substitutedFilingQualified());
        assertEquals("Previous employer compensation is recorded",
                finalized.substitutedFilingReason());
        assertMoney("170000.00", finalized.grossCompensation());
        assertEquals("Employee", savedSnapshot[0].getEmployeeLastName());
        assertEquals("One", savedSnapshot[0].getEmployeeFirstName());
        assertEquals("Filipino", savedSnapshot[0].getEmployeeNationality());
        assertEquals("R", savedSnapshot[0].getEmployeeEmploymentStatusCode());
        assertEquals("Regular", savedSnapshot[0].getEmployeeEmploymentStatusName());
        assertEquals(LocalDate.of(2026, 1, 1), savedSnapshot[0].getEmployeeEmploymentFrom());
        assertEquals(LocalDate.of(2026, 12, 31), savedSnapshot[0].getEmployeeEmploymentTo());
        verify(snapshotPeriodRepository).saveAll(any());
        verify(snapshotPreviousRepository).saveAll(any());
        verify(snapshotItemRepository).saveAll(any());
        when(snapshotRepository.findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
                "EMP-001", 2026, "FINALIZED")).thenReturn(java.util.Optional.of(savedSnapshot[0]));
        AnnualizationEmployeeDTO reopened = service.reopenAnnualization(
                2026, "EMP-001", "Correction required", "payroll.officer", "Bearer token");
        assertEquals("COMPUTED", reopened.status());
        assertEquals("VOIDED", savedSnapshot[0].getStatus());
        assertEquals("Correction required", savedSnapshot[0].getReopenReason());
        verify(auditLogRepository, times(3)).save(any());
    }

    @Test
    void aggregatesTwelveDistinctMonthlyPayrollPeriodsWithoutDuplicatingCompensation() {
        List<PayrollDetail> details = new ArrayList<>();
        List<PayrollDetailEarning> earnings = new ArrayList<>();
        List<PayrollDetailDeduction> deductions = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            PayrollDetail detail = detail();
            detail.setSalaryPeriodKey("2026-" + month + "-1");
            detail.setSalaryDate(LocalDate.of(2026, month, 28));
            detail.setGrossAmount(33_750.0);
            detail.setTaxAmount(1_456.80);
            details.add(detail);
            PayrollDetailEarning basic = new PayrollDetailEarning(
                    detail, "BASIC", "Basic Salary", 27_000.0, true, 0);
            basic.setBir2316ItemNo("39");
            PayrollDetailEarning hazard = new PayrollDetailEarning(
                    detail, "HAZARD", "Hazard Pay", 6_750.0, true, 1);
            hazard.setBir2316ItemNo("49");
            earnings.add(basic);
            earnings.add(hazard);
            deductions.add(new PayrollDetailDeduction(detail, "GSIS", "GSIS", 2_430.0, 0.0, 0));
            deductions.add(new PayrollDetailDeduction(detail, "PHIC", "PhilHealth", 675.0, 0.0, 1));
            deductions.add(new PayrollDetailDeduction(detail, "HDMF", "Pag-IBIG", 100.0, 0.0, 2));
            deductions.add(new PayrollDetailDeduction(detail, "WTX", "Withholding Tax", 1_456.80, 0.0, 3));
        }
        when(detailRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(details);
        when(earningRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(earnings);
        when(deductionRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(deductions);
        expectPermission();
        expectTaxIdentities();
        expectMweSources(List.of(new AnnualizationServiceImpl.EmployeeMweStatusSource(
                1L, "EMP-001", LocalDate.of(2026, 1, 1), null,
                false, null, null, "hr.officer", null, "hr.officer", null)), List.of());
        when(restTemplate.exchange(
                contains("/api/wh-tax/annual-brackets?year=2026"), eq(HttpMethod.GET),
                any(HttpEntity.class),
                org.mockito.ArgumentMatchers
                        .<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualTaxBracket>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.AnnualTaxBracket(
                        BigDecimal.ZERO, new BigDecimal("1000000"),
                        BigDecimal.ZERO, new BigDecimal("0.10")))));

        AnnualizationEmployeeDTO employee = service.getEmployeeAnnualization(
                2026, "EMP-001", "Bearer token");

        assertEquals(12, employee.periods().size());
        assertMoney("405000.00", employee.grossCompensation());
        assertMoney("38460.00", employee.nonTaxableCompensation());
        assertMoney("366540.00", employee.taxableCompensation());
        assertMoney("17481.60", employee.taxWithheld());
        assertMoney("36654.00", employee.annualTaxDue());
    }

    @Test
    void createsValidatedPreviousEmployerDataWithAuditMetadata() {
        expectPermission();
        when(previousEmployerRepository.save(any(PreviousEmployerTaxData.class)))
                .thenAnswer(invocation -> {
                    PreviousEmployerTaxData saved = invocation.getArgument(0);
                    saved.setId(7L);
                    return saved;
                });
        PreviousEmployerTaxDataDTO request = new PreviousEmployerTaxDataDTO(
                null, null, null, "Previous Agency", "000-000-000",
                "Previous Employer Address", "1000",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 31),
                new BigDecimal("100000.00"), new BigDecimal("20000.00"),
                new BigDecimal("80000.00"), new BigDecimal("5000.00"),
                null, null, null, null);

        PreviousEmployerTaxDataDTO result = service.createPreviousEmployer(
                2026, "EMP-001", request, "payroll.officer", "Bearer token");

        assertEquals(7L, result.id());
        assertEquals("EMP-001", result.employeeNo());
        assertEquals("payroll.officer", result.createdBy());
        assertEquals("payroll.officer", result.updatedBy());
        assertMoney("80000.00", result.taxableCompensation());
        verify(previousEmployerRepository).save(any(PreviousEmployerTaxData.class));
    }

    @Test
    void savesVerifiedPeraEvidenceAndCalculatesFivePercentServerSide() {
        expectPermission();
        when(peraTaxCreditRepository.save(any(AnnualPeraTaxCredit.class)))
                .thenAnswer(invocation -> {
                    AnnualPeraTaxCredit saved = invocation.getArgument(0);
                    saved.setId(9L);
                    return saved;
                });
        PeraTaxCreditDTO request = new PeraTaxCreditDTO(
                null, null, null, new BigDecimal("10000.00"),
                new BigDecimal("99999.00"), "PERA-CERT-001", "Accredited Administrator",
                LocalDate.of(2027, 1, 15), null, null, null, null);

        PeraTaxCreditDTO result = service.savePeraTaxCredit(
                2026, "EMP-001", request, "payroll.officer", "Bearer token");

        assertEquals(9L, result.id());
        assertMoney("500.00", result.certifiedTaxCredit());
        assertEquals("payroll.officer", result.createdBy());
        verify(auditLogRepository).save(any());
    }

    @Test
    void appliesPeraCreditOnlyAgainstRemainingTaxAndNeverCreatesARefund() {
        PayrollDetail detail = detail();
        detail.setGrossAmount(100_000.0);
        PayrollDetailEarning basic = new PayrollDetailEarning(
                detail, "BASIC", "Basic Salary", 100_000.0, true, 0);
        basic.setBir2316ItemNo("39");
        when(detailRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(detail));
        when(earningRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(basic));
        when(deductionRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(new PayrollDetailDeduction(
                        detail, "WTX", "Withholding Tax", 1_000.0, 0.0, 0)));
        AnnualPeraTaxCredit evidence = new AnnualPeraTaxCredit();
        evidence.setEmployeeNo("EMP-001");
        evidence.setTaxYear(2026);
        evidence.setContributionAmount(new BigDecimal("10000.00"));
        evidence.setCertifiedTaxCredit(new BigDecimal("500.00"));
        evidence.setCertificateNo("PERA-CERT-001");
        evidence.setAdministratorName("Accredited Administrator");
        evidence.setCertificateIssuedDate(LocalDate.of(2026, 12, 20));
        when(peraTaxCreditRepository.findByTaxYear(2026)).thenReturn(List.of(evidence));
        expectPermission();
        expectTaxIdentities();
        expectMweSources(List.of(new AnnualizationServiceImpl.EmployeeMweStatusSource(
                1L, "EMP-001", LocalDate.of(2026, 1, 1), null,
                false, null, null, "hr.officer", null, "hr.officer", null)), List.of());
        when(restTemplate.exchange(
                contains("/api/wh-tax/annual-brackets?year=2026"),
                eq(HttpMethod.GET), any(HttpEntity.class),
                org.mockito.ArgumentMatchers
                        .<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualTaxBracket>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.AnnualTaxBracket(
                        BigDecimal.ZERO, null, BigDecimal.ZERO, new BigDecimal("0.10")))));

        AnnualizationEmployeeDTO result = service.getEmployeeAnnualization(
                2026, "EMP-001", "Bearer token");

        assertMoney("10000.00", result.annualTaxDue());
        assertMoney("500.00", result.peraTaxCredit());
        assertMoney("8500.00", result.adjustmentAmount());
        assertEquals("PERA-CERT-001", result.peraTaxCreditSource().certificateNo());

        detail.setGrossAmount(10_000.0);
        basic.setAmount(10_000.0);
        AnnualizationEmployeeDTO noRefund = service.getEmployeeAnnualization(
                2026, "EMP-001", "Bearer token");
        assertMoney("0.00", noRefund.peraTaxCredit());
        assertMoney("0.00", noRefund.adjustmentAmount());
    }

    @Test
    void rejectsPreviousEmployerCreationWithoutAddPermission() {
        when(restTemplate.exchange(
                contains("/api/authorization/effective?featureKey=payroll.annualization"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AnnualizationServiceImpl.EffectivePermission.class)))
                .thenReturn(ResponseEntity.ok(
                        new AnnualizationServiceImpl.EffectivePermission(
                                false, true, false, true, true, false)));
        PreviousEmployerTaxDataDTO request = new PreviousEmployerTaxDataDTO(
                null, null, null, "Previous Agency", "000-000-000",
                "Previous Employer Address", "1000",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 31),
                new BigDecimal("100000.00"), new BigDecimal("20000.00"),
                new BigDecimal("80000.00"), new BigDecimal("5000.00"),
                null, null, null, null);

        assertThrows(AccessDeniedException.class, () -> service.createPreviousEmployer(
                2026, "EMP-001", request, "payroll.officer", "Bearer token"));
        verify(previousEmployerRepository, never()).save(any());
    }

    @Test
    void resolvesMidYearMinimumWageRatesAndExcludesMweItemsFromTaxablePay() {
        PayrollDetail january = detail();
        january.setGrossAmount(20_000.0);
        PayrollDetail july = detail();
        july.setSalaryPeriodKey("2026-7-1");
        july.setSalaryDate(LocalDate.of(2026, 7, 15));
        july.setGrossAmount(22_000.0);
        PayrollDetailEarning januaryBasic = new PayrollDetailEarning(
                january, "BASIC", "Basic Salary", 20_000.0, true, 0);
        PayrollDetailEarning julyBasic = new PayrollDetailEarning(
                july, "BASIC", "Basic Salary", 22_000.0, true, 0);
        januaryBasic.setBir2316ItemNo("29");
        julyBasic.setBir2316ItemNo("29");

        when(detailRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(january, july));
        when(earningRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(januaryBasic, julyBasic));
        when(deductionRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(new PayrollDetailDeduction(
                        january, "GSIS", "GSIS", 2_000.0, 0.0, 0)));
        expectPermission();
        expectTaxIdentities();
        expectMweSources(
                List.of(new AnnualizationServiceImpl.EmployeeMweStatusSource(
                        1L, "EMP-001", LocalDate.of(2026, 1, 1), null,
                        true, "NCR", null, "hr.officer", null, "hr.officer", null)),
                List.of(
                        new AnnualizationServiceImpl.MinimumWageRateSource(
                                1L, "NCR", "National Capital Region",
                                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30),
                                new BigDecimal("645.00"), new BigDecimal("16827.00"),
                                "Wage Order NCR-01", null),
                        new AnnualizationServiceImpl.MinimumWageRateSource(
                                2L, "NCR", "National Capital Region",
                                LocalDate.of(2026, 7, 1), null,
                                new BigDecimal("695.00"), new BigDecimal("18139.00"),
                                "Wage Order NCR-02", null)));
        when(restTemplate.exchange(
                contains("/api/wh-tax/annual-brackets?year=2026"),
                eq(HttpMethod.GET), any(HttpEntity.class),
                org.mockito.ArgumentMatchers
                        .<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualTaxBracket>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.AnnualTaxBracket(
                        BigDecimal.ZERO, null, BigDecimal.ZERO, new BigDecimal("0.10")))));

        AnnualizationEmployeeDTO result = service.getEmployeeAnnualization(
                2026, "EMP-001", "Bearer token");

        assertTrue(result.readiness().ready());
        assertMoney("42000.00", result.nonTaxableCompensation());
        assertMoney("0.00", result.taxableCompensation());
        assertMoney("40000.00", result.bir2316Items().item29BasicSalaryMwe());
        assertMoney("2000.00", result.bir2316Items().item36MandatoryContributionsAndUnionDues());
        assertMoney("42000.00", result.bir2316Items().item38TotalNonTaxable());
        assertEquals("NCR", result.periods().get(0).minimumWageRegionCode());
        assertMoney("645.00", result.periods().get(0).minimumWageDailyRate());
        assertMoney("695.00", result.periods().get(1).minimumWageDailyRate());
        assertEquals("Wage Order NCR-02", result.periods().get(1).minimumWageLegalBasis());
    }

    @Test
    void reportsTheExactPayrollPeriodWhenMweStatusIsMissing() {
        PayrollDetail detail = detail();
        PayrollDetailEarning basic = new PayrollDetailEarning(
                detail, "BASIC", "Basic Salary", 110_000.0, true, 0);
        basic.setBir2316ItemNo("39");
        when(detailRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(detail));
        when(earningRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(basic));
        expectPermission();
        expectTaxIdentities();
        expectMweSources(List.of(), List.of());
        when(restTemplate.exchange(
                contains("/api/wh-tax/annual-brackets?year=2026"),
                eq(HttpMethod.GET), any(HttpEntity.class),
                org.mockito.ArgumentMatchers
                        .<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualTaxBracket>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.AnnualTaxBracket(
                        BigDecimal.ZERO, null, BigDecimal.ZERO, new BigDecimal("0.10")))));

        AnnualizationEmployeeDTO result = service.getAnnualization(2026, "Bearer token")
                .records().get(0);

        assertEquals("FOR_REVIEW", result.status());
        assertTrue(result.readiness().issues().contains(
                "MWE status is missing for salary period 2026-1-1 (2026-01-15)"));
    }

    @Test
    void buildsAlphalistPreviewFromFinalizedSnapshotsAndReportsBlockingIssues() {
        AnnualizationSnapshot ready = alphalistSnapshot(
                10L, "EMP-001", "Employee One", "000-000-002",
                new BigDecimal("120000.00"), new BigDecimal("20000.00"),
                new BigDecimal("100000.00"), new BigDecimal("10000.00"),
                new BigDecimal("9500.00"), new BigDecimal("500.00"),
                BigDecimal.ZERO, true);
        AnnualizationSnapshot invalid = alphalistSnapshot(
                11L, "EMP-002", "Employee Two", "123",
                new BigDecimal("80000.00"), new BigDecimal("10000.00"),
                new BigDecimal("70000.00"), new BigDecimal("7000.00"),
                new BigDecimal("6000.00"), BigDecimal.ZERO,
                new BigDecimal("25000.00"), true);
        AnnualizationSnapshotPeriod january = alphalistPeriod(
                10L, "2026-1-1", LocalDate.of(2026, 1, 15));
        AnnualizationSnapshotPeriod december = alphalistPeriod(
                10L, "2026-12-2", LocalDate.of(2026, 12, 31));

        when(snapshotRepository.findByTaxYearAndStatusOrderByEmployeeNameAsc(2026, "FINALIZED"))
                .thenReturn(List.of(ready, invalid));
        when(snapshotPeriodRepository
                .findBySnapshotIdInOrderBySnapshotIdAscSalaryDateAscSalaryPeriodKeyAsc(
                        List.of(10L, 11L)))
                .thenReturn(List.of(january, december));
        expectPermission();

        AnnualizationAlphalistPreviewDTO result = service.getAlphalistPreview(
                2026, "Bearer token");

        assertEquals(false, result.exportEnabled());
        assertEquals(2, result.employees());
        assertEquals(1, result.validEmployees());
        assertEquals(1, result.employeesWithIssues());
        assertEquals(2, result.substitutedFilingQualified());
        assertMoney("200000.00", result.totalGrossCompensation());
        assertMoney("170000.00", result.totalTaxableCompensation());
        assertMoney("17000.00", result.totalTaxDue());
        assertMoney("16000.00", result.totalTaxesWithheld());
        assertEquals(LocalDate.of(2026, 1, 15), result.records().get(0).payrollPeriodFrom());
        assertEquals(LocalDate.of(2026, 12, 31), result.records().get(0).payrollPeriodTo());
        assertEquals("Employee", result.records().get(0).employeeLastName());
        assertEquals("Filipino", result.records().get(0).employeeNationality());
        assertEquals("Regular", result.records().get(0).employeeEmploymentStatusName());
        assertEquals(1, result.records().get(0).birScheduleNo());
        assertEquals("NCR", result.records().get(0).birRegionCode());
        assertMoney("500.00", result.records().get(0).peraTaxCredit());
        assertMoney("0.00", result.records().get(0).amountWithheldAndPaidInDecember());
        assertMoney("0.00", result.records().get(0).overWithheldTaxRefunded());
        assertMoney("10000.00", result.records().get(0).actualTaxWithheldAdjusted());
        assertEquals(LocalDate.of(2026, 1, 1), result.records().get(0).employeeEmploymentFrom());
        assertTrue(result.records().get(1).issues().contains(
                "Employee TIN must contain exactly nine digits."));
        assertTrue(result.records().get(1).issues().contains(
                "Finalized payroll-period coverage is missing."));
        assertTrue(result.records().get(1).issues().contains(
                "Qualified substituted filing conflicts with previous-employer compensation."));
        verify(snapshotPeriodRepository)
                .findBySnapshotIdInOrderBySnapshotIdAscSalaryDateAscSalaryPeriodKeyAsc(
                        List.of(10L, 11L));
    }

    @Test
    void blocksSeparatedEmployeeUntilBirSeparationReasonIsMapped() {
        AnnualizationSnapshot separated = alphalistSnapshot(
                12L, "EMP-003", "Employee Three", "000-000-003",
                new BigDecimal("33750.00"), new BigDecimal("3205.00"),
                new BigDecimal("30545.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false);
        separated.setEmployeeEmploymentTo(LocalDate.of(2026, 6, 30));
        when(snapshotRepository.findByTaxYearAndStatusOrderByEmployeeNameAsc(2026, "FINALIZED"))
                .thenReturn(List.of(separated));
        when(snapshotPeriodRepository
                .findBySnapshotIdInOrderBySnapshotIdAscSalaryDateAscSalaryPeriodKeyAsc(List.of(12L)))
                .thenReturn(List.of(alphalistPeriod(
                        12L, "2026-6-1", LocalDate.of(2026, 6, 30))));
        expectPermission();

        AnnualizationAlphalistPreviewDTO preview = service.getAlphalistPreview(
                2026, "Bearer token");

        assertEquals(false, preview.exportEnabled());
        assertTrue(preview.records().get(0).issues().contains(
                "Map the employee's HR separation nature to a BIR reason (T, TR, R, or D) and refinalize."));
    }

    @Test
    void rejectsAlphalistExportWhenPreviewContainsUnresolvedRecords() {
        AnnualizationSnapshot invalid = alphalistSnapshot(
                11L, "EMP-002", "Employee Two", "123",
                new BigDecimal("80000.00"), new BigDecimal("10000.00"),
                new BigDecimal("70000.00"), new BigDecimal("7000.00"),
                new BigDecimal("6000.00"), BigDecimal.ZERO,
                BigDecimal.ZERO, true);

        when(snapshotRepository.findByTaxYearAndStatusOrderByEmployeeNameAsc(2026, "FINALIZED"))
                .thenReturn(List.of(invalid));
        when(snapshotPeriodRepository
                .findBySnapshotIdInOrderBySnapshotIdAscSalaryDateAscSalaryPeriodKeyAsc(
                        List.of(11L)))
                .thenReturn(List.of());
        expectPermission();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.exportAlphalist(
                        2026, "payroll.officer", "Bearer token"));

        assertEquals(409, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains(
                "Employee TIN must contain exactly nine digits."));
        verify(snapshotItemRepository, never()).findBySnapshotIdOrderByItemNoAsc(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void doesNotClaimCalculatedRefundWasPaidBeforeAdjustmentIsPosted() {
        AnnualizationSnapshot snapshot = alphalistSnapshot(
                13L, "EMP-004", "Employee One", "000-000-004",
                new BigDecimal("33750.00"), new BigDecimal("3205.00"),
                new BigDecimal("30545.00"), BigDecimal.ZERO,
                new BigDecimal("1456.80"), BigDecimal.ZERO, BigDecimal.ZERO, false);
        snapshot.setAdjustmentHeaderId(99L);
        snapshot.setAdjustmentSalaryPeriodKey("2026-10-1");
        when(periodLockRepository.existsBySalaryPeriodKey("2026-10-1"))
                .thenReturn(true);
        PayrollAdjustmentDTO pending = new PayrollAdjustmentDTO();
        pending.setId(99L);
        pending.setStatus("PENDING");
        when(payrollAdjustmentService.findAdjustment("EMP-004", "2026-10-1"))
                .thenReturn(java.util.Optional.of(pending));
        when(snapshotRepository.findByTaxYearAndStatusOrderByEmployeeNameAsc(2026, "FINALIZED"))
                .thenReturn(List.of(snapshot));
        when(snapshotPeriodRepository
                .findBySnapshotIdInOrderBySnapshotIdAscSalaryDateAscSalaryPeriodKeyAsc(List.of(13L)))
                .thenReturn(List.of(alphalistPeriod(13L, "2026-10-1", LocalDate.of(2026, 10, 30))));
        expectPermission();

        AnnualizationAlphalistEmployeeDTO result = service.getAlphalistPreview(
                2026, "Bearer token").records().get(0);

        assertMoney("0.00", result.overWithheldTaxRefunded());
        assertMoney("1456.80", result.actualTaxWithheldAdjusted());
        assertTrue(result.issues().contains(
                "Year-end adjustment is not posted in a locked payroll period or supported by a recorded paid refund voucher."));
    }

    @Test
    void reflectsPostedRefundInAlphalist() {
        AnnualizationSnapshot snapshot = alphalistSnapshot(
                14L, "EMP-005", "Employee One", "000-000-005",
                new BigDecimal("33750.00"), new BigDecimal("3205.00"),
                new BigDecimal("30545.00"), BigDecimal.ZERO,
                new BigDecimal("1456.80"), BigDecimal.ZERO, BigDecimal.ZERO, false);
        snapshot.setAdjustmentHeaderId(100L);
        snapshot.setAdjustmentSalaryPeriodKey("2026-10-1");
        when(periodLockRepository.existsBySalaryPeriodKey("2026-10-1"))
                .thenReturn(true);
        PayrollAdjustmentDTO posted = new PayrollAdjustmentDTO();
        posted.setId(100L);
        posted.setStatus("POSTED");
        posted.setLines(List.of(new PayrollAdjustmentLineDTO(
                "DEDUCTION", "WTX_ANNUALIZATION", "Annual withholding tax adjustment",
                -1456.80, null, false)));
        when(payrollAdjustmentService.findAdjustment("EMP-005", "2026-10-1"))
                .thenReturn(java.util.Optional.of(posted));
        when(snapshotRepository.findByTaxYearAndStatusOrderByEmployeeNameAsc(2026, "FINALIZED"))
                .thenReturn(List.of(snapshot));
        when(snapshotPeriodRepository
                .findBySnapshotIdInOrderBySnapshotIdAscSalaryDateAscSalaryPeriodKeyAsc(List.of(14L)))
                .thenReturn(List.of(alphalistPeriod(14L, "2026-10-1", LocalDate.of(2026, 10, 30))));
        expectPermission();

        AnnualizationAlphalistEmployeeDTO result = service.getAlphalistPreview(
                2026, "Bearer token").records().get(0);

        assertMoney("1456.80", result.overWithheldTaxRefunded());
        assertMoney("0.00", result.actualTaxWithheldAdjusted());
        assertTrue(result.valid());
    }

    @Test
    void postedAdjustmentInOpenPeriodIsNotReportedAsRefund() {
        AnnualizationSnapshot snapshot = alphalistSnapshot(
                15L, "EMP-006", "Employee One", "000-000-006",
                new BigDecimal("33750.00"), new BigDecimal("3205.00"),
                new BigDecimal("30545.00"), BigDecimal.ZERO,
                new BigDecimal("1456.80"), BigDecimal.ZERO, BigDecimal.ZERO, false);
        snapshot.setAdjustmentHeaderId(101L);
        snapshot.setAdjustmentSalaryPeriodKey("2026-10-1");
        when(snapshotRepository.findByTaxYearAndStatusOrderByEmployeeNameAsc(2026, "FINALIZED"))
                .thenReturn(List.of(snapshot));
        when(snapshotPeriodRepository
                .findBySnapshotIdInOrderBySnapshotIdAscSalaryDateAscSalaryPeriodKeyAsc(List.of(15L)))
                .thenReturn(List.of(alphalistPeriod(15L, "2026-10-1", LocalDate.of(2026, 10, 30))));
        expectPermission();

        AnnualizationAlphalistEmployeeDTO result = service.getAlphalistPreview(
                2026, "Bearer token").records().get(0);

        assertMoney("0.00", result.overWithheldTaxRefunded());
        assertTrue(result.issues().contains(
                "Year-end adjustment is not posted in a locked payroll period or supported by a recorded paid refund voucher."));
        verify(payrollAdjustmentService, never()).findAdjustment(any(), any());
    }

    @Test
    void paidVoucherReconcilesLockedPayrollWithoutChangingPayrollAdjustment() {
        AnnualizationSnapshot snapshot = alphalistSnapshot(
                16L, "EMP-007", "Employee One", "000-000-007",
                new BigDecimal("33750.00"), new BigDecimal("3205.00"),
                new BigDecimal("30545.00"), BigDecimal.ZERO,
                new BigDecimal("1456.80"), BigDecimal.ZERO, BigDecimal.ZERO, false);
        AnnualizationRefundVoucher voucher = new AnnualizationRefundVoucher();
        voucher.setSnapshotId(16L);
        voucher.setAmount(new BigDecimal("1456.80"));
        voucher.setVoucherReference("SIM-001");
        when(refundVoucherRepository.findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(16L))
                .thenReturn(java.util.Optional.of(voucher));
        when(snapshotPeriodRepository.findBySnapshotIdOrderBySalaryDateAscSalaryPeriodKeyAsc(16L))
                .thenReturn(List.of(alphalistPeriod(16L, "2026-10-1", LocalDate.of(2026, 10, 30))));
        when(periodLockRepository.existsBySalaryPeriodKey("2026-10-1")).thenReturn(true);
        when(snapshotRepository.findByTaxYearAndStatusOrderByEmployeeNameAsc(2026, "FINALIZED"))
                .thenReturn(List.of(snapshot));
        when(snapshotPeriodRepository
                .findBySnapshotIdInOrderBySnapshotIdAscSalaryDateAscSalaryPeriodKeyAsc(List.of(16L)))
                .thenReturn(List.of(alphalistPeriod(16L, "2026-10-1", LocalDate.of(2026, 10, 30))));
        expectPermission();

        AnnualizationAlphalistEmployeeDTO result = service.getAlphalistPreview(
                2026, "Bearer token").records().get(0);

        assertMoney("1456.80", result.overWithheldTaxRefunded());
        assertMoney("0.00", result.actualTaxWithheldAdjusted());
        assertTrue(result.valid());
        verify(payrollAdjustmentService, never()).findAdjustment(any(), any());
    }

    @Test
    void voucherRecordingRequiresPaymentAttestationAndRejectsDuplicate() {
        AnnualizationSnapshot snapshot = alphalistSnapshot(
                17L, "EMP-008", "Employee One", "000-000-008",
                new BigDecimal("33750.00"), new BigDecimal("3205.00"),
                new BigDecimal("30545.00"), BigDecimal.ZERO,
                new BigDecimal("1456.80"), BigDecimal.ZERO, BigDecimal.ZERO, false);
        snapshot.setAdjustmentType("REFUND");
        when(snapshotRepository.findFinalizedForUpdate("EMP-008", 2026))
                .thenReturn(java.util.Optional.of(snapshot));
        when(snapshotPeriodRepository.findBySnapshotIdOrderBySalaryDateAscSalaryPeriodKeyAsc(17L))
                .thenReturn(List.of(alphalistPeriod(17L, "2026-10-1", LocalDate.of(2026, 10, 30))));
        when(periodLockRepository.existsBySalaryPeriodKey("2026-10-1")).thenReturn(true);
        expectPermission();
        assertThrows(IllegalArgumentException.class, () -> service.recordRefundVoucher(
                2026, "EMP-008", new AnnualizationRefundVoucherRequest(
                        LocalDate.of(2026, 10, 30), "V-001", false),
                "officer", "Bearer token"));
        verify(refundVoucherRepository, never()).save(any());

        when(refundVoucherRepository.save(any(AnnualizationRefundVoucher.class)))
                .thenAnswer(invocation -> {
                    AnnualizationRefundVoucher saved = invocation.getArgument(0);
                    saved.setId(77L);
                    return saved;
                });
        var saved = service.recordRefundVoucher(2026, "EMP-008",
                new AnnualizationRefundVoucherRequest(
                        LocalDate.of(2026, 10, 30), " V-001 ", true),
                "officer", "Bearer token");
        assertEquals(77L, saved.id());
        assertMoney("1456.80", saved.amount());
        assertEquals("V-001", saved.voucherReference());

        AnnualizationRefundVoucher existing = new AnnualizationRefundVoucher();
        when(refundVoucherRepository.findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(17L))
                .thenReturn(java.util.Optional.of(existing));
        assertThrows(ResponseStatusException.class, () -> service.recordRefundVoucher(
                2026, "EMP-008", new AnnualizationRefundVoucherRequest(
                        LocalDate.of(2026, 10, 30), "V-002", true),
                "officer", "Bearer token"));
    }

    @Test
    void separationAdjustmentRejectsOpenPeriodExcludedFromAnnualization() {
        AnnualizationSnapshot snapshot = new AnnualizationSnapshot();
        snapshot.setAnnualizationType("SEPARATION");
        when(snapshotRepository.findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
                "EMP-008", 2026, "FINALIZED"))
                .thenReturn(java.util.Optional.of(snapshot));
        when(snapshotRepository.findFinalizedForUpdate("EMP-008", 2026))
                .thenReturn(java.util.Optional.of(snapshot));
        expectPermission();

        ResponseStatusException periodError = assertThrows(ResponseStatusException.class,
                () -> service.getEligibleAdjustmentPeriods(2026, "EMP-008", "Bearer token"));
        assertTrue(periodError.getReason().contains("excludes that period's compensation"));
        ResponseStatusException applyError = assertThrows(ResponseStatusException.class,
                () -> service.applyAdjustment(2026, "EMP-008", "2026-10-1", "officer", "Bearer token"));
        assertTrue(applyError.getReason().contains("excludes that period's compensation"));
        verify(payrollAdjustmentService, never()).createAnnualizationAdjustment(
                any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(), any(), any());
    }

    @Test
    void separationPreviewIncludesOpenLastSalaryAndLimitsWithholdingToAvailablePay() {
        PayrollDetail finalPay = detail();
        finalPay.setGrossAmount(110_000.0);
        finalPay.setNetAmount(20_000.0);
        PayrollDetailEarning basic = new PayrollDetailEarning(
                finalPay, "BASIC", "Basic Salary", 110_000.0, true, 0);
        basic.setBir2316ItemNo("39");
        finalPay.getEarnings().add(basic);
        finalPay.getDeductions().add(new PayrollDetailDeduction(
                finalPay, "GSIS", "GSIS", 11_000.0, 0.0, 0));
        finalPay.getDeductions().add(new PayrollDetailDeduction(
                finalPay, "WTX", "Withholding tax", 1_000.0, 0.0, 1));
        when(detailRepository.findByEmployeeNoAndSalaryPeriodKey("EMP-001", "2026-1-1"))
                .thenReturn(java.util.Optional.of(finalPay));
        when(detailRepository.findByEmployeeNoIgnoreCaseAndSalaryDateBetweenOrderBySalaryDateDesc(
                eq("EMP-001"), any(), any())).thenReturn(List.of(finalPay));
        expectPermission();
        expectTaxIdentities();
        doReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.AnnualizationEmployeeIdentity(
                "EMP-001", "000-000-002", "123", "0000", "NCR",
                "Employee Address", "2000", "Employee Local Address", "2001",
                LocalDate.of(1990, 5, 10), "09171234567", 7L,
                "Employee", "One", "Middle", null, "Filipino", 4L, 1,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))))
                .when(restTemplate).exchange(contains("/api/employees/annualization-identities"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualizationEmployeeIdentity>>>any());
        doReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.NatureOfSeparationIdentity(
                1L, "RESIGN", "Resignation", "R"))))
                .when(restTemplate).exchange(contains("/api/natureOfSeparation/get-all"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.NatureOfSeparationIdentity>>>any());
        expectMweSources(List.of(new AnnualizationServiceImpl.EmployeeMweStatusSource(
                1L, "EMP-001", LocalDate.of(2026, 1, 1), null,
                false, null, null, "hr.officer", null, "hr.officer", null)), List.of());
        when(restTemplate.exchange(contains("/api/wh-tax/annual-brackets?year=2026"),
                eq(HttpMethod.GET), any(HttpEntity.class),
                org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualTaxBracket>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.AnnualTaxBracket(
                        BigDecimal.ZERO, new BigDecimal("1000000"), BigDecimal.ZERO,
                        new BigDecimal("0.10")))));

        var review = service.reviewSeparationFinalPay(2026, "EMP-001", "2026-1-1", "Bearer token");
        assertMoney("99000.00", review.provisionalAnnualization().taxableCompensation());
        assertMoney("8900.00", review.provisionalAnnualization().adjustmentAmount());
        assertTrue(review.canApply(), review.actionRequired());
        when(payrollAdjustmentService.createAnnualizationAdjustment(
                eq("EMP-001"), eq("Employee One"), eq("2026-1-1"),
                eq(new BigDecimal("8900.00")), eq(2026), eq(0), eq("officer"), eq("Bearer token")))
                .thenReturn(new PayrollAdjustmentDTO());
        var prepared = service.prepareSeparationFinalPayAdjustment(
                2026, "EMP-001", "2026-1-1", "officer", "Bearer token");
        assertTrue(!prepared.canApply());
        assertTrue(prepared.actionRequired().contains("Post it"));

        finalPay.setNetAmount(5_000.0);
        var insufficient = service.reviewSeparationFinalPay(
                2026, "EMP-001", "2026-1-1", "Bearer token");
        assertTrue(!insufficient.canApply());
        assertTrue(insufficient.actionRequired().contains("Employer-advance evidence"));
        assertThrows(ResponseStatusException.class, () -> service.prepareSeparationFinalPayAdjustment(
                2026, "EMP-001", "2026-1-1", "officer", "Bearer token"));
        verify(payrollAdjustmentService, times(1)).createAnnualizationAdjustment(
                eq("EMP-001"), eq("Employee One"), eq("2026-1-1"),
                eq(new BigDecimal("8900.00")), eq(2026), eq(0), eq("officer"), eq("Bearer token"));
    }

    @Test
    void voidingVoucherPreservesEvidenceAndRequiresReason() {
        AnnualizationSnapshot snapshot = new AnnualizationSnapshot();
        snapshot.setId(18L);
        snapshot.setEmployeeNo("EMP-009");
        when(snapshotRepository.findFinalizedForUpdate("EMP-009", 2026))
                .thenReturn(java.util.Optional.of(snapshot));
        AnnualizationRefundVoucher voucher = new AnnualizationRefundVoucher();
        voucher.setVoucherReference("V-009");
        when(refundVoucherRepository.findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(18L))
                .thenReturn(java.util.Optional.of(voucher));
        expectPermission();

        assertThrows(IllegalArgumentException.class, () -> service.voidRefundVoucher(
                2026, "EMP-009", " ", "officer", "Bearer token"));
        verify(refundVoucherRepository, never()).save(any());

        service.voidRefundVoucher(2026, "EMP-009", "Wrong voucher reference", "officer", "Bearer token");
        assertEquals("officer", voucher.getVoidedBy());
        assertEquals("Wrong voucher reference", voucher.getVoidReason());
        assertTrue(voucher.getVoidedAt() != null);
        verify(refundVoucherRepository).save(voucher);
    }


    @Test
    void resolvesScheduleTwoRegionFromFinalizedMwePeriods() {
        AnnualizationSnapshot snapshot = alphalistSnapshot(
                12L, "EMP-003", "Employee Three", "000-000-003",
                new BigDecimal("90000.00"), new BigDecimal("90000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, false);
        AnnualizationSnapshotPeriod period = alphalistPeriod(
                12L, "2026-1-1", LocalDate.of(2026, 1, 15));
        period.setMinimumWageEarner(true);
        period.setMinimumWageRegionCode("NCR");
        period.setMinimumWageDailyRate(new BigDecimal("610.00"));
        period.setMinimumWageMonthlyRate(new BigDecimal("15921.00"));
        when(snapshotRepository.findByTaxYearAndStatusOrderByEmployeeNameAsc(2026, "FINALIZED"))
                .thenReturn(List.of(snapshot));
        when(snapshotPeriodRepository
                .findBySnapshotIdInOrderBySnapshotIdAscSalaryDateAscSalaryPeriodKeyAsc(List.of(12L)))
                .thenReturn(List.of(period));
        expectPermission();

        AnnualizationAlphalistEmployeeDTO result = service.getAlphalistPreview(
                2026, "Bearer token").records().get(0);

        assertEquals(2, result.birScheduleNo());
        assertEquals("NCR", result.birRegionCode());
        assertTrue(result.valid());
    }

    private void expectPermission() {
        when(restTemplate.exchange(
                contains("/api/authorization/effective?featureKey=payroll.annualization"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AnnualizationServiceImpl.EffectivePermission.class)))
                .thenReturn(ResponseEntity.ok(
                        new AnnualizationServiceImpl.EffectivePermission(
                                false, true, true, true, true, true)));
    }

    @Test
    void existingGeneratedEarningsUseSavedAdministrativeClassification() {
        PayrollDetail detail = detail();
        detail.setGrossAmount(33_750.0);
        PayrollDetailEarning basic = new PayrollDetailEarning(
                detail, "BASIC", "Basic Salary", 27_000.0, true, 0);
        PayrollDetailEarning hazard = new PayrollDetailEarning(
                detail, "HAZARD", "Hazard Pay", 6_750.0, false, 1);
        when(detailRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(detail));
        when(earningRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(basic, hazard));
        when(deductionRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(
                        new PayrollDetailDeduction(detail, "GSIS", "GSIS", 2_430.0, 0.0, 0),
                        new PayrollDetailDeduction(detail, "PHIC", "PhilHealth", 675.0, 0.0, 1),
                        new PayrollDetailDeduction(detail, "HDMF", "Pag-IBIG", 100.0, 0.0, 2)));
        org.mockito.Mockito.doReturn(ResponseEntity.ok(List.of(
                new AnnualizationServiceImpl.EarningTypeBenefitSource(
                        11L, "Basic", "Basic", null, true, false, true, "39"),
                new AnnualizationServiceImpl.EarningTypeBenefitSource(
                        15L, "Hazard Pay", "Hazard Pay", null, false, true, true, "49"))))
                .when(restTemplate).exchange(eq("http://administrative.test/api/earningType/get-all"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers
                                .<ParameterizedTypeReference<List<AnnualizationServiceImpl.EarningTypeBenefitSource>>>any());
        expectPermission();
        expectTaxIdentities();
        expectMweSources(List.of(new AnnualizationServiceImpl.EmployeeMweStatusSource(
                1L, "EMP-001", LocalDate.of(2026, 1, 1), null,
                false, null, null, "hr.officer", null, "hr.officer", null)), List.of());

        AnnualizationEmployeeDTO result = service.getAnnualization(2026, "Bearer token").records().get(0);

        assertMoney("23795.00", result.bir2316Items().item39BasicSalary());
        assertMoney("6750.00", result.bir2316Items().item49HazardPay());
        assertMoney("30545.00", result.taxableCompensation());
        assertTrue(result.readiness().issues().stream()
                .noneMatch(issue -> issue.contains("classification is missing")));
        assertTrue(result.readiness().issues().stream()
                .noneMatch(issue -> issue.contains("lower than mandatory contributions")));
    }

    @Test
    void benefitReviewRequiresOfficerSplitAndKeepsOriginalGross() {
        PayrollDetail detail = detail();
        PayrollDetailEarning basic = new PayrollDetailEarning(detail, "BASIC", "Basic Salary", 100_000.0, true, 0);
        basic.setBir2316ItemNo("39");
        PayrollDetailEarning clothing = new PayrollDetailEarning(detail, "CLOTHING", "Clothing Allowance", 10_000.0, false, 1);
        clothing.setId(44L);
        clothing.setBir2316ItemNo("35");
        when(detailRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(detail));
        when(earningRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(basic, clothing));
        expectPermission();
        expectTaxIdentities();
        expectMweSources(List.of(new AnnualizationServiceImpl.EmployeeMweStatusSource(
                1L, "EMP-001", LocalDate.of(2026, 1, 1), null,
                false, null, null, "hr.officer", null, "hr.officer", null)), List.of());
        org.mockito.Mockito.doReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.BenefitTaxProfileSource(
                1L, "CLOTHING", 2026, "Clothing", new BigDecimal("8000.00"), "ANNUAL",
                null, "35", null, "BIR RR 29-2025", "https://bir-cdn.bir.gov.ph/"))))
                .when(restTemplate).exchange(eq("http://administrative.test/api/benefit-tax-profiles?year=2026"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.BenefitTaxProfileSource>>>any());
        org.mockito.Mockito.doReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.EarningTypeBenefitSource(
                1L, "CLOTHING", "Clothing Allowance", "CLOTHING"))))
                .when(restTemplate).exchange(eq("http://administrative.test/api/earningType/get-all"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.EarningTypeBenefitSource>>>any());
        when(restTemplate.exchange(contains("/api/wh-tax/annual-brackets?year=2026"),
                eq(HttpMethod.GET), any(HttpEntity.class),
                org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualTaxBracket>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.AnnualTaxBracket(
                        BigDecimal.ZERO, new BigDecimal("1000000"), BigDecimal.ZERO, new BigDecimal("0.10")))));

        AnnualizationEmployeeDTO pending = service.getAnnualization(2026, "Bearer token").records().get(0);
        assertEquals("FOR_REVIEW", pending.status());
        assertTrue(pending.readiness().issues().stream().anyMatch(value -> value.contains("Clothing Allowance")));

        // A matching display name alone must not impose an Administrative profile.
        clothing.setEarningCode("UNLINKED_ACCOUNTING_CODE");
        AnnualizationEmployeeDTO unlinked = service.getAnnualization(2026, "Bearer token").records().get(0);
        assertTrue(unlinked.readiness().issues().stream().noneMatch(value -> value.contains("Clothing Allowance")));
        clothing.setEarningCode("CLOTHING");

        com.payroll.entitymodels.AnnualBenefitReview reviewed = new com.payroll.entitymodels.AnnualBenefitReview();
        reviewed.setSourceType("PAYROLL_EARNING");
        reviewed.setSourceId(44L);
        reviewed.setEmployeeNo("EMP-001");
        reviewed.setTaxYear(2026);
        reviewed.setSourceAmount(new BigDecimal("10000.00"));
        reviewed.setExemptAmount(new BigDecimal("8000.00"));
        reviewed.setExemptBirItem("35");
        reviewed.setOtherExemptAmount(new BigDecimal("1000.00"));
        reviewed.setOtherExemptBirItem("34");
        reviewed.setTaxableAmount(new BigDecimal("1000.00"));
        reviewed.setTaxableBirItem("48");
        reviewed.setProfileCode("CLOTHING");
        reviewed.setProfileLegalBasis("BIR RR 29-2025");
        reviewed.setProfileGuidanceCeiling(new BigDecimal("8000.00"));
        reviewed.setProfileCeilingPeriod("ANNUAL");
        when(benefitReviewRepository.findByTaxYear(2026)).thenReturn(List.of(reviewed));

        AnnualizationEmployeeDTO result = service.getAnnualization(2026, "Bearer token").records().get(0);
        assertEquals("COMPUTED", result.status());
        assertMoney("110000.00", result.presentGrossCompensation());
        assertMoney("9000.00", result.presentNonTaxableCompensation());
        assertMoney("101000.00", result.presentTaxableCompensation());
        assertMoney("8000.00", result.bir2316Items().item35DeMinimisBenefits());
        assertMoney("1000.00", result.bir2316Items().item34ThirteenthMonthAndOtherBenefits());
        assertMoney("1000.00", result.bir2316Items().item48TaxableThirteenthMonthBenefits());
    }

    @Test
    void rejectsBenefitReviewThatDoesNotReconcileToFinalizedEarning() {
        PayrollDetail detail = detail();
        PayrollDetailEarning clothing = new PayrollDetailEarning(detail, "CLOTHING", "Clothing Allowance", 10_000.0, false, 0);
        clothing.setId(44L);
        when(earningRepository.findFinalizedForAnnualization(any(), any(), eq(PayrollStatus.CANCELLED)))
                .thenReturn(List.of(clothing));
        expectPermission();
        org.mockito.Mockito.doReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.BenefitTaxProfileSource(
                1L, "CLOTHING", 2026, "Clothing", new BigDecimal("8000.00"), "ANNUAL",
                null, "35", null, "BIR RR 29-2025", "https://bir-cdn.bir.gov.ph/"))))
                .when(restTemplate).exchange(eq("http://administrative.test/api/benefit-tax-profiles?year=2026"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.BenefitTaxProfileSource>>>any());
        com.payroll.dtos.AnnualBenefitReviewRequest request = new com.payroll.dtos.AnnualBenefitReviewRequest(
                "CLOTHING", new BigDecimal("8000.00"), "35", new BigDecimal("1000.00"), "34",
                new BigDecimal("500.00"), "48", "Reviewed source evidence");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () ->
                service.saveBenefitReview(2026, "EMP-001", "PAYROLL_EARNING", 44L,
                        request, "payroll.officer", "Bearer token"));

        assertEquals(400, error.getStatusCode().value());
        verify(benefitReviewRepository, never()).save(any());
    }

    private void expectTaxIdentities() {
        when(systemConfigResolver.resolveApiUrl(
                eq(SystemConfigRuntimeResolver.API_HRM), any()))
                .thenReturn("http://hrm.test");
        when(restTemplate.exchange(
                contains("/api/settings/annualization-identity"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AnnualizationServiceImpl.AnnualizationEmployerIdentity.class)))
                .thenReturn(ResponseEntity.ok(
                        new AnnualizationServiceImpl.AnnualizationEmployerIdentity(
                                "ISOFT Agency", "000-000-001", "0000", "001",
                                "Agency Address", "1000")));
        org.mockito.Mockito.lenient().when(restTemplate.exchange(
                contains("/api/employees/annualization-identities"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                org.mockito.ArgumentMatchers
                        .<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualizationEmployeeIdentity>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(
                        new AnnualizationServiceImpl.AnnualizationEmployeeIdentity(
                                "EMP-001", "000-000-002", "123", "0000", "NCR",
                                "Employee Address", "2000",
                                "Employee Local Address", "2001",
                                LocalDate.of(1990, 5, 10), "09171234567", 7L,
                                "Employee", "One", "Middle", null,
                                "Filipino", 4L, null,
                                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))));
        when(restTemplate.exchange(
                contains("/api/job-position/get-all"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                org.mockito.ArgumentMatchers
                        .<ParameterizedTypeReference<List<AnnualizationServiceImpl.JobPositionIdentity>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(
                        new AnnualizationServiceImpl.JobPositionIdentity(7L, "Accountant III"))));
        when(restTemplate.exchange(
                contains("/api/natureOfAppointment/get-all"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                org.mockito.ArgumentMatchers
                        .<ParameterizedTypeReference<List<AnnualizationServiceImpl.NatureOfAppointmentIdentity>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(
                        new AnnualizationServiceImpl.NatureOfAppointmentIdentity(
                                4L, "PERM", "Permanent", false, "R"))));
    }

    private void expectMweSources(
            List<AnnualizationServiceImpl.EmployeeMweStatusSource> statuses,
            List<AnnualizationServiceImpl.MinimumWageRateSource> rates) {
        when(restTemplate.exchange(
                contains("/api/employee-mwe-statuses"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                org.mockito.ArgumentMatchers
                        .<ParameterizedTypeReference<List<AnnualizationServiceImpl.EmployeeMweStatusSource>>>any()))
                .thenReturn(ResponseEntity.ok(statuses));
        when(restTemplate.exchange(
                contains("/api/minimum-wage-rates"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                org.mockito.ArgumentMatchers
                        .<ParameterizedTypeReference<List<AnnualizationServiceImpl.MinimumWageRateSource>>>any()))
                .thenReturn(ResponseEntity.ok(rates));
    }

    private PayrollDetail detail() {
        PayrollDetail detail = new PayrollDetail();
        detail.setEmployeeNo("EMP-001");
        detail.setEmployeeName("Employee One");
        detail.setDepartment("Accounting");
        detail.setSalaryPeriodKey("2026-1-1");
        detail.setSalaryDate(LocalDate.of(2026, 1, 15));
        detail.setGrossAmount(110_000.0);
        detail.setTaxAmount(10_000.0);
        detail.setStatus(PayrollStatus.COMPUTED);
        return detail;
    }

    private AnnualizationSnapshot alphalistSnapshot(
            Long id, String employeeNo, String employeeName, String employeeTin,
            BigDecimal gross, BigDecimal nonTaxable, BigDecimal taxable,
            BigDecimal taxDue, BigDecimal taxWithheld, BigDecimal peraCredit,
            BigDecimal previousGross, Boolean substitutedFilingQualified) {
        AnnualizationSnapshot snapshot = new AnnualizationSnapshot();
        snapshot.setId(id);
        snapshot.setTaxYear(2026);
        snapshot.setEmployeeNo(employeeNo);
        snapshot.setEmployeeName(employeeName);
        snapshot.setEmployeeTin(employeeTin);
        snapshot.setEmployeeBirBranchCode("0000");
        snapshot.setEmployeeBirRegionCode("NCR");
        snapshot.setEmployeeLastName("Employee");
        snapshot.setEmployeeFirstName(employeeName.endsWith("One") ? "One" : "Two");
        snapshot.setEmployeeMiddleName("Middle");
        snapshot.setEmployeeNationality("Filipino");
        snapshot.setEmployeeEmploymentStatusCode("R");
        snapshot.setEmployeeEmploymentStatusName("Regular");
        snapshot.setEmployeeEmploymentFrom(LocalDate.of(2026, 1, 1));
        snapshot.setEmployeeEmploymentTo(LocalDate.of(2026, 12, 31));
        snapshot.setEmployerName("ISOFT Agency");
        snapshot.setEmployerTin("000-000-001");
        snapshot.setEmployerBirBranchCode("0000");
        snapshot.setEmployerRdoCode("001");
        snapshot.setGrossCompensation(gross);
        snapshot.setNonTaxableCompensation(nonTaxable);
        snapshot.setTaxableCompensation(taxable);
        snapshot.setAnnualTaxDue(taxDue);
        snapshot.setTaxWithheld(taxWithheld);
        snapshot.setPresentTaxWithheld(taxWithheld);
        snapshot.setPreviousTaxWithheld(BigDecimal.ZERO);
        snapshot.setPeraTaxCredit(peraCredit);
        snapshot.setAdjustmentAmount(taxDue.subtract(taxWithheld).subtract(peraCredit));
        snapshot.setPreviousGrossCompensation(previousGross);
        snapshot.setSubstitutedFilingQualified(substitutedFilingQualified);
        snapshot.setSubstitutedFilingReason("Reviewed by payroll");
        return snapshot;
    }

    private AnnualizationSnapshotPeriod alphalistPeriod(
            Long snapshotId, String salaryPeriodKey, LocalDate salaryDate) {
        AnnualizationSnapshotPeriod period = new AnnualizationSnapshotPeriod();
        period.setSnapshotId(snapshotId);
        period.setSalaryPeriodKey(salaryPeriodKey);
        period.setSalaryDate(salaryDate);
        period.setMinimumWageEarner(false);
        return period;
    }

    private PayrollAdjustmentLine adjustment(
            PayrollAdjustmentHeader header,
            String type,
            String code,
            double amount,
            Boolean taxable) {
        PayrollAdjustmentLine line = new PayrollAdjustmentLine();
        line.setHeader(header);
        line.setType(type);
        line.setCode(code);
        line.setName(code);
        line.setAmount(amount);
        line.setIsTaxable(taxable);
        return line;
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
