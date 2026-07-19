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
        dto.setDutyShiftCode(e.getDutyShiftCode());
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
        return createInternal(dto, false);
    }

    @Transactional
    @Override
    public OvertimeRequestDTO createEmergencyOverride(OvertimeRequestDTO dto) throws Exception {
        return createInternal(dto, true);
    }

    private OvertimeRequestDTO createInternal(OvertimeRequestDTO dto,
                                              boolean emergencyOverride) throws Exception {
        try {
            if (dto.getEmployeeId() == null || dto.getDateTimeFrom() == null || dto.getDateTimeTo() == null) {
                log.warn("OvertimeRequest creation rejected: missing required fields");
                throw new IllegalArgumentException("Employee and inclusive date/time are required.");
            }
            if (!dto.getDateTimeTo().isAfter(dto.getDateTimeFrom())) {
                throw new IllegalArgumentException("Date/time To must be after Date/time From.");
            }

            boolean employeePostFiling = Boolean.TRUE.equals(dto.getEmergencyPostFiling());
            boolean postFilingAllowed = emergencyOverride || employeePostFiling;
            if (dto.getDateTimeFrom().isBefore(LocalDateTime.now()) && !postFilingAllowed) {
                throw new IllegalArgumentException("Overtime/Holiday Duty authority must be filed before the scheduled work. For duly justified work already rendered, select Emergency/Post-filing authority and provide the justification.");
            }
            if (postFilingAllowed && (dto.getEmergencyJustification() == null || dto.getEmergencyJustification().isBlank())) {
                throw new IllegalArgumentException("Emergency/post-filing justification is required.");
            }

            long totalMinutes = Duration.between(dto.getDateTimeFrom(), dto.getDateTimeTo()).toMinutes();
            int breakMinutes = validateManualBreakMinutes(dto.getBreakMinutes(), totalMinutes);
            double totalHours = roundHours(totalMinutes);
            double netAuthorizedHours = roundHours(totalMinutes - breakMinutes);
            if (netAuthorizedHours <= 0) {
                throw new IllegalArgumentException("Computed overtime duration must be greater than zero.");
            }

            OvertimeRequest entity = new OvertimeRequest();
            entity.setEmployeeId(dto.getEmployeeId());
            entity.setDateFiled(dto.getDateFiled() != null ? dto.getDateFiled() : dto.getDateTimeFrom().toLocalDate());
            entity.setDateTimeFrom(dto.getDateTimeFrom());
            entity.setDateTimeTo(dto.getDateTimeTo());
            entity.setTotalHours(totalHours);
            String workType = dto.getWorkType() != null ? dto.getWorkType() : "REGULAR_OVERTIME";
            entity.setWorkType(workType);
            entity.setDutyShiftCode(validateDutyShiftCode(workType, dto.getDutyShiftCode()));
            entity.setAuthorityReference(dto.getAuthorityReference());
            entity.setEmergencyPostFiling(postFilingAllowed);
            entity.setEmergencyJustification(postFilingAllowed ? dto.getEmergencyJustification().trim() : null);
            entity.setBreakMinutes(breakMinutes);
            entity.setNetAuthorizedHours(netAuthorizedHours);
            entity.setPurpose(dto.getPurpose());
            entity.setStatus(emergencyOverride && dto.getStatus() != null ? dto.getStatus() : "Pending");
            entity.setApprovedById(emergencyOverride ? dto.getApprovedById() : null);
            entity.setApprovedAt(emergencyOverride && dto.getApprovedById() != null ? LocalDateTime.now() : null);
            entity.setApprovalRemarks(emergencyOverride ? dto.getApprovalRemarks() : null);
            entity.setRecommendationStatus(emergencyOverride ? dto.getRecommendationStatus() : "Pending");
            entity.setRecommendedById(emergencyOverride ? dto.getRecommendedById() : null);
            entity.setRecommendationRemarks(emergencyOverride ? dto.getRecommendationRemarks() : null);
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

    private int validateManualBreakMinutes(Integer requestedBreakMinutes, long totalMinutes) {
        int breakMinutes = requestedBreakMinutes == null ? 0 : requestedBreakMinutes;
        if (breakMinutes < 0 || breakMinutes >= totalMinutes) {
            throw new IllegalArgumentException("Break minutes must be zero or greater and less than the requested duration.");
        }
        return breakMinutes;
    }

    private String validateDutyShiftCode(String workType, String dutyShiftCode) {
        boolean specialDuty = "HOLIDAY_DUTY".equalsIgnoreCase(workType)
                || "DAY_OFF_DUTY".equalsIgnoreCase(workType)
                || "REST_DAY_DUTY".equalsIgnoreCase(workType);
        String normalized = dutyShiftCode == null ? null : dutyShiftCode.trim();
        if (specialDuty && (normalized == null || normalized.isEmpty())) {
            throw new IllegalArgumentException("A configured Duty Shift is required for Holiday, Rest-Day, or Scheduled Day-Off duty.");
        }
        return specialDuty ? normalized : null;
    }

    private double roundHours(long minutes) {
        return Math.round((minutes / 60.0) * 100.0) / 100.0;
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
    public OvertimeRequestDTO recommend(Long overtimeRequestId, Long recommendedById, String remarks,
                                        String dutyShiftCode, Integer requestedBreakMinutes) throws Exception {
        OvertimeRequest entity = overtimeRequestRepository.findById(overtimeRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Overtime request not found."));
        if (!"Pending".equalsIgnoreCase(entity.getStatus())) {
            throw new IllegalStateException("Only pending overtime requests may be recommended.");
        }
        if (recommendedById == null) {
            throw new IllegalArgumentException("The IS recommending officer is required.");
        }
        entity.setDutyShiftCode(validateDutyShiftCode(
                entity.getWorkType(),
                dutyShiftCode != null ? dutyShiftCode : entity.getDutyShiftCode()
        ));
        if (requestedBreakMinutes != null) {
            long totalMinutes = Duration.between(entity.getDateTimeFrom(), entity.getDateTimeTo()).toMinutes();
            int breakMinutes = validateManualBreakMinutes(requestedBreakMinutes, totalMinutes);
            entity.setBreakMinutes(breakMinutes);
            entity.setNetAuthorizedHours(roundHours(totalMinutes - breakMinutes));
        }
        entity.setRecommendationStatus("Recommended");
        entity.setRecommendedById(recommendedById);
        entity.setRecommendationRemarks(remarks);
        entity.setUpdatedAt(LocalDateTime.now());
        return toDTO(overtimeRequestRepository.save(entity));
    }

    private OvertimeRequestDTO updateStatus(Long id, String newStatus, Long approvedById, String remarks) {
        OvertimeRequest entity = overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Overtime request not found."));
        if (!"Recommended".equalsIgnoreCase(entity.getRecommendationStatus())) {
            throw new IllegalStateException("The IS recommendation must be completed before the final decision.");
        }
        entity.setDutyShiftCode(validateDutyShiftCode(entity.getWorkType(), entity.getDutyShiftCode()));
        if (!"Pending".equalsIgnoreCase(entity.getStatus())) {
            throw new IllegalStateException("A final decision has already been recorded for this overtime request.");
        }
        if (approvedById == null) {
            throw new IllegalArgumentException("The final approving officer is required.");
        }
        entity.setStatus(newStatus);
        entity.setApprovedById(approvedById);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setApprovalRemarks(remarks);
        entity.setUpdatedAt(LocalDateTime.now());
        return toDTO(overtimeRequestRepository.save(entity));
    }

    @Transactional
    @Override
    public OvertimeRequestDTO update(Long overtimeRequestId, OvertimeRequestDTO dto) throws Exception {
        try {
            Optional<OvertimeRequest> optional = overtimeRequestRepository.findById(overtimeRequestId);
            if (optional.isEmpty()) return null;
            OvertimeRequest entity = optional.get();

            if (!"Pending".equalsIgnoreCase(entity.getStatus())
                    || "Recommended".equalsIgnoreCase(entity.getRecommendationStatus())) {
                throw new IllegalStateException("Only a pending overtime request that has not entered approval may be edited.");
            }

            if (dto.getDateTimeFrom() != null) entity.setDateTimeFrom(dto.getDateTimeFrom());
            if (dto.getDateTimeTo() != null) entity.setDateTimeTo(dto.getDateTimeTo());
            if (entity.getDateTimeFrom() == null || entity.getDateTimeTo() == null
                    || !entity.getDateTimeTo().isAfter(entity.getDateTimeFrom())) {
                throw new IllegalArgumentException("Date/time To must be after Date/time From.");
            }
            boolean postFilingRequested = dto.getEmergencyPostFiling() != null
                    ? Boolean.TRUE.equals(dto.getEmergencyPostFiling())
                    : Boolean.TRUE.equals(entity.getEmergencyPostFiling());
            if (!postFilingRequested && entity.getDateTimeFrom().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("A normal overtime request cannot be changed to a past start time.");
            }
            if (postFilingRequested
                    && (dto.getEmergencyJustification() == null || dto.getEmergencyJustification().isBlank())
                    && (entity.getEmergencyJustification() == null || entity.getEmergencyJustification().isBlank())) {
                throw new IllegalArgumentException("Emergency/post-filing justification is required.");
            }

            if (dto.getPurpose() != null) entity.setPurpose(dto.getPurpose());
            if (dto.getWorkType() != null) entity.setWorkType(dto.getWorkType());
            entity.setDutyShiftCode(validateDutyShiftCode(entity.getWorkType(), dto.getDutyShiftCode()));
            if (dto.getAuthorityReference() != null) entity.setAuthorityReference(dto.getAuthorityReference());
            entity.setEmergencyPostFiling(postFilingRequested);
            entity.setEmergencyJustification(postFilingRequested
                    ? (dto.getEmergencyJustification() != null
                        ? dto.getEmergencyJustification().trim()
                        : entity.getEmergencyJustification())
                    : null);

            long totalMinutes = Duration.between(entity.getDateTimeFrom(), entity.getDateTimeTo()).toMinutes();
            int breakMinutes = validateManualBreakMinutes(
                    dto.getBreakMinutes() != null ? dto.getBreakMinutes() : entity.getBreakMinutes(),
                    totalMinutes
            );
            entity.setBreakMinutes(breakMinutes);
            entity.setTotalHours(roundHours(totalMinutes));
            entity.setNetAuthorizedHours(roundHours(totalMinutes - breakMinutes));

            entity.setUpdatedAt(LocalDateTime.now());
            entity = overtimeRequestRepository.save(entity);
            return toDTO(entity);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error updating OvertimeRequest id {}: ", overtimeRequestId, ex);
            return null;
        }
    }

    /**
     * HRM maintenance edit. Request details and the administrator-selected
     * workflow values are updated regardless of current status or whether the
     * duty date is already in the past.
     */
    @Transactional
    @Override
    public OvertimeRequestDTO administrativeUpdate(Long overtimeRequestId,
                                                   OvertimeRequestDTO dto) throws Exception {
        try {
            Optional<OvertimeRequest> optional = overtimeRequestRepository.findById(overtimeRequestId);
            if (optional.isEmpty()) return null;
            OvertimeRequest entity = optional.get();

            if (dto.getDateTimeFrom() != null) entity.setDateTimeFrom(dto.getDateTimeFrom());
            if (dto.getDateTimeTo() != null) entity.setDateTimeTo(dto.getDateTimeTo());
            if (entity.getDateTimeFrom() == null || entity.getDateTimeTo() == null
                    || !entity.getDateTimeTo().isAfter(entity.getDateTimeFrom())) {
                throw new IllegalArgumentException("Date/time To must be after Date/time From.");
            }

            if (dto.getPurpose() != null) entity.setPurpose(dto.getPurpose());
            if (dto.getWorkType() != null) entity.setWorkType(dto.getWorkType());
            entity.setDutyShiftCode(validateDutyShiftCode(entity.getWorkType(),
                    dto.getDutyShiftCode() != null ? dto.getDutyShiftCode() : entity.getDutyShiftCode()));
            if (dto.getAuthorityReference() != null) entity.setAuthorityReference(dto.getAuthorityReference());

            boolean postFilingRequested = dto.getEmergencyPostFiling() != null
                    ? Boolean.TRUE.equals(dto.getEmergencyPostFiling())
                    : Boolean.TRUE.equals(entity.getEmergencyPostFiling());
            String postFilingJustification = dto.getEmergencyJustification() != null
                    ? dto.getEmergencyJustification().trim()
                    : entity.getEmergencyJustification();
            if (postFilingRequested && (postFilingJustification == null || postFilingJustification.isBlank())) {
                throw new IllegalArgumentException("Emergency/post-filing justification is required.");
            }
            entity.setEmergencyPostFiling(postFilingRequested);
            entity.setEmergencyJustification(postFilingRequested ? postFilingJustification : null);

            long totalMinutes = Duration.between(entity.getDateTimeFrom(), entity.getDateTimeTo()).toMinutes();
            int breakMinutes = validateManualBreakMinutes(
                    dto.getBreakMinutes() != null ? dto.getBreakMinutes() : entity.getBreakMinutes(),
                    totalMinutes
            );
            entity.setBreakMinutes(breakMinutes);
            entity.setTotalHours(roundHours(totalMinutes));
            entity.setNetAuthorizedHours(roundHours(totalMinutes - breakMinutes));
            if (entity.getNetAuthorizedHours() <= 0) {
                throw new IllegalArgumentException("Computed overtime duration must be greater than zero.");
            }

            applyAdministrativeWorkflow(entity, dto);
            entity.setUpdatedAt(LocalDateTime.now());
            return toDTO(overtimeRequestRepository.save(entity));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error administratively updating OvertimeRequest id {}: ", overtimeRequestId, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean delete(Long overtimeRequestId) throws Exception {
        try {
            Optional<OvertimeRequest> optional = overtimeRequestRepository.findById(overtimeRequestId);
            if (optional.isEmpty()) return false;
            OvertimeRequest entity = optional.get();
            if (!"Pending".equalsIgnoreCase(entity.getStatus())
                    || "Recommended".equalsIgnoreCase(entity.getRecommendationStatus())) {
                throw new IllegalStateException("Only a pending overtime request that has not entered approval may be deleted.");
            }
            overtimeRequestRepository.deleteById(overtimeRequestId);
            return true;
        } catch (Exception ex) {
            log.error("Error deleting OvertimeRequest id {}: ", overtimeRequestId, ex);
            return false;
        }
    }

    @Transactional
    @Override
    public Boolean administrativeDelete(Long overtimeRequestId) throws Exception {
        if (!overtimeRequestRepository.existsById(overtimeRequestId)) return false;
        overtimeRequestRepository.deleteById(overtimeRequestId);
        return true;
    }

    /**
     * HRM is the administrative maintenance surface, so its selected workflow
     * values are authoritative even when a record already has a final decision.
     */
    private void applyAdministrativeWorkflow(OvertimeRequest entity, OvertimeRequestDTO dto) {
        String previousStatus = entity.getStatus();
        String finalStatus = normalizeFinalStatus(dto.getStatus(), previousStatus);
        String recommendationStatus = normalizeRecommendationStatus(
                dto.getRecommendationStatus(), entity.getRecommendationStatus());

        entity.setStatus(finalStatus);
        entity.setRecommendationStatus(recommendationStatus);
        entity.setRecommendationRemarks(dto.getRecommendationRemarks());
        entity.setApprovalRemarks(dto.getApprovalRemarks());

        if ("Pending".equalsIgnoreCase(recommendationStatus)) {
            entity.setRecommendedById(null);
        } else {
            entity.setRecommendedById(dto.getRecommendedById());
        }

        if ("Pending".equalsIgnoreCase(finalStatus)) {
            entity.setApprovedById(null);
            entity.setApprovedAt(null);
        } else {
            entity.setApprovedById(dto.getApprovedById());
            if (entity.getApprovedAt() == null
                    || previousStatus == null
                    || !finalStatus.equalsIgnoreCase(previousStatus)) {
                entity.setApprovedAt(LocalDateTime.now());
            }
        }
    }

    private String normalizeFinalStatus(String requested, String current) {
        if (requested == null || requested.isBlank()) return current != null ? current : "Pending";
        return switch (requested.trim().toLowerCase()) {
            case "pending" -> "Pending";
            case "approved" -> "Approved";
            case "disapproved" -> "Disapproved";
            case "cancel", "canceled", "cancelled" -> "Cancelled";
            default -> throw new IllegalArgumentException("Unsupported final status: " + requested);
        };
    }

    private String normalizeRecommendationStatus(String requested, String current) {
        if (requested == null || requested.isBlank()) return current != null ? current : "Pending";
        return switch (requested.trim().toLowerCase()) {
            case "pending" -> "Pending";
            case "approved" -> "Approved";
            case "recommended" -> "Recommended";
            case "disapproved" -> "Disapproved";
            case "cancel", "canceled", "cancelled" -> "Cancelled";
            default -> throw new IllegalArgumentException("Unsupported recommendation status: " + requested);
        };
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
