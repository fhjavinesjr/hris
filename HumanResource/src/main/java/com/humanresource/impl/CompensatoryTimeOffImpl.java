package com.humanresource.impl;

import com.humanresource.dtos.CompensatoryTimeOffDTO;
import com.humanresource.entitymodels.CompensatoryTimeOff;
import com.humanresource.repositories.CompensatoryTimeOffRepository;
import com.humanresource.services.CompensatoryOvertimeCreditService;
import com.humanresource.services.CompensatoryTimeOffService;
import com.humanresource.services.DateConflictChecker;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompensatoryTimeOffImpl implements CompensatoryTimeOffService {

    private static final Logger log = LoggerFactory.getLogger(CompensatoryTimeOffImpl.class);

    private final CompensatoryTimeOffRepository ctoRepository;
    private final CompensatoryOvertimeCreditService cocService;
    private final DateConflictChecker conflictChecker;

    public CompensatoryTimeOffImpl(CompensatoryTimeOffRepository ctoRepository,
                                   CompensatoryOvertimeCreditService cocService,
                                   DateConflictChecker conflictChecker) {
        this.ctoRepository = ctoRepository;
        this.cocService = cocService;
        this.conflictChecker = conflictChecker;
    }

    private CompensatoryTimeOffDTO toDTO(CompensatoryTimeOff e) {
        CompensatoryTimeOffDTO dto = new CompensatoryTimeOffDTO();
        dto.setCtoId(e.getCtoId());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setDateFiled(e.getDateFiled());
        dto.setDateOfOffset(e.getDateOfOffset());
        dto.setHoursUsed(e.getHoursUsed());
        dto.setCocBalanceAtFiling(e.getCocBalanceAtFiling());
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

    @Transactional
    @Override
    public CompensatoryTimeOffDTO create(CompensatoryTimeOffDTO dto) throws Exception {
        // Validate date conflict across all leave-type applications
        conflictChecker.checkSingleDate(dto.getEmployeeId(), dto.getDateOfOffset());

        // Validate COC balance — throw so the controller returns 400 with the message
        double currentBalance = cocService.getAvailableBalance(dto.getEmployeeId());
        if (dto.getHoursUsed() > currentBalance) {
            throw new IllegalArgumentException(
                    "Insufficient COC balance. Requested: " + dto.getHoursUsed() +
                    " hours, Available: " + currentBalance + " hours.");
        }

        try {

            CompensatoryTimeOff entity = new CompensatoryTimeOff();
            entity.setEmployeeId(dto.getEmployeeId());
            entity.setDateFiled(dto.getDateFiled());
            entity.setDateOfOffset(dto.getDateOfOffset());
            entity.setHoursUsed(dto.getHoursUsed());
            entity.setCocBalanceAtFiling(currentBalance);
            entity.setReason(dto.getReason());
            entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "Pending");
            entity.setApprovedById(dto.getApprovedById());
            entity.setApprovalRemarks(dto.getApprovalRemarks());
            entity.setRecommendationStatus(dto.getRecommendationStatus());
            entity.setRecommendedById(dto.getRecommendedById());
            entity.setRecommendationRemarks(dto.getRecommendationRemarks());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity = ctoRepository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error creating CTO for employeeId {}: ", dto.getEmployeeId(), ex);
            return null;
        }
    }

    @Override
    public List<CompensatoryTimeOffDTO> getAll() throws Exception {
        return ctoRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CompensatoryTimeOffDTO> getAllByEmployeeId(Long employeeId) throws Exception {
        List<CompensatoryTimeOffDTO> list = ctoRepository
                .findByEmployeeIdOrderByDateFiledDesc(employeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());

        Double balance = cocService.getAvailableBalance(employeeId);
        list.forEach(dto -> dto.setCurrentBalance(balance));
        return list;
    }

    @Override
    public List<CompensatoryTimeOffDTO> getPendingAll() throws Exception {
        return ctoRepository.findByStatusOrderByDateFiledDesc("Pending")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CompensatoryTimeOffDTO approve(Long ctoId, Long approvedById, String remarks) throws Exception {
        return updateStatus(ctoId, "Approved", approvedById, remarks);
    }

    @Transactional
    @Override
    public CompensatoryTimeOffDTO disapprove(Long ctoId, Long approvedById, String remarks) throws Exception {
        return updateStatus(ctoId, "Disapproved", approvedById, remarks);
    }

    private CompensatoryTimeOffDTO updateStatus(Long ctoId, String newStatus, Long approvedById, String remarks) {
        try {
            Optional<CompensatoryTimeOff> optional = ctoRepository.findById(ctoId);
            if (optional.isEmpty()) return null;
            CompensatoryTimeOff entity = optional.get();
            entity.setStatus(newStatus);
            entity.setApprovedById(approvedById);
            entity.setApprovedAt(LocalDateTime.now());
            entity.setApprovalRemarks(remarks);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = ctoRepository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error updating CTO status for id {}: ", ctoId, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public CompensatoryTimeOffDTO update(Long ctoId, CompensatoryTimeOffDTO dto) throws Exception {
        try {
            Optional<CompensatoryTimeOff> optional = ctoRepository.findById(ctoId);
            if (optional.isEmpty()) return null;
            CompensatoryTimeOff entity = optional.get();
            entity.setDateFiled(dto.getDateFiled());
            entity.setDateOfOffset(dto.getDateOfOffset());
            entity.setHoursUsed(dto.getHoursUsed());
            entity.setReason(dto.getReason());
            entity.setStatus(dto.getStatus() != null ? dto.getStatus() : entity.getStatus());
            entity.setApprovedById(dto.getApprovedById());
            entity.setApprovalRemarks(dto.getApprovalRemarks());
            entity.setRecommendationStatus(dto.getRecommendationStatus());
            entity.setRecommendedById(dto.getRecommendedById());
            entity.setRecommendationRemarks(dto.getRecommendationRemarks());
            entity.setUpdatedAt(LocalDateTime.now());
            entity = ctoRepository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error updating CTO id {}: ", ctoId, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean delete(Long ctoId) throws Exception {
        try {
            if (!ctoRepository.existsById(ctoId)) return false;
            ctoRepository.deleteById(ctoId);
            return true;
        } catch (Exception ex) {
            log.error("Error deleting CTO id {}: ", ctoId, ex);
            return false;
        }
    }
}
