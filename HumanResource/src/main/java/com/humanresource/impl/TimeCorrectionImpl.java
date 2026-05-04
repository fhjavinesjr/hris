package com.humanresource.impl;

import com.humanresource.dtos.TimeCorrectionDTO;
import com.humanresource.entitymodels.TimeCorrection;
import com.humanresource.repositories.TimeCorrectionRepository;
import com.humanresource.services.DateConflictChecker;
import com.humanresource.services.TimeCorrectionService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TimeCorrectionImpl implements TimeCorrectionService {

    private static final Logger log = LoggerFactory.getLogger(TimeCorrectionImpl.class);

    private final TimeCorrectionRepository repository;
    private final DateConflictChecker conflictChecker;

    public TimeCorrectionImpl(TimeCorrectionRepository repository, DateConflictChecker conflictChecker) {
        this.repository = repository;
        this.conflictChecker = conflictChecker;
    }

    private TimeCorrectionDTO toDTO(TimeCorrection e) {
        TimeCorrectionDTO dto = new TimeCorrectionDTO();
        dto.setTimeCorrectionId(e.getTimeCorrectionId());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setDateFiled(e.getDateFiled());
        dto.setWorkDate(e.getWorkDate());
        dto.setCorrectedTimeIn(e.getCorrectedTimeIn());
        dto.setCorrectedTimeOut(e.getCorrectedTimeOut());
        dto.setCorrectedBreakOut(e.getCorrectedBreakOut());
        dto.setCorrectedBreakIn(e.getCorrectedBreakIn());
        dto.setReason(e.getReason());
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

    private TimeCorrection toEntity(TimeCorrectionDTO dto) {
        TimeCorrection e = new TimeCorrection();
        e.setEmployeeId(dto.getEmployeeId());
        e.setDateFiled(dto.getDateFiled() != null ? dto.getDateFiled() : LocalDate.now());
        e.setWorkDate(dto.getWorkDate());
        e.setCorrectedTimeIn(dto.getCorrectedTimeIn());
        e.setCorrectedTimeOut(dto.getCorrectedTimeOut());
        e.setCorrectedBreakOut(dto.getCorrectedBreakOut());
        e.setCorrectedBreakIn(dto.getCorrectedBreakIn());
        e.setReason(dto.getReason());
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
    public TimeCorrectionDTO create(TimeCorrectionDTO dto) throws Exception {
        // Validate before persisting — throws IllegalArgumentException on conflict
        conflictChecker.checkSingleDate(dto.getEmployeeId(), dto.getWorkDate());
        try {
            TimeCorrection entity = toEntity(dto);
            entity.setDateFiled(LocalDate.now());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error creating TimeCorrection for employeeId {}: ", dto.getEmployeeId(), ex);
            return null;
        }
    }

    @Override
    public List<TimeCorrectionDTO> getAll() throws Exception {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TimeCorrectionDTO> getAllByEmployeeId(Long employeeId) throws Exception {
        return repository.findByEmployeeId(employeeId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TimeCorrectionDTO> getPendingAll() throws Exception {
        return repository.findByStatusOrderByDateFiledDesc("Pending").stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TimeCorrectionDTO approve(Long id, Long approvedById, String remarks) throws Exception {
        return updateApprovalStatus(id, "Approved", approvedById, remarks);
    }

    @Transactional
    @Override
    public TimeCorrectionDTO disapprove(Long id, Long approvedById, String remarks) throws Exception {
        return updateApprovalStatus(id, "Disapproved", approvedById, remarks);
    }

    private TimeCorrectionDTO updateApprovalStatus(Long id, String newStatus, Long approvedById, String remarks) {
        try {
            Optional<TimeCorrection> optional = repository.findById(id);
            if (optional.isEmpty()) return null;
            TimeCorrection entity = optional.get();
            entity.setStatus(newStatus);
            entity.setApprovedById(approvedById);
            entity.setApprovedAt(LocalDateTime.now());
            entity.setApprovalRemarks(remarks);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error updating TimeCorrection status for id {}: ", id, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public TimeCorrectionDTO update(Long id, TimeCorrectionDTO dto) throws Exception {
        try {
            Optional<TimeCorrection> optional = repository.findById(id);
            if (optional.isEmpty()) return null;
            TimeCorrection entity = optional.get();
            entity.setWorkDate(dto.getWorkDate());
            entity.setCorrectedTimeIn(dto.getCorrectedTimeIn());
            entity.setCorrectedTimeOut(dto.getCorrectedTimeOut());
            entity.setReason(dto.getReason());
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
            log.error("Error updating TimeCorrection id {}: ", id, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean delete(Long id) throws Exception {
        try {
            if (!repository.existsById(id)) return false;
            repository.deleteById(id);
            return true;
        } catch (Exception ex) {
            log.error("Error deleting TimeCorrection id {}: ", id, ex);
            return false;
        }
    }
}
