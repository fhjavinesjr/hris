package com.humanresource.impl;

import com.humanresource.dtos.PersonnelActionReportData;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.entitymodels.PersonalData;
import com.humanresource.repositories.EmployeeAppointmentRepository;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.PersonalDataRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class EmployeeAppointmentReportDataLoader {

    private static final DateTimeFormatter REPORT_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    private final EmployeeAppointmentRepository appointmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PersonalDataRepository personalDataRepository;
    private final JdbcTemplate jdbc;

    public EmployeeAppointmentReportDataLoader(
            EmployeeAppointmentRepository appointmentRepository,
            EmployeeRepository employeeRepository,
            PersonalDataRepository personalDataRepository,
            JdbcTemplate jdbc) {
        this.appointmentRepository = appointmentRepository;
        this.employeeRepository = employeeRepository;
        this.personalDataRepository = personalDataRepository;
        this.jdbc = jdbc;
    }

    public PersonnelActionReportData load(Long employeeAppointmentId) {
        if (employeeAppointmentId == null) {
            throw new IllegalArgumentException("employeeAppointmentId is required.");
        }

        EmployeeAppointment selected = appointmentRepository.findById(employeeAppointmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Employee appointment not found: " + employeeAppointmentId));
        EmployeeAppointment previous = appointmentRepository
                .findTop1ByEmployeeIdAndAssumptionToDutyDateBeforeOrderByAssumptionToDutyDateDescEmployeeAppointmentIdDesc(
                        selected.getEmployeeId(), selected.getAssumptionToDutyDate())
                .orElse(null);
        Employee employee = employeeRepository.findById(selected.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Employee not found for appointment: " + employeeAppointmentId));
        PersonalData personalData = personalDataRepository.findByEmployeeId(selected.getEmployeeId());

        SettingsRow settings = loadSettings();
        OrganizationRow organization = loadOrganization(selected.getEmployeeId());
        String selectedNature = lookupText(
                "SELECT nature FROM natureofappointment WHERE natureofappointmentId = ?",
                selected.getNatureOfAppointmentId());

        return new PersonnelActionReportData(
                settings.companyName(),
                settings.address(),
                employeeName(personalData, employee),
                firstNonBlank(selected.getDetails(), selectedNature),
                selected.getAssumptionToDutyDate().format(REPORT_DATE),
                previous == null ? "" : lookupText(
                        "SELECT jobPositionName FROM job_position WHERE jobPositionId = ?",
                        previous.getJobPositionId()),
                lookupText(
                        "SELECT jobPositionName FROM job_position WHERE jobPositionId = ?",
                        selected.getJobPositionId()),
                money(previous == null ? null : previous.getSalaryPerMonth()),
                money(selected.getSalaryPerMonth()),
                "",
                organization.businessUnit(),
                "",
                organization.area(),
                selectedNature,
                settings.leftLogo(),
                settings.rightLogo()
        );
    }

    private SettingsRow loadSettings() {
        List<SettingsRow> rows = jdbc.query(
                "SELECT companyName, address, leftHeaderLogo, rightHeaderLogo FROM settings ORDER BY settingsId DESC",
                (rs, rowNum) -> new SettingsRow(
                        text(rs.getString("companyName")),
                        text(rs.getString("address")),
                        rs.getBytes("leftHeaderLogo"),
                        rs.getBytes("rightHeaderLogo")));
        return rows.isEmpty() ? new SettingsRow("", "", null, null) : rows.get(0);
    }

    private OrganizationRow loadOrganization(Long employeeId) {
        List<OrganizationRow> rows = jdbc.query(
                """
                SELECT a.areasName, bu.businessUnitsName
                FROM manage_personnel mp
                LEFT JOIN areas a ON a.areasId = mp.areaId
                LEFT JOIN businessunits bu ON bu.businessUnitsId = mp.businessUnitId
                WHERE mp.employeeId = ?
                ORDER BY mp.id DESC
                """,
                (rs, rowNum) -> new OrganizationRow(
                        text(rs.getString("areasName")),
                        text(rs.getString("businessUnitsName"))),
                employeeId);
        return rows.isEmpty() ? new OrganizationRow("", "") : rows.get(0);
    }

    private String lookupText(String sql, Object id) {
        if (id == null) {
            return "";
        }
        List<String> values = jdbc.query(sql, (rs, rowNum) -> text(rs.getString(1)), id);
        return values.isEmpty() ? "" : values.get(0);
    }

    private static String employeeName(PersonalData personalData, Employee employee) {
        if (personalData != null) {
            return familyNameFormat(
                    personalData.getSurname(),
                    personalData.getFirstname(),
                    personalData.getMiddlename(),
                    personalData.getExtname());
        }
        return familyNameFormat(employee.getLastname(), employee.getFirstname(), "", employee.getSuffix());
    }

    private static String familyNameFormat(String surname, String firstname, String middlename, String suffix) {
        String middleInitial = text(middlename).isEmpty()
                ? ""
                : " " + text(middlename).substring(0, 1).toUpperCase() + ".";
        String extension = text(suffix).isEmpty() ? "" : " " + text(suffix);
        String givenNames = (text(firstname) + middleInitial + extension).trim();
        if (text(surname).isEmpty()) {
            return givenNames;
        }
        return givenNames.isEmpty() ? text(surname) : text(surname) + ", " + givenNames;
    }

    private static String money(BigDecimal value) {
        return value == null ? "0.00" : MONEY.format(value);
    }

    private static String firstNonBlank(String primary, String fallback) {
        return text(primary).isEmpty() ? text(fallback) : text(primary);
    }

    private static String text(String value) {
        return value == null ? "" : value.trim();
    }

    private record SettingsRow(String companyName, String address, byte[] leftLogo, byte[] rightLogo) {}
    private record OrganizationRow(String area, String businessUnit) {}
}
