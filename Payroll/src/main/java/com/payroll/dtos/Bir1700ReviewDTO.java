package com.payroll.dtos;

import java.math.BigDecimal;
import java.util.List;

/** HRIS-held facts for an individual's Form 1700 review, not a filing decision. */
public record Bir1700ReviewDTO(
        String employeeNo,
        int taxYear,
        int snapshotVersion,
        String employeeName,
        String reviewStatus,
        List<String> reviewNotes,
        int payrollPeriods,
        BigDecimal grossCompensation,
        BigDecimal nonTaxableCompensation,
        BigDecimal taxableCompensation,
        BigDecimal annualTaxDue,
        BigDecimal taxWithheldAfterAdjustment,
        List<EmployerLine> employers) {

    public record EmployerLine(String employerName, String employerTin,
                               BigDecimal grossCompensation, BigDecimal taxWithheld) {}
}
