package com.humanresource.impl;

import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.entitymodels.LeaveMonetization;
import com.humanresource.entitymodels.Separation;
import com.humanresource.repositories.LeaveApplicationRepository;
import com.humanresource.repositories.LeaveMonetizationRepository;
import com.humanresource.repositories.SeparationRepository;
import com.humanresource.services.LeaveFormReportService;
import jakarta.annotation.PostConstruct;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

    @PostConstruct
    private void disableSchemaValidation() {
        System.setProperty("net.sf.jasperreports.compiler.xml.parser.validation", "false");
    }

    @Override
    public void generateLeaveForm(Long leaveApplicationId, OutputStream out) throws Exception {
        JasperReport report = compile("reports/leave_form_2020.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("LEAVE_APPLICATION_ID", leaveApplicationId);
        params.put("LEAVE_MONETIZATION_ID", null);
        LeaveApplication leaveApplication = leaveApplicationId == null
                ? null
                : leaveApplicationRepository.findById(leaveApplicationId).orElse(null);
        putDateParameters(
                params,
                leaveApplication == null ? null : leaveApplication.getStartDate(),
                leaveApplication == null ? null : leaveApplication.getEndDate(),
                leaveApplication == null ? null : leaveApplication.getDateFiled(),
                leaveApplication == null ? null : leaveApplication.getNoOfDays()
        );

        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
    }

    @Override
    public void generateLeaveFormForMonetization(Long leaveMonetizationId, OutputStream out) throws Exception {
        JasperReport report = compile("reports/leave_form_2020.jrxml");

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

        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
    }

    @Override
    public void generateLeaveCard(Long employeeId, Integer year, OutputStream out) throws Exception {
        JasperReport report = compile("reports/leave_card.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("EMPLOYEE_ID", employeeId);
        params.put("REPORT_YEAR", year);
        List<Separation> separations = employeeId == null
                ? List.of()
                : separationRepository.findByEmployeeId(employeeId);
        params.put("SEPARATION_TEXT", separationText(separations));

        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);
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

    private JasperReport compile(String classpathPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        try (InputStream is = resource.getInputStream()) {
            return JasperCompileManager.compileReport(is);
        }
    }
}
