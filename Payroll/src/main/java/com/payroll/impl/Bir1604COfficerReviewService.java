package com.payroll.impl;

import com.payroll.dtos.AnnualizationAlphalistPreviewDTO;
import com.payroll.dtos.Bir1604COfficerReviewDTO;
import com.payroll.dtos.Bir1604COfficerReviewRequest;
import com.payroll.dtos.TaxRemittance1601CReviewDTO;
import com.payroll.entitymodels.AnnualizationSnapshot;
import com.payroll.entitymodels.Bir1604COfficerReview;
import com.payroll.entitymodels.Bir1604COfficerReviewAudit;
import com.payroll.repositories.AnnualizationRefundVoucherRepository;
import com.payroll.repositories.AnnualizationSnapshotRepository;
import com.payroll.repositories.Bir1604COfficerReviewAuditRepository;
import com.payroll.repositories.Bir1604COfficerReviewRepository;
import com.payroll.services.AnnualizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class Bir1604COfficerReviewService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private final Bir1604COfficerReviewRepository repository;
    private final Bir1604COfficerReviewAuditRepository auditRepository;
    private final AnnualizationSnapshotRepository snapshotRepository;
    private final AnnualizationRefundVoucherRepository voucherRepository;
    private final AnnualizationService annualizationService;
    private final TaxRemittance1601CService remittanceService;

    public Bir1604COfficerReviewService(Bir1604COfficerReviewRepository repository,
            Bir1604COfficerReviewAuditRepository auditRepository,
            AnnualizationSnapshotRepository snapshotRepository,
            AnnualizationRefundVoucherRepository voucherRepository,
            AnnualizationService annualizationService,
            TaxRemittance1601CService remittanceService) {
        this.repository = repository;
        this.auditRepository = auditRepository;
        this.snapshotRepository = snapshotRepository;
        this.voucherRepository = voucherRepository;
        this.annualizationService = annualizationService;
        this.remittanceService = remittanceService;
    }

    @Transactional(readOnly = true)
    public Bir1604COfficerReviewDTO review(int year, String authorizationHeader) {
        validateYear(year);
        remittanceService.authorize(authorizationHeader, false);
        AnnualizationAlphalistPreviewDTO alphalist = annualizationService.getAlphalistPreview(year, authorizationHeader);
        TaxRemittance1601CReviewDTO remittances = remittanceService.review(year, authorizationHeader);
        Bir1604COfficerReview record = repository.findByTaxYear(year).orElse(null);
        BigDecimal refunds = snapshotRepository.findByTaxYearAndStatusOrderByEmployeeNameAsc(year, "FINALIZED")
                .stream().map(AnnualizationSnapshot::getId)
                .map(voucherRepository::findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc)
                .filter(java.util.Optional::isPresent)
                .map(value -> value.get().getAmount())
                .reduce(ZERO, BigDecimal::add);
        BigDecimal gap = remittances.confirmedTaxesWithheld().subtract(alphalist.totalTaxesWithheld());
        List<String> issues = new ArrayList<>(remittances.issues());
        if (!alphalist.exportEnabled())
            issues.add("Alphalist preflight has unresolved employee or employer issues.");
        if (record == null || !"CONFIRMED".equals(record.getStatus())) {
            issues.add("Employer 1604-C review has not been confirmed by an authorized officer.");
        } else {
            if (record.getBasis1601CWithheld() == null || record.getBasisAlphalistWithheld() == null
                    || record.getBasisPaidRefunds() == null
                    || record.getBasis1601CWithheld().compareTo(remittances.confirmedTaxesWithheld()) != 0
                    || record.getBasisAlphalistWithheld().compareTo(alphalist.totalTaxesWithheld()) != 0
                    || record.getBasisPaidRefunds().compareTo(refunds) != 0)
                issues.add("Underlying tax or paid-refund amounts changed; reconfirm the employer review.");
            if (remittances.months() != null && remittances.months().stream()
                    .anyMatch(month -> month.updatedAt() != null && record.getUpdatedAt() != null
                            && month.updatedAt().isAfter(record.getUpdatedAt())))
                issues.add("A 1601-C record changed after this review; reconfirm employer evidence.");
            if (refunds.signum() > 0 && !"YES".equals(record.getRefundsReleased()))
                issues.add("Paid employee refund vouchers conflict with the officer's Item 11 answer.");
            if (gap.signum() != 0 && (record.getRemarks() == null || record.getRemarks().isBlank()))
                issues.add("Explain the difference between filed 1601-C withholding and adjusted employee withholding.");
        }
        return new Bir1604COfficerReviewDTO(year, alphalist.exportEnabled(), remittances.confirmedTaxesWithheld(),
                alphalist.totalTaxesWithheld(), gap, refunds,
                record == null ? null : record.getContactNumber(),
                record == null ? null : record.getEmailAddress(),
                record == null ? null : record.getSheetsAttached(),
                record == null ? null : record.getRefundsReleased(),
                record == null ? null : record.getRefundDate(),
                record == null ? null : record.getOverremittanceAmount(),
                record == null ? null : record.getFirstCreditingMonth(),
                record == null ? null : record.getEvidenceReference(),
                record == null ? null : record.getSignatoryName(),
                record == null ? null : record.getSignatoryTitle(),
                record == null ? null : record.getSignatoryTin(),
                record == null ? null : record.getRemarks(),
                record == null ? "MISSING" : record.getStatus(),
                record == null ? null : record.getUpdatedBy(),
                record == null ? null : record.getUpdatedAt(), List.copyOf(issues));
    }

    @Transactional
    public Bir1604COfficerReviewDTO save(int year, Bir1604COfficerReviewRequest request,
            String actor, String authorizationHeader) {
        validateYear(year);
        remittanceService.authorize(authorizationHeader, true);
        if (actor == null || actor.isBlank()) throw new AccessDeniedException("Officer identity is required.");
        if (request == null) throw new IllegalArgumentException("1604-C review details are required.");
        String status = trim(request.status(), 20);
        if (!"DRAFT".equals(status) && !"CONFIRMED".equals(status))
            throw new IllegalArgumentException("Review status must be DRAFT or CONFIRMED.");
        String refundsReleased = trim(request.refundsReleased(), 3);
        if (refundsReleased != null && !"YES".equals(refundsReleased) && !"NO".equals(refundsReleased))
            throw new IllegalArgumentException("Item 11 must be YES or NO.");
        if (request.sheetsAttached() != null && (request.sheetsAttached() < 0 || request.sheetsAttached() > 999))
            throw new IllegalArgumentException("Attached sheet count must be 0 through 999.");
        BigDecimal overremittance = request.overremittanceAmount() == null ? null
                : request.overremittanceAmount().setScale(2, RoundingMode.HALF_UP);
        if (overremittance != null && overremittance.signum() < 0)
            throw new IllegalArgumentException("Overremittance cannot be negative.");
        Integer creditMonth = request.firstCreditingMonth();
        if (creditMonth != null && (creditMonth < 1 || creditMonth > 12))
            throw new IllegalArgumentException("First crediting month must be 1 through 12.");
        if ("YES".equals(refundsReleased) != (request.refundDate() != null))
            throw new IllegalArgumentException("Item 11A refund date is required only when refunds were released.");
        if (overremittance != null && overremittance.signum() == 0 && creditMonth != null)
            throw new IllegalArgumentException("A first crediting month requires a positive overremittance.");
        String evidence = trim(request.evidenceReference(), 255);
        String signatoryName = trim(request.signatoryName(), 100);
        String signatoryTitle = trim(request.signatoryTitle(), 100);
        String signatoryTin = trim(request.signatoryTin(), 30);
        if (signatoryTin != null && !signatoryTin.matches("[0-9-]+"))
            throw new IllegalArgumentException("Signatory TIN may contain only digits and hyphens.");
        String remarks = trim(request.remarks(), 500);
        String email = trim(request.emailAddress(), 100);
        if (email != null && (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || email.contains("..")))
            throw new IllegalArgumentException("Enter a valid employer contact email address.");
        Bir1604COfficerReviewDTO figures = null;
        if ("CONFIRMED".equals(status)) {
            if (refundsReleased == null || overremittance == null || evidence == null
                    || trim(request.contactNumber(), 30) == null
                    || email == null || request.sheetsAttached() == null
                    || signatoryName == null || signatoryTitle == null || signatoryTin == null)
                throw new IllegalArgumentException("Confirm Items 3 and 9-12, signatory details, and an evidence reference.");
            int tinLength = signatoryTin.replace("-", "").length();
            if (tinLength < 9 || tinLength > 14)
                throw new IllegalArgumentException("Signatory TIN must contain 9 to 14 digits.");
            if (overremittance.signum() > 0 && creditMonth == null)
                throw new IllegalArgumentException("Item 13 is required when overremittance will be credited.");
            figures = review(year, authorizationHeader);
            if (!figures.alphalistReady())
                throw new IllegalArgumentException("Resolve the Alphalist preflight issues before confirming 1604-C.");
            // The annual form cannot be confirmed while monthly evidence is unreconciled.
            if (!remittanceService.review(year, authorizationHeader).reconciled())
                throw new IllegalArgumentException("Reconcile the 1601-C remittance ledger before confirming 1604-C.");
            if (figures.recordedEmployeeRefunds().signum() > 0 && !"YES".equals(refundsReleased))
                throw new IllegalArgumentException("Paid refund vouchers require Item 11 = Yes.");
            if (figures.withholdingGap().signum() != 0 && remarks == null)
                throw new IllegalArgumentException("Explain the withholding-to-Alphalist gap in remarks.");
        }
        Bir1604COfficerReview value = repository.findByTaxYear(year).orElseGet(Bir1604COfficerReview::new);
        String previous = value.getId() == null ? null : auditValues(value);
        if ("CONFIRMED".equals(value.getStatus()) && remarks == null)
            throw new IllegalArgumentException("Changing a confirmed review requires a reason in remarks.");
        value.setTaxYear(year);
        value.setContactNumber(trim(request.contactNumber(), 30));
        value.setEmailAddress(email);
        value.setSheetsAttached(request.sheetsAttached());
        value.setRefundsReleased(refundsReleased);
        value.setRefundDate(request.refundDate());
        value.setOverremittanceAmount(overremittance);
        value.setFirstCreditingMonth(creditMonth);
        value.setEvidenceReference(evidence);
        value.setSignatoryName(signatoryName);
        value.setSignatoryTitle(signatoryTitle);
        value.setSignatoryTin(signatoryTin);
        value.setRemarks(remarks);
        value.setBasis1601CWithheld(figures == null ? null : figures.confirmed1601CWithheld());
        value.setBasisAlphalistWithheld(figures == null ? null : figures.adjustedAlphalistWithheld());
        value.setBasisPaidRefunds(figures == null ? null : figures.recordedEmployeeRefunds());
        value.setStatus(status);
        value.setUpdatedBy(trim(actor, 100));
        value.setUpdatedAt(LocalDateTime.now());
        repository.save(value);
        Bir1604COfficerReviewAudit audit = new Bir1604COfficerReviewAudit();
        audit.setTaxYear(year);
        audit.setAction(previous == null ? "CREATED" : "UPDATED");
        audit.setPreviousValues(previous);
        audit.setNewValues(auditValues(value));
        audit.setPerformedBy(value.getUpdatedBy());
        audit.setPerformedAt(value.getUpdatedAt());
        auditRepository.save(audit);
        return review(year, authorizationHeader);
    }

    private String auditValues(Bir1604COfficerReview value) {
        return "status=" + value.getStatus() + ", sheets=" + value.getSheetsAttached()
                + ", contact=" + value.getContactNumber() + ", email=" + value.getEmailAddress()
                + ", refunds=" + value.getRefundsReleased() + ", refundDate=" + value.getRefundDate()
                + ", overremittance=" + value.getOverremittanceAmount()
                + ", firstCreditMonth=" + value.getFirstCreditingMonth()
                + ", evidence=" + value.getEvidenceReference()
                + ", signatory=" + value.getSignatoryName() + "/" + value.getSignatoryTitle()
                + "/" + value.getSignatoryTin() + ", basis=" + value.getBasis1601CWithheld()
                + "/" + value.getBasisAlphalistWithheld() + "/" + value.getBasisPaidRefunds()
                + ", remarks=" + value.getRemarks();
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.isBlank()) return null;
        String result = value.trim();
        if (result.length() > maxLength) throw new IllegalArgumentException("1604-C field exceeds " + maxLength + " characters.");
        return result;
    }

    private void validateYear(int year) {
        if (year < 2000 || year > 2100) throw new IllegalArgumentException("Invalid tax year.");
    }
}
