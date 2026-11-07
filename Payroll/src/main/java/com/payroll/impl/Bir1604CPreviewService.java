package com.payroll.impl;

import com.payroll.dtos.AnnualizationAlphalistPreviewDTO;
import com.payroll.dtos.TaxRemittance1601CReviewDTO;
import com.payroll.entitymodels.AnnualizationSnapshot;
import com.payroll.reports.Bir1604COfficialFormWriter;
import com.payroll.repositories.AnnualizationSnapshotRepository;
import com.payroll.services.AnnualizationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
public class Bir1604CPreviewService {
    private final AnnualizationService annualizationService;
    private final TaxRemittance1601CService remittanceService;
    private final AnnualizationSnapshotRepository snapshotRepository;
    private final Bir1604COfficialFormWriter formWriter;
    private final Bir1604COfficerReviewService officerReviewService;

    public Bir1604CPreviewService(AnnualizationService annualizationService,
                                  TaxRemittance1601CService remittanceService,
                                  AnnualizationSnapshotRepository snapshotRepository,
                                  Bir1604COfficialFormWriter formWriter,
                                  Bir1604COfficerReviewService officerReviewService) {
        this.annualizationService = annualizationService;
        this.remittanceService = remittanceService;
        this.snapshotRepository = snapshotRepository;
        this.formWriter = formWriter;
        this.officerReviewService = officerReviewService;
    }

    @Transactional(readOnly = true)
    public void generateDraft(int year, String authorizationHeader, OutputStream output) throws Exception {
        AnnualizationAlphalistPreviewDTO alphalist = annualizationService
                .getAlphalistPreview(year, authorizationHeader);
        TaxRemittance1601CReviewDTO remittances = remittanceService.review(year, authorizationHeader);
        List<AnnualizationSnapshot> snapshots = snapshotRepository
                .findByTaxYearAndStatusOrderByEmployeeNameAsc(year, "FINALIZED");
        if (snapshots.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Finalize at least one employee annualization before previewing Form 1604-C.");
        }
        AnnualizationSnapshot identity = snapshots.get(0);
        if (alphalist.employerTin() == null || alphalist.employerName() == null
                || identity.getEmployerAddress() == null || identity.getEmployerZipCode() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Complete employer BIR identity and address before previewing Form 1604-C.");
        }
        String tinDigits = alphalist.employerTin().replaceAll("\\D", "");
        String formTin = tinDigits.length() == 9 && alphalist.employerBirBranchCode() != null
                ? tinDigits + alphalist.employerBirBranchCode().replaceAll("\\D", "")
                : tinDigits;
        BigDecimal confirmedWithheld = remittances.confirmedTaxesWithheld();
        BigDecimal adjustedAlphalist = alphalist.totalTaxesWithheld();
        BigDecimal difference = confirmedWithheld.subtract(adjustedAlphalist);
        var officerReview = officerReviewService.review(year, authorizationHeader);
        // Officer-confirmed facts, not payroll-derived guesses, populate Items 11-13.
        ByteArrayOutputStream rendered = new ByteArrayOutputStream();
        formWriter.write(new Bir1604COfficialFormWriter.Data(year,
                formTin,
                alphalist.employerRdoCode(), alphalist.employerName(),
                identity.getEmployerAddress(), identity.getEmployerZipCode(), remittances.months(),
                "CONFIRMED".equals(officerReview.status()) && officerReview.issues().isEmpty()
                        ? officerReview : null,
                "DRAFT: confirmed 1601-C withheld " + confirmedWithheld.toPlainString()
                        + "; adjusted alphalist " + adjustedAlphalist.toPlainString()
                        + "; gap " + difference.toPlainString()
                        + ". Verify officer data, signatures and year coverage."),
                rendered);
        rendered.writeTo(output);
    }
}
