package com.humanresource.impl;

import com.humanresource.dtos.OvertimeRequestDTO;
import com.humanresource.dtos.StaffOvertimeRequestDTO;
import com.humanresource.dtos.StaffOvertimeParticipantDTO;
import com.humanresource.entitymodels.OvertimeRequest;
import com.humanresource.repositories.OvertimeRequestRepository;
import com.humanresource.entitymodels.ReportHeaderSettings;
import com.humanresource.repositories.ReportHeaderSettingsRepository;
import com.humanresource.reports.JasperReportRegistry;
import com.humanresource.services.OvertimeRequestService;
import com.humanresource.integration.administrative.StaffOvertimeAuthorizationClient;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OvertimeRequestImpl implements OvertimeRequestService {

    private static final Logger log = LoggerFactory.getLogger(OvertimeRequestImpl.class);
    private final OvertimeRequestRepository overtimeRequestRepository;
    private final ReportHeaderSettingsRepository reportHeaderSettingsRepository;
    private final DataSource dataSource;
    private final StaffOvertimeAuthorizationClient staffAuthorizationClient;

    public OvertimeRequestImpl(OvertimeRequestRepository overtimeRequestRepository,
                               ReportHeaderSettingsRepository reportHeaderSettingsRepository,
                               DataSource dataSource,
                               StaffOvertimeAuthorizationClient staffAuthorizationClient) {
        this.overtimeRequestRepository = overtimeRequestRepository;
        this.reportHeaderSettingsRepository = reportHeaderSettingsRepository;
        this.dataSource = dataSource;
        this.staffAuthorizationClient = staffAuthorizationClient;
    }

    private OvertimeRequestDTO toDTO(OvertimeRequest e) {
        OvertimeRequestDTO dto = new OvertimeRequestDTO();
        dto.setOvertimeRequestId(e.getOvertimeRequestId());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setGroupRequestId(e.getGroupRequestId());
        dto.setFiledByEmployeeId(e.getFiledByEmployeeId());
        dto.setBusinessUnitId(e.getBusinessUnitId());
        dto.setSupervisorFiled(e.getSupervisorFiled());
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
        dto.setExpectedOutput(e.getExpectedOutput());
        dto.setDiscrepancyRemarks(e.getDiscrepancyRemarks());
        dto.setDiscrepancyReportedAt(e.getDiscrepancyReportedAt());
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
            entity.setFiledByEmployeeId(dto.getEmployeeId());
            entity.setSupervisorFiled(false);
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
            entity.setExpectedOutput(dto.getExpectedOutput());
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

    @Transactional
    @Override
    public OvertimeRequestDTO createStaffRequest(StaffOvertimeRequestDTO dto,
                                                 Long filedByEmployeeId,
                                                 String bearerToken) throws Exception {
        if (dto == null || dto.getBusinessUnitId() == null
                || dto.getDateTimeFrom() == null || dto.getDateTimeTo() == null) {
            throw new IllegalArgumentException("Business Unit and inclusive overtime dates are required.");
        }
        if (dto.getParticipants() == null || dto.getParticipants().isEmpty()) {
            throw new IllegalArgumentException("Select at least one staff member.");
        }
        if (dto.getExpectedOutput() == null || dto.getExpectedOutput().isBlank()) {
            throw new IllegalArgumentException("Expected output is required for Staff Overtime.");
        }
        var unit = staffAuthorizationClient.requireAuthorizedUnit(
                bearerToken, dto.getBusinessUnitId(),
                dto.getDateTimeFrom().toLocalDate(), dto.getDateTimeTo().toLocalDate());
        List<StaffOvertimeAuthorizationClient.ApprovalStep> approvalRoute =
                staffAuthorizationClient.requireOvertimeApprovalRoute(bearerToken, dto.getBusinessUnitId());
        int firstApprovalLevel = approvalRoute.stream()
                .mapToInt(StaffOvertimeAuthorizationClient.ApprovalStep::approvalLevel)
                .min().orElseThrow();
        boolean filerIsFirstApprover = approvalRoute.stream().anyMatch(step ->
                step.approvalLevel() == firstApprovalLevel
                        && filedByEmployeeId.equals(step.employeeId()));
        List<Long> participantIds = dto.getParticipants().stream()
                .map(StaffOvertimeParticipantDTO::getEmployeeId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (participantIds.isEmpty()) throw new IllegalArgumentException("Select at least one staff member.");
        if (participantIds.contains(filedByEmployeeId)) {
            throw new IllegalArgumentException("Use My Requests when filing your own overtime.");
        }
        if (!unit.personnelEmployeeIds().containsAll(participantIds)) {
            throw new IllegalArgumentException("Every selected employee must be designated in the selected Business Unit.");
        }

        String groupId = UUID.randomUUID().toString();
        OvertimeRequestDTO first = null;
        Map<Long, Integer> breakMinutesByEmployee = dto.getParticipants().stream()
                .filter(participant -> participant.getEmployeeId() != null)
                .collect(Collectors.toMap(
                        StaffOvertimeParticipantDTO::getEmployeeId,
                        participant -> participant.getBreakMinutes() == null ? 0 : participant.getBreakMinutes(),
                        (firstValue, ignoredDuplicate) -> firstValue,
                        LinkedHashMap::new));
        for (Long participantId : participantIds) {
            OvertimeRequestDTO row = new OvertimeRequestDTO();
            row.setEmployeeId(participantId);
            row.setDateFiled(dto.getDateFiled());
            row.setDateTimeFrom(dto.getDateTimeFrom());
            row.setDateTimeTo(dto.getDateTimeTo());
            row.setWorkType(dto.getWorkType());
            row.setDutyShiftCode(dto.getDutyShiftCode());
            row.setAuthorityReference(dto.getAuthorityReference());
            row.setEmergencyPostFiling(dto.getEmergencyPostFiling());
            row.setEmergencyJustification(dto.getEmergencyJustification());
            row.setBreakMinutes(breakMinutesByEmployee.get(participantId));
            row.setPurpose(dto.getPurpose());
            row.setExpectedOutput(dto.getExpectedOutput());
            OvertimeRequestDTO created = createInternal(row, false);
            if (created == null) throw new IllegalStateException("Unable to create a Staff Overtime participant record.");
            OvertimeRequest entity = overtimeRequestRepository.findById(created.getOvertimeRequestId())
                    .orElseThrow();
            entity.setGroupRequestId(groupId);
            entity.setFiledByEmployeeId(filedByEmployeeId);
            entity.setBusinessUnitId(dto.getBusinessUnitId());
            entity.setSupervisorFiled(true);
            entity.setExpectedOutput(dto.getExpectedOutput().trim());
            entity.setRecommendationStatus(filerIsFirstApprover ? "Recommended" : "Pending");
            entity.setRecommendedById(filerIsFirstApprover ? filedByEmployeeId : null);
            entity.setRecommendationRemarks(filerIsFirstApprover
                    ? "Filed by the effective Head/OIC who is also the configured Level 1 approver."
                    : null);
            entity = overtimeRequestRepository.save(entity);
            if (first == null) first = toDTO(entity);
        }
        if (first != null) first.setParticipantEmployeeIds(participantIds);
        return first;
    }

    @Transactional
    @Override
    public OvertimeRequestDTO updateStaffRequest(String groupRequestId, StaffOvertimeRequestDTO dto,
                                                 Long filedByEmployeeId, String bearerToken) throws Exception {
        List<OvertimeRequest> existing = requireStaffGroup(groupRequestId);
        if (!existing.get(0).getFiledByEmployeeId().equals(filedByEmployeeId)) {
            throw new IllegalArgumentException("Only the supervisor who filed this Staff Overtime request may edit it.");
        }
        List<StaffOvertimeAuthorizationClient.ApprovalStep> approvalRoute =
                validateStaffCommand(dto, filedByEmployeeId, bearerToken);
        requireSupervisorEditable(existing, approvalRoute);

        Map<Long, Integer> breakMinutesByEmployee = dto.getParticipants().stream()
                .filter(participant -> participant.getEmployeeId() != null)
                .collect(Collectors.toMap(
                        StaffOvertimeParticipantDTO::getEmployeeId,
                        participant -> participant.getBreakMinutes() == null ? 0 : participant.getBreakMinutes(),
                        (firstValue, ignoredDuplicate) -> firstValue,
                        LinkedHashMap::new));
        Set<Long> requestedIds = new HashSet<>(breakMinutesByEmployee.keySet());
        Map<Long, OvertimeRequest> existingByEmployee = existing.stream()
                .collect(Collectors.toMap(OvertimeRequest::getEmployeeId, row -> row));

        List<OvertimeRequest> removed = existing.stream()
                .filter(row -> !requestedIds.contains(row.getEmployeeId()))
                .toList();
        if (!removed.isEmpty()) overtimeRequestRepository.deleteAll(removed);

        List<OvertimeRequest> updated = new java.util.ArrayList<>();
        for (Long employeeId : requestedIds) {
            OvertimeRequest row = existingByEmployee.get(employeeId);
            if (row == null) {
                row = new OvertimeRequest();
                row.setEmployeeId(employeeId);
                row.setGroupRequestId(groupRequestId);
                row.setFiledByEmployeeId(filedByEmployeeId);
                row.setBusinessUnitId(dto.getBusinessUnitId());
                row.setSupervisorFiled(true);
                row.setStatus("Pending");
                row.setRecommendationStatus("Pending");
                row.setCreatedAt(LocalDateTime.now());
            } else if (isLegacyInvalidAutoRecommendation(row, approvalRoute)) {
                row.setRecommendationStatus("Pending");
                row.setRecommendedById(null);
                row.setRecommendationRemarks(null);
            }
            applyStaffFields(row, dto, breakMinutesByEmployee.get(employeeId));
            updated.add(row);
        }
        List<OvertimeRequest> saved = overtimeRequestRepository.saveAll(updated);
        return toStaffGroupDTO(saved);
    }

    @Transactional
    @Override
    public Boolean deleteStaffRequest(String groupRequestId, Long filedByEmployeeId,
                                      String bearerToken) {
        List<OvertimeRequest> rows = requireStaffGroup(groupRequestId);
        if (!rows.get(0).getFiledByEmployeeId().equals(filedByEmployeeId)) {
            throw new IllegalArgumentException("Only the supervisor who filed this Staff Overtime request may delete it.");
        }
        List<StaffOvertimeAuthorizationClient.ApprovalStep> approvalRoute =
                staffAuthorizationClient.requireOvertimeApprovalRoute(
                        bearerToken, rows.get(0).getBusinessUnitId());
        requireSupervisorEditable(rows, approvalRoute);
        overtimeRequestRepository.deleteAll(rows);
        return true;
    }

    private List<StaffOvertimeAuthorizationClient.ApprovalStep> validateStaffCommand(
            StaffOvertimeRequestDTO dto, Long filedByEmployeeId, String bearerToken) {
        if (dto == null || dto.getBusinessUnitId() == null || dto.getDateTimeFrom() == null
                || dto.getDateTimeTo() == null || !dto.getDateTimeTo().isAfter(dto.getDateTimeFrom())) {
            throw new IllegalArgumentException("Business Unit and valid inclusive overtime dates are required.");
        }
        if (dto.getParticipants() == null || dto.getParticipants().isEmpty()) {
            throw new IllegalArgumentException("Select at least one staff member.");
        }
        if (dto.getExpectedOutput() == null || dto.getExpectedOutput().isBlank()) {
            throw new IllegalArgumentException("Expected output is required for Staff Overtime.");
        }
        boolean postFiling = Boolean.TRUE.equals(dto.getEmergencyPostFiling());
        if (dto.getDateTimeFrom().isBefore(LocalDateTime.now()) && !postFiling) {
            throw new IllegalArgumentException("Overtime/Holiday Duty authority must be filed before the scheduled work.");
        }
        if (postFiling && (dto.getEmergencyJustification() == null || dto.getEmergencyJustification().isBlank())) {
            throw new IllegalArgumentException("Emergency/post-filing justification is required.");
        }
        var unit = staffAuthorizationClient.requireAuthorizedUnit(
                bearerToken, dto.getBusinessUnitId(), dto.getDateTimeFrom().toLocalDate(),
                dto.getDateTimeTo().toLocalDate());
        List<Long> participantIds = dto.getParticipants().stream()
                .map(StaffOvertimeParticipantDTO::getEmployeeId)
                .filter(java.util.Objects::nonNull).distinct().toList();
        if (participantIds.isEmpty() || participantIds.contains(filedByEmployeeId)) {
            throw new IllegalArgumentException("Select at least one staff member and use My Requests for your own overtime.");
        }
        if (!unit.personnelEmployeeIds().containsAll(participantIds)) {
            throw new IllegalArgumentException("Every selected employee must be designated in the selected Business Unit.");
        }
        return staffAuthorizationClient.requireOvertimeApprovalRoute(
                bearerToken, dto.getBusinessUnitId());
    }

    private void applyStaffFields(OvertimeRequest row, StaffOvertimeRequestDTO dto,
                                  Integer requestedBreakMinutes) {
        long totalMinutes = Duration.between(dto.getDateTimeFrom(), dto.getDateTimeTo()).toMinutes();
        int breakMinutes = validateManualBreakMinutes(requestedBreakMinutes, totalMinutes);
        row.setBusinessUnitId(dto.getBusinessUnitId());
        row.setDateFiled(dto.getDateFiled() != null ? dto.getDateFiled() : dto.getDateTimeFrom().toLocalDate());
        row.setDateTimeFrom(dto.getDateTimeFrom());
        row.setDateTimeTo(dto.getDateTimeTo());
        row.setTotalHours(roundHours(totalMinutes));
        row.setNetAuthorizedHours(roundHours(totalMinutes - breakMinutes));
        row.setBreakMinutes(breakMinutes);
        row.setWorkType(dto.getWorkType() == null ? "REGULAR_OVERTIME" : dto.getWorkType());
        row.setDutyShiftCode(validateDutyShiftCode(row.getWorkType(), dto.getDutyShiftCode()));
        row.setAuthorityReference(dto.getAuthorityReference());
        row.setEmergencyPostFiling(Boolean.TRUE.equals(dto.getEmergencyPostFiling()));
        row.setEmergencyJustification(Boolean.TRUE.equals(dto.getEmergencyPostFiling())
                ? dto.getEmergencyJustification().trim() : null);
        row.setPurpose(dto.getPurpose());
        row.setExpectedOutput(dto.getExpectedOutput().trim());
        row.setUpdatedAt(LocalDateTime.now());
    }

    private List<OvertimeRequest> requireStaffGroup(String groupRequestId) {
        if (groupRequestId == null || groupRequestId.isBlank()) {
            throw new IllegalArgumentException("Staff Overtime group ID is required.");
        }
        List<OvertimeRequest> rows = overtimeRequestRepository
                .findByGroupRequestIdOrderByOvertimeRequestIdAsc(groupRequestId);
        if (rows.isEmpty() || rows.stream().anyMatch(row -> !Boolean.TRUE.equals(row.getSupervisorFiled()))) {
            throw new IllegalArgumentException("Staff Overtime group was not found.");
        }
        return rows;
    }

    private void requireSupervisorEditable(
            List<OvertimeRequest> rows,
            List<StaffOvertimeAuthorizationClient.ApprovalStep> approvalRoute) {
        boolean editable = rows.stream().allMatch(row ->
                "Pending".equalsIgnoreCase(row.getStatus())
                        && (isRecommendationPending(row.getRecommendationStatus())
                        || isLegacyInvalidAutoRecommendation(row, approvalRoute)));
        if (!editable) {
            throw new IllegalStateException(
                    "Staff Overtime may be modified by the supervisor only while pending and not yet recommended.");
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
        List<OvertimeRequest> records = overtimeRequestRepository.findAll();
        Map<String, List<Long>> participantsByGroup = records.stream()
                .filter(record -> record.getGroupRequestId() != null)
                .collect(Collectors.groupingBy(
                        OvertimeRequest::getGroupRequestId,
                        Collectors.mapping(OvertimeRequest::getEmployeeId, Collectors.toList())));
        return records.stream().map(record -> {
            OvertimeRequestDTO dto = toDTO(record);
            if (record.getGroupRequestId() != null) {
                dto.setParticipantEmployeeIds(participantsByGroup.get(record.getGroupRequestId()));
                String discrepancies = records.stream()
                        .filter(candidate -> record.getGroupRequestId().equals(candidate.getGroupRequestId()))
                        .filter(candidate -> candidate.getDiscrepancyRemarks() != null && !candidate.getDiscrepancyRemarks().isBlank())
                        .map(candidate -> "Employee #" + candidate.getEmployeeId() + ": " + candidate.getDiscrepancyRemarks())
                        .collect(Collectors.joining(" | "));
                dto.setGroupDiscrepancySummary(discrepancies.isBlank() ? null : discrepancies);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<OvertimeRequestDTO> getAllByEmployeeId(Long employeeId) throws Exception {
        return overtimeRequestRepository.findByEmployeeIdOrderByDateFiledDesc(employeeId)
                .stream().map(record -> record.getGroupRequestId() == null
                        ? toDTO(record)
                        : toStaffGroupDTO(overtimeRequestRepository
                                .findByGroupRequestIdOrderByOvertimeRequestIdAsc(record.getGroupRequestId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<OvertimeRequestDTO> getStaffRequestsFiledBy(Long employeeId) throws Exception {
        Map<String, List<OvertimeRequest>> groups = overtimeRequestRepository
                .findByFiledByEmployeeIdAndSupervisorFiledTrueOrderByDateFiledDesc(employeeId)
                .stream().collect(Collectors.groupingBy(
                        OvertimeRequest::getGroupRequestId, LinkedHashMap::new, Collectors.toList()));
        return groups.values().stream().map(rows -> {
            return toStaffGroupDTO(rows);
        }).toList();
    }

    private OvertimeRequestDTO toStaffGroupDTO(List<OvertimeRequest> rows) {
        OvertimeRequestDTO result = toDTO(rows.get(0));
        result.setParticipantEmployeeIds(rows.stream().map(OvertimeRequest::getEmployeeId).toList());
        String discrepancies = rows.stream()
                .filter(row -> row.getDiscrepancyRemarks() != null && !row.getDiscrepancyRemarks().isBlank())
                .map(row -> "Employee #" + row.getEmployeeId() + ": " + row.getDiscrepancyRemarks())
                .collect(Collectors.joining(" | "));
        result.setGroupDiscrepancySummary(discrepancies.isBlank() ? null : discrepancies);
        result.setDiscrepancyReportedAt(rows.stream()
                .map(OvertimeRequest::getDiscrepancyReportedAt)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        return result;
    }

    @Transactional
    @Override
    public OvertimeRequestDTO reportDiscrepancy(Long overtimeRequestId, Long employeeId, String remarks) {
        OvertimeRequest record = overtimeRequestRepository.findById(overtimeRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Staff Overtime assignment was not found."));
        if (!Boolean.TRUE.equals(record.getSupervisorFiled()) || !record.getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("Only the assigned employee may report a discrepancy for Staff Overtime.");
        }
        if (remarks == null || remarks.isBlank()) {
            throw new IllegalArgumentException("Describe the incorrect date, time, or assignment.");
        }
        record.setDiscrepancyRemarks(remarks.trim());
        record.setDiscrepancyReportedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        return toDTO(overtimeRequestRepository.save(record));
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
    public OvertimeRequestDTO approve(Long overtimeRequestId, Long approvedById, String remarks,
                                      String bearerToken) throws Exception {
        return updateStatus(overtimeRequestId, "Approved", approvedById, remarks, bearerToken);
    }

    @Transactional
    @Override
    public OvertimeRequestDTO disapprove(Long overtimeRequestId, Long approvedById, String remarks,
                                         String bearerToken) throws Exception {
        OvertimeRequest entity = overtimeRequestRepository.findById(overtimeRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Overtime request not found."));
        if (Boolean.TRUE.equals(entity.getSupervisorFiled())) {
            List<StaffOvertimeAuthorizationClient.ApprovalStep> route =
                    staffAuthorizationClient.requireOvertimeApprovalRoute(bearerToken, entity.getBusinessUnitId());
            int firstLevel = route.stream().mapToInt(StaffOvertimeAuthorizationClient.ApprovalStep::approvalLevel)
                    .min().orElseThrow();
            int finalLevel = route.stream().mapToInt(StaffOvertimeAuthorizationClient.ApprovalStep::approvalLevel)
                    .max().orElseThrow();
            int actorLevel = requireActorLevel(route, approvedById);
            if (actorLevel == firstLevel && firstLevel < finalLevel) {
                List<OvertimeRequest> rows = requireStaffGroup(entity.getGroupRequestId());
                for (OvertimeRequest row : rows) {
                    if (!"Pending".equalsIgnoreCase(row.getStatus())
                            || (!isRecommendationPending(row.getRecommendationStatus())
                            && !isLegacyInvalidAutoRecommendation(row, route))) {
                        throw new IllegalStateException("Only a pending, unrecommended Staff Overtime request may be disapproved by Level 1.");
                    }
                    row.setRecommendationStatus("Disapproved");
                    row.setRecommendedById(approvedById);
                    row.setRecommendationRemarks(remarks);
                    row.setUpdatedAt(LocalDateTime.now());
                }
                return toStaffGroupDTO(overtimeRequestRepository.saveAll(rows));
            }
        }
        return updateStatus(overtimeRequestId, "Disapproved", approvedById, remarks, bearerToken);
    }

    @Transactional
    @Override
    public OvertimeRequestDTO recommend(Long overtimeRequestId, Long recommendedById, String remarks,
                                        String dutyShiftCode, Integer requestedBreakMinutes,
                                        String bearerToken) throws Exception {
        OvertimeRequest entity = overtimeRequestRepository.findById(overtimeRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Overtime request not found."));
        if (Boolean.TRUE.equals(entity.getSupervisorFiled())) {
            List<StaffOvertimeAuthorizationClient.ApprovalStep> route =
                    staffAuthorizationClient.requireOvertimeApprovalRoute(bearerToken, entity.getBusinessUnitId());
            int firstLevel = route.stream().mapToInt(StaffOvertimeAuthorizationClient.ApprovalStep::approvalLevel)
                    .min().orElseThrow();
            if (requireActorLevel(route, recommendedById) != firstLevel) {
                throw new IllegalStateException("Only the configured Level 1 approver may recommend this Staff Overtime request.");
            }
            List<OvertimeRequest> rows = requireStaffGroup(entity.getGroupRequestId());
            for (OvertimeRequest row : rows) {
                if (!"Pending".equalsIgnoreCase(row.getStatus())
                        || (!isRecommendationPending(row.getRecommendationStatus())
                        && !isLegacyInvalidAutoRecommendation(row, route))) {
                    throw new IllegalStateException("Only a pending, unrecommended Staff Overtime request may be recommended.");
                }
                row.setRecommendationStatus("Recommended");
                row.setRecommendedById(recommendedById);
                row.setRecommendationRemarks(remarks);
                row.setUpdatedAt(LocalDateTime.now());
            }
            return toStaffGroupDTO(overtimeRequestRepository.saveAll(rows));
        }
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

    private OvertimeRequestDTO updateStatus(Long id, String newStatus, Long approvedById, String remarks,
                                            String bearerToken) {
        OvertimeRequest entity = overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Overtime request not found."));
        if (approvedById == null) {
            throw new IllegalArgumentException("The final approving officer is required.");
        }
        List<OvertimeRequest> rows = entity.getGroupRequestId() == null
                ? List.of(entity)
                : overtimeRequestRepository.findByGroupRequestIdOrderByOvertimeRequestIdAsc(entity.getGroupRequestId());
        if (Boolean.TRUE.equals(entity.getSupervisorFiled())) {
            List<StaffOvertimeAuthorizationClient.ApprovalStep> route =
                    staffAuthorizationClient.requireOvertimeApprovalRoute(bearerToken, entity.getBusinessUnitId());
            int finalLevel = route.stream().mapToInt(StaffOvertimeAuthorizationClient.ApprovalStep::approvalLevel)
                    .max().orElseThrow();
            if (requireActorLevel(route, approvedById) != finalLevel) {
                throw new IllegalStateException("Only the configured final approver may record the final Staff Overtime decision.");
            }
            if (rows.stream().anyMatch(row -> !isValidFirstLevelRecommendation(row, route))) {
                throw new IllegalStateException(
                        "The configured Level 1 approver must recommend this Staff Overtime request before the final decision.");
            }
        }
        LocalDateTime decidedAt = LocalDateTime.now();
        for (OvertimeRequest row : rows) {
            if (!"Recommended".equalsIgnoreCase(row.getRecommendationStatus())) {
                throw new IllegalStateException("The IS recommendation must be completed before the final decision.");
            }
            row.setDutyShiftCode(validateDutyShiftCode(row.getWorkType(), row.getDutyShiftCode()));
            if (!"Pending".equalsIgnoreCase(row.getStatus())) {
                throw new IllegalStateException("A final decision has already been recorded for this overtime request.");
            }
            row.setStatus(newStatus);
            row.setApprovedById(approvedById);
            row.setApprovedAt(decidedAt);
            row.setApprovalRemarks(remarks);
            row.setUpdatedAt(decidedAt);
        }
        List<OvertimeRequest> saved = overtimeRequestRepository.saveAll(rows);
        return entity.getGroupRequestId() == null ? toDTO(saved.get(0)) : toStaffGroupDTO(saved);
    }

    private int requireActorLevel(List<StaffOvertimeAuthorizationClient.ApprovalStep> route, Long actorId) {
        if (actorId == null) throw new IllegalArgumentException("The authenticated approver is required.");
        return route.stream()
                .filter(step -> actorId.equals(step.employeeId()))
                .map(StaffOvertimeAuthorizationClient.ApprovalStep::approvalLevel)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "You are not configured in this Business Unit's Overtime Request approval workflow."));
    }

    private boolean isRecommendationPending(String status) {
        return status == null || status.isBlank() || "Pending".equalsIgnoreCase(status)
                || "Not Recommended".equalsIgnoreCase(status);
    }

    private boolean isValidFirstLevelRecommendation(
            OvertimeRequest row,
            List<StaffOvertimeAuthorizationClient.ApprovalStep> route) {
        if (!"Recommended".equalsIgnoreCase(row.getRecommendationStatus())
                || row.getRecommendedById() == null) return false;
        int firstLevel = route.stream()
                .mapToInt(StaffOvertimeAuthorizationClient.ApprovalStep::approvalLevel)
                .min().orElseThrow();
        return route.stream().anyMatch(step -> step.approvalLevel() == firstLevel
                && row.getRecommendedById().equals(step.employeeId()));
    }

    private boolean isLegacyInvalidAutoRecommendation(
            OvertimeRequest row,
            List<StaffOvertimeAuthorizationClient.ApprovalStep> route) {
        return "Filed by the effective Head/OIC for staff approval."
                .equals(row.getRecommendationRemarks())
                && row.getFiledByEmployeeId() != null
                && row.getFiledByEmployeeId().equals(row.getRecommendedById())
                && !isValidFirstLevelRecommendation(row, route);
    }

    @Transactional
    @Override
    public OvertimeRequestDTO update(Long overtimeRequestId, OvertimeRequestDTO dto) throws Exception {
        try {
            Optional<OvertimeRequest> optional = overtimeRequestRepository.findById(overtimeRequestId);
            if (optional.isEmpty()) return null;
            OvertimeRequest entity = optional.get();
            if (Boolean.TRUE.equals(entity.getSupervisorFiled())) {
                throw new IllegalStateException("Supervisor-filed Staff Overtime is a group request and cannot be edited as an individual employee record.");
            }

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
            if (Boolean.TRUE.equals(entity.getSupervisorFiled())) {
                throw new IllegalStateException("Supervisor-filed Staff Overtime is a group request and cannot be edited as an individual employee record.");
            }

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
    public OvertimeRequestDTO administrativeUpdateStaffRequest(String groupRequestId,
                                                                OvertimeRequestDTO dto) throws Exception {
        List<OvertimeRequest> rows = requireStaffGroup(groupRequestId);
        for (OvertimeRequest entity : rows) {
            if (dto.getDateTimeFrom() != null) entity.setDateTimeFrom(dto.getDateTimeFrom());
            if (dto.getDateTimeTo() != null) entity.setDateTimeTo(dto.getDateTimeTo());
            if (entity.getDateTimeFrom() == null || entity.getDateTimeTo() == null
                    || !entity.getDateTimeTo().isAfter(entity.getDateTimeFrom())) {
                throw new IllegalArgumentException("Date/time To must be after Date/time From.");
            }
            if (dto.getDateFiled() != null) entity.setDateFiled(dto.getDateFiled());
            if (dto.getPurpose() != null) entity.setPurpose(dto.getPurpose());
            if (dto.getExpectedOutput() != null) entity.setExpectedOutput(dto.getExpectedOutput());
            if (dto.getWorkType() != null) entity.setWorkType(dto.getWorkType());
            entity.setDutyShiftCode(validateDutyShiftCode(entity.getWorkType(),
                    dto.getDutyShiftCode() != null ? dto.getDutyShiftCode() : entity.getDutyShiftCode()));
            if (dto.getAuthorityReference() != null) entity.setAuthorityReference(dto.getAuthorityReference());

            boolean postFiling = dto.getEmergencyPostFiling() != null
                    ? Boolean.TRUE.equals(dto.getEmergencyPostFiling())
                    : Boolean.TRUE.equals(entity.getEmergencyPostFiling());
            String justification = dto.getEmergencyJustification() != null
                    ? dto.getEmergencyJustification().trim() : entity.getEmergencyJustification();
            if (postFiling && (justification == null || justification.isBlank())) {
                throw new IllegalArgumentException("Emergency/post-filing justification is required.");
            }
            entity.setEmergencyPostFiling(postFiling);
            entity.setEmergencyJustification(postFiling ? justification : null);

            long totalMinutes = Duration.between(entity.getDateTimeFrom(), entity.getDateTimeTo()).toMinutes();
            int breakMinutes = validateManualBreakMinutes(
                    dto.getBreakMinutes() != null ? dto.getBreakMinutes() : entity.getBreakMinutes(), totalMinutes);
            entity.setBreakMinutes(breakMinutes);
            entity.setTotalHours(roundHours(totalMinutes));
            entity.setNetAuthorizedHours(roundHours(totalMinutes - breakMinutes));
            applyAdministrativeWorkflow(entity, dto);
            entity.setUpdatedAt(LocalDateTime.now());
        }
        return toStaffGroupDTO(overtimeRequestRepository.saveAll(rows));
    }

    @Transactional
    @Override
    public Boolean administrativeDeleteStaffRequest(String groupRequestId) {
        List<OvertimeRequest> rows = requireStaffGroup(groupRequestId);
        overtimeRequestRepository.deleteAll(rows);
        return true;
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
        Optional<OvertimeRequest> record = overtimeRequestRepository.findById(overtimeRequestId);
        if (record.isEmpty()) return false;
        if (Boolean.TRUE.equals(record.get().getSupervisorFiled())) {
            throw new IllegalStateException("Supervisor-filed Staff Overtime is a group request and cannot be deleted as an individual employee record.");
        }
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

    @Transactional(readOnly = true)
    @Override
    public void generateOvertimeAuthorization(Long overtimeRequestId, OutputStream out) throws Exception {
        JasperReport report = JasperReportRegistry.get("reports/OvertimeAuthorization.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("OVERTIME_REQUEST_ID", overtimeRequestId);
        putHeaderLogoParameters(params);

        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
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
