package com.payroll.reports;

import com.payroll.dtos.AnnualizationEmployeeDTO;
import com.payroll.dtos.AnnualizationPeriodDTO;
import com.payroll.dtos.AnnualizationTaxIdentityDTO;
import com.payroll.dtos.Bir2316CompensationItemsDTO;
import com.payroll.dtos.Bir2316ReportDTO;
import com.payroll.dtos.PreviousEmployerTaxDataDTO;
import com.payroll.dtos.PeraTaxCreditDTO;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Bir2316ReportTemplateTest {

    @Test
    void mapsFinalizedSnapshotOntoOfficialBirPdf() throws Exception {
        ClassPathResource resource = new ClassPathResource(
                "reports/annualization/bir_2316_official_2021.pdf");
        assertArrayEquals("%PDF".getBytes(StandardCharsets.US_ASCII),
                Arrays.copyOf(resource.getInputStream().readNBytes(4), 4));

        Bir2316ReportDTO reportData = Bir2316ReportDTO.from(representativeSnapshot(List.of()));
        assertEquals("01/01", reportData.periodFrom());
        assertEquals("12/31", reportData.periodTo());
        assertEquals(new BigDecimal("610.00"), reportData.statutoryDailyRate());
        assertEquals("X", reportData.mweMark());
        assertEquals(new BigDecimal("500"), reportData.item27PeraTaxCredit());
        assertEquals(new BigDecimal("52500.00"), reportData.item28TotalTaxesWithheld());
        assertEquals("SUBSTITUTED FILING — QUALIFIED", reportData.substitutedFilingTitle());

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new Bir2316OfficialFormWriter().write(reportData, output);
        byte[] pdf = output.toByteArray();
        assertArrayEquals("%PDF".getBytes(StandardCharsets.US_ASCII), Arrays.copyOf(pdf, 4));
        PdfReader reader = new PdfReader(pdf);
        try {
            assertEquals(1, reader.getNumberOfPages());
            assertEquals(612f, reader.getPageSize(1).getWidth());
            assertEquals(936f, reader.getPageSize(1).getHeight());
            String page = new PdfTextExtractor(reader).getTextFromPage(1);
            assertTrue(page.contains("Certificate of Compensation"));
            assertTrue(page.contains("2026"));
            assertTrue(page.contains("Dela Cruz, Juan Santos"));
            assertTrue(page.contains("ISOFT Test Agency"));
            assertTrue(page.contains("52,500.00"));
        } finally {
            reader.close();
        }
        Path preview = Path.of("target", "bir-2316-official-preview.pdf");
        Files.createDirectories(preview.getParent());
        Files.write(preview, pdf);
    }

    @Test
    void rejectsNonFinalizedAndMultiplePreviousEmployers() {
        AnnualizationEmployeeDTO computed = copyWithStatusAndPreviousEmployers(
                representativeSnapshot(List.of()), "COMPUTED", List.of());
        assertThrows(IllegalArgumentException.class, () -> Bir2316ReportDTO.from(computed));

        PreviousEmployerTaxDataDTO previous = previousEmployer(1L, "Previous Agency");
        AnnualizationEmployeeDTO multiple = copyWithStatusAndPreviousEmployers(
                representativeSnapshot(List.of()), "FINALIZED",
                List.of(previous, previousEmployer(2L, "Earlier Agency")));
        assertThrows(IllegalArgumentException.class, () -> Bir2316ReportDTO.from(multiple));
    }

    @Test
    void onePayrollPeriodStillUsesActualEmploymentDatesOn2316() {
        AnnualizationEmployeeDTO onePeriod = representativeSnapshot(List.of(), List.of(
                period("2026-10-1", LocalDate.of(2026, 10, 30), false, null, null)));

        Bir2316ReportDTO report = Bir2316ReportDTO.from(onePeriod);

        assertEquals("01/01", report.periodFrom());
        assertEquals("12/31", report.periodTo());
    }

    @Test
    void reportsOnlyPostedRefundAsReducedPresentEmployerWithholding() {
        AnnualizationEmployeeDTO source = representativeSnapshot(List.of());
        Bir2316ReportDTO unposted = Bir2316ReportDTO.from(source);
        Bir2316ReportDTO posted = Bir2316ReportDTO.from(source, new BigDecimal("-500.00"));

        assertEquals(unposted.item25APresentWithheld().subtract(new BigDecimal("500.00")),
                posted.item25APresentWithheld());
        assertEquals(unposted.item26TotalWithheldAdjusted().subtract(new BigDecimal("500.00")),
                posted.item26TotalWithheldAdjusted());
        assertEquals(unposted.item28TotalTaxesWithheld().subtract(new BigDecimal("500.00")),
                posted.item28TotalTaxesWithheld());
    }

    @Test
    void rejectsMissingEmploymentDatesInsteadOfUsingPayrollDate() {
        AnnualizationEmployeeDTO source = mock(AnnualizationEmployeeDTO.class);
        when(source.status()).thenReturn("FINALIZED");
        when(source.previousEmployers()).thenReturn(List.of());
        when(source.taxIdentity()).thenReturn(mock(AnnualizationTaxIdentityDTO.class));
        when(source.bir2316Items()).thenReturn(mock(Bir2316CompensationItemsDTO.class));
        when(source.periods()).thenReturn(List.of(
                period("2026-10-1", LocalDate.of(2026, 10, 30), false, null, null)));

        assertThrows(IllegalArgumentException.class, () -> Bir2316ReportDTO.from(source));
    }

    private AnnualizationEmployeeDTO representativeSnapshot(List<PreviousEmployerTaxDataDTO> previousEmployers) {
        return representativeSnapshot(previousEmployers, List.of(
                period("2026-01", LocalDate.of(2026, 1, 15), false, null, null),
                period("2026-06", LocalDate.of(2026, 6, 15), true, "NCR", "570.00"),
                period("2026-12", LocalDate.of(2026, 12, 15), true, "NCR", "610.00")));
    }

    private AnnualizationEmployeeDTO representativeSnapshot(
            List<PreviousEmployerTaxDataDTO> previousEmployers, List<AnnualizationPeriodDTO> periods) {
        Bir2316CompensationItemsDTO items = new Bir2316CompensationItemsDTO(
                bd("120000"), bd("5000"), bd("3000"), bd("1000"), bd("0"),
                bd("90000"), bd("24000"), bd("36000"), "Other exempt", bd("1000"),
                bd("280000"), bd("360000"), bd("12000"), bd("12000"), bd("0"),
                bd("0"), "Other regular", bd("1000"), "", bd("0"), bd("0"),
                bd("0"), bd("0"), bd("30000"), bd("0"), bd("10000"),
                "Supplementary", bd("1000"), "", bd("0"), bd("426000"));
        AnnualizationTaxIdentityDTO identity = new AnnualizationTaxIdentityDTO(
                "ISOFT Test Agency", "123-456-789-000", "0000", "001", "Test City", "1000",
                "987-654-321-000", "0000", "0001", "002", "Employee Address", "1001",
                "Employee Local Address", "1002", LocalDate.of(1990, 6, 15),
                "09171234567", "Payroll Officer",
                "Dela Cruz", "Juan", "Santos", null, "Filipino",
                "R", "Regular", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), null);
        return new AnnualizationEmployeeDTO(
                "EMP-001", "JUAN DELA CRUZ", "Finance", 2026,
                bd("706000"), bd("280000"), bd("426000"), bd("52000"),
                bd("0"), bd("0"), bd("0"), bd("0"),
                bd("706000"), bd("280000"), bd("426000"),
                bd("24000"), bd("6000"), bd("6000"), bd("52000"),
                bd("52500"), bd("500"),
                new PeraTaxCreditDTO(null, "EMP-001", 2026, bd("10000"), bd("500"),
                        "PERA-2026-001", "Test PERA Administrator", LocalDate.of(2026, 12, 20),
                        null, null, null, null),
                bd("0"), "NO_ADJUSTMENT", "FINALIZED", "Ready", 3,
                null, "payroll.admin", true,
                "Pure compensation income from this employer only; withholding reconciled",
                LocalDateTime.of(2027, 1, 10, 9, 0), "payroll.admin",
                null, null, null, null,
                previousEmployers, periods, items, identity, null);
    }

    private AnnualizationPeriodDTO period(
            String key, LocalDate date, Boolean mwe, String region, String dailyRate) {
        return new AnnualizationPeriodDTO(
                key, date, bd("100000"), bd("20000"), bd("80000"),
                bd("4000"), bd("1000"), bd("1000"), bd("8000"), mwe, region,
                dailyRate == null ? null : bd(dailyRate),
                dailyRate == null ? null : bd("15860.00"),
                dailyRate == null ? null : "Wage Order Test");
    }

    private PreviousEmployerTaxDataDTO previousEmployer(Long id, String name) {
        return new PreviousEmployerTaxDataDTO(
                id, "EMP-001", 2026, name, "111-222-333-000", "Address", "1000",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28),
                bd("100000"), bd("20000"), bd("80000"), bd("5000"),
                null, null, "user", "user");
    }

    private AnnualizationEmployeeDTO copyWithStatusAndPreviousEmployers(
            AnnualizationEmployeeDTO source, String status,
            List<PreviousEmployerTaxDataDTO> previousEmployers) {
        return new AnnualizationEmployeeDTO(
                source.employeeNo(), source.employeeName(), source.department(), source.taxYear(),
                source.presentGrossCompensation(), source.presentNonTaxableCompensation(),
                source.presentTaxableCompensation(), source.presentTaxWithheld(),
                source.previousGrossCompensation(), source.previousNonTaxableCompensation(),
                source.previousTaxableCompensation(), source.previousTaxWithheld(),
                source.grossCompensation(), source.nonTaxableCompensation(), source.taxableCompensation(),
                source.gsisContribution(), source.philHealthContribution(), source.pagIbigContribution(),
                source.taxWithheld(), source.annualTaxDue(), source.peraTaxCredit(),
                source.peraTaxCreditSource(), source.adjustmentAmount(),
                source.adjustmentType(), status, source.reviewMessage(), source.snapshotVersion(),
                source.finalizedAt(), source.finalizedBy(), source.substitutedFilingQualified(),
                source.substitutedFilingReason(), source.substitutedFilingReviewedAt(),
                source.substitutedFilingReviewedBy(), source.adjustmentHeaderId(),
                source.adjustmentSalaryPeriodKey(), source.adjustmentAppliedAt(),
                source.adjustmentAppliedBy(), previousEmployers, source.periods(),
                source.bir2316Items(), source.taxIdentity(), source.readiness());
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
