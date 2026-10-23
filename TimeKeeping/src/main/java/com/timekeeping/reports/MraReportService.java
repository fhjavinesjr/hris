package com.timekeeping.reports;

import com.timekeeping.dtos.DtrReportRow;
import com.timekeeping.services.WorkScheduleService;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Builds a short-lived, queryable DTR projection; Jasper owns the report query and layout. */
@Service
public class MraReportService {
    private final JdbcTemplate jdbc;
    private final DataSource dataSource;
    private final WorkScheduleService workScheduleService;

    public MraReportService(JdbcTemplate jdbc, DataSource dataSource,
                            WorkScheduleService workScheduleService) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
        this.workScheduleService = workScheduleService;
    }

    public byte[] generate(int year, int month, Long areaId, Long businessUnitId,
                           Long preparedById, Long certifiedById, Long verifiedById) throws Exception {
        return generateReport(null, year, month, areaId, businessUnitId,
                preparedById, certifiedById, verifiedById);
    }

    public byte[] generateSummary(AttendanceSummaryType summaryType, int year, int month,
                                  Long areaId, Long businessUnitId,
                                  Long preparedById, Long certifiedById, Long verifiedById) throws Exception {
        if (summaryType == null) throw new IllegalArgumentException("Select an attendance summary.");
        return generateReport(summaryType, year, month, areaId, businessUnitId,
                preparedById, certifiedById, verifiedById);
    }

    private byte[] generateReport(AttendanceSummaryType summaryType, int year, int month,
                                  Long areaId, Long businessUnitId,
                                  Long preparedById, Long certifiedById, Long verifiedById) throws Exception {
        if (year < 1900 || year > 9999) {
            throw new IllegalArgumentException("Select a valid four-digit calendar year.");
        }
        YearMonth period = YearMonth.of(year, month);
        if (areaId == null || areaId < 1 || preparedById == null || certifiedById == null) {
            throw new IllegalArgumentException("Area, prepared-by, and certified-by employees are required.");
        }
        String area = name("SELECT areasName FROM areas WHERE areasId = ?", areaId);
        if (area.isBlank()) throw new IllegalArgumentException("The selected Area does not exist.");
        String businessUnit = "All Business Units";
        if (businessUnitId != null) {
            businessUnit = name("SELECT businessUnitsName FROM businessunits "
                    + "WHERE businessUnitsId = ? AND areasId = ?", businessUnitId, areaId);
            if (businessUnit.isBlank()) {
                throw new IllegalArgumentException("The selected Business Unit does not belong to the Area.");
            }
        }
        Map<String, String> prepared = signatory(preparedById);
        Map<String, String> certified = signatory(certifiedById);
        Map<String, String> verified = verifiedById == null ? Map.of("fullName", "", "position", "")
                : signatory(verifiedById);

        List<Person> people = people(areaId, businessUnitId);
        if (people.isEmpty()) throw new IllegalArgumentException("No employees are assigned to that Area/Business Unit.");

        String runId = UUID.randomUUID().toString();
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();
        LocalDate today = LocalDate.now();
        if (start.isAfter(today)) {
            throw new IllegalArgumentException("A future calendar month has no attendance to report.");
        }
        LocalDate reportThrough = end.isAfter(today) ? today : end;
        try {
            removeExpiredSnapshots();
            List<Object[]> parameters = new ArrayList<>();
            int reviewDays = 0;
            Timestamp createdAt = Timestamp.from(Instant.now());
            DtrReportDataLoader loader = new DtrReportDataLoader(jdbc);
            NightWorkMinutesLoader nightLoader = summaryType == AttendanceSummaryType.NIGHT_DIFFERENTIAL
                    ? new NightWorkMinutesLoader(jdbc) : null;
            for (Person person : people) {
                List<DtrReportRow> dtr = loader.load(String.valueOf(person.id()), start, reportThrough);
                Map<LocalDate, Integer> nightMinutes = nightLoader == null ? Map.of()
                        : nightLoader.load(person.id(), person.employeeNo(), start, reportThrough);
                for (MraDailyProjection.Day day : MraDailyProjection.fromDtr(dtr, start, reportThrough)) {
                    if (day.status().contains("REVIEW")) reviewDays++;
                    int metricMinutes = summaryType == null ? 0 : switch (summaryType) {
                        case TARDINESS -> day.lateMinutes();
                        case UNDERTIME -> day.undertimeMinutes();
                        case OVERTIME -> day.overtimeMinutes();
                        case NIGHT_DIFFERENTIAL -> nightMinutes.getOrDefault(day.date(), 0);
                    };
                    parameters.add(new Object[]{runId, createdAt, person.id(), person.employeeNo(), person.name(),
                            person.businessUnitId(), person.businessUnit(), Date.valueOf(day.date()), day.status(), day.leaveType(),
                            day.workMinutes(), day.lateMinutes(), day.undertimeMinutes(),
                            day.overtimeMinutes(), day.absentMinutes(), day.overtimeApproved() ? 1 : 0,
                            truncate(day.remarks(), 1000), metricMinutes});
                }
            }
            if (parameters.isEmpty()) {
                throw new IllegalArgumentException("No DTR days were available for this calendar month.");
            }
            jdbc.batchUpdate("INSERT INTO mra_print_day (run_id, created_at, employee_id, employee_no, "
                    + "employee_name, business_unit_id, business_unit, work_date, attendance_status, leave_type, "
                    + "work_minutes, late_minutes, undertime_minutes, overtime_minutes, "
                    + "absent_minutes, overtime_approved, remarks, metric_minutes) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    parameters);

            Map<String, Object> reportParams = new HashMap<>();
            reportParams.put("runId", runId);
            reportParams.put("monthLabel", period.getMonth().toString() + " " + year
                    + (reportThrough.isBefore(end) ? " - DRAFT THROUGH " + reportThrough : ""));
            reportParams.put("area", area);
            reportParams.put("businessUnit", businessUnit);
            reportParams.put("reviewNotice", reviewDays == 0
                    ? "Daily DTR-derived record; verify absences and supporting approvals before certification."
                    : "REVIEW REQUIRED: " + reviewDays
                    + " daily entries need checking against DTR/schedules/approvals before certification.");
            reportParams.put("preparedBy", prepared.get("fullName"));
            reportParams.put("preparedPosition", prepared.get("position"));
            reportParams.put("certifiedBy", certified.get("fullName"));
            reportParams.put("certifiedPosition", certified.get("position"));
            reportParams.put("verifiedBy", verified.get("fullName"));
            reportParams.put("verifiedPosition", verified.get("position"));
            reportParams.put("agency", name("SELECT companyName FROM settings "
                    + "WHERE settingsId = (SELECT MAX(settingsId) FROM settings)"));
            if (summaryType != null) {
                reportParams.put("summaryTitle", summaryType.title());
                reportParams.put("summaryNote", summaryType == AttendanceSummaryType.NIGHT_DIFFERENTIAL
                        ? "Recorded DTR minutes within 18:00-06:00, excluding complete recorded breaks. "
                        + "Not a determination of payable night-shift differential; verify official schedule and eligibility."
                        : "Derived from DTR daily results. Review provisional/conflicting DTR and approvals before signing.");
                reportParams.put("noDataMessage", "No recorded " + summaryType.title().toLowerCase()
                        + " minutes were found for the selected calendar month and assignment.");
            }
            try (Connection connection = dataSource.getConnection()) {
                JasperPrint print = JasperFillManager.fillReport(
                        JasperReportRegistry.get(summaryType == null
                                ? "reports/monthly_report_attendance.jrxml"
                                : "reports/attendance_summary.jrxml"),
                        reportParams, connection);
                return JasperExportManager.exportReportToPdf(print);
            }
        } finally {
            jdbc.update("DELETE FROM mra_print_day WHERE run_id = ?", runId);
        }
    }

    @Scheduled(fixedDelayString = "${timekeeping.mra.snapshot-cleanup-ms:600000}")
    public void removeExpiredSnapshots() {
        jdbc.update("DELETE FROM mra_print_day WHERE created_at < ?",
                Timestamp.valueOf(LocalDateTime.now().minusHours(1)));
    }

    private List<Person> people(Long areaId, Long businessUnitId) {
        String sql = "SELECT DISTINCT e.employeeId, e.employeeNo, e.lastname, e.firstname, "
                + "mp.businessUnitId, bu.businessUnitsName FROM manage_personnel mp "
                + "JOIN employee e ON e.employeeId = mp.employeeId "
                + "JOIN businessunits bu ON bu.businessUnitsId = mp.businessUnitId "
                + "WHERE mp.areaId = ?";
        List<Object> args = new ArrayList<>();
        args.add(areaId);
        if (businessUnitId != null) {
            sql += " AND mp.businessUnitId = ?";
            args.add(businessUnitId);
        }
        sql += " ORDER BY bu.businessUnitsName, e.lastname, e.firstname, e.employeeId";
        return jdbc.query(sql, (rs, index) -> {
            long id = rs.getLong("employeeId");
            String number = rs.getString("employeeNo");
            if (number == null || number.isBlank()) number = String.valueOf(id);
            String last = value(rs.getString("lastname"));
            String first = value(rs.getString("firstname"));
            String fullName = (last + (first.isBlank() ? "" : ", " + first)).trim();
            return new Person(id, number, fullName, rs.getLong("businessUnitId"), rs.getString("businessUnitsName"));
        }, args.toArray());
    }

    private Map<String, String> signatory(Long employeeId) {
        Map<String, String> result = workScheduleService.getWorkScheduleSignatoryInfo(employeeId.toString());
        if (result.getOrDefault("fullName", "").isBlank()) {
            throw new IllegalArgumentException("Select an existing employee for each signatory.");
        }
        return result;
    }

    private String name(String sql, Object... args) {
        List<String> names = jdbc.queryForList(sql, String.class, args);
        return names.isEmpty() ? "" : value(names.get(0));
    }

    private static String value(String text) { return text == null ? "" : text.trim(); }
    private static String truncate(String text, int limit) {
        return text == null || text.length() <= limit ? text : text.substring(0, limit);
    }
    private record Person(long id, String employeeNo, String name,
                          long businessUnitId, String businessUnit) { }
}
