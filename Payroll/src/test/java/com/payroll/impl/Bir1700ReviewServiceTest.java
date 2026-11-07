package com.payroll.impl;

import com.payroll.dtos.AnnualizationEmployeeDTO;
import com.payroll.dtos.AnnualizationTaxIdentityDTO;
import com.payroll.dtos.Bir1700ReviewDTO;
import com.payroll.dtos.Bir2316ReportDTO;
import com.payroll.reports.Bir1700OfficialFormWriter;
import com.payroll.repositories.AnnualizationAuditLogRepository;
import com.payroll.services.Bir2316ReportService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Bir1700ReviewServiceTest {
    private final Bir2316ReportService certificateService = mock(Bir2316ReportService.class);
    private final Bir1700ReviewService service = new Bir1700ReviewService(certificateService,
            mock(Bir1700OfficialFormWriter.class), mock(AnnualizationAuditLogRepository.class));

    @Test
    void paidRefundIsNotClaimedAgainAsTaxWithheld() {
        AnnualizationEmployeeDTO annualization = mock(AnnualizationEmployeeDTO.class);
        AnnualizationTaxIdentityDTO identity = mock(AnnualizationTaxIdentityDTO.class);
        Bir2316ReportDTO certificate = mock(Bir2316ReportDTO.class);
        when(annualization.taxIdentity()).thenReturn(identity);
        when(annualization.previousEmployers()).thenReturn(List.of());
        when(annualization.periods()).thenReturn(List.of());
        when(annualization.grossCompensation()).thenReturn(new BigDecimal("33750.00"));
        when(annualization.presentGrossCompensation()).thenReturn(new BigDecimal("33750.00"));
        when(annualization.nonTaxableCompensation()).thenReturn(new BigDecimal("3205.00"));
        when(annualization.taxableCompensation()).thenReturn(new BigDecimal("30545.00"));
        when(annualization.annualTaxDue()).thenReturn(BigDecimal.ZERO);
        when(annualization.taxYear()).thenReturn(2026);
        when(annualization.employeeNo()).thenReturn("202600001");
        when(annualization.employeeName()).thenReturn("Maria Reyes");
        when(annualization.snapshotVersion()).thenReturn(1);
        when(identity.employerName()).thenReturn("Test Employer");
        when(identity.employerTin()).thenReturn("123456789");
        when(certificate.item25APresentWithheld()).thenReturn(BigDecimal.ZERO);
        when(certificate.item26TotalWithheldAdjusted()).thenReturn(BigDecimal.ZERO);
        when(certificateService.prepareReportData(annualization)).thenReturn(certificate);

        Bir1700ReviewDTO review = service.review(annualization);

        assertEquals(new BigDecimal("33750.00"), review.grossCompensation());
        assertEquals(BigDecimal.ZERO, review.taxWithheldAfterAdjustment());
        assertEquals(BigDecimal.ZERO, review.employers().get(0).taxWithheld());
        assertEquals(1, review.employers().size());
    }

    @Test
    void rejectsEmployerRowsThatDoNotReconcileWith2316() {
        AnnualizationEmployeeDTO annualization = mock(AnnualizationEmployeeDTO.class);
        Bir2316ReportDTO certificate = mock(Bir2316ReportDTO.class);
        when(annualization.previousEmployers()).thenReturn(List.of());
        when(annualization.taxIdentity()).thenReturn(mock(AnnualizationTaxIdentityDTO.class));
        when(annualization.presentGrossCompensation()).thenReturn(new BigDecimal("33750.00"));
        when(annualization.grossCompensation()).thenReturn(new BigDecimal("33750.00"));
        when(certificate.item25APresentWithheld()).thenReturn(BigDecimal.ZERO);
        when(certificate.item26TotalWithheldAdjusted()).thenReturn(new BigDecimal("1456.80"));
        when(certificateService.prepareReportData(annualization)).thenReturn(certificate);

        assertThrows(IllegalStateException.class, () -> service.review(annualization));
    }
}
