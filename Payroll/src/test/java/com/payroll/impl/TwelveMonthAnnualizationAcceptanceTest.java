package com.payroll.impl;

import com.hris.common.config.SystemConfigRuntimeResolver;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.payroll.dtos.AnnualizationAlphalistPreviewDTO;
import com.payroll.dtos.AnnualizationDatExportDTO;
import com.payroll.dtos.AnnualizationEmployeeDTO;
import com.payroll.dtos.Bir1604COfficerReviewRequest;
import com.payroll.dtos.AnnualizationRefundVoucherRequest;
import com.payroll.entitymodels.PayrollDetail;
import com.payroll.entitymodels.PayrollDetailDeduction;
import com.payroll.entitymodels.PayrollDetailEarning;
import com.payroll.entitymodels.PayrollPeriodLock;
import com.payroll.entitymodels.PayrollStatus;
import com.payroll.entitymodels.TaxRemittance1601C;
import com.payroll.reports.Bir1604COfficialFormWriter;
import com.payroll.reports.Bir2316OfficialFormWriter;
import com.payroll.repositories.AnnualBenefitReviewRepository;
import com.payroll.repositories.AnnualPeraTaxCreditRepository;
import com.payroll.repositories.AnnualizationAuditLogRepository;
import com.payroll.repositories.AnnualizationRefundVoucherRepository;
import com.payroll.repositories.AnnualizationSnapshotItemRepository;
import com.payroll.repositories.AnnualizationSnapshotPeriodRepository;
import com.payroll.repositories.AnnualizationSnapshotPreviousEmployerRepository;
import com.payroll.repositories.AnnualizationSnapshotRepository;
import com.payroll.repositories.Bir1604COfficerReviewAuditRepository;
import com.payroll.repositories.Bir1604COfficerReviewRepository;
import com.payroll.repositories.PayrollAdjustmentLineRepository;
import com.payroll.repositories.PayrollDetailDeductionRepository;
import com.payroll.repositories.PayrollDetailEarningRepository;
import com.payroll.repositories.PayrollDetailRepository;
import com.payroll.repositories.PayrollPeriodLockRepository;
import com.payroll.repositories.PreviousEmployerTaxDataRepository;
import com.payroll.repositories.TaxRemittance1601CAuditRepository;
import com.payroll.repositories.TaxRemittance1601CRepository;
import com.payroll.services.PayrollAdjustmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Isolated report-chain acceptance test. H2 rolls every synthetic row back after the test. */
@DataJpaTest(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TwelveMonthAnnualizationAcceptanceTest {
    private static final int YEAR = 2025;
    private static final String EMPLOYEE = "ACCEPT-2025-001";
    private static final BigDecimal MONTHLY_TAX = new BigDecimal("1456.75");

    @Autowired private PayrollDetailRepository details;
    @Autowired private PayrollDetailEarningRepository earnings;
    @Autowired private PayrollDetailDeductionRepository deductions;
    @Autowired private PayrollPeriodLockRepository locks;
    @Autowired private PayrollAdjustmentLineRepository adjustmentLines;
    @Autowired private PreviousEmployerTaxDataRepository previousEmployers;
    @Autowired private AnnualizationSnapshotRepository snapshots;
    @Autowired private AnnualizationSnapshotPeriodRepository snapshotPeriods;
    @Autowired private AnnualizationSnapshotPreviousEmployerRepository snapshotPrevious;
    @Autowired private AnnualizationSnapshotItemRepository snapshotItems;
    @Autowired private AnnualizationAuditLogRepository audits;
    @Autowired private AnnualPeraTaxCreditRepository peraCredits;
    @Autowired private AnnualBenefitReviewRepository benefitReviews;
    @Autowired private AnnualizationRefundVoucherRepository vouchers;
    @Autowired private TaxRemittance1601CRepository filed1601C;
    @Autowired private TaxRemittance1601CAuditRepository filed1601CAudits;
    @Autowired private Bir1604COfficerReviewRepository officerReviews;
    @Autowired private Bir1604COfficerReviewAuditRepository officerReviewAudits;

    @Test
    void twelveLockedMonthsReconcileThrough2316Dat1601CAnd1604CDraft() throws Exception {
        seedTwelveLockedPayrollPeriods();
        locks.deleteById(YEAR + "-12-1");
        locks.flush();
        assertEquals(11, details.findFinalizedForAnnualization(
                LocalDate.of(YEAR, 1, 1), LocalDate.of(YEAR, 12, 31), PayrollStatus.CANCELLED).size());
        locks.saveAndFlush(new PayrollPeriodLock(YEAR + "-12-1", "test.officer"));
        assertEquals(12, details.findFinalizedForAnnualization(
                LocalDate.of(YEAR, 1, 1), LocalDate.of(YEAR, 12, 31), PayrollStatus.CANCELLED).size());
        RestTemplate annualClient = mock(RestTemplate.class);
        RestTemplate remittanceClient = mock(RestTemplate.class);
        SystemConfigRuntimeResolver resolver = mock(SystemConfigRuntimeResolver.class);
        PayrollAdjustmentService adjustments = mock(PayrollAdjustmentService.class);
        stubAdministrativeAndHrm(annualClient, remittanceClient, resolver);

        AnnualizationServiceImpl annualization = annualization(annualClient, resolver, adjustments);

        AnnualizationEmployeeDTO computed = annualization.getEmployeeAnnualization(YEAR, EMPLOYEE, "Bearer test");
        assertEquals(12, computed.periods().size());
        assertMoney("405000.00", computed.grossCompensation());
        assertMoney("38460.00", computed.nonTaxableCompensation());
        assertMoney("366540.00", computed.taxableCompensation());
        assertMoney("17481.00", computed.taxWithheld());
        assertMoney("17481.00", computed.annualTaxDue());
        assertMoney("0.00", computed.adjustmentAmount());
        assertTrue(computed.readiness().ready(), () -> computed.readiness().issues().toString());

        AnnualizationEmployeeDTO finalized = annualization.finalizeAnnualization(
                YEAR, EMPLOYEE, "12-month synthetic acceptance fixture", true,
                "One employer; annual tax equals withholding", "test.officer", "Bearer test");
        assertEquals("FINALIZED", finalized.status());
        assertEquals(12, snapshotPeriods.findBySnapshotIdOrderBySalaryDateAscSalaryPeriodKeyAsc(
                snapshots.findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
                        EMPLOYEE, YEAR, "FINALIZED").orElseThrow().getId()).size());

        ByteArrayOutputStream certificate = new ByteArrayOutputStream();
        new Bir2316ReportServiceImpl(audits, adjustments, locks, snapshots, vouchers,
                snapshotPeriods, new Bir2316OfficialFormWriter())
                .generatePdf(finalized, "test.officer", certificate);
        String certificateText = pdfText(certificate.toByteArray());
        Files.write(Path.of("target", "acceptance-12-month-bir2316.pdf"), certificate.toByteArray());
        assertTrue(certificateText.contains("405,000.00"));
        assertTrue(certificateText.contains("366,540.00"));
        assertTrue(certificateText.contains("17,481.00"));

        AnnualizationAlphalistPreviewDTO preview = annualization.getAlphalistPreview(YEAR, "Bearer test");
        assertTrue(preview.exportEnabled(), () -> preview.records().get(0).issues().toString());
        assertEquals(1, preview.records().size());
        assertMoney("405000.00", preview.totalGrossCompensation());
        assertMoney("366540.00", preview.totalTaxableCompensation());
        assertMoney("17481.00", preview.totalTaxDue());
        assertMoney("17481.00", preview.totalTaxesWithheld());
        AnnualizationDatExportDTO dat = annualization.exportAlphalist(YEAR, "test.officer", "Bearer test");
        String datText = new String(dat.content(), StandardCharsets.US_ASCII);
        Files.write(Path.of("target", "acceptance-12-month-1604c.DAT"), dat.content());
        assertTrue(datText.contains("D1,\"1604C\""));
        assertTrue(datText.contains("405000.00"));
        assertTrue(datText.contains("366540.00"));
        assertTrue(datText.contains("17481.00"));

        TaxRemittance1601CService remittances = new TaxRemittance1601CService(
                filed1601C, filed1601CAudits, details, deductions, adjustmentLines,
                remittanceClient, resolver);
        ReflectionTestUtils.setField(remittances, "administrativeUrl", "http://administrative.test");
        var ledger = remittances.review(YEAR, "Bearer test");
        assertTrue(ledger.reconciled(), () -> ledger.issues().toString());
        assertMoney("17481.00", ledger.expectedPayrollWithholding());
        assertMoney("17481.00", ledger.confirmedTaxesWithheld());
        assertMoney("17481.00", ledger.confirmedAmountRemitted());

        Bir1604COfficerReviewService officer = new Bir1604COfficerReviewService(
                officerReviews, officerReviewAudits, snapshots, vouchers, annualization, remittances);
        var review = officer.save(YEAR, new Bir1604COfficerReviewRequest(
                "09170000000", "test@example.invalid", 1, "NO", null,
                BigDecimal.ZERO, null, "SIMULATED-1601C-2025",
                "Test Officer", "Payroll Officer", "123-456-789-000",
                "No employee refund or employer overremittance in this fixture.", "CONFIRMED"),
                "test.officer", "Bearer test");
        assertTrue(review.issues().isEmpty(), () -> review.issues().toString());
        ByteArrayOutputStream employerReturn = new ByteArrayOutputStream();
        new Bir1604CPreviewService(annualization, remittances, snapshots,
                new Bir1604COfficialFormWriter(), officer)
                .generateDraft(YEAR, "Bearer test", employerReturn);
        String employerText = pdfText(employerReturn.toByteArray());
        Files.write(Path.of("target", "acceptance-12-month-bir1604c-draft.pdf"), employerReturn.toByteArray());
        assertTrue(employerText.contains("DRAFT PREVIEW - NOT FOR FILING"));
        assertTrue(employerText.contains("17,481.00"));
        assertTrue(employerText.contains("Test Officer"));
        assertTrue(employerText.contains("SIM-1601C-1"));
        assertTrue(employerText.contains("SIM-1601C-12"));

        // A later correction must not leave the officer confirmation looking current.
        TaxRemittance1601C october = filed1601C.findByTaxYearAndTaxMonth(YEAR, 10).orElseThrow();
        october.setTaxesWithheld(MONTHLY_TAX.add(BigDecimal.ONE));
        october.setAmountRemitted(MONTHLY_TAX.add(BigDecimal.ONE));
        october.setUpdatedAt(LocalDateTime.now().plusSeconds(1));
        filed1601C.saveAndFlush(october);
        assertFalse(remittances.review(YEAR, "Bearer test").reconciled());
        assertFalse(officer.review(YEAR, "Bearer test").issues().isEmpty());
    }

    @Test
    void twelveMonthOverwithholdingNeedsPaidVoucherBefore2316AndDatAreReady() throws Exception {
        seedTwelveLockedPayrollPeriods();
        PayrollDetail december = details.findByEmployeeNoAndSalaryPeriodKey(EMPLOYEE, YEAR + "-12-1")
                .orElseThrow();
        december.setTaxAmount(1457.35);
        december.setTotalDeduction(4662.35);
        december.setNetAmount(29087.65);
        details.saveAndFlush(december);
        PayrollDetailDeduction withholding = deductions.findFinalizedForAnnualization(
                LocalDate.of(YEAR, 12, 1), LocalDate.of(YEAR, 12, 31), PayrollStatus.CANCELLED)
                .stream().filter(line -> "WTX".equals(line.getDeductionCode())).findFirst().orElseThrow();
        withholding.setAmount(1457.35);
        deductions.saveAndFlush(withholding);
        TaxRemittance1601C decemberReturn = filed1601C.findByTaxYearAndTaxMonth(YEAR, 12).orElseThrow();
        decemberReturn.setTaxesWithheld(new BigDecimal("1457.35"));
        decemberReturn.setAmountRemitted(new BigDecimal("1457.35"));
        filed1601C.saveAndFlush(decemberReturn);

        RestTemplate annualClient = mock(RestTemplate.class);
        RestTemplate remittanceClient = mock(RestTemplate.class);
        SystemConfigRuntimeResolver resolver = mock(SystemConfigRuntimeResolver.class);
        PayrollAdjustmentService adjustments = mock(PayrollAdjustmentService.class);
        stubAdministrativeAndHrm(annualClient, remittanceClient, resolver);
        AnnualizationServiceImpl annualization = annualization(annualClient, resolver, adjustments);
        AnnualizationEmployeeDTO computed = annualization.getEmployeeAnnualization(YEAR, EMPLOYEE, "Bearer test");
        assertEquals(12, computed.periods().size());
        assertMoney("17481.60", computed.taxWithheld());
        assertMoney("17481.00", computed.annualTaxDue());
        assertMoney("-0.60", computed.adjustmentAmount());
        AnnualizationEmployeeDTO finalized = annualization.finalizeAnnualization(
                YEAR, EMPLOYEE, "12-month refund fixture", false,
                "Employer-paid refund required before issuance", "test.officer", "Bearer test");
        Bir2316ReportServiceImpl certificateService = new Bir2316ReportServiceImpl(
                audits, adjustments, locks, snapshots, vouchers, snapshotPeriods,
                new Bir2316OfficialFormWriter());
        assertThrows(ResponseStatusException.class, () -> certificateService.prepareReportData(finalized));
        assertFalse(annualization.getAlphalistPreview(YEAR, "Bearer test").exportEnabled());

        var voucher = annualization.recordRefundVoucher(YEAR, EMPLOYEE,
                new AnnualizationRefundVoucherRequest(LocalDate.of(2026, 1, 5),
                        "SIMULATED-PAID-REFUND-2025", true), "test.officer", "Bearer test");
        assertMoney("0.60", voucher.amount());
        ByteArrayOutputStream certificate = new ByteArrayOutputStream();
        certificateService.generatePdf(finalized, "test.officer", certificate);
        Files.write(Path.of("target", "acceptance-12-month-refund-bir2316.pdf"), certificate.toByteArray());
        assertTrue(pdfText(certificate.toByteArray()).contains("17,481.00"));
        AnnualizationAlphalistPreviewDTO preview = annualization.getAlphalistPreview(YEAR, "Bearer test");
        assertTrue(preview.exportEnabled(), () -> preview.records().get(0).issues().toString());
        assertMoney("17481.00", preview.totalTaxesWithheld());
        AnnualizationDatExportDTO dat = annualization.exportAlphalist(YEAR, "test.officer", "Bearer test");
        Files.write(Path.of("target", "acceptance-12-month-refund-1604c.DAT"), dat.content());
        assertTrue(new String(dat.content(), StandardCharsets.US_ASCII).contains("17481.00"));

        TaxRemittance1601CService remittances = new TaxRemittance1601CService(
                filed1601C, filed1601CAudits, details, deductions, adjustmentLines,
                remittanceClient, resolver);
        ReflectionTestUtils.setField(remittances, "administrativeUrl", "http://administrative.test");
        assertTrue(remittances.review(YEAR, "Bearer test").reconciled());
        assertMoney("17481.60", remittances.review(YEAR, "Bearer test").confirmedTaxesWithheld());
        Bir1604COfficerReviewService officer = new Bir1604COfficerReviewService(
                officerReviews, officerReviewAudits, snapshots, vouchers, annualization, remittances);
        var review = officer.save(YEAR, new Bir1604COfficerReviewRequest(
                "09170000000", "test@example.invalid", 1, "YES", LocalDate.of(2026, 1, 5),
                new BigDecimal("0.60"), 2, "INDEPENDENT-SIMULATED-EMPLOYER-CREDIT-0.60",
                "Test Officer", "Payroll Officer", "123-456-789-000",
                "Paid employee voucher and separately reviewed employer credit of 0.60.", "CONFIRMED"),
                "test.officer", "Bearer test");
        assertMoney("0.60", review.withholdingGap());
        assertMoney("0.60", review.recordedEmployeeRefunds());
        assertTrue(review.issues().isEmpty(), () -> review.issues().toString());
        ByteArrayOutputStream employerReturn = new ByteArrayOutputStream();
        new Bir1604CPreviewService(annualization, remittances, snapshots,
                new Bir1604COfficialFormWriter(), officer)
                .generateDraft(YEAR, "Bearer test", employerReturn);
        Files.write(Path.of("target", "acceptance-12-month-refund-bir1604c-draft.pdf"),
                employerReturn.toByteArray());
        assertTrue(pdfText(employerReturn.toByteArray()).contains("DRAFT PREVIEW - NOT FOR FILING"));
    }

    @Test
    void juneSeparationUsesLastSixLockedPaymentsAndOfficialReasonInDat() throws Exception {
        seedTwelveLockedPayrollPeriods();
        for (int month = 7; month <= 12; month++) {
            String period = YEAR + "-" + month + "-1";
            locks.deleteById(period);
            PayrollDetail unusedFuturePayroll = details
                    .findByEmployeeNoAndSalaryPeriodKey(EMPLOYEE, period).orElseThrow();
            unusedFuturePayroll.setStatus(PayrollStatus.CANCELLED);
            details.save(unusedFuturePayroll);
        }
        locks.flush();
        details.flush();
        RestTemplate annualClient = mock(RestTemplate.class);
        RestTemplate remittanceClient = mock(RestTemplate.class);
        SystemConfigRuntimeResolver resolver = mock(SystemConfigRuntimeResolver.class);
        PayrollAdjustmentService adjustments = mock(PayrollAdjustmentService.class);
        stubAdministrativeAndHrm(annualClient, remittanceClient, resolver);
        doReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.AnnualizationEmployeeIdentity(
                EMPLOYEE, "987-654-321", "1234", "0000", "NCR", "Employee Address", "1000",
                "Employee Local Address", "1000", LocalDate.of(1990, 5, 10), "09170000001", 7L,
                "Acceptance", "Employee", "Test", null, "Filipino", 4L, 9,
                LocalDate.of(YEAR, 1, 1), LocalDate.of(YEAR, 6, 30)))))
                .when(annualClient).exchange(contains("/api/employees/annualization-identities"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualizationEmployeeIdentity>>>any());
        doReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.NatureOfSeparationIdentity(
                9L, "RES", "Resigned", "T"))))
                .when(annualClient).exchange(contains("/api/natureOfSeparation/get-all"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.NatureOfSeparationIdentity>>>any());
        AnnualizationServiceImpl annualization = annualization(annualClient, resolver, adjustments);
        AnnualizationEmployeeDTO computed = annualization.getEmployeeAnnualization(YEAR, EMPLOYEE, "Bearer test");
        assertEquals(6, computed.periods().size());
        assertMoney("202500.00", computed.grossCompensation());
        assertMoney("183270.00", computed.taxableCompensation());
        assertMoney("0.00", computed.annualTaxDue());
        assertMoney("-8740.50", computed.adjustmentAmount());
        assertTrue(computed.readiness().ready(), () -> computed.readiness().issues().toString());
        AnnualizationEmployeeDTO finalized = annualization.finalizeAnnualization(
                YEAR, EMPLOYEE, "June separation and last-pay review", false,
                "Separated during tax year", "test.officer", "Bearer test");
        assertEquals("SEPARATION", snapshots.findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
                EMPLOYEE, YEAR, "FINALIZED").orElseThrow().getAnnualizationType());
        assertFalse(annualization.getAlphalistPreview(YEAR, "Bearer test").exportEnabled());
        assertThrows(ResponseStatusException.class, () -> annualization.recordRefundVoucher(
                YEAR, EMPLOYEE, new AnnualizationRefundVoucherRequest(
                        LocalDate.of(YEAR, 7, 1), "LATE-REFUND", true),
                "test.officer", "Bearer test"));
        annualization.recordRefundVoucher(YEAR, EMPLOYEE,
                new AnnualizationRefundVoucherRequest(LocalDate.of(YEAR, 6, 28),
                        "SIMULATED-JUNE-LAST-PAY-REFUND", true), "test.officer", "Bearer test");
        Bir2316ReportServiceImpl certificateService = new Bir2316ReportServiceImpl(
                audits, adjustments, locks, snapshots, vouchers, snapshotPeriods,
                new Bir2316OfficialFormWriter());
        ByteArrayOutputStream certificate = new ByteArrayOutputStream();
        certificateService.generatePdf(finalized, "test.officer", certificate);
        Files.write(Path.of("target", "acceptance-june-separation-bir2316.pdf"), certificate.toByteArray());
        assertTrue(pdfText(certificate.toByteArray()).contains("06/30"));
        var preview = annualization.getAlphalistPreview(YEAR, "Bearer test");
        assertTrue(preview.exportEnabled(), () -> preview.records().get(0).issues().toString());
        assertEquals("T", preview.records().get(0).employeeBirSeparationReasonCode());
        String dat = new String(annualization.exportAlphalist(YEAR, "test.officer", "Bearer test").content(),
                StandardCharsets.US_ASCII);
        Files.writeString(Path.of("target", "acceptance-june-separation-1604c.DAT"), dat,
                StandardCharsets.US_ASCII);
        assertTrue(dat.contains("\"Filipino\",\"R\",\"T\",\"N\""), dat);
    }

    @Test
    void separationWithoutBirReasonCannotBeFinalized() throws Exception {
        seedTwelveLockedPayrollPeriods();
        for (int month = 7; month <= 12; month++) locks.deleteById(YEAR + "-" + month + "-1");
        locks.flush();
        RestTemplate annualClient = mock(RestTemplate.class);
        RestTemplate remittanceClient = mock(RestTemplate.class);
        SystemConfigRuntimeResolver resolver = mock(SystemConfigRuntimeResolver.class);
        PayrollAdjustmentService adjustments = mock(PayrollAdjustmentService.class);
        stubAdministrativeAndHrm(annualClient, remittanceClient, resolver);
        doReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.AnnualizationEmployeeIdentity(
                EMPLOYEE, "987-654-321", "1234", "0000", "NCR", "Employee Address", "1000",
                "Employee Local Address", "1000", LocalDate.of(1990, 5, 10), "09170000001", 7L,
                "Acceptance", "Employee", "Test", null, "Filipino", 4L, 9,
                LocalDate.of(YEAR, 1, 1), LocalDate.of(YEAR, 6, 30)))))
                .when(annualClient).exchange(contains("/api/employees/annualization-identities"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualizationEmployeeIdentity>>>any());
        AnnualizationServiceImpl annualization = annualization(annualClient, resolver, adjustments);
        AnnualizationEmployeeDTO computed = annualization.getEmployeeAnnualization(YEAR, EMPLOYEE, "Bearer test");
        assertFalse(computed.readiness().ready());
        assertTrue(computed.readiness().issues().stream().anyMatch(issue -> issue.contains("BIR reason")));
        assertThrows(ResponseStatusException.class, () -> annualization.finalizeAnnualization(
                YEAR, EMPLOYEE, "Unmapped nature", false,
                "Separated during tax year", "test.officer", "Bearer test"));
    }

    private AnnualizationServiceImpl annualization(RestTemplate annualClient,
            SystemConfigRuntimeResolver resolver, PayrollAdjustmentService adjustments) {
        AnnualizationServiceImpl service = new AnnualizationServiceImpl(
                details, earnings, deductions, adjustmentLines, previousEmployers,
                snapshots, snapshotPeriods, snapshotPrevious, snapshotItems, audits,
                peraCredits, locks, adjustments, annualClient, resolver,
                new Bir1604CDatWriter(), benefitReviews, vouchers);
        ReflectionTestUtils.setField(service, "administrativeUrl", "http://administrative.test");
        ReflectionTestUtils.setField(service, "humanResourceUrl", "http://hrm.test");
        return service;
    }

    private void seedTwelveLockedPayrollPeriods() {
        for (int month = 1; month <= 12; month++) {
            String key = YEAR + "-" + month + "-1";
            LocalDate salaryDate = LocalDate.of(YEAR, month, 28);
            PayrollDetail detail = new PayrollDetail();
            detail.setEmployeeNo(EMPLOYEE);
            detail.setEmployeeName("Acceptance Employee");
            detail.setDepartment("Test Office");
            detail.setSalaryGrade(11);
            detail.setSalaryStep(1);
            detail.setSalaryPeriodKey(key);
            detail.setCutoffStartDate(salaryDate.withDayOfMonth(1));
            detail.setCutoffEndDate(salaryDate);
            detail.setSalaryDate(salaryDate);
            detail.setBasicPerSalary(27000.0);
            detail.setActualBasic(27000.0);
            detail.setGrossAmount(33750.0);
            detail.setTaxableIncome(30545.0);
            detail.setTaxAmount(1456.75);
            detail.setTotalDeduction(4661.75);
            detail.setNetAmount(29088.25);
            detail.setComputedAt(LocalDateTime.of(YEAR, month, 28, 8, 0));
            detail.setLockedAt(LocalDateTime.of(YEAR, month, 28, 9, 0));
            detail.setIsLocked(true);
            detail.setStatus(PayrollStatus.LOCKED);
            details.save(detail);
            PayrollDetailEarning basic = new PayrollDetailEarning(detail, "BASIC", "Basic Salary", 27000.0, true, 0);
            basic.setBir2316ItemNo("39");
            PayrollDetailEarning hazard = new PayrollDetailEarning(detail, "HAZARD", "Hazard Pay", 6750.0, true, 1);
            hazard.setBir2316ItemNo("49");
            earnings.save(basic);
            earnings.save(hazard);
            deductions.save(new PayrollDetailDeduction(detail, "GSIS", "GSIS", 2430.0, 0.0, 0));
            deductions.save(new PayrollDetailDeduction(detail, "PHIC", "PhilHealth", 675.0, 0.0, 1));
            deductions.save(new PayrollDetailDeduction(detail, "HDMF", "Pag-IBIG", 100.0, 0.0, 2));
            deductions.save(new PayrollDetailDeduction(detail, "WTX", "Withholding Tax", 1456.75, 0.0, 3));
            locks.save(new PayrollPeriodLock(key, "test.officer"));

            TaxRemittance1601C filed = new TaxRemittance1601C();
            filed.setTaxYear(YEAR);
            filed.setTaxMonth(month);
            filed.setTaxesWithheld(MONTHLY_TAX);
            filed.setAdjustment(BigDecimal.ZERO);
            filed.setPenalties(BigDecimal.ZERO);
            filed.setAmountRemitted(MONTHLY_TAX);
            // Synthetic filing/payment evidence is dated early in the following month.
            filed.setFilingDate(salaryDate.withDayOfMonth(5).plusMonths(1));
            filed.setRemittanceDate(salaryDate.withDayOfMonth(5).plusMonths(1));
            filed.setRemittanceChannel("TEST BANK");
            filed.setReferenceNo("SIM-1601C-" + month);
            filed.setEvidenceReference("ROLLBACK-ONLY-TEST");
            filed.setStatus("CONFIRMED");
            filed.setRecordedBy("test.officer");
            filed.setRecordedAt(LocalDateTime.of(YEAR, month, 28, 10, 0));
            filed.setUpdatedBy("test.officer");
            filed.setUpdatedAt(LocalDateTime.of(YEAR, month, 28, 10, 0));
            filed1601C.save(filed);
        }
        details.flush();
    }

    private void stubAdministrativeAndHrm(RestTemplate annualClient, RestTemplate remittanceClient,
            SystemConfigRuntimeResolver resolver) {
        when(resolver.resolveApiUrl(eq(SystemConfigRuntimeResolver.API_ADMINISTRATIVE), any()))
                .thenReturn("http://administrative.test");
        when(resolver.resolveApiUrl(eq(SystemConfigRuntimeResolver.API_HRM), any()))
                .thenReturn("http://hrm.test");
        when(annualClient.exchange(contains("/api/authorization/effective?featureKey=payroll.annualization"),
                eq(HttpMethod.GET), any(HttpEntity.class), eq(AnnualizationServiceImpl.EffectivePermission.class)))
                .thenReturn(ResponseEntity.ok(new AnnualizationServiceImpl.EffectivePermission(
                        false, true, true, true, true, true)));
        when(remittanceClient.exchange(contains("/api/authorization/effective?featureKey=payroll.annualization"),
                eq(HttpMethod.GET), any(HttpEntity.class), eq(TaxRemittance1601CService.Permission.class)))
                .thenReturn(ResponseEntity.ok(new TaxRemittance1601CService.Permission(
                        false, true, true, true, true, true)));
        doReturn(ResponseEntity.ok(List.of())).when(annualClient).exchange(
                eq("http://administrative.test/api/benefit-tax-profiles?year=2025"), eq(HttpMethod.GET),
                any(HttpEntity.class),
                org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.BenefitTaxProfileSource>>>any());
        doReturn(ResponseEntity.ok(List.of())).when(annualClient).exchange(
                eq("http://administrative.test/api/earningType/get-all"), eq(HttpMethod.GET),
                any(HttpEntity.class),
                org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.EarningTypeBenefitSource>>>any());
        doReturn(ResponseEntity.ok(List.of(
                new AnnualizationServiceImpl.AnnualTaxBracket(BigDecimal.ZERO, new BigDecimal("250000"),
                        BigDecimal.ZERO, BigDecimal.ZERO),
                new AnnualizationServiceImpl.AnnualTaxBracket(new BigDecimal("250000"),
                        new BigDecimal("400000"), BigDecimal.ZERO, new BigDecimal("0.15")))))
                .when(annualClient).exchange(contains("/api/wh-tax/annual-brackets?year=2025"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualTaxBracket>>>any());
        when(annualClient.exchange(contains("/api/settings/annualization-identity"),
                eq(HttpMethod.GET), any(HttpEntity.class),
                eq(AnnualizationServiceImpl.AnnualizationEmployerIdentity.class)))
                .thenReturn(ResponseEntity.ok(new AnnualizationServiceImpl.AnnualizationEmployerIdentity(
                        "TEST GOVERNMENT AGENCY", "123-456-789", "0000", "1234", "Test Address", "1000")));
        doReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.AnnualizationEmployeeIdentity(
                EMPLOYEE, "987-654-321", "1234", "0000", "NCR", "Employee Address", "1000",
                "Employee Local Address", "1000", LocalDate.of(1990, 5, 10), "09170000001", 7L,
                "Acceptance", "Employee", "Test", null, "Filipino", 4L, null,
                LocalDate.of(YEAR, 1, 1), LocalDate.of(YEAR, 12, 31)))))
                .when(annualClient).exchange(contains("/api/employees/annualization-identities"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.AnnualizationEmployeeIdentity>>>any());
        doReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.JobPositionIdentity(7L, "Test Officer"))))
                .when(annualClient).exchange(contains("/api/job-position/get-all"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.JobPositionIdentity>>>any());
        doReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.NatureOfAppointmentIdentity(
                4L, "PERM", "Permanent", false, "R"))))
                .when(annualClient).exchange(contains("/api/natureOfAppointment/get-all"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.NatureOfAppointmentIdentity>>>any());
        doReturn(ResponseEntity.ok(List.of())).when(annualClient).exchange(
                contains("/api/natureOfSeparation/get-all"), eq(HttpMethod.GET), any(HttpEntity.class),
                org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.NatureOfSeparationIdentity>>>any());
        doReturn(ResponseEntity.ok(List.of(new AnnualizationServiceImpl.EmployeeMweStatusSource(
                1L, EMPLOYEE, LocalDate.of(YEAR, 1, 1), null,
                false, null, null, "test.officer", null, "test.officer", null))))
                .when(annualClient).exchange(contains("/api/employee-mwe-statuses"),
                        eq(HttpMethod.GET), any(HttpEntity.class),
                        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.EmployeeMweStatusSource>>>any());
        doReturn(ResponseEntity.ok(List.of())).when(annualClient).exchange(
                contains("/api/minimum-wage-rates"), eq(HttpMethod.GET), any(HttpEntity.class),
                org.mockito.ArgumentMatchers.<ParameterizedTypeReference<List<AnnualizationServiceImpl.MinimumWageRateSource>>>any());
    }

    private String pdfText(byte[] bytes) throws Exception {
        PdfReader reader = new PdfReader(bytes);
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            StringBuilder allPages = new StringBuilder();
            for (int page = 1; page <= reader.getNumberOfPages(); page++)
                allPages.append(extractor.getTextFromPage(page));
            return allPages.toString();
        } finally {
            reader.close();
        }
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
