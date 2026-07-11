package com.payroll.impl;

import com.payroll.dtos.PayslipDTO;
import com.payroll.dtos.PayslipLineDTO;
import com.payroll.services.PayslipService;
import com.payroll.services.PayrollPeriodLockService;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PayslipServiceImpl implements PayslipService {

    private final NamedParameterJdbcTemplate jdbc;
    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;
    private final PayrollPeriodLockService payrollPeriodLockService;

    public PayslipServiceImpl(NamedParameterJdbcTemplate jdbc,
                              DataSource dataSource,
                              ResourceLoader resourceLoader,
                              PayrollPeriodLockService payrollPeriodLockService) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
        this.payrollPeriodLockService = payrollPeriodLockService;
    }

    @Override
    public PayslipDTO getPayslip(String employeeNo, String salaryPeriodKey, boolean releasedOnly) {
        if (employeeNo == null || employeeNo.isBlank()) {
            throw new IllegalArgumentException("Employee number is required.");
        }
        if (salaryPeriodKey == null || salaryPeriodKey.isBlank()) {
            throw new IllegalArgumentException("Salary period is required.");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("employeeNo", employeeNo.trim());
        params.put("salaryPeriodKey", salaryPeriodKey.trim());
        boolean periodLocked = payrollPeriodLockService.isPeriodLocked(salaryPeriodKey.trim());
        params.put("releasedOnly", releasedOnly ? 1 : 0);
        params.put("periodLocked", periodLocked ? 1 : 0);

        String detailSql = """
            SELECT TOP 1 *
            FROM payroll_detail
            WHERE employeeNo = :employeeNo
              AND salaryPeriodKey = :salaryPeriodKey
              AND (:releasedOnly = 0 OR isLocked = 1 OR :periodLocked = 1)
            ORDER BY id DESC
            """;

        Map<String, Object> row;
        try {
            row = jdbc.queryForMap(detailSql, params);
        } catch (EmptyResultDataAccessException ex) {
            throw new NoSuchElementException(
                    "No payroll detail found for " + employeeNo + " in period " + salaryPeriodKey + ".");
        }

        PayslipDTO dto = mapDetail(row);
        applyPeriodLockStatus(dto, periodLocked);
        Long payrollDetailId = dto.getPayrollDetailId();

        dto.setEarnings(loadEarnings(payrollDetailId));
        dto.setDeductions(loadDeductions(payrollDetailId));

        List<PayslipLineDTO> adjustments = loadPostedAdjustments(employeeNo.trim(), salaryPeriodKey.trim());
        dto.setAdjustments(adjustments);
        computeAdjustmentTotals(dto);

        return dto;
    }

    private void applyPeriodLockStatus(PayslipDTO dto, boolean periodLocked) {
        boolean detailLocked = Boolean.TRUE.equals(dto.getLocked());
        boolean finalLocked = detailLocked || periodLocked;
        dto.setLocked(finalLocked);
        if (finalLocked) {
            dto.setStatus("FINAL / RELEASED");
        } else if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            dto.setStatus("COMPUTED");
        }
    }

    @Override
    public List<String> getSalaryPeriods(String employeeNo) {
        Map<String, Object> params = new HashMap<>();
        String cleanEmployeeNo = employeeNo != null && !employeeNo.isBlank() ? employeeNo.trim() : null;
        params.put("employeeNo", cleanEmployeeNo);

        String sql = """
            SELECT salaryPeriodKey
            FROM payroll_detail
            WHERE (:employeeNo IS NULL OR employeeNo = :employeeNo)
            GROUP BY salaryPeriodKey
            ORDER BY MAX(salaryDate) DESC, MAX(cutoffEndDate) DESC, salaryPeriodKey DESC
            """;

        return jdbc.query(sql, params, (rs, rowNum) -> rs.getString("salaryPeriodKey"));
    }

    private PayslipDTO mapDetail(Map<String, Object> row) {
        PayslipDTO dto = new PayslipDTO();

        dto.setPayrollDetailId(longValue(row.get("id")));
        dto.setEmployeeNo(stringValue(row.get("employeeNo")));
        dto.setEmployeeName(stringValue(row.get("employeeName")));
        dto.setDepartment(stringValue(row.get("department")));
        dto.setSalaryGrade(intValue(row.get("salaryGrade")));
        dto.setSalaryStep(intValue(row.get("salaryStep")));

        dto.setSalaryPeriodKey(stringValue(row.get("salaryPeriodKey")));
        dto.setCutoffStartDate(localDateValue(row.get("cutoffStartDate")));
        dto.setCutoffEndDate(localDateValue(row.get("cutoffEndDate")));
        dto.setSalaryDate(localDateValue(row.get("salaryDate")));

        dto.setBasicPerSalary(doubleValue(row.get("basicPerSalary")));
        dto.setSalaryPerDay(doubleValue(row.get("salaryPerDay")));
        dto.setSalaryPerMinute(doubleValue(row.get("salaryPerMinute")));
        dto.setCutoffDays(intValue(row.get("cutoffDays")));

        dto.setWorkDays(intValue(row.get("workDays")));
        dto.setWorkDaysPresent(doubleValue(row.get("workDaysPresent")));
        dto.setAbsentDays(doubleValue(row.get("absentDays")));
        dto.setAbsentParticulars(stringValue(row.get("absentParticulars")));

        dto.setLateMinutes(intValue(row.get("lateMinutes")));
        dto.setLateValue(doubleValue(row.get("lateValue")));
        dto.setUndertimeMinutes(intValue(row.get("undertimeMinutes")));
        dto.setUndertimeValue(doubleValue(row.get("undertimeValue")));

        dto.setEarnedLeave(doubleValue(row.get("earnedLeave")));
        dto.setVacationLeaveUsed(doubleValue(row.get("vacationLeaveUsed")));
        dto.setSickLeaveUsed(doubleValue(row.get("sickLeaveUsed")));
        dto.setForceLeaveUsed(doubleValue(row.get("forceLeaveUsed")));
        dto.setVlDeductedDays(doubleValue(row.get("vlDeductedDays")));
        dto.setVlBalance(doubleValue(row.get("vlBalance")));
        dto.setSlBalance(doubleValue(row.get("slBalance")));

        dto.setActualBasic(doubleValue(row.get("actualBasic")));
        dto.setGrossAmount(doubleValue(row.get("grossAmount")));
        dto.setTotalDeduction(doubleValue(row.get("totalDeduction")));
        dto.setNetAmount(doubleValue(row.get("netAmount")));

        dto.setTaxableIncome(doubleValue(row.get("taxableIncome")));
        dto.setTaxAmount(doubleValue(row.get("taxAmount")));
        dto.setTaxableIncomeToDate(doubleValue(row.get("taxableIncomeToDate")));
        dto.setTaxToDate(doubleValue(row.get("taxToDate")));

        dto.setStatus(stringValue(row.get("status")));
        dto.setLocked(booleanValue(row.get("isLocked")));
        dto.setComputedAt(localDateTimeValue(row.get("computedAt")));
        dto.setLockedAt(localDateTimeValue(row.get("lockedAt")));

        return dto;
    }

    private List<PayslipLineDTO> loadEarnings(Long payrollDetailId) {
        Map<String, Object> params = Map.of("payrollDetailId", payrollDetailId);
        String sql = """
            SELECT earningCode, earningName, amount, isTaxable, indexNo
            FROM payroll_detail_earning
            WHERE payroll_detail_id = :payrollDetailId
            ORDER BY indexNo ASC, id ASC
            """;

        return jdbc.query(sql, params, (rs, rowNum) -> new PayslipLineDTO(
                "PAYROLL_DETAIL",
                "EARNING",
                rs.getString("earningCode"),
                rs.getString("earningName"),
                rs.getDouble("amount"),
                rs.getObject("isTaxable") != null ? rs.getBoolean("isTaxable") : null,
                false,
                rs.getObject("indexNo") != null ? rs.getInt("indexNo") : rowNum
        ));
    }

    private List<PayslipLineDTO> loadDeductions(Long payrollDetailId) {
        Map<String, Object> params = Map.of("payrollDetailId", payrollDetailId);
        String sql = """
            SELECT deductionCode, deductionName, amount, isFixedPerSalary, indexNo
            FROM payroll_detail_deduction
            WHERE payroll_detail_id = :payrollDetailId
            ORDER BY indexNo ASC, id ASC
            """;

        return jdbc.query(sql, params, (rs, rowNum) -> new PayslipLineDTO(
                "PAYROLL_DETAIL",
                "DEDUCTION",
                rs.getString("deductionCode"),
                rs.getString("deductionName"),
                rs.getDouble("amount"),
                null,
                false,
                rs.getObject("indexNo") != null ? rs.getInt("indexNo") : rowNum
        ));
    }

    private List<PayslipLineDTO> loadPostedAdjustments(String employeeNo, String salaryPeriodKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("employeeNo", employeeNo);
        params.put("salaryPeriodKey", salaryPeriodKey);

        String sql = """
            SELECT
                l.type,
                l.code,
                l.name,
                l.amount,
                l.isTaxable,
                l.isAutoComputed,
                l.indexNo,
                h.version,
                h.authorityNo,
                h.reason
            FROM payroll_adjustment_header h
            INNER JOIN payroll_adjustment_line l
                ON l.header_id = h.id
            WHERE h.employeeNo = :employeeNo
              AND h.salaryPeriodKey = :salaryPeriodKey
              AND UPPER(h.status) = 'POSTED'
            ORDER BY h.version ASC, l.indexNo ASC, l.id ASC
            """;

        return jdbc.query(sql, params, (rs, rowNum) -> {
            String name = rs.getString("name");
            String authorityNo = rs.getString("authorityNo");
            if (authorityNo != null && !authorityNo.isBlank()) {
                name = name + " (" + authorityNo + ")";
            }

            return new PayslipLineDTO(
                    "ADJUSTMENT",
                    rs.getString("type"),
                    rs.getString("code"),
                    name,
                    rs.getDouble("amount"),
                    rs.getObject("isTaxable") != null ? rs.getBoolean("isTaxable") : null,
                    rs.getObject("isAutoComputed") != null && rs.getBoolean("isAutoComputed"),
                    rs.getObject("indexNo") != null ? rs.getInt("indexNo") : rowNum
            );
        });
    }

    private void computeAdjustmentTotals(PayslipDTO dto) {
        double adjustmentEarnings = 0.0;
        double adjustmentDeductions = 0.0;
        double adjustmentNet = 0.0;

        for (PayslipLineDTO line : dto.getAdjustments()) {
            double amount = safe(line.getAmount());
            if ("EARNING".equalsIgnoreCase(line.getType())) {
                adjustmentEarnings += amount;
                adjustmentNet += amount;
            } else if ("DEDUCTION".equalsIgnoreCase(line.getType())) {
                adjustmentDeductions += amount;
                adjustmentNet -= amount;
            }
        }

        dto.setAdjustmentEarnings(round(adjustmentEarnings));
        dto.setAdjustmentDeductions(round(adjustmentDeductions));
        dto.setAdjustmentNet(round(adjustmentNet));

        dto.setAdjustedGrossAmount(round(safe(dto.getGrossAmount()) + adjustmentEarnings));
        dto.setAdjustedTotalDeduction(round(safe(dto.getTotalDeduction()) + adjustmentDeductions));
        dto.setAdjustedNetAmount(round(safe(dto.getNetAmount()) + adjustmentNet));
    }

    @Override
    public void generatePayslipPdf(String employeeNo, String salaryPeriodKey, boolean releasedOnly, OutputStream outputStream) throws Exception {
        if (employeeNo == null || employeeNo.isBlank()) {
            throw new IllegalArgumentException("Employee number is required.");
        }
        if (salaryPeriodKey == null || salaryPeriodKey.isBlank()) {
            throw new IllegalArgumentException("Salary period is required.");
        }

        // Keep the same friendly validation behavior as the UI JSON endpoint.
        // If there is no record, this throws NoSuchElementException before Jasper runs.
        PayslipDTO validatedPayslip = getPayslip(employeeNo, salaryPeriodKey, releasedOnly);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("employeeNo", employeeNo.trim());
        parameters.put("salaryPeriodKey", salaryPeriodKey.trim());
        // The JRXML still filters by payroll_detail.isLocked. If the period is locked,
        // the payslip is already validated as released, so do not let Jasper hide it
        // just because payroll_detail.isLocked itself is still false.
        parameters.put("releasedOnly", Boolean.TRUE.equals(validatedPayslip.getLocked()) ? 0 : (releasedOnly ? 1 : 0));

        Resource reportResource = resourceLoader.getResource("classpath:reports/payslip.jrxml");
        if (!reportResource.exists()) {
            reportResource = resourceLoader.getResource("classpath:payslip.jrxml");
        }
        if (!reportResource.exists()) {
            throw new IllegalStateException("Payslip JRXML template was not found. Expected reports/payslip.jrxml in resources.");
        }

        try (Connection connection = dataSource.getConnection()) {
            JasperReport report = JasperCompileManager.compileReport(reportResource.getInputStream());
            JasperPrint print = JasperFillManager.fillReport(report, parameters, connection);
            JasperExportManager.exportReportToPdfStream(print, outputStream);
        }
    }

    private String stringValue(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private Long longValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private Integer intValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(value));
    }

    private Double doubleValue(Object value) {
        if (value == null) return 0.0;
        if (value instanceof BigDecimal bd) return bd.doubleValue();
        if (value instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(value));
    }

    private Boolean booleanValue(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private LocalDate localDateValue(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        if (value instanceof java.util.Date d) {
            return new java.sql.Date(d.getTime()).toLocalDate();
        }
        return LocalDate.parse(String.valueOf(value).substring(0, 10));
    }

    private LocalDateTime localDateTimeValue(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        if (value instanceof java.util.Date d) {
            return new Timestamp(d.getTime()).toLocalDateTime();
        }
        return LocalDateTime.parse(String.valueOf(value).replace(" ", "T"));
    }

    private double safe(Double value) {
        return value != null ? value : 0.0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
