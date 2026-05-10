package com.humanresource.impl;

import com.humanresource.dtos.OfficialEngagementApplicationDTO;
import com.humanresource.entitymodels.OfficialEngagementApplication;
import com.humanresource.repositories.OfficialEngagementApplicationRepository;
import com.humanresource.services.DateConflictChecker;
import com.humanresource.services.OfficialEngagementApplicationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OfficialEngagementApplicationImpl implements OfficialEngagementApplicationService {

    private static final Logger log = LoggerFactory.getLogger(OfficialEngagementApplicationImpl.class);

    private final OfficialEngagementApplicationRepository repository;
    private final DateConflictChecker conflictChecker;

    public OfficialEngagementApplicationImpl(OfficialEngagementApplicationRepository repository,
                                             DateConflictChecker conflictChecker) {
        this.repository = repository;
        this.conflictChecker = conflictChecker;
    }

    private OfficialEngagementApplicationDTO toDTO(OfficialEngagementApplication e) {
        OfficialEngagementApplicationDTO dto = new OfficialEngagementApplicationDTO();
        dto.setOfficialEngagementApplicationId(e.getOfficialEngagementApplicationId());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setDateFiled(e.getDateFiled());
        dto.setOfficialType(e.getOfficialType());
        dto.setStartDate(e.getStartDate());
        dto.setStartTime(e.getStartTime());
        dto.setEndDate(e.getEndDate());
        dto.setEndTime(e.getEndTime());
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

    private OfficialEngagementApplication toEntity(OfficialEngagementApplicationDTO dto) {
        OfficialEngagementApplication e = new OfficialEngagementApplication();
        e.setEmployeeId(dto.getEmployeeId());
        e.setDateFiled(dto.getDateFiled() != null ? dto.getDateFiled() : LocalDate.now());
        e.setOfficialType(dto.getOfficialType());
        e.setStartDate(dto.getStartDate());
        e.setStartTime(dto.getStartTime());
        e.setEndDate(dto.getEndDate());
        e.setEndTime(dto.getEndTime());
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

    /**
     * Checks for datetime overlap with existing active OE records for the same employee.
     * Excludes the record identified by {@code excludeId} (pass null for new records).
     * Two intervals [A, B) and [C, D) overlap if A < D AND B > C.
     */
    private void checkOETimeConflict(Long employeeId, LocalDate startDate, LocalTime startTime,
                                     LocalDate endDate, LocalTime endTime, Long excludeId) {
        LocalDateTime newStart = LocalDateTime.of(startDate, startTime);
        LocalDateTime newEnd   = LocalDateTime.of(endDate, endTime);
        List<OfficialEngagementApplication> existing = repository.findByEmployeeId(employeeId);
        for (OfficialEngagementApplication oe : existing) {
            if (excludeId != null && excludeId.equals(oe.getOfficialEngagementApplicationId())) continue;
            if (!"Pending".equals(oe.getStatus()) && !"Approved".equals(oe.getStatus())) continue;
            if (oe.getStartDate() == null || oe.getStartTime() == null
                    || oe.getEndDate() == null || oe.getEndTime() == null) continue;
            LocalDateTime exStart = LocalDateTime.of(oe.getStartDate(), oe.getStartTime());
            LocalDateTime exEnd   = LocalDateTime.of(oe.getEndDate(), oe.getEndTime());
            if (newStart.isBefore(exEnd) && newEnd.isAfter(exStart)) {
                throw new IllegalArgumentException(
                        "An Official Engagement (" + oe.getOfficialType() + ") already exists from "
                        + oe.getStartDate() + " " + oe.getStartTime().toString().substring(0, 5)
                        + " to " + oe.getEndDate() + " " + oe.getEndTime().toString().substring(0, 5)
                        + ". The selected date/time overlaps with an existing filing.");
            }
        }
    }

    @Transactional
    @Override
    public OfficialEngagementApplicationDTO create(OfficialEngagementApplicationDTO dto) throws Exception {
        // Validate against cross-type conflicts (PassSlip, CTO, TC, Leave) and OE-to-OE datetime overlap
        conflictChecker.checkDateRange(dto.getEmployeeId(), dto.getStartDate(), dto.getEndDate());
        checkOETimeConflict(dto.getEmployeeId(), dto.getStartDate(), dto.getStartTime(),
                dto.getEndDate(), dto.getEndTime(), null);
        try {
            OfficialEngagementApplication entity = toEntity(dto);
            entity.setDateFiled(LocalDate.now());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error creating OfficialEngagementApplication for employeeId {}: ", dto.getEmployeeId(), ex);
            return null;
        }
    }

    @Override
    public List<OfficialEngagementApplicationDTO> getAll() throws Exception {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OfficialEngagementApplicationDTO> getAllByEmployeeId(Long employeeId) throws Exception {
        return repository.findByEmployeeId(employeeId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OfficialEngagementApplicationDTO> getPendingAll() throws Exception {
        return repository.findByStatusOrderByStartDateDesc("Pending").stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public OfficialEngagementApplicationDTO approve(Long id, Long approvedById, String remarks) throws Exception {
        return updateApprovalStatus(id, "Approved", approvedById, remarks);
    }

    @Transactional
    @Override
    public OfficialEngagementApplicationDTO disapprove(Long id, Long approvedById, String remarks) throws Exception {
        return updateApprovalStatus(id, "Disapproved", approvedById, remarks);
    }

    private OfficialEngagementApplicationDTO updateApprovalStatus(Long id, String newStatus, Long approvedById, String remarks) {
        try {
            Optional<OfficialEngagementApplication> optional = repository.findById(id);
            if (optional.isEmpty()) return null;
            OfficialEngagementApplication entity = optional.get();
            entity.setStatus(newStatus);
            entity.setApprovedById(approvedById);
            entity.setApprovedAt(LocalDateTime.now());
            entity.setApprovalRemarks(remarks);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error updating OfficialEngagementApplication status for id {}: ", id, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public OfficialEngagementApplicationDTO update(Long id, OfficialEngagementApplicationDTO dto) throws Exception {
        // Validate against cross-type conflicts and OE-to-OE datetime overlap (excluding self)
        if (dto.getStartDate() != null && dto.getStartTime() != null
                && dto.getEndDate() != null && dto.getEndTime() != null) {
            conflictChecker.checkDateRange(dto.getEmployeeId(), dto.getStartDate(), dto.getEndDate());
            checkOETimeConflict(dto.getEmployeeId(), dto.getStartDate(), dto.getStartTime(),
                    dto.getEndDate(), dto.getEndTime(), id);
        }
        try {
            Optional<OfficialEngagementApplication> optional = repository.findById(id);
            if (optional.isEmpty()) return null;
            OfficialEngagementApplication entity = optional.get();
            entity.setOfficialType(dto.getOfficialType());
            entity.setStartDate(dto.getStartDate());
            entity.setStartTime(dto.getStartTime());
            entity.setEndDate(dto.getEndDate());
            entity.setEndTime(dto.getEndTime());
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
            log.error("Error updating OfficialEngagementApplication id {}: ", id, ex);
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
            log.error("Error deleting OfficialEngagementApplication id {}: ", id, ex);
            return false;
        }
    }
}
