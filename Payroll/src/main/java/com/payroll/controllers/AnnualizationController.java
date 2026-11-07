package com.payroll.controllers;

import com.payroll.dtos.AnnualizationEmployeeDTO;
import com.payroll.dtos.AnnualizationYearDTO;
import com.payroll.dtos.PreviousEmployerTaxDataDTO;
import com.payroll.dtos.AnnualizationActionRequest;
import com.payroll.dtos.AnnualizationAdjustmentPeriodDTO;
import com.payroll.dtos.AnnualizationAdjustmentRequest;
import com.payroll.dtos.AnnualizationRefundVoucherDTO;
import com.payroll.dtos.AnnualizationRefundVoucherRequest;
import com.payroll.dtos.SeparationFinalPayReviewDTO;
import com.payroll.dtos.PeraTaxCreditDTO;
import com.payroll.dtos.Bir1700ReviewDTO;
import com.payroll.impl.Bir1700ReviewService;
import com.payroll.services.AnnualizationService;
import com.payroll.services.Bir2316ReportService;
import com.payroll.dtos.AnnualizationFinalizationRequest;
import com.payroll.dtos.AnnualizationAlphalistPreviewDTO;
import com.payroll.dtos.AnnualizationDatExportDTO;
import com.payroll.dtos.AnnualBenefitReviewLineDTO;
import com.payroll.dtos.AnnualBenefitReviewRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/annualization")
public class AnnualizationController {
    private final AnnualizationService annualizationService;
    private final Bir2316ReportService bir2316ReportService;
    private final Bir1700ReviewService bir1700ReviewService;

    public AnnualizationController(AnnualizationService annualizationService,
                                   Bir2316ReportService bir2316ReportService,
                                   Bir1700ReviewService bir1700ReviewService) {
        this.annualizationService = annualizationService;
        this.bir2316ReportService = bir2316ReportService;
        this.bir1700ReviewService = bir1700ReviewService;
    }

