package com.humanresource.impl;

import com.humanresource.dtos.PassSlipDTO;
import com.humanresource.entitymodels.PassSlip;
import com.humanresource.repositories.PassSlipRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PassSlipImpl implements PassSlipService {

    private static final Logger log = LoggerFactory.getLogger(PassSlipImpl.class);

    private final PassSlipRepository repository;
    private final DateConflictChecker conflictChecker;
    private final DataSource dataSource;

    public PassSlipImpl(PassSlipRepository repository, DateConflictChecker conflictChecker, DataSource dataSource) {
        this.repository = repository;
        this.conflictChecker = conflictChecker;
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

    private PassSlip toEntity(PassSlipDTO dto) {
        PassSlip e = new PassSlip();
        e.setEmployeeId(dto.getEmployeeId());
        e.setDateFiled(dto.getDateFiled());
        e.setPassSlipDate(dto.getPassSlipDate());
        e.setPurpose(dto.getPurpose());
        e.setDepartureTime(dto.getDepartureTime());
        e.setArrivalTime(dto.getArrivalTime());
        e.setDetails(dto.getDetails());
        e.setStatus(dto.getStatus() != null ? dto.getStatus() : "Pending");
        e.setApprovedById(dto.getApprovedById());
        e.setApprovedAt(dto.getApprovedAt());
        e.setApprovalRemarks(dto.getApprovalRemarks());
        e.setRecommendationStatus(dto.getRecommendationStatus());
        e.setRecommendedById(dto.getRecommendedById());
        e.setRecommendationRemarks(dto.getRecommendationRemarks());
        return e;
    }

    @Transactional
    @Override
    public PassSlipDTO create(PassSlipDTO dto) throws Exception {
        // Validate before persisting — throws IllegalArgumentException on conflict
        conflictChecker.checkSingleDate(dto.getEmployeeId(), dto.getPassSlipDate());
        try {
            PassSlip entity = toEntity(dto);
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
        try {
            Optional<PassSlip> optional = repository.findById(passSlipId);
            if (optional.isEmpty()) return null;
            PassSlip entity = optional.get();
            entity.setDateFiled(dto.getDateFiled());
            entity.setPurpose(dto.getPurpose());
            entity.setDepartureTime(dto.getDepartureTime());
            entity.setArrivalTime(dto.getArrivalTime());
            entity.setDetails(dto.getDetails());
            entity.setStatus(dto.getStatus() != null ? dto.getStatus() : entity.getStatus());
            entity.setApprovedById(dto.getApprovedById());
            entity.setApprovalRemarks(dto.getApprovalRemarks());
            entity.setRecommendationStatus(dto.getRecommendationStatus());
            entity.setRecommendedById(dto.getRecommendedById());
            entity.setRecommendationRemarks(dto.getRecommendationRemarks());
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
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

    @Override
    public void generatePassSlipReport(Long passSlipId, OutputStream outputStream) throws Exception {
        if (passSlipId == null) {
            throw new IllegalArgumentException("passSlipId is required.");
        }

        ClassPathResource reportResource = new ClassPathResource("reports/permitSlip.jrxml");
        try (InputStream inputStream = reportResource.getInputStream();
             Connection connection = dataSource.getConnection()) {

            JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

            Map<String, Object> params = new HashMap<>();
            params.put("passSlipId", passSlipId);

            // Header parameters expected by the existing JRXML.
            // These can be wired later to System Config if you want agency-specific values/logos.
            params.put("webAppPath", "");
            params.put("currentCompany", "");
            params.put("currentCompanyAddress", "");
            params.put("logoleft", null);
            params.put("logoright", null);
            params.put("isDOH", Boolean.FALSE);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, connection);
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        }
    }

}
