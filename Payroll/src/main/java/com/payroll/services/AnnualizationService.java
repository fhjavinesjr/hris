package com.payroll.services;

import com.payroll.dtos.AnnualizationEmployeeDTO;
import com.payroll.dtos.AnnualizationYearDTO;
import com.payroll.dtos.AnnualizationAdjustmentPeriodDTO;
import com.payroll.dtos.PreviousEmployerTaxDataDTO;
import com.payroll.dtos.PeraTaxCreditDTO;
import com.payroll.dtos.AnnualizationAlphalistPreviewDTO;
import com.payroll.dtos.AnnualizationDatExportDTO;
import com.payroll.dtos.AnnualBenefitReviewLineDTO;
import com.payroll.dtos.AnnualBenefitReviewRequest;
import com.payroll.dtos.AnnualizationRefundVoucherDTO;
import com.payroll.dtos.AnnualizationRefundVoucherRequest;
import com.payroll.dtos.SeparationFinalPayReviewDTO;

import java.util.List;

public interface AnnualizationService {
    List<AnnualBenefitReviewLineDTO> getBenefitReviewLines(int taxYear, String employeeNo, String authorizationHeader);
    AnnualBenefitReviewLineDTO saveBenefitReview(int taxYear, String employeeNo,
            String sourceType, Long sourceId, AnnualBenefitReviewRequest request,
            String performedBy, String authorizationHeader);
    AnnualizationYearDTO getAnnualization(int taxYear, String authorizationHeader);
    AnnualizationEmployeeDTO getEmployeeAnnualization(
            int taxYear, String employeeNo, String authorizationHeader);
    AnnualizationAlphalistPreviewDTO getAlphalistPreview(
            int taxYear, String authorizationHeader);
    AnnualizationDatExportDTO exportAlphalist(
            int taxYear, String performedBy, String authorizationHeader);
    List<PreviousEmployerTaxDataDTO> getPreviousEmployers(
            int taxYear, String employeeNo, String authorizationHeader);
    PreviousEmployerTaxDataDTO createPreviousEmployer(
            int taxYear, String employeeNo, PreviousEmployerTaxDataDTO request,
            String performedBy, String authorizationHeader);
    PreviousEmployerTaxDataDTO updatePreviousEmployer(
            int taxYear, String employeeNo, Long id, PreviousEmployerTaxDataDTO request,
            String performedBy, String authorizationHeader);
    void deletePreviousEmployer(
            int taxYear, String employeeNo, Long id, String performedBy, String authorizationHeader);
    PeraTaxCreditDTO getPeraTaxCredit(
            int taxYear, String employeeNo, String authorizationHeader);
    PeraTaxCreditDTO savePeraTaxCredit(
            int taxYear, String employeeNo, PeraTaxCreditDTO request,
            String performedBy, String authorizationHeader);
    void deletePeraTaxCredit(
            int taxYear, String employeeNo, String performedBy, String authorizationHeader);
    AnnualizationEmployeeDTO finalizeAnnualization(
            int taxYear, String employeeNo, String remarks,
            Boolean substitutedFilingQualified, String substitutedFilingReason,
            String performedBy,
            String authorizationHeader);
    AnnualizationEmployeeDTO reopenAnnualization(
            int taxYear, String employeeNo, String reason, String performedBy,
            String authorizationHeader);
    List<AnnualizationAdjustmentPeriodDTO> getEligibleAdjustmentPeriods(
            int taxYear, String employeeNo, String authorizationHeader);
    AnnualizationEmployeeDTO applyAdjustment(
            int taxYear, String employeeNo, String salaryPeriodKey, String performedBy,
            String authorizationHeader);
    List<AnnualizationAdjustmentPeriodDTO> getSeparationFinalPayPeriods(
            int taxYear, String employeeNo, String authorizationHeader);
    SeparationFinalPayReviewDTO reviewSeparationFinalPay(
            int taxYear, String employeeNo, String salaryPeriodKey, String authorizationHeader);
    SeparationFinalPayReviewDTO prepareSeparationFinalPayAdjustment(
            int taxYear, String employeeNo, String salaryPeriodKey,
            String performedBy, String authorizationHeader);
    AnnualizationRefundVoucherDTO getRefundVoucher(
            int taxYear, String employeeNo, String authorizationHeader);
    AnnualizationRefundVoucherDTO recordRefundVoucher(
            int taxYear, String employeeNo, AnnualizationRefundVoucherRequest request,
            String performedBy, String authorizationHeader);
    void voidRefundVoucher(
            int taxYear, String employeeNo, String reason, String performedBy,
            String authorizationHeader);
}
