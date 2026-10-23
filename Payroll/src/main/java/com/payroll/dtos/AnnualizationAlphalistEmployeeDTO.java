package com.payroll.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AnnualizationAlphalistEmployeeDTO(
        int sequenceNo,
        String employeeNo,
        String employeeName,
        String employeeTin,
        String employeeBirBranchCode,
        String employeeLastName,
        String employeeFirstName,
        String employeeMiddleName,
        String employeeExtensionName,
        String employeeNationality,
        String employeeEmploymentStatusCode,
        String employeeEmploymentStatusName,
        Integer birScheduleNo,
        String birRegionCode,
        LocalDate employeeEmploymentFrom,
        LocalDate employeeEmploymentTo,
        String employeeBirSeparationReasonCode,
        LocalDate payrollPeriodFrom,
        LocalDate payrollPeriodTo,
        BigDecimal grossCompensation,
        BigDecimal nonTaxableCompensation,
        BigDecimal taxableCompensation,
        BigDecimal annualTaxDue,
        BigDecimal taxWithheld,
        BigDecimal previousEmployerTaxWithheld,
        BigDecimal presentEmployerTaxWithheld,
        BigDecimal peraTaxCredit,
        BigDecimal amountWithheldAndPaidInDecember,
        BigDecimal overWithheldTaxRefunded,
        BigDecimal actualTaxWithheldAdjusted,
        BigDecimal totalTaxesWithheld,
        Boolean substitutedFilingQualified,
        String substitutedFilingReason,
        boolean valid,
        List<String> issues) {
}
