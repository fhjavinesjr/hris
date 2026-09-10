package com.humanresource.onboarding;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppointmentDocumentReportRendererTest {
    private final AppointmentDocumentReportRenderer renderer = new AppointmentDocumentReportRenderer();

    @Test
    void rendersCurrentCscOathAndAssumptionForms() throws Exception {
        AppointmentReportData.LegalDocument data = new AppointmentReportData.LegalDocument(
                "Zamboanga City Medical Center", "Dr. Evangelista Street, Zamboanga City",
                "JUAN DELA CRUZ", "Zamboanga City", "Administrative Officer V",
                "Human Resource Management Office", "PhilSys ID", "0000-0000-0000",
                "August 1, 2026", "August 3, 2026", "August 3, 2026",
                "August 4, 2026", "Zamboanga City", "MARIA SANTOS", "Medical Center Chief",
                "MARIA SANTOS", "Medical Center Chief", "PEDRO REYES", "HRMO",
                "CSC-CS-FORM", "REVISED-2025", "a".repeat(64),
                "tester at 2026-08-04 08:00 PHT; Asia/Manila");

        byte[] oath = renderer.oath(data);
        byte[] assumption = renderer.assumption(data);
        assertPdf(oath);
        assertPdf(assumption);
        Files.write(Path.of("target/phase5f2-oath.pdf"), oath);
        Files.write(Path.of("target/phase5f2-assumption.pdf"), assumption);
    }

    @Test
    void rendersMultipageOnboardingCompletionWithoutEvidenceStoragePaths() throws Exception {
        List<AppointmentReportData.CompletionRow> rows = new ArrayList<>();
        for (int index = 1; index <= 75; index++) {
            rows.add(new AppointmentReportData.CompletionRow(index, "ITEM-" + index,
                    "Verified onboarding requirement " + index, "VERIFIED",
                    "f".repeat(64), "encoder", "independent-verifier"));
        }
        AppointmentReportData.OnboardingCompletion data = new AppointmentReportData.OnboardingCompletion(
                "Zamboanga City Medical Center", "Dr. Evangelista Street, Zamboanga City",
                "intake-1", "handoff-1", "selection-1", "application-1",
                "7 / JUAN DELA CRUZ", "8", "DEFAULT v1", "2026-08-04 08:00 PHT",
                "b".repeat(64), "tester at 2026-08-04 08:00 PHT; Asia/Manila", rows);

        byte[] pdf = renderer.onboarding(data);
        assertPdf(pdf);
        String bytes = new String(pdf, StandardCharsets.ISO_8859_1);
        assertTrue(!bytes.contains("C:\\uploads") && !bytes.contains("/uploads/"));
        Files.write(Path.of("target/phase5f2-onboarding.pdf"), pdf);
    }

    private static void assertPdf(byte[] pdf) {
        assertTrue(pdf.length > 1_000);
        assertTrue(new String(pdf, 0, 5, StandardCharsets.US_ASCII).startsWith("%PDF-"));
    }
}
