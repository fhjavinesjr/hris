package com.payroll.impl;

import com.payroll.dtos.AnnualizationEmployeeDTO;
import com.payroll.dtos.Bir2316ReportDTO;
import com.payroll.dtos.PayrollAdjustmentDTO;
import com.payroll.entitymodels.AnnualizationAuditLog;
import com.payroll.reports.Bir2316OfficialFormWriter;
import com.payroll.repositories.AnnualizationAuditLogRepository;
import com.payroll.repositories.AnnualizationRefundVoucherRepository;
import com.payroll.repositories.AnnualizationSnapshotRepository;
import com.payroll.repositories.AnnualizationSnapshotPeriodRepository;
import com.payroll.repositories.PayrollPeriodLockRepository;
import com.payroll.services.Bir2316ReportService;
import com.payroll.services.PayrollAdjustmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class Bir2316ReportServiceImpl implements Bir2316ReportService {
    private final AnnualizationAuditLogRepository auditRepository;
    private final PayrollAdjustmentService payrollAdjustmentService;
    private final PayrollPeriodLockRepository periodLockRepository;
    private final AnnualizationSnapshotRepository snapshotRepository;
    private final AnnualizationRefundVoucherRepository refundVoucherRepository;
    private final AnnualizationSnapshotPeriodRepository snapshotPeriodRepository;
    private final Bir2316OfficialFormWriter formWriter;

    public Bir2316ReportServiceImpl(
            AnnualizationAuditLogRepository auditRepository,
            PayrollAdjustmentService payrollAdjustmentService,
            PayrollPeriodLockRepository periodLockRepository,
            AnnualizationSnapshotRepository snapshotRepository,
            AnnualizationRefundVoucherRepository refundVoucherRepository,
            AnnualizationSnapshotPeriodRepository snapshotPeriodRepository,
            Bir2316OfficialFormWriter formWriter) {
        this.auditRepository = auditRepository;
        this.payrollAdjustmentService = payrollAdjustmentService;
        this.periodLockRepository = periodLockRepository;
        this.snapshotRepository = snapshotRepository;
        this.refundVoucherRepository = refundVoucherRepository;
        this.snapshotPeriodRepository = snapshotPeriodRepository;
        this.formWriter = formWriter;
    }

    @Override
    @Transactional
    public void generatePdf(
            AnnualizationEmployeeDTO annualization, String performedBy, OutputStream output)
            throws Exception {
        Bir2316ReportDTO reportData = prepareReportData(annualization);
        formWriter.write(reportData, output);

        AnnualizationAuditLog audit = new AnnualizationAuditLog();
        audit.setEmployeeNo(annualization.employeeNo());
        audit.setTaxYear(annualization.taxYear());
        audit.setAction("BIR2316_GENERATED");
        audit.setOldStatus("FINALIZED");
        audit.setNewStatus("FINALIZED");
        audit.setRemarks("Generated from annualization snapshot version "
                + annualization.snapshotVersion());
        audit.setPerformedBy(performedBy == null || performedBy.isBlank()
                ? "SYSTEM" : performedBy.trim());
        audit.setPerformedAt(LocalDateTime.now());
        auditRepository.save(audit);
    }

    @Override
    @Transactional(readOnly = true)
    public Bir2316ReportDTO prepareReportData(AnnualizationEmployeeDTO annualization) {
        BigDecimal postedAdjustment = BigDecimal.ZERO;
        if (annualization.adjustmentHeaderId() != null
                && annualization.adjustmentSalaryPeriodKey() != null
                && periodLockRepository.existsBySalaryPeriodKey(
                        annualization.adjustmentSalaryPeriodKey())) {
            PayrollAdjustmentDTO adjustment = payrollAdjustmentService.findAdjustment(
                            annualization.employeeNo(), annualization.adjustmentSalaryPeriodKey())
                    .filter(header -> annualization.adjustmentHeaderId().equals(header.getId()))
                    .filter(header -> AnnualizationAdjustmentEvidence.matches(
                            annualization.adjustmentHeaderId(), annualization.adjustmentAmount(), header))
                    .orElse(null);
            if (adjustment != null) postedAdjustment = annualization.adjustmentAmount();
        }
        if (annualization.adjustmentHeaderId() == null
                && annualization.adjustmentAmount() != null
                && annualization.adjustmentAmount().signum() < 0) {
            postedAdjustment = snapshotRepository
                    .findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
                            annualization.employeeNo(), annualization.taxYear(), "FINALIZED")
                    .filter(snapshot -> snapshot.getVersion().equals(annualization.snapshotVersion()))
                    .filter(snapshot -> {
                        var periods = snapshotPeriodRepository
                                .findBySnapshotIdOrderBySalaryDateAscSalaryPeriodKeyAsc(snapshot.getId());
                        return !periods.isEmpty() && periods.stream().allMatch(period ->
                                period.getSalaryPeriodKey() != null
                                        && periodLockRepository.existsBySalaryPeriodKey(
                                                period.getSalaryPeriodKey()));
                    })
                    .flatMap(snapshot -> refundVoucherRepository
                            .findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(snapshot.getId()))
                    .filter(voucher -> voucher.getAmount() != null
                            && voucher.getAmount().compareTo(
                                    annualization.adjustmentAmount().abs()) == 0)
                    .map(voucher -> annualization.adjustmentAmount())
                    .orElse(BigDecimal.ZERO);
        }
        if (annualization.adjustmentAmount() != null
                && annualization.adjustmentAmount().signum() != 0
                && postedAdjustment.compareTo(annualization.adjustmentAmount()) != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Settle the year-end withholding adjustment before issuing BIR Form 2316.");
        }
        return Bir2316ReportDTO.from(annualization, postedAdjustment);
    }
}
