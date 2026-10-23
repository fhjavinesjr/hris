package com.timekeeping.reports;

import com.timekeeping.dtos.DtrReportRow;
import com.timekeeping.services.WorkScheduleService;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** An officer review queue, not an automatic finding of employee absence or misconduct. */
@Service
public class IncompleteDtrReportService {
    private final JdbcTemplate jdbc;
    private final WorkScheduleService schedules;

    public IncompleteDtrReportService(JdbcTemplate jdbc, WorkScheduleService schedules) {
        this.jdbc = jdbc;
        this.schedules = schedules;
    }

    public byte[] generate(LocalDate from, LocalDate to, long areaId, Long unitId,
                           long preparedById, long certifiedById) throws Exception {
        if (from == null || to == null || to.isBefore(from) || from.plusYears(1).isBefore(to)) {
            throw new IllegalArgumentException("Select a valid date range of no more than one year.");
        }
        LocalDate through = LocalDate.now().minusDays(1);
        if (from.isAfter(through)) {
            throw new IllegalArgumentException("Incomplete DTR review is available through yesterday only.");
        }
        LocalDate end = to.isAfter(through) ? through : to;
        String area = lookup("SELECT areasName FROM areas WHERE areasId = ?", areaId);
        if (area.isBlank()) throw new IllegalArgumentException("Select an existing Area.");
        String unit = "All Business Units";
        if (unitId != null) {
            unit = lookup("SELECT businessUnitsName FROM businessunits WHERE businessUnitsId = ? AND areasId = ?",
                    unitId, areaId);
            if (unit.isBlank()) throw new IllegalArgumentException("Select a Business Unit in the chosen Area.");
        }
        Map<String, String> prepared = signatory(preparedById);
        Map<String, String> certified = signatory(certifiedById);
        List<Person> people = people(areaId, unitId);
        if (people.isEmpty()) throw new IllegalArgumentException("No employees are assigned to the selected Area/Business Unit.");
        DtrReportDataLoader dtrLoader = new DtrReportDataLoader(jdbc);
        List<Finding> findings = new ArrayList<>();
        for (Person person : people) {
            List<DtrReportRow> dtr = dtrLoader.load(String.valueOf(person.id()), from, end);
            Map<LocalDate, Set<String>> reasons = classifyDays(MraDailyProjection.fromDtr(dtr, from, end));
            loadSegmentFindings(person, from, end, reasons);
            loadScheduleGaps(person, from, end, reasons);
            reasons.forEach((date, categories) -> categories.forEach(category ->
                    findings.add(new Finding(person.number(), person.name(), person.unit(),
                            Date.valueOf(date), category))));
        }
        findings.sort((a, b) -> {
            int unitCompare = a.getBusinessUnit().compareToIgnoreCase(b.getBusinessUnit());
            if (unitCompare != 0) return unitCompare;
            int nameCompare = a.getEmployeeName().compareToIgnoreCase(b.getEmployeeName());
            if (nameCompare != 0) return nameCompare;
            int dateCompare = a.getWorkDate().compareTo(b.getWorkDate());
            return dateCompare != 0 ? dateCompare : a.getFinding().compareTo(b.getFinding());
        });
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("agency", lookup("SELECT companyName FROM settings WHERE settingsId = (SELECT MAX(settingsId) FROM settings)"));
        parameters.put("area", area);
        parameters.put("businessUnit", unit);
        parameters.put("rangeLabel", from + " to " + end + (end.isBefore(to) ? " (through yesterday)" : ""));
        parameters.put("preparedBy", prepared.get("fullName"));
        parameters.put("preparedPosition", prepared.get("position"));
        parameters.put("certifiedBy", certified.get("fullName"));
        parameters.put("certifiedPosition", certified.get("position"));
        return JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(
                JasperReportRegistry.get("reports/incomplete_dtr_review.jrxml"), parameters,
                new JRBeanCollectionDataSource(findings)));
    }

    static Map<LocalDate, Set<String>> classifyDays(List<MraDailyProjection.Day> days) {
        Map<LocalDate, Set<String>> results = new LinkedHashMap<>();
        for (MraDailyProjection.Day day : days) {
            String status = day.status();
            Set<String> categories = results.computeIfAbsent(day.date(), ignored -> new LinkedHashSet<>());
            // An unscheduled day with no DTR is not evidence that the employee was due to work.
            if (status.startsWith("ABSENT")) categories.add("ABSENT / NO DTR - verify schedule and approved records");
            if (status.equals("AWOL")) categories.add("AWOL STATUS - verify source and approvals");
            if (status.contains("CONFLICT")) categories.add("DTR / leave or absence conflict");
            if (status.equals("DTR IN PROGRESS - REVIEW")) categories.add("DTR pending finalization");
            if (status.equals("DTR WITHOUT WORK - REVIEW")) categories.add("DTR has no computed work");
            if (status.equals("REST DAY WORKED - REVIEW")) categories.add("Rest-day work - verify authorization");
            if (day.undertimeMinutes() >= 500) categories.add("Unusual undertime (500+ minutes) - verify computation");
            if (categories.isEmpty()) results.remove(day.date());
        }
        return results;
    }

    private void loadSegmentFindings(Person person, LocalDate from, LocalDate to,
                                     Map<LocalDate, Set<String>> findings) {
        jdbc.query("SELECT d.work_date, s.time_in, s.time_out, s.break_out, s.break_in "
                        + "FROM dtr_segment s JOIN dtr_daily d ON d.dtr_daily_id = s.dtr_daily_id "
                        + "WHERE d.employee_id IN (?, ?) AND d.work_date BETWEEN ? AND ?",
                rs -> {
                    LocalDate date = rs.getDate("work_date").toLocalDate();
                    boolean missingPunch = rs.getTime("time_in") == null || rs.getTime("time_out") == null;
                    boolean missingBreak = (rs.getTime("break_out") == null) != (rs.getTime("break_in") == null);
                    if (missingPunch || missingBreak) {
                        Set<String> categories = findings.computeIfAbsent(date, ignored -> new LinkedHashSet<>());
                        if (missingPunch) categories.add("INCOMPLETE PUNCH - missing time in or out");
                        if (missingBreak) categories.add("INCOMPLETE BREAK - missing break in or out");
                    }
                }, String.valueOf(person.id()), person.number(), Date.valueOf(from), Date.valueOf(to));
    }

    private void loadScheduleGaps(Person person, LocalDate from, LocalDate to,
                                  Map<LocalDate, Set<String>> findings) {
        Object[] aliasesAndRange = {String.valueOf(person.id()), person.number(),
                Timestamp.valueOf(from.atStartOfDay()), Timestamp.valueOf(to.plusDays(1).atStartOfDay())};
        Set<LocalDate> scheduled = new LinkedHashSet<>(jdbc.query(
                "SELECT wsDateTime FROM work_schedule WHERE employeeId IN (?, ?) "
                        + "AND wsDateTime >= ? AND wsDateTime < ?",
                (rs, index) -> rs.getTimestamp("wsDateTime").toLocalDateTime().toLocalDate(), aliasesAndRange));
        jdbc.query("SELECT work_date FROM dtr_daily WHERE employee_id IN (?, ?) AND work_date BETWEEN ? AND ?",
                rs -> {
                    LocalDate date = rs.getDate("work_date").toLocalDate();
                    if (!scheduled.contains(date)) findings.computeIfAbsent(date, ignored -> new LinkedHashSet<>())
                            .add("DTR EXISTS WITHOUT SCHEDULE - verify shift assignment");
                }, String.valueOf(person.id()), person.number(), Date.valueOf(from), Date.valueOf(to));
    }

    private List<Person> people(long areaId, Long unitId) {
        String sql = "SELECT DISTINCT e.employeeId, e.employeeNo, e.lastname, e.firstname, "
                + "bu.businessUnitsName FROM manage_personnel mp "
                + "JOIN employee e ON e.employeeId = mp.employeeId "
                + "JOIN businessunits bu ON bu.businessUnitsId = mp.businessUnitId WHERE mp.areaId = ?";
        List<Object> args = new ArrayList<>();
        args.add(areaId);
        if (unitId != null) { sql += " AND mp.businessUnitId = ?"; args.add(unitId); }
        sql += " ORDER BY bu.businessUnitsName, e.lastname, e.firstname, e.employeeId";
        return jdbc.query(sql, (rs, index) -> new Person(rs.getLong("employeeId"), rs.getString("employeeNo"),
                (rs.getString("lastname") + ", " + rs.getString("firstname")), rs.getString("businessUnitsName")),
                args.toArray());
    }

    private Map<String, String> signatory(long employeeId) {
        Map<String, String> result = schedules.getWorkScheduleSignatoryInfo(Long.toString(employeeId));
        if (result.getOrDefault("fullName", "").isBlank()) {
            throw new IllegalArgumentException("Select an existing employee signatory.");
        }
        return result;
    }

    private String lookup(String sql, Object... args) {
        List<String> values = jdbc.queryForList(sql, String.class, args);
        return values.isEmpty() || values.get(0) == null ? "" : values.get(0).trim();
    }

    private record Person(long id, String number, String name, String unit) { }

    public static final class Finding {
        private final String employeeNo;
        private final String employeeName;
        private final String businessUnit;
        private final Date workDate;
        private final String finding;

        public Finding(String employeeNo, String employeeName, String businessUnit, Date workDate, String finding) {
            this.employeeNo = employeeNo;
            this.employeeName = employeeName;
            this.businessUnit = businessUnit;
            this.workDate = workDate;
            this.finding = finding;
        }
        public String getEmployeeNo() { return employeeNo; }
        public String getEmployeeName() { return employeeName; }
        public String getBusinessUnit() { return businessUnit; }
        public Date getWorkDate() { return workDate; }
        public String getFinding() { return finding; }
    }
}
