package com.timekeeping.impl;

import com.timekeeping.dtos.DTRDailyDTO;
import com.timekeeping.dtos.DTRSegmentDTO;
import com.timekeeping.entitymodels.DTRDaily;
import com.timekeeping.entitymodels.DTRSegment;
import com.timekeeping.repositories.DTRDailyRepository;
import com.timekeeping.services.DTRDailyService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DTRDailyServiceImpl implements DTRDailyService {
    private final DTRDailyRepository dtrDailyRepository;
    private final JdbcTemplate jdbc;

    public DTRDailyServiceImpl(DTRDailyRepository dtrDailyRepository, JdbcTemplate jdbc) {
        this.dtrDailyRepository = dtrDailyRepository;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public DTRDailyDTO createOrUpdateDTRDaily(DTRDailyDTO dto) {
        DTRDaily entity = toEntity(dto);
        DTRDaily saved = dtrDailyRepository.save(entity);
        return toDTO(saved);
    }

    @Override
    public List<DTRDailyDTO> getEmployeeDTRDaily(String employeeId, LocalDateTime fromDate, LocalDateTime toDate) {
        // Convert LocalDateTime to LocalDate for the repository query
        // DTR_DAILY uses LocalDate (work_date) not LocalDateTime
        return dtrDailyRepository
                .findByEmployeeIdAndWorkDateBetween(employeeId, fromDate.toLocalDate(), toDate.toLocalDate())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getBulkDtrSummary(LocalDate from, LocalDate to) {
        // Query combines actual DTR records + rest days + approved requests (OB, OT, Pass Slip, Time Correction, CTO, Training)
        // This ensures the payroll service has complete attendance data with all excused absences
        String sql = "WITH AllDates AS ( " +
                "    SELECT " +
                "        e.employeeNo, " +
                "        e.employeeId, " +
                "        COALESCE(d.work_date, CONVERT(DATE, ws.wsDateTime)) AS dtrDate, " +
                "        COALESCE(d.attendance_status, 'REST DAY') AS status, " +
                "        COALESCE(d.total_work_minutes, 0) AS workMinutes, " +
                "        COALESCE(d.total_late_minutes, 0) AS lateMinutes, " +
                "        COALESCE(d.total_undertime_minutes, 0) AS undertimeMinutes, " +
                "        COALESCE(d.total_overtime_minutes, 0) AS overtimeMinutes, " +
                "        CASE WHEN ws.isDayOff = 1 THEN 1 ELSE 0 END AS isRestDay " +
                "    FROM work_schedule ws " +
                "    INNER JOIN employee e ON e.employeeId = ws.employeeId " +
                "    LEFT JOIN dtr_daily d ON d.employee_id = CAST(ws.employeeId AS VARCHAR) " +
                "        AND d.work_date = CONVERT(DATE, ws.wsDateTime) " +
                "    WHERE CONVERT(DATE, ws.wsDateTime) BETWEEN ? AND ? " +
                "    UNION " +
                "    SELECT " +
                "        e.employeeNo, " +
                "        e.employeeId, " +
                "        d.work_date AS dtrDate, " +
                "        d.attendance_status AS status, " +
                "        d.total_work_minutes AS workMinutes, " +
                "        d.total_late_minutes AS lateMinutes, " +
                "        d.total_undertime_minutes AS undertimeMinutes, " +
                "        d.total_overtime_minutes AS overtimeMinutes, " +
                "        0 AS isRestDay " +
                "    FROM dtr_daily d " +
                "    INNER JOIN employee e ON e.employeeId = CAST(d.employee_id AS INT) " +
                "    WHERE d.work_date BETWEEN ? AND ? " +
                "        AND NOT EXISTS ( " +
                "            SELECT 1 FROM work_schedule ws2 " +
                "            WHERE ws2.employeeId = CAST(d.employee_id AS INT) " +
                "            AND CONVERT(DATE, ws2.wsDateTime) = d.work_date " +
                "        ) " +
                ") " +
                "SELECT " +
                "    ad.employeeNo, " +
                "    ad.dtrDate, " +
                "    ad.status, " +
                "    ad.workMinutes, " +
                "    ad.lateMinutes, " +
                "    ad.undertimeMinutes, " +
                "    ad.overtimeMinutes, " +
                "    ad.isRestDay, " +
                "    CASE WHEN ob.officialEngagementApplicationId IS NOT NULL THEN 1 ELSE 0 END AS hasApprovedOb, " +
                "    CASE WHEN ot.overtimeRequestId IS NOT NULL THEN 1 ELSE 0 END AS hasApprovedOt, " +
                "    CASE WHEN ps.passSlipId IS NOT NULL THEN 1 ELSE 0 END AS hasApprovedPs, " +
                "    CASE WHEN tc.timeCorrectionId IS NOT NULL THEN 1 ELSE 0 END AS hasApprovedTc, " +
                "    CASE WHEN cto.ctoId IS NOT NULL THEN 1 ELSE 0 END AS hasApprovedCto, " +
                "    CASE WHEN ld.learningAndDevelopmentId IS NOT NULL THEN 1 ELSE 0 END AS hasTraining " +
                "FROM AllDates ad " +
                "LEFT JOIN official_engagement_application ob " +
                "    ON ob.employeeId = ad.employeeId " +
                "    AND ob.status = 'Approved' " +
                "    AND ad.dtrDate BETWEEN ob.startDate AND ob.endDate " +
                "LEFT JOIN overtime_request ot " +
                "    ON ot.employeeId = ad.employeeId " +
                "    AND ot.status = 'Approved' " +
                "    AND ad.dtrDate = CONVERT(DATE, ot.dateTimeFrom) " +
                "LEFT JOIN pass_slip ps " +
                "    ON ps.employeeId = ad.employeeId " +
                "    AND ps.status = 'Approved' " +
                "    AND ad.dtrDate = ps.passSlipDate " +
                "LEFT JOIN time_correction tc " +
                "    ON tc.employeeId = ad.employeeId " +
                "    AND tc.status = 'Approved' " +
                "    AND ad.dtrDate = tc.workDate " +
                "LEFT JOIN compensatory_time_off cto " +
                "    ON cto.employeeId = ad.employeeId " +
                "    AND cto.status = 'Approved' " +
                "    AND ad.dtrDate = cto.dateOfOffset " +
                "LEFT JOIN personaldata pd " +
                "    ON pd.employeeId = ad.employeeId " +
                "LEFT JOIN learninganddevelopment ld " +
                "    ON ld.personalDataId = pd.personalDataId " +
                "    AND ad.dtrDate BETWEEN ld.fromDate AND ld.toDate " +
                "ORDER BY ad.employeeNo, ad.dtrDate";

        return jdbc.query(sql, 
                new Object[]{
                    java.sql.Date.valueOf(from), 
                    java.sql.Date.valueOf(to),
                    java.sql.Date.valueOf(from), 
                    java.sql.Date.valueOf(to)
                },
                (rs, rowNum) -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("employeeNo", rs.getString("employeeNo"));
                    row.put("dtrDate", rs.getDate("dtrDate").toLocalDate());
                    
                    String status = rs.getString("status");
                    Integer workMin = rs.getInt("workMinutes");
                    Integer isRestDayFlag = rs.getInt("isRestDay");
                    
                    // Get approval flags from database
                    Integer hasOb = rs.getInt("hasApprovedOb");
                    Integer hasOt = rs.getInt("hasApprovedOt");
                    Integer hasPs = rs.getInt("hasApprovedPs");
                    Integer hasTc = rs.getInt("hasApprovedTc");
                    Integer hasCto = rs.getInt("hasApprovedCto");
                    Integer hasTraining = rs.getInt("hasTraining");
                    
                    // Determine if present (status PRESENT or has work minutes > 0)
                    // But NOT if it's a rest day
                    boolean present = ("PRESENT".equalsIgnoreCase(status) || 
                                      "Present".equalsIgnoreCase(status) ||
                                      (workMin != null && workMin > 0)) && isRestDayFlag != 1;
                    row.put("present", present);
                    
                    // Check if rest day (from work_schedule isDayOff flag)
                    boolean restDay = isRestDayFlag == 1 || 
                                      "REST DAY".equalsIgnoreCase(status) || 
                                      "RESTDAY".equalsIgnoreCase(status);
                    row.put("restDay", restDay);
                    
                    row.put("lateMinutes", rs.getInt("lateMinutes"));
                    row.put("undertimeMinutes", rs.getInt("undertimeMinutes"));
                    
                    // Set approval flags from database query results
                    // These flags tell payroll that the employee had approved requests to excuse absence/tardiness
                    row.put("hasApprovedOb", hasOb == 1);
                    row.put("hasApprovedOt", hasOt == 1);
                    row.put("hasApprovedPs", hasPs == 1);
                    row.put("hasApprovedTc", hasTc == 1);
                    row.put("hasApprovedCto", hasCto == 1);
                    row.put("hasTraining", hasTraining == 1);
                    
                    return row;
                });
    }

    private DTRDailyDTO toDTO(DTRDaily entity) {
        DTRDailyDTO dto = new DTRDailyDTO();
        dto.setDtrDailyId(entity.getDtrDailyId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setWorkDate(entity.getWorkDate().atStartOfDay());
        dto.setTotalWorkMinutes(entity.getTotalWorkMinutes());
        dto.setTotalLateMinutes(entity.getTotalLateMinutes());
        dto.setTotalUndertimeMinutes(entity.getTotalUndertimeMinutes());
        dto.setTotalOvertimeMinutes(entity.getTotalOvertimeMinutes());
        dto.setAttendanceStatus(entity.getAttendanceStatus());
        if (entity.getSegments() != null) {
            dto.setSegments(entity.getSegments().stream().map(this::toSegmentDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private DTRSegmentDTO toSegmentDTO(DTRSegment segment) {
        DTRSegmentDTO dto = new DTRSegmentDTO();
        dto.setDtrSegmentId(segment.getDtrSegmentId());
        dto.setSegmentNo(segment.getSegmentNo());
        dto.setTimeIn(segment.getTimeIn());
        dto.setBreakOut(segment.getBreakOut());
        dto.setBreakIn(segment.getBreakIn());
        dto.setTimeOut(segment.getTimeOut());
        dto.setWorkMinutes(segment.getWorkMinutes());
        dto.setLateMinutes(segment.getLateMinutes());
        dto.setUndertimeMinutes(segment.getUndertimeMinutes());
        dto.setOvertimeMinutes(segment.getOvertimeMinutes());
        return dto;
    }

    private DTRDaily toEntity(DTRDailyDTO dto) {
        DTRDaily entity = new DTRDaily();
        entity.setDtrDailyId(dto.getDtrDailyId());
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setWorkDate(dto.getWorkDate().toLocalDate());
        entity.setTotalWorkMinutes(dto.getTotalWorkMinutes());
        entity.setTotalLateMinutes(dto.getTotalLateMinutes());
        entity.setTotalUndertimeMinutes(dto.getTotalUndertimeMinutes());
        entity.setTotalOvertimeMinutes(dto.getTotalOvertimeMinutes());
        entity.setAttendanceStatus(dto.getAttendanceStatus());
        if (dto.getSegments() != null) {
            entity.setSegments(dto.getSegments().stream().map(s -> toSegmentEntity(s, entity)).collect(Collectors.toList()));
        }
        return entity;
    }

    private DTRSegment toSegmentEntity(DTRSegmentDTO dto, DTRDaily parent) {
        DTRSegment entity = new DTRSegment();
        entity.setDtrSegmentId(dto.getDtrSegmentId());
        entity.setDtrDaily(parent);
        entity.setSegmentNo(dto.getSegmentNo());
        entity.setTimeIn(dto.getTimeIn());
        entity.setBreakOut(dto.getBreakOut());
        entity.setBreakIn(dto.getBreakIn());
        entity.setTimeOut(dto.getTimeOut());
        entity.setWorkMinutes(dto.getWorkMinutes());
        entity.setLateMinutes(dto.getLateMinutes());
        entity.setUndertimeMinutes(dto.getUndertimeMinutes());
        entity.setOvertimeMinutes(dto.getOvertimeMinutes());
        return entity;
    }
}

