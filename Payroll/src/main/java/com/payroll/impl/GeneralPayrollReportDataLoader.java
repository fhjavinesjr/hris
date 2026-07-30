package com.payroll.impl;

import com.payroll.dtos.GeneralPayrollReportRow;
import com.payroll.services.PayrollPeriodLockService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads and aggregates General Payroll data in provider-neutral Java.
 *
 * <p>The legacy JRXML performed ordered {@code STRING_AGG}, SQL Server money
 * formatting, Boolean filtering, and signatory lookups in one vendor-specific
 * statement. This loader keeps database retrieval simple and performs dynamic
 * line aggregation and formatting in Java.</p>
 */
@Component
public class GeneralPayrollReportDataLoader {

    private static final String FINAL_MODE =
            "FINAL / LOCKED - POSTED ADJUSTMENTS ONLY";
    private static final String PREVIEW_MODE =
            "DRAFT / PREVIEW - POSTED + PENDING ADJUSTMENTS INCLUDED";

    private static final Comparator<String> TEXT_ORDER =
            String.CASE_INSENSITIVE_ORDER.thenComparing(
                    Comparator.naturalOrder()
            );
    private static final Comparator<LineTotal> LINE_ORDER =
            Comparator.comparingInt(LineTotal::indexNo)
                    .thenComparing(LineTotal::displayName, TEXT_ORDER)
                    .thenComparing(LineTotal::code, TEXT_ORDER);

    private final NamedParameterJdbcTemplate jdbc;
    private final PayrollPeriodLockService payrollPeriodLockService;

    public GeneralPayrollReportDataLoader(NamedParameterJdbcTemplate jdbc,
                                          PayrollPeriodLockService payrollPeriodLockService) {
        this.jdbc = jdbc;
        this.payrollPeriodLockService = payrollPeriodLockService;
    }

