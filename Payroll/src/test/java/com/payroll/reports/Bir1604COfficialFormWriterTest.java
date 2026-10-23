package com.payroll.reports;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.payroll.dtos.TaxRemittance1601CMonthDTO;
import com.payroll.dtos.Bir1604COfficerReviewDTO;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Bir1604COfficialFormWriterTest {
    @Test
    void overlaysConfirmedRemittanceOnOfficialFormAsDraft() throws Exception {
        List<TaxRemittance1601CMonthDTO> months = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            boolean confirmed = month == 10;
            months.add(new TaxRemittance1601CMonthDTO(2026, month,
                    confirmed ? new BigDecimal("1456.80") : BigDecimal.ZERO,
                    confirmed ? new BigDecimal("1456.80") : null,
                    confirmed ? BigDecimal.ZERO : null,
                    confirmed ? BigDecimal.ZERO : null,
                    confirmed ? new BigDecimal("1456.80") : null,
                    confirmed ? LocalDate.of(2026, 11, 5) : null,
                    confirmed ? LocalDate.of(2026, 11, 7) : null,
                    confirmed ? "AGENCY BANK" : null,
                    confirmed ? "TEST-REF" : null,
                    null, null, confirmed ? "CONFIRMED" : "MISSING", null, null, confirmed));
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Bir1604COfficerReviewDTO officer = new Bir1604COfficerReviewDTO(
                2026, true, new BigDecimal("1456.80"), BigDecimal.ZERO, new BigDecimal("1456.80"),
                new BigDecimal("1456.80"), "09123456789", "payroll@example.test", 1,
                "YES", LocalDate.of(2026, 11, 1), new BigDecimal("1456.80"), 12,
                "PAYMENT-REF", "Sample Officer", "Payroll Officer", "123-456-789-000",
                "Employee refund and employer credit independently verified.",
                "CONFIRMED", "officer", null, List.of());
        new Bir1604COfficialFormWriter().write(new Bir1604COfficialFormWriter.Data(
                2026, "1234567891231", "1234", "QUEZON CITY GENERAL HOSPITAL",
                "TEST ADDRESS", "1234", months, officer, "SIMULATION/DRAFT"), output);
        Files.write(Path.of("target", "bir1604c-draft-preview.pdf"), output.toByteArray());
        PdfReader reader = new PdfReader(output.toByteArray());
        try {
            assertEquals(1, reader.getNumberOfPages());
            assertEquals(612f, reader.getPageSize(1).getWidth());
            String text = new PdfTextExtractor(reader).getTextFromPage(1);
            assertTrue(text.contains("Annual Information Return"));
            assertTrue(text.contains("DRAFT PREVIEW - NOT FOR FILING"));
            assertTrue(text.contains("1,456.80"));
            assertTrue(text.contains("TEST-REF"));
            assertTrue(text.contains("09123456789"));
            assertTrue(text.contains("payroll@example.test"));
            assertTrue(text.contains("11/01/2026"));
        } finally {
            reader.close();
        }
    }
}
