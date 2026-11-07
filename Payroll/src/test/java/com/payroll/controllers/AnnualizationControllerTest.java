package com.payroll.controllers;

import com.payroll.dtos.AnnualizationEmployeeDTO;
import com.payroll.dtos.AnnualizationAlphalistPreviewDTO;
import com.payroll.dtos.AnnualizationDatExportDTO;
import com.payroll.dtos.AnnualizationFinalizationRequest;
import com.payroll.dtos.PeraTaxCreditDTO;
import com.payroll.dtos.AnnualBenefitReviewRequest;
import com.payroll.dtos.AnnualBenefitReviewLineDTO;
import com.payroll.dtos.Bir1700ReviewDTO;
import com.payroll.services.AnnualizationService;
import com.payroll.services.Bir2316ReportService;
import com.payroll.impl.Bir1700ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnualizationControllerTest {

    @Mock private AnnualizationService annualizationService;
    @Mock private Bir2316ReportService reportService;
    @Mock private Bir1700ReviewService bir1700ReviewService;
    @Mock private Authentication authentication;

    private AnnualizationController controller;

    @BeforeEach
    void setUp() {
        controller = new AnnualizationController(annualizationService, reportService,
                bir1700ReviewService);
    }

    @Test
    void generatesInline2316OnlyFromFinalizedSnapshot() throws Exception {
        AnnualizationEmployeeDTO annualization = mock(AnnualizationEmployeeDTO.class);
        when(annualization.status()).thenReturn("FINALIZED");
        when(annualizationService.getEmployeeAnnualization(2026, "EMP/001", "Bearer token"))
                .thenReturn(annualization);
        when(authentication.getName()).thenReturn("payroll.admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.getBir2316("EMP/001", 2026, authentication, "Bearer token", response);

        assertEquals("application/pdf", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition")
                .contains("BIR_2316_EMP_001_2026.pdf"));
        verify(reportService).generatePdf(
                eq(annualization), eq("payroll.admin"), any(OutputStream.class));
    }

    @Test
    void rejectsCertificateBeforeFinalization() throws Exception {
        AnnualizationEmployeeDTO annualization = mock(AnnualizationEmployeeDTO.class);
        when(annualization.status()).thenReturn("COMPUTED");
        when(annualizationService.getEmployeeAnnualization(2026, "EMP-001", null))
                .thenReturn(annualization);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.getBir2316(
                        "EMP-001", 2026, authentication, null, new MockHttpServletResponse()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(reportService, never()).generatePdf(any(), any(), any());
    }

    @Test
    void reviews1700ThroughAuthorizedAnnualizationLookup() {
        AnnualizationEmployeeDTO annualization = mock(AnnualizationEmployeeDTO.class);
        Bir1700ReviewDTO review = mock(Bir1700ReviewDTO.class);
        when(annualizationService.getEmployeeAnnualization(2026, "EMP-001", "Bearer token"))
                .thenReturn(annualization);
        when(bir1700ReviewService.review(annualization)).thenReturn(review);

        assertEquals(review, controller.reviewBir1700("EMP-001", 2026, "Bearer token").getBody());
        verify(bir1700ReviewService).review(annualization);
    }

    @Test
    void returnsBufferedOfficial1700Draft() throws Exception {
        AnnualizationEmployeeDTO annualization = mock(AnnualizationEmployeeDTO.class);
        byte[] pdf = new byte[] { 37, 80, 68, 70 };
        when(annualizationService.getEmployeeAnnualization(2026, "EMP/001", "Bearer token"))
                .thenReturn(annualization);
        when(authentication.getName()).thenReturn("payroll.admin");
        when(bir1700ReviewService.generateDraft(annualization, "payroll.admin")).thenReturn(pdf);

        var response = controller.getBir1700Draft("EMP/001", 2026, authentication, "Bearer token");

        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertEquals("inline; filename=\"BIR_1700_DRAFT_EMP_001_2026.pdf\"",
                response.getHeaders().getFirst("Content-Disposition"));
        assertEquals(pdf, response.getBody());
    }

    @Test
    void savesAndReadsPeraTaxCreditThroughTypedEndpoints() {
        PeraTaxCreditDTO request = new PeraTaxCreditDTO(
                null, null, null, new BigDecimal("10000.00"), null,
                "PERA-CERT-001", "Accredited Administrator",
                LocalDate.of(2026, 12, 20), null, null, null, null);
        PeraTaxCreditDTO saved = new PeraTaxCreditDTO(
                4L, "EMP-001", 2026, new BigDecimal("10000.00"),
                new BigDecimal("500.00"), "PERA-CERT-001", "Accredited Administrator",
                LocalDate.of(2026, 12, 20), null, null, "admin", "admin");
        when(authentication.getName()).thenReturn("admin");
        when(annualizationService.savePeraTaxCredit(
                2026, "EMP-001", request, "admin", "Bearer token")).thenReturn(saved);
        when(annualizationService.getPeraTaxCredit(2026, "EMP-001", "Bearer token"))
                .thenReturn(saved);

        assertEquals(saved, controller.savePeraTaxCredit(
                "EMP-001", 2026, request, authentication, "Bearer token").getBody());
        assertEquals(saved, controller.getPeraTaxCredit(
                "EMP-001", 2026, "Bearer token").getBody());
    }

    @Test
    void savesAndListsBenefitReviewsThroughTypedEndpoints() {
        AnnualBenefitReviewRequest request = new AnnualBenefitReviewRequest("CLOTHING",
                new BigDecimal("8000.00"), "35", new BigDecimal("1000.00"), "34",
                new BigDecimal("1000.00"), "48", "Checked BIR rules and prior benefits");
        AnnualBenefitReviewLineDTO line = mock(AnnualBenefitReviewLineDTO.class);
        when(authentication.getName()).thenReturn("payroll.admin");
        when(annualizationService.saveBenefitReview(2026, "EMP-001", "PAYROLL_EARNING", 44L,
                request, "payroll.admin", "Bearer token")).thenReturn(line);
        when(annualizationService.getBenefitReviewLines(2026, "EMP-001", "Bearer token"))
                .thenReturn(List.of(line));

        assertEquals(line, controller.saveBenefitReview("EMP-001", "PAYROLL_EARNING", 44L,
                2026, request, authentication, "Bearer token").getBody());
        assertEquals(List.of(line), controller.getBenefitReviews("EMP-001", 2026, "Bearer token").getBody());
    }

    @Test
    void returnsNoContentWhenPeraEvidenceDoesNotExist() {
        when(annualizationService.getPeraTaxCredit(2026, "EMP-001", "Bearer token"))
                .thenReturn(null);

        assertEquals(HttpStatus.NO_CONTENT, controller.getPeraTaxCredit(
                "EMP-001", 2026, "Bearer token").getStatusCode());
    }

    @Test
    void finalizesWithTypedSubstitutedFilingAssessment() {
        AnnualizationEmployeeDTO finalized = mock(AnnualizationEmployeeDTO.class);
        AnnualizationFinalizationRequest request = new AnnualizationFinalizationRequest(
                "Reviewed", true,
                "Pure compensation income from this employer only; withholding reconciled");
        when(authentication.getName()).thenReturn("payroll.admin");
        when(annualizationService.finalizeAnnualization(
                2026, "EMP-001", request.remarks(), true,
                request.substitutedFilingReason(), "payroll.admin", "Bearer token"))
                .thenReturn(finalized);

        assertEquals(finalized, controller.finalizeAnnualization(
                "EMP-001", 2026, request, authentication, "Bearer token").getBody());
        verify(annualizationService).finalizeAnnualization(
                2026, "EMP-001", request.remarks(), true,
                request.substitutedFilingReason(), "payroll.admin", "Bearer token");
    }

    @Test
    void returnsTypedAlphalistPreview() {
        AnnualizationAlphalistPreviewDTO preview = new AnnualizationAlphalistPreviewDTO(
                2026, "BIR Alphalist v7.4 review preview", false,
                "ISOFT Agency", "000-000-001", "0000", "001",
                0, 0, 0, 0, 0, 0,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                List.of("No finalized records"), List.of("Export disabled"), List.of());
        when(annualizationService.getAlphalistPreview(2026, "Bearer token"))
                .thenReturn(preview);

        assertEquals(preview, controller.getAlphalistPreview(
                2026, "Bearer token").getBody());
        verify(annualizationService).getAlphalistPreview(2026, "Bearer token");
    }

    @Test
    void downloadsAlphalistUsingServiceGeneratedFileName() {
        byte[] content = "H1604C\r\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        AnnualizationDatExportDTO export = new AnnualizationDatExportDTO(
                "1234567890000123120261604C.DAT", content);
        when(authentication.getName()).thenReturn("payroll.admin");
        when(annualizationService.exportAlphalist(
                2026, "payroll.admin", "Bearer token")).thenReturn(export);

        var response = controller.exportAlphalist(
                2026, authentication, "Bearer token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("application/octet-stream", response.getHeaders().getContentType().toString());
        assertEquals("attachment; filename=\"1234567890000123120261604C.DAT\"",
                response.getHeaders().getFirst("Content-Disposition"));
        assertEquals("Content-Disposition",
                response.getHeaders().getFirst("Access-Control-Expose-Headers"));
        assertTrue(java.util.Arrays.equals(content, response.getBody()));
        verify(annualizationService).exportAlphalist(
                2026, "payroll.admin", "Bearer token");
    }
}
