package com.humanresource.impl;

import com.humanresource.dtos.OvertimeRequestDTO;
import com.humanresource.entitymodels.OvertimeRequest;
import com.humanresource.repositories.OvertimeRequestRepository;
import com.humanresource.services.OvertimeRequestService;
import jakarta.transaction.Transactional;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OvertimeRequestImpl implements OvertimeRequestService {

    private static final Logger log = LoggerFactory.getLogger(OvertimeRequestImpl.class);

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final DataSource dataSource;

    public OvertimeRequestImpl(OvertimeRequestRepository overtimeRequestRepository,
                               DataSource dataSource) {
        this.overtimeRequestRepository = overtimeRequestRepository;
        this.dataSource = dataSource;
    }

    private OvertimeRequestDTO toDTO(OvertimeRequest e) {
        OvertimeRequestDTO dto = new OvertimeRequestDTO();
        dto.setOvertimeRequestId(e.getOvertimeRequestId());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setDateFiled(e.getDateFiled());
        dto.setDateTimeFrom(e.getDateTimeFrom());
        dto.setDateTimeTo(e.getDateTimeTo());
        dto.setTotalHours(e.getTotalHours());
        dto.setWorkType(e.getWorkType());
        dto.setAuthorityReference(e.getAuthorityReference());
        dto.setEmergencyPostFiling(e.getEmergencyPostFiling());
        dto.setEmergencyJustification(e.getEmergencyJustification());
        dto.setBreakMinutes(e.getBreakMinutes());
        dto.setNetAuthorizedHours(e.getNetAuthorizedHours());
        dto.setPurpose(e.getPurpose());
        dto.setStatus(e.getStatus());
        dto.setApprovedById(e.getApprovedById());
        dto.setApprovedAt(e.getApprovedAt());
        dto.setApprovalRemarks(e.getApprovalRemarks());
        dto.setRecommendationStatus(e.getRecommendationStatus());
        dto.setRecommendedById(e.getRecommendedById());
        dto.setRecommendationRemarks(e.getRecommendationRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    @Transactional
    @Override
    public OvertimeRequestDTO create(OvertimeRequestDTO dto) throws Exception {
        try {
            if (dto.getEmployeeId() == null || dto.getDateTimeFrom() == null || dto.getDateTimeTo() == null) {
                log.warn("OvertimeRequest creation rejected: missing required fields");
                return null;
            }
            if (!dto.getDateTimeTo().isAfter(dto.getDateTimeFrom())) {
                log.warn("OvertimeRequest creation rejected: dateTimeTo must be after dateTimeFrom");
                return null;
            }

            boolean emergency = Boolean.TRUE.equals(dto.getEmergencyPostFiling());
            if (dto.getDateTimeFrom().isBefore(LocalDateTime.now()) && !emergency) {
                throw new IllegalArgumentException("Overtime/Holiday Duty authority must be filed before the scheduled work. Use Emergency/Post-filing only for duly justified exceptional cases.");
            }
            if (emergency && (dto.getEmergencyJustification() == null || dto.getEmergencyJustification().isBlank())) {
                throw new IllegalArgumentException("Emergency/post-filing justification is required.");
            }
            int breakMinutes = dto.getBreakMinutes() != null ? Math.max(0, dto.getBreakMinutes()) : 0;
            double totalHours = Duration.between(dto.getDateTimeFrom(), dto.getDateTimeTo()).toMinutes() / 60.0;
            totalHours = Math.round(totalHours * 100.0) / 100.0;
            double netAuthorizedHours = Math.round(Math.max(0, totalHours - (breakMinutes / 60.0)) * 100.0) / 100.0;
            if (netAuthorizedHours <= 0) {
                log.warn("OvertimeRequest creation rejected: computed totalHours is zero or negative");
                return null;
            }

            OvertimeRequest entity = new OvertimeRequest();
            entity.setEmployeeId(dto.getEmployeeId());
            entity.setDateFiled(dto.getDateFiled() != null ? dto.getDateFiled() : dto.getDateTimeFrom().toLocalDate());
            entity.setDateTimeFrom(dto.getDateTimeFrom());
            entity.setDateTimeTo(dto.getDateTimeTo());
            entity.setTotalHours(totalHours);
            entity.setWorkType(dto.getWorkType() != null ? dto.getWorkType() : "REGULAR_OVERTIME");
            entity.setAuthorityReference(dto.getAuthorityReference());
            entity.setEmergencyPostFiling(emergency);
            entity.setEmergencyJustification(dto.getEmergencyJustification());
            entity.setBreakMinutes(breakMinutes);
            entity.setNetAuthorizedHours(netAuthorizedHours);
            entity.setPurpose(dto.getPurpose());
            entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "Pending");
            entity.setApprovedById(dto.getApprovedById());
            entity.setApprovalRemarks(dto.getApprovalRemarks());
            entity.setRecommendationStatus(dto.getRecommendationStatus());
            entity.setRecommendedById(dto.getRecommendedById());
            entity.setRecommendationRemarks(dto.getRecommendationRemarks());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

            entity = overtimeRequestRepository.save(entity);
            return toDTO(entity);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error creating OvertimeRequest for employeeId {}: ", dto.getEmployeeId(), ex);
            return null;
        }
    }

    @Override
    public List<OvertimeRequestDTO> getAll() throws Exception {
        return overtimeRequestRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OvertimeRequestDTO> getAllByEmployeeId(Long employeeId) throws Exception {
        return overtimeRequestRepository.findByEmployeeIdOrderByDateFiledDesc(employeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OvertimeRequestDTO> getPendingAll() throws Exception {
        return overtimeRequestRepository.findByStatusOrderByDateFiledDesc("Pending")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OvertimeRequestDTO> getApprovedByEmployeeId(Long employeeId) throws Exception {
        return overtimeRequestRepository.findByEmployeeIdAndStatusOrderByDateFiledDesc(employeeId, "Approved")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public OvertimeRequestDTO approve(Long overtimeRequestId, Long approvedById, String remarks) throws Exception {
        return updateStatus(overtimeRequestId, "Approved", approvedById, remarks);
    }

    @Transactional
    @Override
    public OvertimeRequestDTO disapprove(Long overtimeRequestId, Long approvedById, String remarks) throws Exception {
        return updateStatus(overtimeRequestId, "Disapproved", approvedById, remarks);
    }

    @Transactional
    @Override
    public OvertimeRequestDTO recommend(Long overtimeRequestId, Long recommendedById, String remarks) throws Exception {
        try {
            Optional<OvertimeRequest> optional = overtimeRequestRepository.findById(overtimeRequestId);
            if (optional.isEmpty()) return null;
            OvertimeRequest entity = optional.get();
            entity.setRecommendationStatus("Recommended");
            entity.setRecommendedById(recommendedById);
            entity.setRecommendationRemarks(remarks);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = overtimeRequestRepository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error recommending OvertimeRequest for id {}: ", overtimeRequestId, ex);
            return null;
        }
    }

    private OvertimeRequestDTO updateStatus(Long id, String newStatus, Long approvedById, String remarks) {
        try {
            Optional<OvertimeRequest> optional = overtimeRequestRepository.findById(id);
            if (optional.isEmpty()) return null;
            OvertimeRequest entity = optional.get();
            entity.setStatus(newStatus);
            entity.setApprovedById(approvedById);
            entity.setApprovedAt(LocalDateTime.now());
            entity.setApprovalRemarks(remarks);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = overtimeRequestRepository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error updating OvertimeRequest status for id {}: ", id, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public OvertimeRequestDTO update(Long overtimeRequestId, OvertimeRequestDTO dto) throws Exception {
        try {
            Optional<OvertimeRequest> optional = overtimeRequestRepository.findById(overtimeRequestId);
            if (optional.isEmpty()) return null;
            OvertimeRequest entity = optional.get();
            if (dto.getDateFiled() != null) entity.setDateFiled(dto.getDateFiled());
            if (dto.getDateTimeFrom() != null) entity.setDateTimeFrom(dto.getDateTimeFrom());
            if (dto.getDateTimeTo() != null) entity.setDateTimeTo(dto.getDateTimeTo());
            if (dto.getPurpose() != null) entity.setPurpose(dto.getPurpose());
            if (dto.getWorkType() != null) entity.setWorkType(dto.getWorkType());
            if (dto.getAuthorityReference() != null) entity.setAuthorityReference(dto.getAuthorityReference());
            if (dto.getEmergencyPostFiling() != null) entity.setEmergencyPostFiling(dto.getEmergencyPostFiling());
            if (dto.getEmergencyJustification() != null) entity.setEmergencyJustification(dto.getEmergencyJustification());
            if (dto.getBreakMinutes() != null) entity.setBreakMinutes(Math.max(0, dto.getBreakMinutes()));
            if (dto.getDateTimeFrom() != null && dto.getDateTimeTo() != null) {
                double raw = Duration.between(dto.getDateTimeFrom(), dto.getDateTimeTo()).toMinutes() / 60.0;
                int brk = entity.getBreakMinutes() != null ? entity.getBreakMinutes() : 0;
                entity.setNetAuthorizedHours(Math.round(Math.max(0, raw - brk / 60.0) * 100.0) / 100.0);
            }
            if (dto.getDateTimeFrom() != null && dto.getDateTimeTo() != null
                    && dto.getDateTimeTo().isAfter(dto.getDateTimeFrom())) {
                double totalHours = Duration.between(dto.getDateTimeFrom(), dto.getDateTimeTo()).toMinutes() / 60.0;
                entity.setTotalHours(Math.round(totalHours * 100.0) / 100.0);
            }
            if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
            if (dto.getApprovedById() != null) entity.setApprovedById(dto.getApprovedById());
            if (dto.getApprovalRemarks() != null) entity.setApprovalRemarks(dto.getApprovalRemarks());
            if (dto.getRecommendationStatus() != null) entity.setRecommendationStatus(dto.getRecommendationStatus());
            if (dto.getRecommendedById() != null) entity.setRecommendedById(dto.getRecommendedById());
            if (dto.getRecommendationRemarks() != null) entity.setRecommendationRemarks(dto.getRecommendationRemarks());
            entity.setUpdatedAt(LocalDateTime.now());
            entity = overtimeRequestRepository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error updating OvertimeRequest id {}: ", overtimeRequestId, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean delete(Long overtimeRequestId) throws Exception {
        try {
            Optional<OvertimeRequest> optional = overtimeRequestRepository.findById(overtimeRequestId);
            if (optional.isEmpty()) return false;
            overtimeRequestRepository.deleteById(overtimeRequestId);
            return true;
        } catch (Exception ex) {
            log.error("Error deleting OvertimeRequest id {}: ", overtimeRequestId, ex);
            return false;
        }
    }

    @Override
    public void generateOvertimeAuthorization(Long overtimeRequestId, OutputStream out) throws Exception {
        JasperReport report = compile("reports/OvertimeAuthorization.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("OVERTIME_REQUEST_ID", overtimeRequestId);

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
