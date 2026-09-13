package com.humanresource.impl;

import com.humanresource.dtos.PersonnelActionReportData;
import com.humanresource.reports.JasperReportRegistry;
import com.humanresource.services.EmployeeAppointmentReportService;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeAppointmentReportServiceImpl implements EmployeeAppointmentReportService {

    private final EmployeeAppointmentReportDataLoader dataLoader;

    public EmployeeAppointmentReportServiceImpl(EmployeeAppointmentReportDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @Transactional(readOnly = true)
    @Override
    public void generatePersonnelActionReport(Long employeeAppointmentId, OutputStream outputStream) throws Exception {
        PersonnelActionReportData data = dataLoader.load(employeeAppointmentId);
        JasperReport report = JasperReportRegistry.get("reports/personnel_action.jrxml");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logoleft", imageStream(data.getLeftHeaderLogo()));
        parameters.put("logoright", imageStream(data.getRightHeaderLogo()));
        JasperPrint print = JasperFillManager.fillReport(
                report,
                parameters,
                new JRBeanCollectionDataSource(List.of(data)));
        JasperExportManager.exportReportToPdfStream(print, outputStream);
    }

    private static InputStream imageStream(byte[] bytes) {
        return bytes == null || bytes.length == 0 ? null : new ByteArrayInputStream(bytes);
    }
}
