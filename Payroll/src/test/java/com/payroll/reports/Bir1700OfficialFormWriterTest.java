package com.payroll.reports;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.payroll.dtos.AnnualizationTaxIdentityDTO;
import com.payroll.dtos.Bir1700ReviewDTO;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Bir1700OfficialFormWriterTest {
    @Test
    void overlaysOfficialTwoPageFormAndMarksItDraft() throws Exception {
        AnnualizationTaxIdentityDTO identity = mock(AnnualizationTaxIdentityDTO.class);
        when(identity.employeeTin()).thenReturn("000000001");
        when(identity.employeeBirBranchCode()).thenReturn("0000");
        when(identity.employeeRdoCode()).thenReturn("1234");
        when(identity.employeeLastName()).thenReturn("Reyes");
        when(identity.employeeFirstName()).thenReturn("Maria");
        when(identity.employeeAddress()).thenReturn("2 Mabini Ave, San Jose");
        when(identity.employeeZipCode()).thenReturn("1001");
        when(identity.employeeNationality()).thenReturn("Filipino");
        Bir1700ReviewDTO review = new Bir1700ReviewDTO("202600001", 2026, 1,
                "Maria Reyes", "TAX_YEAR_IN_PROGRESS", List.of(), 1,
                new BigDecimal("33750.00"), new BigDecimal("3205.00"),
                new BigDecimal("30545.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                List.of(new Bir1700ReviewDTO.EmployerLine("Test Employer", "123456789",
                        new BigDecimal("33750.00"), BigDecimal.ZERO)));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new Bir1700OfficialFormWriter().write(review, identity, output);

        PdfReader reader = new PdfReader(output.toByteArray());
        try {
            assertEquals(2, reader.getNumberOfPages());
            String secondPage = new PdfTextExtractor(reader).getTextFromPage(2);
            assertTrue(secondPage.contains("33750"));
            assertTrue(secondPage.contains("3205"));
            assertTrue(secondPage.contains("30545"));
            assertTrue(new PdfTextExtractor(reader).getTextFromPage(1).contains("DRAFT - NOT FOR FILING"));
        } finally {
            reader.close();
        }
        String preview = System.getProperty("bir1700.preview");
        if (preview != null) Files.write(Path.of(preview), output.toByteArray());
    }
}
