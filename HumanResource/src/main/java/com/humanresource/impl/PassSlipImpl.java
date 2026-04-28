package com.humanresource.impl;

import com.humanresource.dtos.PassSlipDTO;
import com.humanresource.entitymodels.PassSlip;
import com.humanresource.repositories.PassSlipRepository;
import com.humanresource.services.DateConflictChecker;
import com.humanresource.services.PassSlipService;
import jakarta.transaction.Transactional;
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

    public PassSlipImpl(PassSlipRepository repository, DateConflictChecker conflictChecker) {
        this.repository = repository;
        this.conflictChecker = conflictChecker;
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
        return e;
    }

    @Transactional
    @Override
    public PassSlipDTO create(PassSlipDTO dto) throws Exception {
        // Validate before persisting — throws IllegalArgumentException on conflict
        conflictChecker.checkSingleDate(dto.getEmployeeId(), dto.getPassSlipDate());
        try {
            PassSlip entity = toEntity(dto);
            entity.setStatus("Pending");
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
}
