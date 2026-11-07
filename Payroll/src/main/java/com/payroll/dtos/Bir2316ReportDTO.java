package com.payroll.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public record Bir2316ReportDTO(
        String taxYear, String periodFrom, String periodTo,
        String employeeTin, String employeeName, String employeeRdoCode,
        String employeeRegisteredAddress, String employeeZipCode,
        String employeeLocalAddress, String employeeLocalZipCode,
        String employeeBirthDate, String employeeContactNo,
        BigDecimal statutoryDailyRate, BigDecimal statutoryMonthlyRate, String mweMark,
        String employerTin, String employerName, String employerAddress,
        String employerZipCode, String mainEmployerMark,
        String previousEmployerTin, String previousEmployerName,
        String previousEmployerAddress, String previousEmployerZipCode,
        BigDecimal item19PresentGross, BigDecimal item20PresentNonTaxable,
        BigDecimal item21PresentTaxable, BigDecimal item22PreviousTaxable,
        BigDecimal item23GrossTaxable, BigDecimal item24TaxDue,
        BigDecimal item25APresentWithheld, BigDecimal item25BPreviousWithheld,
        BigDecimal item26TotalWithheldAdjusted, BigDecimal item27PeraTaxCredit,
        BigDecimal item28TotalTaxesWithheld,
        BigDecimal item29, BigDecimal item30, BigDecimal item31, BigDecimal item32,
        BigDecimal item33, BigDecimal item34, BigDecimal item35, BigDecimal item36,
        String item37Description, BigDecimal item37, BigDecimal item38,
        BigDecimal item39, BigDecimal item40, BigDecimal item41, BigDecimal item42,
        BigDecimal item43, String item44ADescription, BigDecimal item44A,
        String item44BDescription, BigDecimal item44B, BigDecimal item45,
        BigDecimal item46, BigDecimal item47, BigDecimal item48, BigDecimal item49,
        BigDecimal item50, String item51ADescription, BigDecimal item51A,
        String item51BDescription, BigDecimal item51B, BigDecimal item52,
        String substitutedFilingTitle, String substitutedFilingText,
        String dateIssued) {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final DateTimeFormatter MONTH_DAY = DateTimeFormatter.ofPattern("MM/dd");
    private static final DateTimeFormatter FULL_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public static Bir2316ReportDTO from(AnnualizationEmployeeDTO source) {
        return from(source, ZERO);
    }

    public static Bir2316ReportDTO from(
            AnnualizationEmployeeDTO source, BigDecimal postedWithholdingAdjustment) {
        if (source == null || !"FINALIZED".equals(source.status())) {
            throw new IllegalArgumentException("BIR Form 2316 requires a finalized annualization snapshot.");
        }
        if (source.previousEmployers().size() > 1) {
            throw new IllegalArgumentException(
                    "BIR Form 2316 supports one previous-employer section. Review the employee's multiple previous-employer records before generating the certificate.");
        }
        AnnualizationTaxIdentityDTO identity = source.taxIdentity();
        Bir2316CompensationItemsDTO items = source.bir2316Items();
        if (identity == null || items == null) {
            throw new IllegalArgumentException("The finalized annualization snapshot is incomplete for BIR Form 2316.");
        }
        List<AnnualizationPeriodDTO> periods = source.periods();
        LocalDate periodFrom = identity.employeeEmploymentFrom();
        LocalDate periodTo = identity.employeeEmploymentTo();
        if (periodFrom == null || periodTo == null || periodFrom.isAfter(periodTo)
                || periodFrom.getYear() != source.taxYear() || periodTo.getYear() != source.taxYear()) {
            throw new IllegalArgumentException(
                    "Verified employee employment dates for the tax year are required for BIR Form 2316.");
        }
        AnnualizationPeriodDTO latestMwePeriod = periods.stream()
                .filter(period -> Boolean.TRUE.equals(period.minimumWageEarner()))
                .filter(period -> period.salaryDate() != null)
                .max(Comparator.comparing(AnnualizationPeriodDTO::salaryDate)).orElse(null);
        PreviousEmployerTaxDataDTO previous = source.previousEmployers().isEmpty()
                ? null : source.previousEmployers().get(0);
        BigDecimal presentWithheldAdjusted = amount(source.presentTaxWithheld())
                .add(amount(postedWithholdingAdjustment));
        BigDecimal totalWithheld = presentWithheldAdjusted.add(amount(source.previousTaxWithheld()));
        if (presentWithheldAdjusted.signum() < 0) {
            throw new IllegalArgumentException("Posted refund exceeds tax withheld by the present employer.");
        }
        BigDecimal peraTaxCredit = amount(source.peraTaxCredit());

        return new Bir2316ReportDTO(
                String.valueOf(source.taxYear()), format(periodFrom, MONTH_DAY), format(periodTo, MONTH_DAY),
                fullTin(identity.employeeTin(), identity.employeeBirBranchCode()),
                legalName(identity, source.employeeName()), text(identity.employeeRdoCode()),
                text(identity.employeeAddress()), text(identity.employeeZipCode()),
                text(identity.employeeLocalAddress()), text(identity.employeeLocalZipCode()),
                format(identity.employeeBirthDate(), FULL_DATE), text(identity.employeeContactNo()),
                latestMwePeriod == null ? ZERO : amount(latestMwePeriod.minimumWageDailyRate()),
                latestMwePeriod == null ? ZERO : amount(latestMwePeriod.minimumWageMonthlyRate()),
                latestMwePeriod == null ? "" : "X",
                fullTin(identity.employerTin(), identity.employerBirBranchCode()),
                text(identity.employerName()), text(identity.employerAddress()),
                text(identity.employerZipCode()), "X",
                previous == null ? "" : text(previous.employerTin()),
                previous == null ? "" : text(previous.employerName()),
                previous == null ? "" : text(previous.employerAddress()),
                previous == null ? "" : text(previous.employerZipCode()),
                amount(source.presentGrossCompensation()), amount(source.presentNonTaxableCompensation()),
                amount(source.presentTaxableCompensation()), amount(source.previousTaxableCompensation()),
                amount(source.taxableCompensation()), amount(source.annualTaxDue()),
                presentWithheldAdjusted, amount(source.previousTaxWithheld()),
                totalWithheld, peraTaxCredit, totalWithheld.add(peraTaxCredit),
                amount(items.item29BasicSalaryMwe()), amount(items.item30HolidayPayMwe()),
                amount(items.item31OvertimePayMwe()), amount(items.item32NightShiftDifferentialMwe()),
                amount(items.item33HazardPayMwe()), amount(items.item34ThirteenthMonthAndOtherBenefits()),
                amount(items.item35DeMinimisBenefits()), amount(items.item36MandatoryContributionsAndUnionDues()),
                text(items.item37OtherNonTaxableDescription()), amount(items.item37OtherNonTaxable()),
                amount(items.item38TotalNonTaxable()), amount(items.item39BasicSalary()),
                amount(items.item40Representation()), amount(items.item41Transportation()),
                amount(items.item42CostOfLivingAllowance()), amount(items.item43FixedHousingAllowance()),
                text(items.item44AOtherRegularDescription()), amount(items.item44AOtherRegular()),
                text(items.item44BOtherRegularDescription()), amount(items.item44BOtherRegular()),
                amount(items.item45Commission()), amount(items.item46ProfitSharing()),
                amount(items.item47FeesIncludingDirectorsFees()), amount(items.item48TaxableThirteenthMonthBenefits()),
                amount(items.item49HazardPay()), amount(items.item50OvertimePay()),
                text(items.item51AOtherSupplementaryDescription()), amount(items.item51AOtherSupplementary()),
                text(items.item51BOtherSupplementaryDescription()), amount(items.item51BOtherSupplementary()),
                amount(items.item52TotalTaxable()), substitutedFilingTitle(source),
                substitutedFilingText(source), format(LocalDate.now(), FULL_DATE));
    }

    private static String substitutedFilingTitle(AnnualizationEmployeeDTO source) {
        if (source.substitutedFilingQualified() == null) {
            return "SUBSTITUTED FILING — NOT ASSESSED";
        }
        return Boolean.TRUE.equals(source.substitutedFilingQualified())
                ? "SUBSTITUTED FILING — QUALIFIED"
                : "SUBSTITUTED FILING — NOT QUALIFIED";
    }

    private static String substitutedFilingText(AnnualizationEmployeeDTO source) {
        if (source.substitutedFilingQualified() == null) {
            return "No authorized substituted-filing assessment is stored in this snapshot.";
        }
        if (!Boolean.TRUE.equals(source.substitutedFilingQualified())) {
            return "Reason: " + text(source.substitutedFilingReason())
                    + "\nReview the employee's applicable BIR income tax return filing requirements.";
        }
        return "Employer confirms that this information is included in BIR Form 1604-C. "
                + "Employee confirms pure compensation income from one Philippine employer and correct withholding.\n"
                + "55  ____________________  Present Employer / Authorized Agent    "
                + "56  ____________________  Employee";
    }

    private static BigDecimal amount(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private static String text(String value) {
        return value == null ? "" : value.trim();
    }

    private static String fullTin(String tin, String branchCode) {
        String digits = text(tin).replaceAll("\\D", "");
        String branch = text(branchCode);
        return digits.length() >= 9 && branch.matches("\\d{4}")
                ? digits.substring(0, 9) + branch : digits;
    }

    private static String legalName(AnnualizationTaxIdentityDTO identity, String fallback) {
        String last = text(identity.employeeLastName());
        String first = text(identity.employeeFirstName());
        if (last.isEmpty() || first.isEmpty()) return text(fallback);
        String given = String.join(" ", java.util.stream.Stream.of(
                first, text(identity.employeeMiddleName()), text(identity.employeeExtensionName()))
                .filter(value -> !value.isEmpty()).toList());
        return last + ", " + given;
    }

    private static String format(LocalDate value, DateTimeFormatter formatter) {
        return value == null ? "" : formatter.format(value);
    }
}
