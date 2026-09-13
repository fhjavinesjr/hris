package com.humanresource.impl;

import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.entitymodels.LeaveMonetization;
import com.humanresource.entitymodels.ReportHeaderSettings;
import com.humanresource.entitymodels.Separation;
import com.humanresource.dtos.LeaveBalanceDTO;
import com.humanresource.repositories.LeaveApplicationRepository;
import com.humanresource.repositories.LeaveMonetizationRepository;
import com.humanresource.repositories.ReportHeaderSettingsRepository;
import com.humanresource.repositories.SeparationRepository;
import com.humanresource.reports.JasperReportRegistry;
import com.humanresource.services.LeaveBalanceService;
import com.humanresource.services.LeaveFormReportService;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class LeaveFormReportServiceImpl implements LeaveFormReportService {

    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    @Autowired
    private DataSource dataSource;

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private LeaveMonetizationRepository leaveMonetizationRepository;

    @Autowired
    private SeparationRepository separationRepository;

    @Autowired
    private ReportHeaderSettingsRepository reportHeaderSettingsRepository;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Transactional(readOnly = true)
    @Override
    public void generateLeaveForm(Long leaveApplicationId, OutputStream out) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("LEAVE_APPLICATION_ID", leaveApplicationId);
        params.put("LEAVE_MONETIZATION_ID", null);
        LeaveApplication leaveApplication = leaveApplicationId == null
                ? null
                : leaveApplicationRepository.findById(leaveApplicationId).orElse(null);
        double applicationDays = leaveApplication == null
                ? 0.0
                : workingDaysApplied(
                        leaveApplication.getStartDate(),
                        leaveApplication.getEndDate(),
                        leaveApplication.getNoOfDays());
        putDateParameters(
                params,
                leaveApplication == null ? null : leaveApplication.getStartDate(),
                leaveApplication == null ? null : leaveApplication.getEndDate(),
                leaveApplication == null ? null : leaveApplication.getDateFiled(),
                leaveApplication == null ? null : leaveApplication.getNoOfDays()
        );
        putBalanceParameters(
                params,
                leaveApplication == null ? null : leaveApplication.getEmployeeId(),
                leaveApplication == null
                        ? null
                        : (leaveApplication.getStartDate() == null
                        ? leaveApplication.getDateFiled()
                        : leaveApplication.getStartDate()),
                consumesVacationCredit(leaveApplication == null ? null : leaveApplication.getLeaveType())
                        ? applicationDays : 0.0,
                normalize(leaveApplication == null ? null : leaveApplication.getLeaveType()).equals("sick leave")
                        ? applicationDays : 0.0
        );
        putLeaveDetailParameters(
                params,
                leaveApplication == null ? null : leaveApplication.getLeaveType(),
                leaveApplication == null ? null : leaveApplication.getDetails()
        );

        putHeaderLogoParameters(params);
        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(
                    JasperReportRegistry.get("reports/leave_form_2020.jrxml"), params, conn);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public void generateLeaveFormForMonetization(Long leaveMonetizationId, OutputStream out) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("LEAVE_APPLICATION_ID", null);
        params.put("LEAVE_MONETIZATION_ID", leaveMonetizationId);
        LeaveMonetization leaveMonetization = leaveMonetizationId == null
                ? null
                : leaveMonetizationRepository.findById(leaveMonetizationId).orElse(null);
        putDateParameters(
                params,
                null,
                null,
                leaveMonetization == null ? null : leaveMonetization.getDateFiled(),
                leaveMonetization == null ? null : leaveMonetization.getTotalDays()
        );
        putBalanceParameters(
                params,
                leaveMonetization == null ? null : leaveMonetization.getEmployeeId(),
                leaveMonetization == null ? null : leaveMonetization.getDateFiled(),
                leaveMonetization == null ? 0.0 : nvl(leaveMonetization.getNoOfDaysVL()),
                leaveMonetization == null ? 0.0 : nvl(leaveMonetization.getNoOfDaysSL())
        );
        putLeaveDetailParameters(params, "Leave Monetization", null);

        putHeaderLogoParameters(params);
        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(
                    JasperReportRegistry.get("reports/leave_form_2020.jrxml"), params, conn);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public void generateLeaveCard(Long employeeId, Integer year, OutputStream out) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("EMPLOYEE_ID", employeeId);
        params.put("REPORT_YEAR", year);
        List<Separation> separations = employeeId == null
                ? List.of()
                : separationRepository.findByEmployeeId(employeeId);
        params.put("SEPARATION_TEXT", separationText(separations));
        putHeaderLogoParameters(params);

        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(
                    JasperReportRegistry.get("reports/leave_card.jrxml"), params, conn);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
    }

    private static void putDateParameters(Map<String, Object> params,
                                          LocalDate startDate,
                                          LocalDate endDate,
                                          LocalDate dateFiled,
                                          Double fallbackDays) {
        params.put("WORKING_DAYS_APPLIED", workingDaysApplied(startDate, endDate, fallbackDays));
        params.put("INCLUSIVE_DATES", inclusiveDates(startDate, endDate, dateFiled));
    }

    private void putBalanceParameters(Map<String, Object> params,
                                      Long employeeId,
                                      LocalDate referenceDate,
                                      double appliedVl,
                                      double appliedSl) throws Exception {
        LeaveBalanceDTO balance = employeeId == null
                ? null
                : leaveBalanceService.getCurrentBalance(employeeId);
        double currentVl = balance == null ? 0.0 : nvl(balance.getVacationLeaveBalance());
        double currentSl = balance == null ? 0.0 : nvl(balance.getSickLeaveBalance());

        params.put("CREDIT_DATE_AS_OF", java.sql.Date.valueOf(previousMonthEnd(referenceDate)));
        // Section 7.A must remain stable before and after Leave Information posts
        // the transaction: top = current dashboard + this application; the JRXML
        // then shows this application under Less and returns to current dashboard.
        params.put("CERTIFIED_VL_CREDIT", balanceBeforeApplication(currentVl, appliedVl));
        params.put("CERTIFIED_SL_CREDIT", balanceBeforeApplication(currentSl, appliedSl));
    }

    static LocalDate previousMonthEnd(LocalDate referenceDate) {
        LocalDate effectiveDate = referenceDate == null ? LocalDate.now() : referenceDate;
        return effectiveDate.withDayOfMonth(1).minusDays(1);
    }

    private static double nvl(Double value) {
        return value == null ? 0.0 : value;
    }

    static double balanceBeforeApplication(double currentBalance, double appliedDays) {
        return Math.round((currentBalance + appliedDays) * 1000.0) / 1000.0;
    }

    private static boolean consumesVacationCredit(String leaveType) {
        String normalized = normalize(leaveType);
        return normalized.equals("vacation leave")
                || normalized.equals("forced leave")
                || normalized.equals("mandatory forced leave");
    }

    static double workingDaysApplied(LocalDate startDate, LocalDate endDate, Double fallbackDays) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return fallbackDays == null ? 0.0 : fallbackDays;
        }
        return ChronoUnit.DAYS.between(startDate, endDate) + 1.0;
    }

    static String inclusiveDates(LocalDate startDate, LocalDate endDate, LocalDate dateFiled) {
        if (startDate != null && endDate != null) {
            return REPORT_DATE_FORMAT.format(startDate) + " - " + REPORT_DATE_FORMAT.format(endDate);
        }
        return dateFiled == null ? "" : REPORT_DATE_FORMAT.format(dateFiled);
    }

    static void putLeaveDetailParameters(Map<String, Object> params, String leaveType, String details) {
        LeaveFormDetails formDetails = LeaveFormDetails.from(leaveType, details);
        params.put("LEAVE_TYPE_CODE", leaveTypeCode(leaveType));
        params.put("OTHER_LEAVE_TYPE", isStandardFormLeaveType(leaveType) ? "" : clean(leaveType));
        params.put("VL_IN_COUNTRY", formDetails.vacationLocationType() == VacationLocation.WITHIN ? 1 : 0);
        params.put("VL_ABROAD", formDetails.vacationLocationType() == VacationLocation.ABROAD ? 1 : 0);
        params.put("VL_LOCATION", formDetails.vacationLocation());
        params.put("SL_IN_HOSPITAL", formDetails.sickLocationType() == SickLocation.HOSPITAL ? 1 : 0);
        params.put("SL_OUT_PATIENT", formDetails.sickLocationType() == SickLocation.OUT_PATIENT ? 1 : 0);
        params.put("SL_ILLNESS", formDetails.sickIllness());
        params.put("WOMEN_ILLNESS", formDetails.womenIllness());
        params.put("STUDY_LEAVE_CASE", formDetails.studyLeaveCase());
    }

    static int leaveTypeCode(String leaveType) {
        String normalized = normalize(leaveType);
        if (normalized.equals("vacation leave")) return 1;
        if (normalized.equals("sick leave")) return 2;
        if (normalized.equals("paternity leave")) return 3;
        if (normalized.equals("maternity leave")) return 4;
        if (normalized.equals("forced leave") || normalized.equals("mandatory forced leave")) return 5;
        if (normalized.equals("special privilege leave")) return 6;
        if (normalized.equals("solo parent leave")) return 7;
        if (normalized.equals("rehabilitation leave") || normalized.equals("rehabilitation privilege")) return 8;
        if (normalized.equals("gynecological leave") || normalized.equals("special leave benefits for women")) return 9;
        if (normalized.equals("study leave")) return 10;
        if (normalized.equals("terminal leave")) return 11;
        if (normalized.contains("vawc")) return 15;
        if (normalized.equals("special emergency leave") || normalized.equals("special emergency calamity leave")) return 16;
        if (normalized.equals("adoption leave")) return 17;
        return 0;
    }

    private static boolean isStandardFormLeaveType(String leaveType) {
        return leaveTypeCode(leaveType) != 0 || normalize(leaveType).equals("leave monetization");
    }

    private static String normalize(String value) {
        return clean(value)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    enum VacationLocation { NONE, WITHIN, ABROAD }
    enum SickLocation { NONE, HOSPITAL, OUT_PATIENT }

    record LeaveFormDetails(
            VacationLocation vacationLocationType,
            String vacationLocation,
            SickLocation sickLocationType,
            String sickIllness,
            String womenIllness,
            int studyLeaveCase
    ) {
        private static final String WITHIN = "Within the Philippines";
        private static final String ABROAD = "Abroad";
        private static final String HOSPITAL = "In Hospital";
        private static final String OUT_PATIENT = "Out Patient";
        private static final String MASTERS = "Completion of Master's Degree";
        private static final String BAR = "BAR/Board Examination Review";

        static LeaveFormDetails from(String leaveType, String details) {
            String value = clean(details);
            String type = normalize(leaveType);
            if (type.equals("vacation leave") || type.equals("special privilege leave")) {
                ParsedDetail parsed = parse(value, WITHIN, ABROAD);
                VacationLocation locationType = parsed.option().equals(ABROAD)
                        ? VacationLocation.ABROAD : VacationLocation.WITHIN;
                return new LeaveFormDetails(locationType, parsed.value(), SickLocation.NONE, "", "", 0);
            }
            if (type.equals("sick leave")) {
                // The legacy form treated its single free-text illness field as
                // outpatient. Keep that behavior only when no explicit option exists.
                ParsedDetail parsed = parse(value, OUT_PATIENT, HOSPITAL);
                SickLocation locationType = parsed.option().equals(HOSPITAL)
                        ? SickLocation.HOSPITAL : SickLocation.OUT_PATIENT;
                return new LeaveFormDetails(VacationLocation.NONE, "", locationType, parsed.value(), "", 0);
            }
            if (type.equals("gynecological leave") || type.equals("special leave benefits for women")) {
                return new LeaveFormDetails(VacationLocation.NONE, "", SickLocation.NONE, "", value, 0);
            }
            if (type.equals("study leave")) {
                int studyCase = normalize(value).equals(normalize(BAR)) ? 2
                        : normalize(value).equals(normalize(MASTERS)) ? 1 : 0;
                return new LeaveFormDetails(VacationLocation.NONE, "", SickLocation.NONE, "", "", studyCase);
            }
            return new LeaveFormDetails(VacationLocation.NONE, "", SickLocation.NONE, "", "", 0);
        }

        private static ParsedDetail parse(String value, String firstOption, String secondOption) {
            for (String option : List.of(firstOption, secondOption)) {
                if (value.equalsIgnoreCase(option)) return new ParsedDetail(option, "");
                String prefix = option + ":";
                if (value.regionMatches(true, 0, prefix, 0, prefix.length())) {
                    return new ParsedDetail(option, value.substring(prefix.length()).trim());
                }
            }
            // Legacy records contained only free text. Preserve it and use the
            // historically implied first/default choice for the correct leave section.
            return new ParsedDetail(firstOption, value);
        }
    }

    record ParsedDetail(String option, String value) {}

    static String separationText(List<Separation> separations) {
        if (separations == null) {
            return "";
        }
        return separations.stream()
                .filter(separation -> separation != null && separation.getSeparationDate() != null)
                .max(Comparator
                        .comparing(
                                Separation::getSeparationDate,
                                Comparator.nullsFirst(Comparator.naturalOrder())
                        )
                        .thenComparing(
                                Separation::getSeparationId,
                                Comparator.nullsFirst(Comparator.naturalOrder())
                        ))
                .map(separation -> "Separated effective "
                        + REPORT_DATE_FORMAT.format(separation.getSeparationDate()))
                .orElse("");
    }

    private void putHeaderLogoParameters(Map<String, Object> params) {
        ReportHeaderSettings settings = reportHeaderSettingsRepository
                .findFirstByOrderBySettingsIdDesc()
                .orElse(null);
        params.put("logoleft", imageStream(settings == null ? null : settings.getLeftHeaderLogo()));
        params.put("logoright", imageStream(settings == null ? null : settings.getRightHeaderLogo()));
    }

    private static InputStream imageStream(byte[] bytes) {
        return bytes == null ? null : new ByteArrayInputStream(bytes);
    }

}