    public List<GeneralPayrollReportRow> load(String salaryPeriodKey,
                                               String payrollGroup) {
        boolean periodLocked = payrollPeriodLockService.isPeriodLocked(salaryPeriodKey);
        List<BaseDetail> baseDetails = loadBaseDetails(salaryPeriodKey, payrollGroup);
        if (baseDetails.isEmpty()) {
            return List.of();
        }

        List<Long> detailIds = baseDetails.stream().map(BaseDetail::id).toList();
        Map<Long, String> employeeByDetailId = new LinkedHashMap<>();
        for (BaseDetail detail : baseDetails) {
            employeeByDetailId.put(detail.id(), detail.employeeNo());
        }

        List<LineItem> earningItems = loadBaseEarnings(detailIds, employeeByDetailId);
        List<LineItem> deductionItems = loadBaseDeductions(detailIds, employeeByDetailId);
        addEligibleAdjustments(
                salaryPeriodKey,
                periodLocked,
                earningItems,
                deductionItems
        );

        List<LineTotal> earningTotals = aggregateByEmployee(earningItems);
        List<LineTotal> deductionTotals = aggregateByEmployee(deductionItems);
        Map<String, List<LineTotal>> earningsByEmployee = groupByEmployee(earningTotals);
        Map<String, List<LineTotal>> deductionsByEmployee = groupByEmployee(deductionTotals);

        baseDetails.sort(
                Comparator.comparing(BaseDetail::displayToLastPage)
                        .thenComparing(BaseDetail::department, TEXT_ORDER)
                        .thenComparing(BaseDetail::employeeName, TEXT_ORDER)
                        .thenComparing(BaseDetail::employeeNo, TEXT_ORDER)
        );

        List<GeneralPayrollReportRow> rows = new ArrayList<>(baseDetails.size());
        BigDecimal grandActualBasic = BigDecimal.ZERO;
        BigDecimal grandGross = BigDecimal.ZERO;
        BigDecimal grandDeduction = BigDecimal.ZERO;
        BigDecimal grandNet = BigDecimal.ZERO;

        for (int index = 0; index < baseDetails.size(); index++) {
            BaseDetail detail = baseDetails.get(index);
            List<LineTotal> employeeEarnings =
                    earningsByEmployee.getOrDefault(detail.employeeNo(), List.of());
            List<LineTotal> employeeDeductions =
                    deductionsByEmployee.getOrDefault(detail.employeeNo(), List.of());

            BigDecimal gross = sum(employeeEarnings);
            BigDecimal deductions = sum(employeeDeductions);
            BigDecimal net = gross.subtract(deductions);

            GeneralPayrollReportRow row = new GeneralPayrollReportRow();
            row.setRowNo(index + 1);
            row.setEmployeeNo(detail.employeeNo());
            row.setEmployeeName(detail.employeeName());
            row.setDepartment(detail.department());
            row.setSalaryGrade(detail.salaryGrade());
            row.setSalaryStep(detail.salaryStep());
            row.setSalaryPeriodKey(detail.salaryPeriodKey());
            row.setCutoffStartDate(detail.cutoffStartDate());
            row.setCutoffEndDate(detail.cutoffEndDate());
            row.setSalaryDate(detail.salaryDate());
            row.setActualBasic(detail.actualBasic().doubleValue());
            row.setEarningBreakdown(formatBreakdown(employeeEarnings));
            row.setGrossAmount(gross.doubleValue());
            row.setDeductionBreakdown(formatBreakdown(employeeDeductions));
            row.setTotalDeduction(deductions.doubleValue());
            row.setNetAmount(net.doubleValue());
            row.setReportMode(periodLocked ? FINAL_MODE : PREVIEW_MODE);
            rows.add(row);

            grandActualBasic = grandActualBasic.add(detail.actualBasic());
            grandGross = grandGross.add(gross);
            grandDeduction = grandDeduction.add(deductions);
            grandNet = grandNet.add(net);
        }

        String grandEarningsBreakdown =
                formatBreakdown(aggregateGrandTotals(earningTotals));
        String grandDeductionsBreakdown =
                formatBreakdown(aggregateGrandTotals(deductionTotals));

        for (GeneralPayrollReportRow row : rows) {
            row.setGrandActualBasic(grandActualBasic.doubleValue());
            row.setGrandGrossAmount(grandGross.doubleValue());
            row.setGrandTotalDeduction(grandDeduction.doubleValue());
            row.setGrandNetAmount(grandNet.doubleValue());
            row.setGrandEarningsBreakdown(grandEarningsBreakdown);
            row.setGrandDeductionsBreakdown(grandDeductionsBreakdown);
        }

        return rows;
    }

    private List<BaseDetail> loadBaseDetails(String salaryPeriodKey,
                                              String payrollGroup) {
        String normalizedGroup =
                payrollGroup == null || payrollGroup.isBlank()
                        ? "REGULAR"
                        : payrollGroup.trim().toUpperCase(Locale.ROOT);
        MapSqlParameterSource params =
                new MapSqlParameterSource("salaryPeriodKey", salaryPeriodKey);
        String groupFilter = "";
        if (!"ALL".equals(normalizedGroup)) {
            params.addValue("payrollGroup", normalizedGroup);
            groupFilter = """
                      AND UPPER(COALESCE(pd.payrollGroup, 'REGULAR')) = :payrollGroup
                    """;
        }

        String sql = """
                SELECT
                    pd.id,
                    pd.employeeNo,
                    pd.employeeName,
                    pd.department,
                    pd.salaryGrade,
                    pd.salaryStep,
                    pd.salaryPeriodKey,
                    pd.cutoffStartDate,
                    pd.cutoffEndDate,
                    pd.salaryDate,
                    pd.actualBasic,
                    pd.displayToLastPage
                FROM payroll_detail pd
                WHERE pd.salaryPeriodKey = :salaryPeriodKey
                  AND UPPER(COALESCE(pd.status, 'COMPUTED')) <> 'CANCELLED'
                """ + groupFilter;

        return jdbc.query(sql, params, (rs, rowNum) -> new BaseDetail(
                rs.getLong("id"),
                safeText(rs.getString("employeeNo")),
                safeText(rs.getString("employeeName")),
                safeText(rs.getString("department")),
                nullableInteger(rs.getObject("salaryGrade")),
                nullableInteger(rs.getObject("salaryStep")),
                safeText(rs.getString("salaryPeriodKey")),
                rs.getDate("cutoffStartDate"),
                rs.getDate("cutoffEndDate"),
                rs.getDate("salaryDate"),
                decimal(rs.getObject("actualBasic")),
                databaseBoolean(rs.getObject("displayToLastPage"))
        ));
    }

