package com.payroll.impl;

import com.payroll.dtos.AnnualizationEmployeeDTO;
import com.payroll.dtos.AnnualizationTaxIdentityDTO;
import com.payroll.dtos.Bir2316CompensationItemsDTO;
import com.payroll.dtos.Bir2316ReportDTO;
import com.payroll.entitymodels.AnnualizationAuditLog;
import com.payroll.entitymodels.AnnualizationRefundVoucher;
import com.payroll.entitymodels.AnnualizationSnapshot;
import com.payroll.entitymodels.AnnualizationSnapshotPeriod;
import com.payroll.repositories.AnnualizationAuditLogRepository;
import com.payroll.repositories.PayrollPeriodLockRepository;
import com.payroll.services.PayrollAdjustmentService;
import com.payroll.reports.Bir2316OfficialFormWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Bir2316ReportServiceImplTest {

    @Mock
    private AnnualizationAuditLogRepository auditRepository;
    @Mock
    private PayrollAdjustmentService payrollAdjustmentService;
    @Mock
    private PayrollPeriodLockRepository periodLockRepository;
    @Mock
    private com.payroll.repositories.AnnualizationSnapshotRepository snapshotRepository;
    @Mock
    private com.payroll.repositories.AnnualizationRefundVoucherRepository refundVoucherRepository;
    @Mock
    private com.payroll.repositories.AnnualizationSnapshotPeriodRepository snapshotPeriodRepository;

    @Test
    void exportsPdfAndAuditsGeneration() throws Exception {
        AnnualizationEmployeeDTO annualization = mock(AnnualizationEmployeeDTO.class);
        when(annualization.status()).thenReturn("FINALIZED");
        when(annualization.employeeNo()).thenReturn("EMP-001");
        when(annualization.employeeName()).thenReturn("JUAN DELA CRUZ");
        when(annualization.taxYear()).thenReturn(2026);
        when(annualization.snapshotVersion()).thenReturn(4);
        when(annualization.previousEmployers()).thenReturn(List.of());
        when(annualization.periods()).thenReturn(List.of());
        AnnualizationTaxIdentityDTO identity = mock(AnnualizationTaxIdentityDTO.class);
        when(identity.employeeEmploymentFrom()).thenReturn(LocalDate.of(2026, 1, 1));
        when(identity.employeeEmploymentTo()).thenReturn(LocalDate.of(2026, 12, 31));
        when(annualization.taxIdentity()).thenReturn(identity);
        when(annualization.bir2316Items()).thenReturn(mock(Bir2316CompensationItemsDTO.class));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new Bir2316ReportServiceImpl(auditRepository, payrollAdjustmentService,
                periodLockRepository, snapshotRepository, refundVoucherRepository,
                snapshotPeriodRepository,
                new Bir2316OfficialFormWriter())
                .generatePdf(annualization, "payroll.admin", output);

        assertArrayEquals(
                "%PDF".getBytes(StandardCharsets.US_ASCII),
                Arrays.copyOf(output.toByteArray(), 4));
        ArgumentCaptor<AnnualizationAuditLog> auditCaptor =
                ArgumentCaptor.forClass(AnnualizationAuditLog.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertEquals("BIR2316_GENERATED", auditCaptor.getValue().getAction());
        assertEquals("payroll.admin", auditCaptor.getValue().getPerformedBy());
        assertEquals("Generated from annualization snapshot version 4",
                auditCaptor.getValue().getRemarks());
    }

    @Test
    void paidVoucherIsReflectedIn2316AdjustedWithholding() throws Exception {
        AnnualizationEmployeeDTO annualization = mock(AnnualizationEmployeeDTO.class);
        when(annualization.status()).thenReturn("FINALIZED");
        when(annualization.employeeNo()).thenReturn("EMP-001");
        when(annualization.taxYear()).thenReturn(2026);
        when(annualization.snapshotVersion()).thenReturn(4);
        when(annualization.adjustmentHeaderId()).thenReturn(null);
        when(annualization.adjustmentAmount()).thenReturn(new BigDecimal("-1456.80"));
        when(annualization.presentTaxWithheld()).thenReturn(new BigDecimal("1456.80"));
        when(annualization.previousEmployers()).thenReturn(List.of());
        when(annualization.periods()).thenReturn(List.of());
        AnnualizationTaxIdentityDTO identity = mock(AnnualizationTaxIdentityDTO.class);
        when(identity.employeeEmploymentFrom()).thenReturn(LocalDate.of(2026, 1, 1));
        when(identity.employeeEmploymentTo()).thenReturn(LocalDate.of(2026, 12, 31));
        when(annualization.taxIdentity()).thenReturn(identity);
        when(annualization.bir2316Items()).thenReturn(mock(Bir2316CompensationItemsDTO.class));

        AnnualizationSnapshot snapshot = new AnnualizationSnapshot();
        snapshot.setId(9L);
        snapshot.setVersion(4);
        when(snapshotRepository.findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
                "EMP-001", 2026, "FINALIZED")).thenReturn(Optional.of(snapshot));
        AnnualizationSnapshotPeriod period = new AnnualizationSnapshotPeriod();
        period.setSalaryPeriodKey("2026-10-1");
        when(snapshotPeriodRepository.findBySnapshotIdOrderBySalaryDateAscSalaryPeriodKeyAsc(9L))
                .thenReturn(List.of(period));
        when(periodLockRepository.existsBySalaryPeriodKey("2026-10-1")).thenReturn(true);
        AnnualizationRefundVoucher voucher = new AnnualizationRefundVoucher();
        voucher.setAmount(new BigDecimal("1456.80"));
        when(refundVoucherRepository.findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(9L))
                .thenReturn(Optional.of(voucher));

        Bir2316OfficialFormWriter writer = mock(Bir2316OfficialFormWriter.class);
        new Bir2316ReportServiceImpl(auditRepository, payrollAdjustmentService,
                periodLockRepository, snapshotRepository, refundVoucherRepository,
                snapshotPeriodRepository, writer)
                .generatePdf(annualization, "payroll.admin", new ByteArrayOutputStream());

        ArgumentCaptor<Bir2316ReportDTO> report = ArgumentCaptor.forClass(Bir2316ReportDTO.class);
        verify(writer).write(report.capture(), org.mockito.ArgumentMatchers.any());
        verify(snapshotRepository).findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
                "EMP-001", 2026, "FINALIZED");
        verify(snapshotPeriodRepository).findBySnapshotIdOrderBySalaryDateAscSalaryPeriodKeyAsc(9L);
        verify(refundVoucherRepository).findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(9L);
        assertEquals(0, report.getValue().item25APresentWithheld().compareTo(BigDecimal.ZERO));
        assertEquals(0, report.getValue().item28TotalTaxesWithheld().compareTo(BigDecimal.ZERO));
    }

    @Test
    void blocksFinal2316WhileCalculatedRefundHasNoPaymentEvidence() {
        AnnualizationEmployeeDTO annualization = mock(AnnualizationEmployeeDTO.class);
        when(annualization.adjustmentHeaderId()).thenReturn(null);
        when(annualization.adjustmentAmount()).thenReturn(new BigDecimal("-1456.80"));
        Bir2316OfficialFormWriter writer = mock(Bir2316OfficialFormWriter.class);
        Bir2316ReportServiceImpl service = new Bir2316ReportServiceImpl(
                auditRepository, payrollAdjustmentService, periodLockRepository,
                snapshotRepository, refundVoucherRepository, snapshotPeriodRepository, writer);

        assertThrows(ResponseStatusException.class, () -> service.generatePdf(
                annualization, "payroll.admin", new ByteArrayOutputStream()));
        org.mockito.Mockito.verifyNoInteractions(writer);
    }
}
