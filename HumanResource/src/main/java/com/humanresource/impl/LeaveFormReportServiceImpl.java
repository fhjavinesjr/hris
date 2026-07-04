package com.humanresource.impl;

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
import java.util.HashMap;
import java.util.Map;

@Service
public class LeaveFormReportServiceImpl implements LeaveFormReportService {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    private void disableSchemaValidation() {
        System.setProperty("net.sf.jasperreports.compiler.xml.parser.validation", "false");
    }

    @Override
    public void generateLeaveForm(Long leaveApplicationId, OutputStream out) throws Exception {
        JasperReport report = compile("reports/leave_form_2020.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("LEAVE_APPLICATION_ID", leaveApplicationId);

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

        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
    }

    private JasperReport compile(String classpathPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        try (InputStream is = resource.getInputStream()) {
            return JasperCompileManager.compileReport(is);
        }
    }
}