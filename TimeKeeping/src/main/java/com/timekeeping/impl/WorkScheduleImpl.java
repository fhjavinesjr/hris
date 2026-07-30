package com.timekeeping.impl;

import com.timekeeping.dtos.WorkScheduleDTO;
import com.timekeeping.dtos.WorkScheduleReportRow;
import com.timekeeping.entitymodels.WorkSchedule;
import com.timekeeping.reports.WorkScheduleReportDataLoader;
import com.timekeeping.repositories.WorkScheduleRepository;
import com.timekeeping.services.WorkScheduleService;
import jakarta.transaction.Transactional;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.sql.DataSource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WorkScheduleImpl implements WorkScheduleService {

    private static final Logger log = LoggerFactory.getLogger(WorkScheduleImpl.class);
    private final WorkScheduleRepository workScheduleRepository;
    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    public WorkScheduleImpl(WorkScheduleRepository workScheduleRepository, JdbcTemplate jdbc, DataSource dataSource) {
        this.workScheduleRepository = workScheduleRepository;
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    @Transactional
    @Override
    public WorkScheduleDTO createWorkSchedule(WorkScheduleDTO workScheduleDTO) throws Exception {
        try {
            WorkSchedule workSchedule = new WorkSchedule(workScheduleDTO.getEmployeeId(), workScheduleDTO.getTsCode(), workScheduleDTO.getWsDateTime());
            workSchedule.setIsDayOff(workScheduleDTO.getIsDayOff() != null && workScheduleDTO.getIsDayOff());
            WorkSchedule wsSaved = workScheduleRepository.save(workSchedule);
            workScheduleDTO.setWsId(wsSaved.getWsId());
            return workScheduleDTO;
        } catch (Exception e) {
            log.info("Creation of Work Schedule Failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<WorkScheduleDTO> getAllWorkSchedule(String employeeId, LocalDateTime monthStart, LocalDateTime monthEnd) throws Exception {
        try {
            List<WorkScheduleDTO> workScheduleDTOList = new ArrayList<>();
            Optional<List<WorkSchedule>> opWorkSchedules = workScheduleRepository.findByEmployeeIdAndWsDateTimeBetween(employeeId, monthStart, monthEnd);
            if(opWorkSchedules.isPresent()) {
                List<WorkSchedule> workSchedules = opWorkSchedules.get();
                for(WorkSchedule workSchedule : workSchedules) {
                    WorkScheduleDTO workScheduleDTO = new WorkScheduleDTO(workSchedule.getWsId(), workSchedule.getEmployeeId(), workSchedule.getTsCode(), workSchedule.getWsDateTime());
                    workScheduleDTO.setIsDayOff(workSchedule.getIsDayOff());
                    workScheduleDTOList.add(workScheduleDTO);
                }
            }

            return workScheduleDTOList;
        } catch (Exception e) {
            log.info("Getting All Work Schedule Failed: {}", e.getMessage());
            return null;
        }
    }

    public WorkSchedule getWorkScheduleById(Long wsId) {
        return workScheduleRepository.findById(wsId).orElseThrow(() -> new RuntimeException("Work Schedule not found"));
    }

    @Transactional
    @Override
    public WorkScheduleDTO updateWorkSchedule(Long wsId, WorkScheduleDTO workScheduleDTO) throws Exception {
        try {
            WorkSchedule workSchedule = getWorkScheduleById(wsId);
            workSchedule = new WorkSchedule(workScheduleDTO.getEmployeeId(), workScheduleDTO.getTsCode(), workScheduleDTO.getWsDateTime());
            workSchedule.setWsId(wsId);
            workSchedule.setIsDayOff(workScheduleDTO.getIsDayOff() != null && workScheduleDTO.getIsDayOff());
            workScheduleRepository.save(workSchedule);
            return workScheduleDTO;
        } catch(Exception e) {
            log.info("Update Work Schedule Failed: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean deleteWorkSchedule(Long wsId) throws Exception {
        try {
            workScheduleRepository.deleteById(wsId);
            return true;
        } catch(Exception e) {
            log.info("Delete Work Schedule Failed: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    @Override
    public int bulkCreateDayOff(List<WorkScheduleDTO> dtos) throws Exception {
        int count = 0;
        for (WorkScheduleDTO dto : dtos) {
            try {
                WorkSchedule ws = new WorkSchedule();
                ws.setEmployeeId(dto.getEmployeeId());
                ws.setWsDateTime(dto.getWsDateTime());
                ws.setIsDayOff(true);
                workScheduleRepository.save(ws);
                count++;
            } catch (Exception e) {
                log.warn("Failed to save day-off for {}: {}", dto.getEmployeeId(), e.getMessage());
            }
        }
        return count;
    }

    @Override
    public void generateWorkScheduleReport(Long areaId, Long businessUnitId, LocalDate fromDate, LocalDate toDate, String preparedBy, String preparedByPos, String approvedBy, String approvedByPos, OutputStream out) throws Exception {
        JasperReport report = compile("reports/works_schedule.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("areaId", areaId);
        params.put("businessUnitId", businessUnitId);
        params.put("fromDate", java.sql.Date.valueOf(fromDate));
        params.put("toDate", java.sql.Date.valueOf(toDate));
        params.put("dateRangeLabel", fromDate + " to " + toDate);
        params.put("reportArea", resolveAreaName(areaId));
        params.put("reportBusinessUnit", businessUnitId == null ? "All Business Units" : resolveBusinessUnitName(businessUnitId));
        params.put("approvedBy", blankIfNull(approvedBy));
        params.put("printedBy", blankIfNull(preparedBy));
        params.put("approvedByPos", blankIfNull(approvedByPos));
        params.put("printedByPos", blankIfNull(preparedByPos));
        params.put("formCode", "");
        params.put("nchLogoPath", "");
        params.put("dohLogoPath", "");
        params.put("webAppPath", "");
        params.put("supervisingNurse", "");

        Map<String, Object> settings = loadLatestReportSettings();

        params.put("currentCompany", settings.getOrDefault("companyName", ""));
        params.put("currentCompanyAddress", settings.getOrDefault("address", ""));
        params.put("isDOH", isDbTrue(settings.get("hospitalAgency")));
        params.put("logoleft", toValidImageInputStream((byte[]) settings.get("leftHeaderLogo")));
        params.put("logoright", toValidImageInputStream((byte[]) settings.get("rightHeaderLogo")));

        List<WorkScheduleReportRow> reportRows =
                new WorkScheduleReportDataLoader(jdbc).load(areaId, businessUnitId, fromDate, toDate);
        JasperPrint print = JasperFillManager.fillReport(
                report,
                params,
                new JRBeanCollectionDataSource(reportRows)
        );
        JasperExportManager.exportReportToPdfStream(print, out);
    }

    Map<String, Object> loadLatestReportSettings() {
        Map<String, Object> settings = jdbc.query(
                connection -> {
                    java.sql.PreparedStatement statement = connection.prepareStatement(
                            "SELECT companyName, address, hospitalAgency, leftHeaderLogo, rightHeaderLogo "
                                    + "FROM settings ORDER BY settingsId DESC"
                    );
                    statement.setMaxRows(1);
                    return statement;
                },
                rs -> {
                    if (!rs.next()) return Collections.<String, Object>emptyMap();
                    Map<String, Object> result = new HashMap<>();
                    result.put("companyName", rs.getString("companyName"));
                    result.put("address", rs.getString("address"));
                    result.put("hospitalAgency", rs.getObject("hospitalAgency"));
                    result.put("leftHeaderLogo", rs.getBytes("leftHeaderLogo"));
                    result.put("rightHeaderLogo", rs.getBytes("rightHeaderLogo"));
                    return result;
                }
        );
        return settings == null ? Collections.emptyMap() : settings;
    }


    @Override
    public Map<String, String> getWorkScheduleSignatoryInfo(String employeeId) {
        Map<String, String> result = new HashMap<>();
        result.put("employeeId", employeeId == null ? "" : employeeId);
        result.put("fullName", "");
        result.put("position", "");
        result.put("salaryGrade", "");

        if (employeeId == null || employeeId.trim().isEmpty()) {
            return result;
        }

        Long numericEmployeeId;
        try {
            numericEmployeeId = Long.valueOf(employeeId.trim());
        } catch (NumberFormatException ex) {
            return result;
        }

        String sql = """
                WITH ranked_appointments AS (
                    SELECT
                        ea0.employeeId,
                        ea0.jobPositionId,
                        ea0.salaryGrade,
                        ROW_NUMBER() OVER (
                            PARTITION BY ea0.employeeId
                            ORDER BY
                                CASE WHEN ea0.activeAppointment IS NULL THEN 1 ELSE 0 END,
                                ea0.activeAppointment DESC,
                                CASE WHEN ea0.assumptionToDutyDate IS NULL THEN 1 ELSE 0 END,
                                ea0.assumptionToDutyDate DESC,
                                ea0.employeeAppointmentId DESC
                        ) AS appointmentRank
                    FROM employeeAppointment ea0
                )
                SELECT
                    LTRIM(RTRIM(CONCAT(COALESCE(e.lastname, ''),
                        CASE WHEN COALESCE(e.suffix, '') = '' THEN '' ELSE CONCAT(' ', e.suffix) END,
                        CASE WHEN COALESCE(e.firstname, '') = '' THEN '' ELSE CONCAT(', ', e.firstname) END
                    ))) AS fullName,
                    COALESCE(jp.jobPositionName, COALESCE(e.position, '')) AS position,
                    COALESCE(CAST(ea.salaryGrade AS VARCHAR(20)), '') AS salaryGrade
                FROM employee e
                LEFT JOIN ranked_appointments ea
                    ON ea.employeeId = e.employeeId
                    AND ea.appointmentRank = 1
                LEFT JOIN job_position jp ON jp.jobPositionId = ea.jobPositionId
                WHERE e.employeeId = ?
                """;

        return jdbc.query(sql, rs -> {
            if (!rs.next()) {
                return result;
            }

            result.put("fullName", blankIfNull(rs.getString("fullName")));
            result.put("position", blankIfNull(rs.getString("position")));
            result.put("salaryGrade", blankIfNull(rs.getString("salaryGrade")));
            return result;
        }, numericEmployeeId);
    }

    private String resolveAreaName(Long areaId) {
        if (areaId == null) return "";
        List<String> names = jdbc.queryForList("SELECT areasName FROM areas WHERE areasId = ?", String.class, areaId);
        return names.isEmpty() ? "" : names.get(0);
    }

    private String resolveBusinessUnitName(Long businessUnitId) {
        if (businessUnitId == null) return "All Business Units";
        List<String> names = jdbc.queryForList("SELECT businessUnitsName FROM businessunits WHERE businessUnitsId = ?", String.class, businessUnitId);
        return names.isEmpty() ? "" : names.get(0);
    }

    private InputStream toValidImageInputStream(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }
        try (ByteArrayInputStream validationStream = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(validationStream);
            if (image == null) {
                return null;
            }
            return new ByteArrayInputStream(imageBytes);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String blankIfNull(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isDbTrue(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        if (value == null) return false;
        String text = value.toString().trim();
        return "true".equalsIgnoreCase(text) || "t".equalsIgnoreCase(text)
                || "yes".equalsIgnoreCase(text) || "y".equalsIgnoreCase(text) || "1".equals(text);
    }

    private JasperReport compile(String classpathPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        try (InputStream is = resource.getInputStream()) {
            return JasperCompileManager.compileReport(is);
        }
    }
}