    @GetMapping("/{employeeNo}/benefit-reviews")
    public ResponseEntity<List<AnnualBenefitReviewLineDTO>> getBenefitReviews(
            @PathVariable String employeeNo, @RequestParam int year,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.getBenefitReviewLines(year, employeeNo, authorizationHeader));
    }

    @PutMapping("/{employeeNo}/benefit-reviews/{sourceType}/{sourceId}")
    public ResponseEntity<AnnualBenefitReviewLineDTO> saveBenefitReview(
            @PathVariable String employeeNo, @PathVariable String sourceType, @PathVariable Long sourceId,
            @RequestParam int year, @RequestBody AnnualBenefitReviewRequest request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.saveBenefitReview(year, employeeNo,
                sourceType, sourceId, request, actor(authentication), authorizationHeader));
    }

    @GetMapping
    public ResponseEntity<AnnualizationYearDTO> getAnnualization(
            @RequestParam int year,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.getAnnualization(year, authorizationHeader));
    }

    @GetMapping("/alphalist-preview")
    public ResponseEntity<AnnualizationAlphalistPreviewDTO> getAlphalistPreview(
            @RequestParam int year,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.getAlphalistPreview(year, authorizationHeader));
    }

    @GetMapping(value = "/alphalist-export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportAlphalist(
            @RequestParam int year,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        AnnualizationDatExportDTO export = annualizationService.exportAlphalist(
                year, actor(authentication), authorizationHeader);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + export.fileName() + "\"")
                .header("Access-Control-Expose-Headers", "Content-Disposition")
                .body(export.content());
    }

    @GetMapping("/{employeeNo}")
    public ResponseEntity<AnnualizationEmployeeDTO> getEmployeeAnnualization(
            @PathVariable String employeeNo,
            @RequestParam int year,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.getEmployeeAnnualization(
                year, employeeNo, authorizationHeader));
    }

    @GetMapping(value = "/{employeeNo}/2316", produces = MediaType.APPLICATION_PDF_VALUE)
    public void getBir2316(
            @PathVariable String employeeNo,
            @RequestParam int year,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            HttpServletResponse response) throws Exception {
        AnnualizationEmployeeDTO annualization = annualizationService.getEmployeeAnnualization(
                year, employeeNo, authorizationHeader);
        if (!"FINALIZED".equals(annualization.status())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.CONFLICT, "Finalize annualization before generating BIR Form 2316.");
        }
        String safeEmployeeNo = employeeNo.replaceAll("[^A-Za-z0-9._-]", "_");
        String fileName = "BIR_2316_" + safeEmployeeNo + "_" + year + ".pdf";
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + fileName + "\"; filename*=UTF-8''" + encoded);
        bir2316ReportService.generatePdf(
                annualization, actor(authentication), response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/{employeeNo}/1700-review")
    public ResponseEntity<Bir1700ReviewDTO> reviewBir1700(
            @PathVariable String employeeNo, @RequestParam int year,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        AnnualizationEmployeeDTO annualization = annualizationService.getEmployeeAnnualization(
                year, employeeNo, authorizationHeader);
        return ResponseEntity.ok(bir1700ReviewService.review(annualization));
    }

    @GetMapping(value = "/{employeeNo}/1700-draft", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getBir1700Draft(
            @PathVariable String employeeNo, @RequestParam int year,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) throws Exception {
        AnnualizationEmployeeDTO annualization = annualizationService.getEmployeeAnnualization(
                year, employeeNo, authorizationHeader);
        byte[] pdf = bir1700ReviewService.generateDraft(annualization, actor(authentication));
        String fileName = "BIR_1700_DRAFT_" + employeeNo.replaceAll("[^A-Za-z0-9._-]", "_")
                + "_" + year + ".pdf";
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .body(pdf);
    }

    @GetMapping("/{employeeNo}/previous-employers")
    public ResponseEntity<List<PreviousEmployerTaxDataDTO>> getPreviousEmployers(
            @PathVariable String employeeNo,
            @RequestParam int year,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.getPreviousEmployers(
                year, employeeNo, authorizationHeader));
    }

    @PostMapping("/{employeeNo}/previous-employers")
    public ResponseEntity<PreviousEmployerTaxDataDTO> createPreviousEmployer(
            @PathVariable String employeeNo,
            @RequestParam int year,
            @RequestBody PreviousEmployerTaxDataDTO request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                annualizationService.createPreviousEmployer(
                        year, employeeNo, request, actor(authentication), authorizationHeader));
    }

    @PutMapping("/{employeeNo}/previous-employers/{id}")
    public ResponseEntity<PreviousEmployerTaxDataDTO> updatePreviousEmployer(
            @PathVariable String employeeNo,
            @PathVariable Long id,
            @RequestParam int year,
            @RequestBody PreviousEmployerTaxDataDTO request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.updatePreviousEmployer(
                year, employeeNo, id, request, actor(authentication), authorizationHeader));
    }

    @DeleteMapping("/{employeeNo}/previous-employers/{id}")
    public ResponseEntity<Void> deletePreviousEmployer(
            @PathVariable String employeeNo,
            @PathVariable Long id,
            @RequestParam int year,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        annualizationService.deletePreviousEmployer(
                year, employeeNo, id, actor(authentication), authorizationHeader);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{employeeNo}/pera-tax-credit")
    public ResponseEntity<PeraTaxCreditDTO> getPeraTaxCredit(
            @PathVariable String employeeNo,
            @RequestParam int year,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        PeraTaxCreditDTO result = annualizationService.getPeraTaxCredit(
                year, employeeNo, authorizationHeader);
        return result == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }

    @PutMapping("/{employeeNo}/pera-tax-credit")
    public ResponseEntity<PeraTaxCreditDTO> savePeraTaxCredit(
            @PathVariable String employeeNo,
            @RequestParam int year,
            @RequestBody PeraTaxCreditDTO request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.savePeraTaxCredit(
                year, employeeNo, request, actor(authentication), authorizationHeader));
    }

    @DeleteMapping("/{employeeNo}/pera-tax-credit")
    public ResponseEntity<Void> deletePeraTaxCredit(
            @PathVariable String employeeNo,
            @RequestParam int year,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        annualizationService.deletePeraTaxCredit(
                year, employeeNo, actor(authentication), authorizationHeader);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{employeeNo}/finalize")
    public ResponseEntity<AnnualizationEmployeeDTO> finalizeAnnualization(
            @PathVariable String employeeNo,
            @RequestParam int year,
            @RequestBody AnnualizationFinalizationRequest request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.finalizeAnnualization(
                year, employeeNo, request.remarks(), request.substitutedFilingQualified(),
                request.substitutedFilingReason(),
                actor(authentication), authorizationHeader));
    }

    @PostMapping("/{employeeNo}/reopen")
    public ResponseEntity<AnnualizationEmployeeDTO> reopenAnnualization(
            @PathVariable String employeeNo,
            @RequestParam int year,
            @RequestBody AnnualizationActionRequest request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.reopenAnnualization(
                year, employeeNo, request.remarks(), actor(authentication), authorizationHeader));
    }

    @GetMapping("/{employeeNo}/eligible-adjustment-periods")
    public ResponseEntity<List<AnnualizationAdjustmentPeriodDTO>> getEligibleAdjustmentPeriods(
            @PathVariable String employeeNo,
            @RequestParam int year,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.getEligibleAdjustmentPeriods(
                year, employeeNo, authorizationHeader));
    }

    @PostMapping("/{employeeNo}/apply-adjustment")
    public ResponseEntity<AnnualizationEmployeeDTO> applyAdjustment(
            @PathVariable String employeeNo,
            @RequestParam int year,
            @RequestBody AnnualizationAdjustmentRequest request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.applyAdjustment(
                year, employeeNo, request.salaryPeriodKey(), actor(authentication), authorizationHeader));
    }

    @GetMapping("/{employeeNo}/separation-final-pay-periods")
    public ResponseEntity<List<AnnualizationAdjustmentPeriodDTO>> separationFinalPayPeriods(
            @PathVariable String employeeNo, @RequestParam int year,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.getSeparationFinalPayPeriods(
                year, employeeNo, authorizationHeader));
    }

    @GetMapping("/{employeeNo}/separation-final-pay-review")
    public ResponseEntity<SeparationFinalPayReviewDTO> reviewSeparationFinalPay(
            @PathVariable String employeeNo, @RequestParam int year,
            @RequestParam String salaryPeriodKey,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.reviewSeparationFinalPay(
                year, employeeNo, salaryPeriodKey, authorizationHeader));
    }

    @PostMapping("/{employeeNo}/separation-final-pay-adjustment")
    public ResponseEntity<SeparationFinalPayReviewDTO> prepareSeparationFinalPayAdjustment(
            @PathVariable String employeeNo, @RequestParam int year,
            @RequestBody AnnualizationAdjustmentRequest request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(annualizationService.prepareSeparationFinalPayAdjustment(
                year, employeeNo, request.salaryPeriodKey(), actor(authentication), authorizationHeader));
    }

    @GetMapping("/{employeeNo}/refund-voucher")
    public ResponseEntity<AnnualizationRefundVoucherDTO> getRefundVoucher(
            @PathVariable String employeeNo, @RequestParam int year,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        AnnualizationRefundVoucherDTO voucher = annualizationService.getRefundVoucher(
                year, employeeNo, authorizationHeader);
        return voucher == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(voucher);
    }

    @PostMapping("/{employeeNo}/refund-voucher")
    public ResponseEntity<AnnualizationRefundVoucherDTO> recordRefundVoucher(
            @PathVariable String employeeNo, @RequestParam int year,
            @RequestBody AnnualizationRefundVoucherRequest request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(annualizationService.recordRefundVoucher(
                year, employeeNo, request, actor(authentication), authorizationHeader));
    }

    @DeleteMapping("/{employeeNo}/refund-voucher")
    public ResponseEntity<Void> voidRefundVoucher(
            @PathVariable String employeeNo, @RequestParam int year,
            @RequestBody AnnualizationActionRequest request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        annualizationService.voidRefundVoucher(
                year, employeeNo, request.remarks(), actor(authentication), authorizationHeader);
        return ResponseEntity.noContent().build();
    }

    private String actor(Authentication authentication) {
        return authentication == null ? "SYSTEM" : authentication.getName();
    }
}
