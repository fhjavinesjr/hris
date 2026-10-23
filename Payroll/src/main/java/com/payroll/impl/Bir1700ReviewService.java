package com.payroll.impl;

import com.payroll.dtos.AnnualizationEmployeeDTO;
import com.payroll.dtos.AnnualizationTaxIdentityDTO;
import com.payroll.dtos.Bir1700ReviewDTO;
import com.payroll.dtos.Bir2316ReportDTO;
import com.payroll.dtos.PreviousEmployerTaxDataDTO;
import com.payroll.entitymodels.AnnualizationAuditLog;
import com.payroll.reports.Bir1700OfficialFormWriter;
import com.payroll.repositories.AnnualizationAuditLogRepository;
import com.payroll.services.Bir2316ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class Bir1700ReviewService {
    private final Bir2316ReportService bir2316ReportService;
    private final Bir1700OfficialFormWriter formWriter;
    private final AnnualizationAuditLogRepository auditRepository;

    public Bir1700ReviewService(Bir2316ReportService bir2316ReportService,
                                Bir1700OfficialFormWriter formWriter,
                                AnnualizationAuditLogRepository auditRepository) {
        this.bir2316ReportService = bir2316ReportService;
        this.formWriter = formWriter;
        this.auditRepository = auditRepository;
    }

    @Transactional(readOnly = true)
    public Bir1700ReviewDTO review(AnnualizationEmployeeDTO annualization) {
        // This also verifies that the annual adjustment is actually posted or paid.
        Bir2316ReportDTO certificate = bir2316ReportService.prepareReportData(annualization);
        AnnualizationTaxIdentityDTO identity = annualization.taxIdentity();
        List<Bir1700ReviewDTO.EmployerLine> employers = new ArrayList<>();
        for (PreviousEmployerTaxDataDTO previous : annualization.previousEmployers()) {
            employers.add(new Bir1700ReviewDTO.EmployerLine(previous.employerName(),
                    previous.employerTin(), previous.grossCompensation(), previous.taxWithheld()));
        }
        String presentEmployerTin = certificate.employerTin() == null || certificate.employerTin().isBlank()
                ? identity.employerTin() : certificate.employerTin();
        employers.add(new Bir1700ReviewDTO.EmployerLine(identity.employerName(),
                presentEmployerTin, annualization.presentGrossCompensation(),
                certificate.item25APresentWithheld()));

        BigDecimal gross = employers.stream().map(Bir1700ReviewDTO.EmployerLine::grossCompensation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal withheld = employers.stream().map(Bir1700ReviewDTO.EmployerLine::taxWithheld)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (gross.compareTo(annualization.grossCompensation()) != 0
                || withheld.compareTo(certificate.item26TotalWithheldAdjusted()) != 0) {
            throw new IllegalStateException("Form 1700 employer rows do not reconcile with the finalized Form 2316.");
        }

        List<String> notes = new ArrayList<>();
        LocalDate yearEnd = LocalDate.of(annualization.taxYear(), 12, 31);
        String status = LocalDate.now(ZoneId.of("Asia/Manila")).isBefore(yearEnd)
                ? "TAX_YEAR_IN_PROGRESS" : "PERSONAL_REVIEW_REQUIRED";
        if ("TAX_YEAR_IN_PROGRESS".equals(status)) {
            notes.add("The tax year is still in progress. This is not a completed annual return.");
        }
        notes.add("HRIS has " + annualization.periods().size()
                + " finalized payroll period(s); confirm all compensation for the tax year.");
        notes.add("The employee must confirm other income, spouse information, and personal tax credits before filing advice.");
        notes.add("Form 1700 displays whole pesos; the 2316 and payroll retain centavo-level amounts.");
        if (!annualization.previousEmployers().isEmpty()) {
            notes.add("Previous-employer Form 2316 information must be verified separately.");
        }
        if (annualization.peraTaxCredit() != null && annualization.peraTaxCredit().signum() != 0) {
            notes.add("PERA tax credit requires an officer's review before mapping Form 1700 credits.");
        }
        if (Boolean.TRUE.equals(annualization.substitutedFilingQualified())) {
            notes.add("Annualization marks substituted filing qualified; Form 1700 normally would not be filed.");
        } else if (Boolean.FALSE.equals(annualization.substitutedFilingQualified())) {
            notes.add("Annualization marks substituted filing not qualified; this alone does not establish a Form 1700 filing obligation.");
        }
        return new Bir1700ReviewDTO(annualization.employeeNo(), annualization.taxYear(),
                annualization.snapshotVersion(), annualization.employeeName(), status,
                List.copyOf(notes), annualization.periods().size(), gross,
                annualization.nonTaxableCompensation(), annualization.taxableCompensation(),
                annualization.annualTaxDue(), withheld, List.copyOf(employers));
    }

    @Transactional
    public byte[] generateDraft(AnnualizationEmployeeDTO annualization, String actor) throws Exception {
        Bir1700ReviewDTO data = review(annualization);
        if (annualization.peraTaxCredit() != null && annualization.peraTaxCredit().signum() != 0) {
            throw new IllegalArgumentException("PERA tax credit needs a verified Form 1700 mapping before draft generation.");
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        formWriter.write(data, annualization.taxIdentity(), output);
        AnnualizationAuditLog audit = new AnnualizationAuditLog();
        audit.setEmployeeNo(annualization.employeeNo());
        audit.setTaxYear(annualization.taxYear());
        audit.setAction("BIR1700_DRAFT_GENERATED");
        audit.setOldStatus("FINALIZED");
        audit.setNewStatus("FINALIZED");
        audit.setRemarks("Official-layout draft from annualization snapshot version "
                + annualization.snapshotVersion() + "; no filing decision or submission.");
        audit.setPerformedBy(actor == null || actor.isBlank() ? "SYSTEM" : actor.trim());
        audit.setPerformedAt(LocalDateTime.now());
        auditRepository.save(audit);
        return output.toByteArray();
    }
}
