package com.humanresource.impl;

import com.humanresource.dtos.PassSlipDTO;
import com.humanresource.entitymodels.PassSlip;
import com.humanresource.entitymodels.ReportHeaderSettings;
import com.humanresource.repositories.PassSlipRepository;
import com.humanresource.repositories.ReportHeaderSettingsRepository;
import com.humanresource.reports.JasperReportRegistry;
import com.humanresource.services.DateConflictChecker;
import com.humanresource.services.PassSlipService;
import jakarta.transaction.Transactional;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PassSlipImpl implements PassSlipService {

    private static final Logger log = LoggerFactory.getLogger(PassSlipImpl.class);

    private final PassSlipRepository repository;
    private final DateConflictChecker conflictChecker;
    private final ReportHeaderSettingsRepository reportHeaderSettingsRepository;
    private final DataSource dataSource;

    public PassSlipImpl(PassSlipRepository repository,
                        DateConflictChecker conflictChecker,
                        ReportHeaderSettingsRepository reportHeaderSettingsRepository,
                        DataSource dataSource) {
        this.repository = repository;
        this.conflictChecker = conflictChecker;
        this.reportHeaderSettingsRepository = reportHeaderSettingsRepository;
        this.dataSource = dataSource;
    }

    private PassSlipDTO toDTO(PassSlip e) {
        PassSlipDTO dto = new PassSlipDTO();
        dto.setPassSlipId(e.getPassSlipId());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setDateFiled(e.getDateFiled());
        dto.setPassSlipDate(e.getPassSlipDate());
        dto.setPurpose(e.getPurpose());
        dto.setDepartureTime(e.getDepartureTime());
        dto.setArrivalTime(e.getArrivalTime());
        dto.setDetails(e.getDetails());
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

    private PassSlip toEntity(PassSlipDTO dto, boolean allowWorkflowFields) {
        PassSlip e = new PassSlip();
        e.setEmployeeId(dto.getEmployeeId());
        e.setDateFiled(dto.getDateFiled());
        e.setPassSlipDate(dto.getPassSlipDate());
        e.setPurpose(normalizePurpose(dto.getPurpose()));
        e.setDepartureTime(dto.getDepartureTime());
        e.setArrivalTime(dto.getArrivalTime());
        e.setDetails(dto.getDetails());
        e.setStatus(allowWorkflowFields ? normalizeStatus(dto.getStatus()) : "Pending");
        if (allowWorkflowFields) {
            e.setApprovedById(dto.getApprovedById());
            e.setApprovedAt("Approved".equals(e.getStatus()) ? LocalDateTime.now() : dto.getApprovedAt());
            e.setApprovalRemarks(dto.getApprovalRemarks());
            e.setRecommendationStatus(dto.getRecommendationStatus());
            e.setRecommendedById(dto.getRecommendedById());
            e.setRecommendationRemarks(dto.getRecommendationRemarks());
        }
        return e;
    }

    @Transactional
    @Override
    public PassSlipDTO create(PassSlipDTO dto) throws Exception {
        return create(dto, false);
    }

    @Transactional
    @Override
    public PassSlipDTO createOverride(PassSlipDTO dto) throws Exception {
        return create(dto, true);
    }

    private PassSlipDTO create(PassSlipDTO dto, boolean allowWorkflowFields) {
        validateApplication(dto);
        conflictChecker.checkSingleDate(dto.getEmployeeId(), dto.getPassSlipDate());
        try {
            PassSlip entity = toEntity(dto, allowWorkflowFields);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error creating PassSlip for employeeId {}: ", dto.getEmployeeId(), ex);
            return null;
        }
    }

    @Override
    public List<PassSlipDTO> getAll() throws Exception {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PassSlipDTO> getAllByEmployeeId(Long employeeId) throws Exception {
        return repository.findByEmployeeId(employeeId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PassSlipDTO> getPendingAll() throws Exception {
        return repository.findByStatusOrderByDateFiledDesc("Pending").stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public PassSlipDTO approve(Long passSlipId, Long approvedById, String remarks) throws Exception {
        return updateApprovalStatus(passSlipId, "Approved", approvedById, remarks);
    }

    @Transactional
    @Override
    public PassSlipDTO disapprove(Long passSlipId, Long approvedById, String remarks) throws Exception {
        return updateApprovalStatus(passSlipId, "Disapproved", approvedById, remarks);
    }

    @Transactional
    @Override
    public PassSlipDTO recommend(Long passSlipId, Long recommendedById, String remarks) throws Exception {
        try {
            Optional<PassSlip> optional = repository.findById(passSlipId);
            if (optional.isEmpty()) return null;
            PassSlip entity = optional.get();
            entity.setRecommendationStatus("Recommended");
            entity.setRecommendedById(recommendedById);
            entity.setRecommendationRemarks(remarks);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error recommending PassSlip for id {}: ", passSlipId, ex);
            return null;
        }
    }

    private PassSlipDTO updateApprovalStatus(Long passSlipId, String newStatus, Long approvedById, String remarks) {
        try {
            Optional<PassSlip> optional = repository.findById(passSlipId);
            if (optional.isEmpty()) return null;
            PassSlip entity = optional.get();
            entity.setStatus(newStatus);
            entity.setApprovedById(approvedById);
            entity.setApprovedAt(LocalDateTime.now());
            entity.setApprovalRemarks(remarks);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error updating PassSlip status for id {}: ", passSlipId, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public PassSlipDTO update(Long passSlipId, PassSlipDTO dto) throws Exception {
        return update(passSlipId, dto, false);
    }

    @Transactional
    @Override
    public PassSlipDTO updateOverride(Long passSlipId, PassSlipDTO dto) throws Exception {
        return update(passSlipId, dto, true);
    }

    private PassSlipDTO update(Long passSlipId, PassSlipDTO dto, boolean allowWorkflowFields) {
        try {
            Optional<PassSlip> optional = repository.findById(passSlipId);
            if (optional.isEmpty()) return null;
            PassSlip entity = optional.get();

            if (dto.getEmployeeId() != null
                    && !dto.getEmployeeId().equals(entity.getEmployeeId())) {
                throw new IllegalArgumentException(
                        "A Pass Slip record cannot be moved to another employee."
                );
            }

            // The HRM form has separate Date Filed and Pass Slip Date fields.
            // passSlipDate was previously omitted here, so the edited date was
            // accepted by the UI but the database kept its original value.
            if (dto.getPassSlipDate() != null
                    && !dto.getPassSlipDate().equals(entity.getPassSlipDate())) {
                conflictChecker.checkSingleDate(
                        entity.getEmployeeId(),
                        dto.getPassSlipDate()
                );
            }

            validateApplication(dto);
            if (!allowWorkflowFields && !"Pending".equalsIgnoreCase(entity.getStatus())) {
                throw new IllegalStateException("Only a pending Pass Slip can be edited by the employee.");
            }

            if (dto.getDateFiled() != null) entity.setDateFiled(dto.getDateFiled());
            if (dto.getPassSlipDate() != null) entity.setPassSlipDate(dto.getPassSlipDate());
            if (dto.getPurpose() != null) entity.setPurpose(normalizePurpose(dto.getPurpose()));
            if (dto.getDepartureTime() != null) entity.setDepartureTime(dto.getDepartureTime());
            if (dto.getArrivalTime() != null) entity.setArrivalTime(dto.getArrivalTime());
            if (dto.getDetails() != null) entity.setDetails(dto.getDetails());
            if (allowWorkflowFields) {
                String status = normalizeStatus(dto.getStatus() != null ? dto.getStatus() : entity.getStatus());
                entity.setStatus(status);
                entity.setApprovedById(dto.getApprovedById());
                entity.setApprovedAt("Approved".equals(status) ? LocalDateTime.now() : null);
                entity.setApprovalRemarks(dto.getApprovalRemarks());
                if (dto.getRecommendationStatus() != null) {
                    entity.setRecommendationStatus(dto.getRecommendationStatus());
                }
                entity.setRecommendedById(dto.getRecommendedById());
                entity.setRecommendationRemarks(dto.getRecommendationRemarks());
            }
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error updating PassSlip id {}: ", passSlipId, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean delete(Long passSlipId) throws Exception {
        try {
            if (!repository.existsById(passSlipId)) return false;
            repository.deleteById(passSlipId);
            return true;
        } catch (Exception ex) {
            log.error("Error deleting PassSlip id {}: ", passSlipId, ex);
            return false;
        }
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @Override
    public void generatePassSlipReport(Long passSlipId, OutputStream outputStream) throws Exception {
        if (passSlipId == null) {
            throw new IllegalArgumentException("passSlipId is required.");
        }
        PassSlip passSlip = repository.findById(passSlipId)
                .orElseThrow(() -> new IllegalArgumentException("Pass Slip not found."));
        if (!"Approved".equalsIgnoreCase(passSlip.getStatus())) {
            throw new IllegalStateException("Only an approved Pass Slip can be printed.");
        }

        try (Connection connection = dataSource.getConnection()) {
            JasperReport jasperReport = JasperReportRegistry.get("reports/permitSlip.jrxml");

            Map<String, Object> params = new HashMap<>();
            params.put("passSlipId", passSlipId);

            params.put("webAppPath", "");
            params.put("currentCompany", "");
            params.put("currentCompanyAddress", "");
            params.put("isDOH", Boolean.FALSE);
            putHeaderLogoParameters(params);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, connection);
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
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

    private void validateApplication(PassSlipDTO dto) {
        if (dto == null || dto.getEmployeeId() == null || dto.getDateFiled() == null
                || dto.getPassSlipDate() == null) {
            throw new IllegalArgumentException("Employee, Date Filed, and Pass Slip Date are required.");
        }
        normalizePurpose(dto.getPurpose());
        if (dto.getDepartureTime() == null || dto.getArrivalTime() == null) {
            throw new IllegalArgumentException("Departure and return times are required.");
        }
        if (!dto.getArrivalTime().isAfter(dto.getDepartureTime())) {
            throw new IllegalArgumentException("Return time must be later than departure time.");
        }
        if (dto.getDetails() == null || dto.getDetails().trim().isEmpty()) {
            throw new IllegalArgumentException("Pass Slip details are required.");
        }
    }

    private String normalizePurpose(String purpose) {
        if (purpose == null) throw new IllegalArgumentException("Purpose is required.");
        return switch (purpose.trim().toUpperCase(Locale.ROOT)) {
            case "PERSONAL" -> "Personal";
            case "OFFICIAL", "OFFICIAL BUSINESS", "OFFICIAL TIME" -> "Official";
            default -> throw new IllegalArgumentException("Purpose must be Personal or Official.");
        };
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return "Pending";
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "PENDING" -> "Pending";
            case "APPROVED" -> "Approved";
            case "DISAPPROVED" -> "Disapproved";
            case "CANCELLED" -> "Cancelled";
            default -> throw new IllegalArgumentException("Invalid Pass Slip status.");
        };
    }

}
