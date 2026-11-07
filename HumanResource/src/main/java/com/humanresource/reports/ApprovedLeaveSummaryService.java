package com.humanresource.reports;

import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.repositories.LeaveApplicationRepository;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Lists approved leave filings; this does not recalculate the leave ledger. */
@Service
public class ApprovedLeaveSummaryService {
    private final LeaveApplicationRepository leaves;
    private final JdbcTemplate jdbc;

    public ApprovedLeaveSummaryService(LeaveApplicationRepository leaves, JdbcTemplate jdbc) {
        this.leaves = leaves;
        this.jdbc = jdbc;
    }

    public byte[] generate(LocalDate from, LocalDate to, long areaId, Long unitId,
                           String leaveType, String payFilter) throws Exception {
        if (from == null || to == null || to.isBefore(from) || from.plusYears(1).isBefore(to)) {
            throw new IllegalArgumentException("Select a valid date range of no more than one year.");
        }
        if (!List.of("ALL", "WITH_PAY", "WITHOUT_PAY").contains(payFilter)) {
            throw new IllegalArgumentException("Select a valid leave pay treatment.");
        }
        String area = lookup("SELECT areasName FROM areas WHERE areasId = ?", areaId);
        if (area.isBlank()) throw new IllegalArgumentException("Select an existing Area.");
        String unit = "All Business Units";
        if (unitId != null) {
            unit = lookup("SELECT businessUnitsName FROM businessunits WHERE businessUnitsId = ? AND areasId = ?",
                    unitId, areaId);
            if (unit.isBlank()) throw new IllegalArgumentException("Select a Business Unit in the chosen Area.");
        }
        Map<Long, Person> people = people(areaId, unitId);
        if (people.isEmpty()) throw new IllegalArgumentException("No employees are assigned to the selected Area/Business Unit.");
        List<Row> rows = new ArrayList<>();
        for (LeaveApplication leave : leaves.findByStatusAndApprovedStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                "Approved", "Approved", to, from)) {
            Person person = people.get(leave.getEmployeeId());
            if (person == null || leave.getStartDate() == null || leave.getEndDate() == null) continue;
            if (!matches(leave, leaveType, payFilter)) continue;
            boolean paid = !Boolean.FALSE.equals(leave.getWithPay());
            rows.add(new Row(person.number(), person.name(), person.unit(), leave.getLeaveType(),
                    Date.valueOf(leave.getStartDate()), Date.valueOf(leave.getEndDate()),
                    leave.getNoOfDays(), paid ? "With pay" : "Without pay"));
        }
        rows.sort(Comparator.comparing(Row::getBusinessUnit, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Row::getEmployeeName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Row::getStartDate));
        Map<String, Object> params = new HashMap<>();
        params.put("agency", lookup("SELECT companyName FROM settings WHERE settingsId = (SELECT MAX(settingsId) FROM settings)"));
        params.put("area", area);
        params.put("businessUnit", unit);
        params.put("rangeLabel", from + " to " + to);
        params.put("leaveTypeLabel", leaveType == null || leaveType.isBlank() ? "All leave types" : leaveType);
        params.put("payLabel", switch (payFilter) {
            case "WITH_PAY" -> "With pay";
            case "WITHOUT_PAY" -> "Without pay";
            default -> "With and without pay";
        });
        return JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(
                JasperReportRegistry.get("reports/approved_leave_summary.jrxml"), params,
                new JRBeanCollectionDataSource(rows)));
    }

    static boolean matches(LeaveApplication leave, String leaveType, String payFilter) {
        if (leaveType != null && !leaveType.isBlank() && !leaveType.equalsIgnoreCase(leave.getLeaveType())) return false;
        boolean paid = !Boolean.FALSE.equals(leave.getWithPay());
        return !("WITH_PAY".equals(payFilter) && !paid || "WITHOUT_PAY".equals(payFilter) && paid);
    }

    private Map<Long, Person> people(long areaId, Long unitId) {
        String sql = "SELECT DISTINCT e.employeeId, e.employeeNo, e.lastname, e.firstname, "
                + "bu.businessUnitsName FROM manage_personnel mp "
                + "JOIN employee e ON e.employeeId = mp.employeeId "
                + "JOIN businessunits bu ON bu.businessUnitsId = mp.businessUnitId WHERE mp.areaId = ?";
        List<Object> args = new ArrayList<>();
        args.add(areaId);
        if (unitId != null) { sql += " AND mp.businessUnitId = ?"; args.add(unitId); }
        Map<Long, Person> result = new HashMap<>();
        jdbc.query(sql, (rs, index) -> {
            result.put(rs.getLong("employeeId"), new Person(rs.getString("employeeNo"),
                    rs.getString("lastname") + ", " + rs.getString("firstname"), rs.getString("businessUnitsName")));
            return null;
        }, args.toArray());
        return result;
    }

    private String lookup(String sql, Object... args) {
        List<String> values = jdbc.queryForList(sql, String.class, args);
        return values.isEmpty() || values.get(0) == null ? "" : values.get(0).trim();
    }

    private record Person(String number, String name, String unit) { }

    public static final class Row {
        private final String employeeNo, employeeName, businessUnit, leaveType, payTreatment;
        private final Date startDate, endDate;
        private final Double approvedDays;
        public Row(String employeeNo, String employeeName, String businessUnit, String leaveType,
                   Date startDate, Date endDate, Double approvedDays, String payTreatment) {
            this.employeeNo = employeeNo; this.employeeName = employeeName; this.businessUnit = businessUnit;
            this.leaveType = leaveType; this.startDate = startDate; this.endDate = endDate;
            this.approvedDays = approvedDays; this.payTreatment = payTreatment;
        }
        public String getEmployeeNo() { return employeeNo; }
        public String getEmployeeName() { return employeeName; }
        public String getBusinessUnit() { return businessUnit; }
        public String getLeaveType() { return leaveType; }
        public Date getStartDate() { return startDate; }
        public Date getEndDate() { return endDate; }
        public Double getApprovedDays() { return approvedDays; }
        public String getPayTreatment() { return payTreatment; }
    }
}
