package com.payroll.impl;

import com.payroll.dtos.AnnualizationAlphalistPreviewDTO;
import com.payroll.dtos.Bir1604COfficerReviewRequest;
import com.payroll.dtos.TaxRemittance1601CReviewDTO;
import com.payroll.entitymodels.AnnualizationRefundVoucher;
import com.payroll.entitymodels.AnnualizationSnapshot;
import com.payroll.entitymodels.Bir1604COfficerReviewAudit;
import com.payroll.entitymodels.Bir1604COfficerReview;
import com.payroll.repositories.AnnualizationRefundVoucherRepository;
import com.payroll.repositories.AnnualizationSnapshotRepository;
import com.payroll.repositories.Bir1604COfficerReviewAuditRepository;
import com.payroll.repositories.Bir1604COfficerReviewRepository;
import com.payroll.services.AnnualizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Bir1604COfficerReviewServiceTest {
    private final Bir1604COfficerReviewRepository repository = mock(Bir1604COfficerReviewRepository.class);
    private final Bir1604COfficerReviewAuditRepository auditRepository = mock(Bir1604COfficerReviewAuditRepository.class);
    private final AnnualizationSnapshotRepository snapshots = mock(AnnualizationSnapshotRepository.class);
    private final AnnualizationRefundVoucherRepository vouchers = mock(AnnualizationRefundVoucherRepository.class);
    private final AnnualizationService annualization = mock(AnnualizationService.class);
    private final TaxRemittance1601CService remittances = mock(TaxRemittance1601CService.class);
    private final Bir1604COfficerReviewService service = new Bir1604COfficerReviewService(
            repository, auditRepository, snapshots, vouchers, annualization, remittances);

    @BeforeEach
    void setUp() {
        AnnualizationAlphalistPreviewDTO alphalist = mock(AnnualizationAlphalistPreviewDTO.class);
        TaxRemittance1601CReviewDTO ledger = mock(TaxRemittance1601CReviewDTO.class);
        when(alphalist.totalTaxesWithheld()).thenReturn(BigDecimal.ZERO);
        when(alphalist.exportEnabled()).thenReturn(true);
        when(ledger.confirmedTaxesWithheld()).thenReturn(new BigDecimal("1456.80"));
        when(ledger.reconciled()).thenReturn(true);
        when(ledger.issues()).thenReturn(List.of());
        when(annualization.getAlphalistPreview(eq(2026), any())).thenReturn(alphalist);
        when(remittances.review(eq(2026), any())).thenReturn(ledger);
        when(snapshots.findByTaxYearAndStatusOrderByEmployeeNameAsc(2026, "FINALIZED"))
                .thenReturn(List.of());
        when(repository.findByTaxYear(2026)).thenReturn(Optional.empty());
    }

    private Bir1604COfficerReviewRequest request(String refunds, String remarks) {
        return new Bir1604COfficerReviewRequest("09123456789", "payroll@example.test", 1,
                refunds, "YES".equals(refunds) ? LocalDate.of(2026, 11, 1) : null,
                new BigDecimal("1456.80"), 12, "OFFICER-EVIDENCE",
                "Sample Officer", "Payroll Officer", "123-456-789-000", remarks, "CONFIRMED");
    }

    @Test
    void requiresExplanationOfWithholdingGapAndAuditsConfirmedFacts() {
        assertThrows(IllegalArgumentException.class, () -> service.save(2026,
                request("NO", null), "officer", "Bearer token"));
        service.save(2026, request("NO", "Reviewed return and credit evidence."),
                "officer", "Bearer token");
        ArgumentCaptor<Bir1604COfficerReviewAudit> captor = ArgumentCaptor.forClass(Bir1604COfficerReviewAudit.class);
        verify(auditRepository).save(captor.capture());
        assertEquals("CREATED", captor.getValue().getAction());
        assertEquals("officer", captor.getValue().getPerformedBy());
    }

    @Test
    void paidEmployeeVoucherCannotBeDeclaredUnreleased() {
        AnnualizationSnapshot snapshot = new AnnualizationSnapshot();
        snapshot.setId(7L);
        AnnualizationRefundVoucher voucher = new AnnualizationRefundVoucher();
        voucher.setAmount(new BigDecimal("1456.80"));
        when(snapshots.findByTaxYearAndStatusOrderByEmployeeNameAsc(2026, "FINALIZED"))
                .thenReturn(List.of(snapshot));
        when(vouchers.findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(7L))
                .thenReturn(Optional.of(voucher));
        assertThrows(IllegalArgumentException.class, () -> service.save(2026,
                request("NO", "Voucher reconciled."), "officer", "Bearer token"));
    }

    @Test
    void monthlyRemittanceMustReconcileBeforeConfirmation() {
        TaxRemittance1601CReviewDTO ledger = remittances.review(2026, "Bearer token");
        when(ledger.reconciled()).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.save(2026,
                request("YES", "Gap reviewed."), "officer", "Bearer token"));
    }

    @Test
    void zeroWithholdingWithoutFiledMonthlyReturnsCanBeReviewedWithoutInventedRemittance() {
        TaxRemittance1601CReviewDTO ledger = remittances.review(2026, "Bearer token");
        when(ledger.confirmedTaxesWithheld()).thenReturn(BigDecimal.ZERO);
        service.save(2026, new Bir1604COfficerReviewRequest(
                "09123456789", "payroll@example.test", 1, "NO", null,
                BigDecimal.ZERO, null, "OFFICER-EVIDENCE", "Sample Officer",
                "Payroll Officer", "123-456-789-000", null, "CONFIRMED"),
                "officer", "Bearer token");
        ArgumentCaptor<Bir1604COfficerReview> captor = ArgumentCaptor.forClass(Bir1604COfficerReview.class);
        verify(repository).save(captor.capture());
        assertEquals(BigDecimal.ZERO, captor.getValue().getBasis1601CWithheld());
        assertEquals("NO", captor.getValue().getRefundsReleased());
    }

    @Test
    void changedWithholdingInvalidatesEarlierOfficerConfirmation() {
        Bir1604COfficerReview prior = new Bir1604COfficerReview();
        prior.setStatus("CONFIRMED");
        prior.setRefundsReleased("NO");
        prior.setBasis1601CWithheld(new BigDecimal("1000.00"));
        prior.setBasisAlphalistWithheld(BigDecimal.ZERO);
        prior.setBasisPaidRefunds(BigDecimal.ZERO);
        prior.setRemarks("Earlier reconciliation.");
        when(repository.findByTaxYear(2026)).thenReturn(Optional.of(prior));
        var result = service.review(2026, "Bearer token");
        assertEquals(true, result.issues().stream().anyMatch(issue -> issue.contains("reconfirm")));
    }
}