    private List<LineItem> loadBaseEarnings(
            List<Long> detailIds,
            Map<Long, String> employeeByDetailId) {
        String sql = """
                SELECT
                    payroll_detail_id,
                    earningCode,
                    earningName,
                    indexNo,
                    amount
                FROM payroll_detail_earning
                WHERE payroll_detail_id IN (:detailIds)
                """;
        MapSqlParameterSource params =
                new MapSqlParameterSource("detailIds", detailIds);

        return jdbc.query(sql, params, (rs, rowNum) -> {
            long detailId = rs.getLong("payroll_detail_id");
            return new LineItem(
                    employeeByDetailId.get(detailId),
                    safeText(rs.getString("earningCode")),
                    safeText(rs.getString("earningName")),
                    safeInteger(rs.getObject("indexNo")),
                    decimal(rs.getObject("amount"))
            );
        });
    }

    private List<LineItem> loadBaseDeductions(
            List<Long> detailIds,
            Map<Long, String> employeeByDetailId) {
        String sql = """
                SELECT
                    payroll_detail_id,
                    deductionCode,
                    deductionName,
                    indexNo,
                    amount
                FROM payroll_detail_deduction
                WHERE payroll_detail_id IN (:detailIds)
                """;
        MapSqlParameterSource params =
                new MapSqlParameterSource("detailIds", detailIds);

        return jdbc.query(sql, params, (rs, rowNum) -> {
            long detailId = rs.getLong("payroll_detail_id");
            return new LineItem(
                    employeeByDetailId.get(detailId),
                    safeText(rs.getString("deductionCode")),
                    safeText(rs.getString("deductionName")),
                    safeInteger(rs.getObject("indexNo")),
                    decimal(rs.getObject("amount"))
            );
        });
    }

    private void addEligibleAdjustments(String salaryPeriodKey,
                                        boolean periodLocked,
                                        List<LineItem> earningItems,
                                        List<LineItem> deductionItems) {
        String sql = """
                SELECT
                    h.employeeNo,
                    h.status,
                    h.authorityNo,
                    l.type,
                    l.code,
                    l.name,
                    l.indexNo,
                    l.amount
                FROM payroll_adjustment_header h
                INNER JOIN payroll_adjustment_line l ON l.header_id = h.id
                WHERE h.salaryPeriodKey = :salaryPeriodKey
                """;

        jdbc.query(
                sql,
                new MapSqlParameterSource("salaryPeriodKey", salaryPeriodKey),
                rs -> {
                    String status = safeText(rs.getString("status"))
                            .toUpperCase(Locale.ROOT);
                    boolean eligible = "POSTED".equals(status)
                            || (!periodLocked && "PENDING".equals(status));
                    if (!eligible) {
                        return;
                    }

                    String authorityNo = safeText(rs.getString("authorityNo"));
                    String displayName = safeText(rs.getString("name"))
                            + " [ADJ-" + status
                            + (authorityNo.isEmpty() ? "" : " / " + authorityNo)
                            + "]";
                    LineItem item = new LineItem(
                            safeText(rs.getString("employeeNo")),
                            safeText(rs.getString("code")),
                            displayName,
                            9000 + safeInteger(rs.getObject("indexNo")),
                            decimal(rs.getObject("amount"))
                    );

                    String type = safeText(rs.getString("type"));
                    if ("EARNING".equalsIgnoreCase(type)) {
                        earningItems.add(item);
                    } else if ("DEDUCTION".equalsIgnoreCase(type)) {
                        deductionItems.add(item);
                    }
                }
        );
    }

