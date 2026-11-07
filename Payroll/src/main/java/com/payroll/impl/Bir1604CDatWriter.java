package com.payroll.impl;

import com.payroll.dtos.AnnualizationAlphalistEmployeeDTO;
import com.payroll.entitymodels.AnnualizationSnapshotPeriod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class Bir1604CDatWriter {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final Set<String> BIR_REGION_CODES = Set.of(
            "I", "II", "III", "IV-A", "IV-B", "V", "VI", "VII", "VIII",
            "IX", "X", "XI", "XII", "XIII", "NCR", "ARMM", "CAR",
            "1", "2", "3", "4-A", "4-B", "5", "6", "7", "8", "9",
            "10", "11", "12", "13");

    public ExportFile write(
            int taxYear,
            String employerTin,
            String employerBranchCode,
            List<AnnualizationAlphalistEmployeeDTO> records,
            Map<String, Map<String, BigDecimal>> itemsByEmployee,
            Map<String, List<AnnualizationSnapshotPeriod>> periodsByEmployee) {
        String tin = digits(employerTin);
        if (tin.length() != 9) throw conflict("Employer TIN must contain exactly nine digits.");
        if (employerBranchCode == null || !employerBranchCode.matches("\\d{4}")) {
            throw conflict("Employer BIR branch code must contain exactly four digits.");
        }
        if (records == null || records.isEmpty()) {
            throw conflict("At least one resolved employee is required for BIR export.");
        }
        String period = DATE.format(LocalDate.of(taxYear, 12, 31));
        List<String> lines = new ArrayList<>();
        lines.add(String.join(",", "H1604C", text(tin), text(employerBranchCode), period));

        List<Detail> schedule1 = new ArrayList<>();
        List<Detail> schedule2 = new ArrayList<>();
        int sequence1 = 1;
        int sequence2 = 1;
        for (AnnualizationAlphalistEmployeeDTO record : records) {
            Map<String, BigDecimal> items = itemsByEmployee.getOrDefault(record.employeeNo(), Map.of());
            if (Integer.valueOf(1).equals(record.birScheduleNo())) {
                schedule1.add(schedule1(record, sequence1++, tin, employerBranchCode, period, items));
            } else if (Integer.valueOf(2).equals(record.birScheduleNo())) {
                schedule2.add(schedule2(record, sequence2++, tin, employerBranchCode, period, items,
                        periodsByEmployee.getOrDefault(record.employeeNo(), List.of())));
            } else {
                throw conflict("BIR schedule is unresolved for " + record.employeeNo() + ".");
            }
        }

        schedule1.forEach(detail -> lines.add(detail.line()));
        if (!schedule1.isEmpty()) lines.add(control("C1", tin, employerBranchCode, period, schedule1));
        schedule2.forEach(detail -> lines.add(detail.line()));
        if (!schedule2.isEmpty()) lines.add(control("C2", tin, employerBranchCode, period, schedule2));

        String fileName = tin + employerBranchCode + "1231" + taxYear + "1604C.DAT";
        byte[] content = (String.join("\r\n", lines) + "\r\n").getBytes(StandardCharsets.US_ASCII);
        return new ExportFile(fileName, content);
    }

    private Detail schedule1(
            AnnualizationAlphalistEmployeeDTO r, int sequence, String employerTin,
            String employerBranch, String period, Map<String, BigDecimal> item) {
        List<String> fields = common(r, "D1", sequence, employerTin, employerBranch, period);
        List<BigDecimal> controls = zeros(11);
        fields.addAll(moneyFields(controls));
        fields.add(date(r.employeeEmploymentFrom()));
        fields.add(date(r.employeeEmploymentTo()));

        BigDecimal nontax13 = amount(item, "34");
        BigDecimal deMinimis = amount(item, "35");
        BigDecimal contributions = amount(item, "36");
        BigDecimal otherNontax = amount(item, "37");
        BigDecimal totalNontax = amount(item, "38");
        BigDecimal taxableBasic = amount(item, "39");
        BigDecimal taxable13 = amount(item, "48");
        BigDecimal otherTaxable = nonNegative(amount(item, "52").subtract(taxableBasic).subtract(taxable13));
        List<BigDecimal> present = List.of(
                r.grossCompensation(), amount(item, "29"), nontax13, deMinimis, contributions, otherNontax,
                totalNontax, taxableBasic, taxable13, otherTaxable,
                r.taxableCompensation(), r.taxableCompensation(), r.taxableCompensation(),
                r.annualTaxDue(), r.previousEmployerTaxWithheld(), r.presentEmployerTaxWithheld(),
                r.amountWithheldAndPaidInDecember(), r.overWithheldTaxRefunded(),
                r.actualTaxWithheldAdjusted());
        fields.addAll(moneyFields(present));
        fields.add(text(cleanText(r.employeeNationality(), 30, "nationality", r.employeeNo())));
        fields.add(text(r.employeeEmploymentStatusCode()));
        fields.add(text(separationReason(r)));
        fields.add(text(Boolean.TRUE.equals(r.substitutedFilingQualified()) ? "Y" : "N"));
        fields.add(money(r.peraTaxCredit()));

        List<BigDecimal> totals = new ArrayList<>(controls);
        totals.addAll(present);
        totals.add(scale(r.peraTaxCredit()));
        return new Detail(String.join(",", fields), totals);
    }

    private Detail schedule2(
            AnnualizationAlphalistEmployeeDTO r, int sequence, String employerTin,
            String employerBranch, String period, Map<String, BigDecimal> item,
            List<AnnualizationSnapshotPeriod> periods) {
        List<String> fields = common(r, "D2", sequence, employerTin, employerBranch, period);
        List<BigDecimal> previous = zeros(14);
        fields.addAll(moneyFields(previous));
        fields.add(date(r.employeeEmploymentFrom()));
        fields.add(date(r.employeeEmploymentTo()));

        RateBasis basis = rateBasis(periods, r.employeeNo());
        BigDecimal basic = amount(item, "29");
        BigDecimal taxable13 = amount(item, "48");
        BigDecimal otherTaxable = nonNegative(amount(item, "52").subtract(taxable13));
        List<BigDecimal> present = List.of(
                amount(item, "38"), basis.dailyRate(), basis.monthlyRate(), basic,
                BigDecimal.valueOf(basis.factor()), amount(item, "30"), amount(item, "31"),
                amount(item, "32"), amount(item, "33"), amount(item, "34"), amount(item, "35"),
                amount(item, "36"), amount(item, "37"), amount(item, "38"), taxable13,
                otherTaxable, r.taxableCompensation(), r.taxableCompensation(), r.taxableCompensation(),
                r.annualTaxDue(), r.previousEmployerTaxWithheld(), r.presentEmployerTaxWithheld(),
                r.amountWithheldAndPaidInDecember(), r.overWithheldTaxRefunded(),
                r.actualTaxWithheldAdjusted());
        fields.addAll(moneyFields(present.subList(0, 4)));
        fields.add(Integer.toString(basis.factor()));
        fields.addAll(moneyFields(present.subList(5, present.size())));
        fields.add(text(cleanText(r.employeeNationality(), 30, "nationality", r.employeeNo())));
        fields.add(text(r.employeeEmploymentStatusCode()));
        fields.add(text(separationReason(r)));
        fields.add(text(Boolean.TRUE.equals(r.substitutedFilingQualified()) ? "Y" : "N"));
        fields.add(money(r.peraTaxCredit()));
        fields.add(money(basic));

        List<BigDecimal> totals = new ArrayList<>(previous);
        totals.addAll(present.subList(0, 4));
        totals.addAll(present.subList(5, present.size()));
        totals.add(scale(r.peraTaxCredit()));
        totals.add(scale(basic));
        return new Detail(String.join(",", fields), totals);
    }

    private List<String> common(
            AnnualizationAlphalistEmployeeDTO r, String schedule, int sequence,
            String employerTin, String employerBranch, String period) {
        String employeeTin = digits(r.employeeTin());
        if (employeeTin.length() != 9) {
            throw conflict("Employee " + r.employeeNo() + " TIN must contain exactly nine digits.");
        }
        if (r.employeeBirBranchCode() == null || !r.employeeBirBranchCode().matches("\\d{4}")) {
            throw conflict("Employee " + r.employeeNo() + " BIR branch code must contain exactly four digits.");
        }
        String regionCode = r.birRegionCode() == null
                ? "" : r.birRegionCode().trim().toUpperCase(java.util.Locale.ROOT);
        if (!BIR_REGION_CODES.contains(regionCode)) {
            throw conflict("Employee " + r.employeeNo() + " BIR region must be a valid BIR region code.");
        }
        return new ArrayList<>(List.of(
                schedule, text("1604C"), text(employerTin), text(employerBranch), period,
                Integer.toString(sequence), text(employeeTin),
                text(r.employeeBirBranchCode()),
                text(cleanText(r.employeeLastName(), 30, "last name", r.employeeNo())),
                text(cleanText(r.employeeFirstName(), 30, "first name", r.employeeNo())),
                text(cleanText(r.employeeMiddleName(), 30, "middle name", r.employeeNo())),
                text(regionCode)));
    }

    private String separationReason(AnnualizationAlphalistEmployeeDTO record) {
        boolean separated = record.employeeEmploymentTo() != null
                && record.employeeEmploymentTo().isBefore(
                        java.time.LocalDate.of(record.employeeEmploymentTo().getYear(), 12, 31));
        if (!separated) return "NA";
        String reason = record.employeeBirSeparationReasonCode();
        if (reason == null || !java.util.Set.of("T", "TR", "R", "D").contains(reason)) {
            throw conflict("Employee " + record.employeeNo() + " needs a valid BIR separation reason.");
        }
        return reason;
    }

    private String control(
            String schedule, String tin, String branch, String period, List<Detail> details) {
        int size = details.get(0).controlValues().size();
        List<BigDecimal> totals = zeros(size);
        for (Detail detail : details) {
            for (int index = 0; index < size; index++) {
                totals.set(index, totals.get(index).add(detail.controlValues().get(index)));
            }
        }
        List<String> fields = new ArrayList<>(List.of(
                schedule, text("1604C"), text(tin), text(branch), period));
        fields.addAll(moneyFields(totals));
        return String.join(",", fields);
    }

    private RateBasis rateBasis(List<AnnualizationSnapshotPeriod> periods, String employeeNo) {
        Map<String, RateBasis> distinct = new LinkedHashMap<>();
        for (AnnualizationSnapshotPeriod period : periods) {
            if (!Boolean.TRUE.equals(period.getMinimumWageEarner())
                    || period.getMinimumWageDailyRate() == null
                    || period.getMinimumWageMonthlyRate() == null
                    || period.getMinimumWageDailyRate().signum() <= 0
                    || period.getMinimumWageMonthlyRate().signum() <= 0) {
                throw conflict("A complete minimum-wage rate basis is required for every payroll period of "
                        + employeeNo + ".");
            }
            BigDecimal daily = scale(period.getMinimumWageDailyRate());
            BigDecimal monthly = scale(period.getMinimumWageMonthlyRate());
            int factor = monthly.multiply(BigDecimal.valueOf(12))
                    .divide(daily, 0, RoundingMode.HALF_UP).intValueExact();
            RateBasis basis = new RateBasis(daily, monthly, factor);
            distinct.put(daily + ":" + monthly + ":" + factor, basis);
        }
        if (distinct.size() != 1) {
            throw conflict("A single complete minimum-wage rate basis is required for " + employeeNo + ".");
        }
        return distinct.values().iterator().next();
    }

    private List<BigDecimal> zeros(int count) {
        List<BigDecimal> values = new ArrayList<>(count);
        for (int index = 0; index < count; index++) values.add(ZERO);
        return values;
    }

    private List<String> moneyFields(List<BigDecimal> values) {
        return values.stream().map(this::money).toList();
    }

    private BigDecimal amount(Map<String, BigDecimal> item, String key) {
        return scale(item.getOrDefault(key, ZERO));
    }

    private BigDecimal nonNegative(BigDecimal value) {
        if (value.signum() < 0) throw conflict("BIR compensation itemization does not reconcile.");
        return scale(value);
    }

    private BigDecimal scale(BigDecimal value) {
        return (value == null ? ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String money(BigDecimal value) { return scale(value).toPlainString(); }
    private String date(LocalDate value) { return value == null ? "" : DATE.format(value); }
    private String digits(String value) { return value == null ? "" : value.replaceAll("\\D", ""); }

    private String cleanText(String value, int max, String label, String employeeNo) {
        String clean = value == null ? "" : value.trim().replaceAll("[\\r\\n]", " ");
        if (clean.length() > max) throw conflict("Employee " + employeeNo + " " + label
                + " exceeds the BIR " + max + "-character limit.");
        if (!StandardCharsets.US_ASCII.newEncoder().canEncode(clean)) {
            throw conflict("Employee " + employeeNo + " " + label
                    + " contains characters unsupported by the BIR DAT format.");
        }
        return clean;
    }

    private String text(String value) {
        String clean = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + clean + "\"";
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private record Detail(String line, List<BigDecimal> controlValues) {}
    private record RateBasis(BigDecimal dailyRate, BigDecimal monthlyRate, int factor) {}
    public record ExportFile(String fileName, byte[] content) {}
}
