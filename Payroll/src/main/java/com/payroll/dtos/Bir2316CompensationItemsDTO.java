package com.payroll.dtos;

import java.math.BigDecimal;

/** Typed BIR Form 2316 September 2021 ENCS compensation items for the present employer. */
public record Bir2316CompensationItemsDTO(
        BigDecimal item29BasicSalaryMwe,
        BigDecimal item30HolidayPayMwe,
        BigDecimal item31OvertimePayMwe,
        BigDecimal item32NightShiftDifferentialMwe,
        BigDecimal item33HazardPayMwe,
        BigDecimal item34ThirteenthMonthAndOtherBenefits,
        BigDecimal item35DeMinimisBenefits,
        BigDecimal item36MandatoryContributionsAndUnionDues,
        String item37OtherNonTaxableDescription,
        BigDecimal item37OtherNonTaxable,
        BigDecimal item38TotalNonTaxable,
        BigDecimal item39BasicSalary,
        BigDecimal item40Representation,
        BigDecimal item41Transportation,
        BigDecimal item42CostOfLivingAllowance,
        BigDecimal item43FixedHousingAllowance,
        String item44AOtherRegularDescription,
        BigDecimal item44AOtherRegular,
        String item44BOtherRegularDescription,
        BigDecimal item44BOtherRegular,
        BigDecimal item45Commission,
        BigDecimal item46ProfitSharing,
        BigDecimal item47FeesIncludingDirectorsFees,
        BigDecimal item48TaxableThirteenthMonthBenefits,
        BigDecimal item49HazardPay,
        BigDecimal item50OvertimePay,
        String item51AOtherSupplementaryDescription,
        BigDecimal item51AOtherSupplementary,
        String item51BOtherSupplementaryDescription,
        BigDecimal item51BOtherSupplementary,
        BigDecimal item52TotalTaxable) {
}