    private List<LineTotal> aggregateByEmployee(List<LineItem> items) {
        Map<EmployeeLineKey, BigDecimal> amounts = new LinkedHashMap<>();
        for (LineItem item : items) {
            EmployeeLineKey key = new EmployeeLineKey(
                    item.employeeNo(),
                    item.code(),
                    item.displayName(),
                    item.indexNo()
            );
            amounts.merge(key, item.amount(), BigDecimal::add);
        }

        List<LineTotal> totals = new ArrayList<>(amounts.size());
        amounts.forEach((key, amount) -> totals.add(new LineTotal(
                key.employeeNo(),
                key.code(),
                key.displayName(),
                key.indexNo(),
                amount
        )));
        return totals;
    }

    private Map<String, List<LineTotal>> groupByEmployee(
            List<LineTotal> totals) {
        Map<String, List<LineTotal>> grouped = new LinkedHashMap<>();
        for (LineTotal total : totals) {
            grouped.computeIfAbsent(
                    total.employeeNo(),
                    ignored -> new ArrayList<>()
            ).add(total);
        }
        grouped.values().forEach(lines -> lines.sort(LINE_ORDER));
        return grouped;
    }

    private List<LineTotal> aggregateGrandTotals(List<LineTotal> totals) {
        Map<GrandLineKey, GrandAccumulator> grand = new LinkedHashMap<>();
        for (LineTotal total : totals) {
            GrandLineKey key =
                    new GrandLineKey(total.code(), total.displayName());
            GrandAccumulator accumulator = grand.computeIfAbsent(
                    key,
                    ignored -> new GrandAccumulator(total.indexNo())
            );
            accumulator.indexNo =
                    Math.min(accumulator.indexNo, total.indexNo());
            accumulator.amount = accumulator.amount.add(total.amount());
        }

        List<LineTotal> result = new ArrayList<>(grand.size());
        grand.forEach((key, value) -> result.add(new LineTotal(
                "",
                key.code(),
                key.displayName(),
                value.indexNo,
                value.amount
        )));
        result.sort(LINE_ORDER);
        return result;
    }

    private BigDecimal sum(List<LineTotal> totals) {
        return totals.stream()
                .map(LineTotal::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String formatBreakdown(List<LineTotal> totals) {
        DecimalFormat formatter = new DecimalFormat(
                "#,##0.00",
                DecimalFormatSymbols.getInstance(Locale.US)
        );
        return totals.stream()
                .sorted(LINE_ORDER)
                .map(line -> line.displayName()
                        + " [" + line.code() + "]: "
                        + formatter.format(line.amount()))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }

    private static int safeInteger(Object value) {
        Integer converted = nullableInteger(value);
        return converted == null ? 0 : converted;
    }

    private static Integer nullableInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(value.toString());
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private static boolean databaseBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        if (value == null) {
            return false;
        }
        String text = value.toString().trim();
        return "true".equalsIgnoreCase(text)
                || "t".equalsIgnoreCase(text)
                || "yes".equalsIgnoreCase(text)
                || "y".equalsIgnoreCase(text)
                || "1".equals(text);
    }

    private record BaseDetail(
            long id,
            String employeeNo,
            String employeeName,
            String department,
            Integer salaryGrade,
            Integer salaryStep,
            String salaryPeriodKey,
            java.sql.Date cutoffStartDate,
            java.sql.Date cutoffEndDate,
            java.sql.Date salaryDate,
            BigDecimal actualBasic,
            boolean displayToLastPage) {
    }

    private record LineItem(
            String employeeNo,
            String code,
            String displayName,
            int indexNo,
            BigDecimal amount) {
    }

    private record EmployeeLineKey(
            String employeeNo,
            String code,
            String displayName,
            int indexNo) {
    }

    private record GrandLineKey(String code, String displayName) {
    }

    private record LineTotal(
            String employeeNo,
            String code,
            String displayName,
            int indexNo,
            BigDecimal amount) {
    }

    private static final class GrandAccumulator {
        private int indexNo;
        private BigDecimal amount = BigDecimal.ZERO;

        private GrandAccumulator(int indexNo) {
            this.indexNo = indexNo;
        }
    }
}
