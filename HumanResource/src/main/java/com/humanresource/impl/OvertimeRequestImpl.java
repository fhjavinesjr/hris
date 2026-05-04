package com.humanresource.impl;

import com.humanresource.dtos.OvertimeRequestDTO;
import com.humanresource.entitymodels.OvertimeRequest;
import com.humanresource.repositories.OvertimeRequestRepository;
import com.humanresource.services.OvertimeRequestService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OvertimeRequestImpl implements OvertimeRequestService {

    private static final Logger log = LoggerFactory.getLogger(OvertimeRequestImpl.class);

    private final OvertimeRequestRepository overtimeRequestRepository;

    public OvertimeRequestImpl(OvertimeRequestRepository overtimeRequestRepository) {
        this.overtimeRequestRepository = overtimeRequestRepository;
    }

    private OvertimeRequestDTO toDTO(OvertimeRequest e) {
        OvertimeRequestDTO dto = new OvertimeRequestDTO();
        dto.setOvertimeRequestId(e.getOvertimeRequestId());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setDateFiled(e.getDateFiled());
        dto.setDateTimeFrom(e.getDateTimeFrom());
        dto.setDateTimeTo(e.getDateTimeTo());
        dto.setTotalHours(e.getTotalHours());
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

            // Compute totalHours from the date-time range (rounded to 2 decimal places)
            double totalHours = Duration.between(dto.getDateTimeFrom(), dto.getDateTimeTo()).toMinutes() / 60.0;
            totalHours = Math.round(totalHours * 100.0) / 100.0;
            if (totalHours <= 0) {
                log.warn("OvertimeRequest creation rejected: computed totalHours is zero or negative");
                return null;
            }

            OvertimeRequest entity = new OvertimeRequest();
            entity.setEmployeeId(dto.getEmployeeId());
            entity.setDateFiled(dto.getDateFiled() != null ? dto.getDateFiled() : dto.getDateTimeFrom().toLocalDate());
            entity.setDateTimeFrom(dto.getDateTimeFrom());
            entity.setDateTimeTo(dto.getDateTimeTo());
            entity.setTotalHours(totalHours);
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
            // Cannot delete an approved overtime order
            if ("Approved".equals(optional.get().getStatus())) {
                log.warn("OvertimeRequest delete rejected: cannot delete an Approved OT order (id {})", overtimeRequestId);
                return false;
            }
            overtimeRequestRepository.deleteById(overtimeRequestId);
            return true;
        } catch (Exception ex) {
            log.error("Error deleting OvertimeRequest id {}: ", overtimeRequestId, ex);
            return false;
        }
    }
}
