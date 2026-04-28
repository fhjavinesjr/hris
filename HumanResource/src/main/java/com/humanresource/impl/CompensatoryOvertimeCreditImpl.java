package com.humanresource.impl;

import com.humanresource.dtos.CompensatoryOvertimeCreditDTO;
import com.humanresource.entitymodels.CompensatoryOvertimeCredit;
import com.humanresource.entitymodels.CocBeginningBalance;
import com.humanresource.repositories.CompensatoryOvertimeCreditRepository;
import com.humanresource.repositories.CompensatoryTimeOffRepository;
import com.humanresource.repositories.CocBeginningBalanceRepository;
import com.humanresource.services.CompensatoryOvertimeCreditService;
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
public class CompensatoryOvertimeCreditImpl implements CompensatoryOvertimeCreditService {

    private static final Logger log = LoggerFactory.getLogger(CompensatoryOvertimeCreditImpl.class);

    private final CompensatoryOvertimeCreditRepository cocRepository;
    private final CompensatoryTimeOffRepository ctoRepository;
    private final CocBeginningBalanceRepository cocBegBalRepository;

    public CompensatoryOvertimeCreditImpl(CompensatoryOvertimeCreditRepository cocRepository,
                                          CompensatoryTimeOffRepository ctoRepository,
                                          CocBeginningBalanceRepository cocBegBalRepository) {
        this.cocRepository = cocRepository;
        this.ctoRepository = ctoRepository;
        this.cocBegBalRepository = cocBegBalRepository;
    }

    private CompensatoryOvertimeCreditDTO toDTO(CompensatoryOvertimeCredit e) {
        CompensatoryOvertimeCreditDTO dto = new CompensatoryOvertimeCreditDTO();
        dto.setCocId(e.getCocId());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setDateFiled(e.getDateFiled());
        dto.setDateWorked(e.getDateWorked());
        dto.setHoursWorked(e.getHoursWorked());
        dto.setReason(e.getReason());
        dto.setWorkType(e.getWorkType());
        dto.setStatus(e.getStatus());
        dto.setApprovedById(e.getApprovedById());
        dto.setApprovedAt(e.getApprovedAt());
        dto.setApprovalRemarks(e.getApprovalRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    @Transactional
    @Override
    public CompensatoryOvertimeCreditDTO create(CompensatoryOvertimeCreditDTO dto) throws Exception {
        try {
            // Guard: required fields must be present
            if (dto.getEmployeeId() == null || dto.getDateWorked() == null || dto.getHoursWorked() == null) {
                log.warn("COC creation rejected: missing required fields for employeeId {}", dto.getEmployeeId());
                return null;
            }

            // Guard: hoursWorked must be a positive value and cannot exceed 24 hours in a day
            if (dto.getHoursWorked() <= 0 || dto.getHoursWorked() > 24) {
                log.warn("COC creation rejected: invalid hoursWorked {} for employeeId {}", dto.getHoursWorked(), dto.getEmployeeId());
                return null;
            }

            // Guard: dateWorked must not be a future date
            if (dto.getDateWorked().isAfter(LocalDate.now())) {
                log.warn("COC creation rejected: dateWorked {} is in the future for employeeId {}", dto.getDateWorked(), dto.getEmployeeId());
                return null;
            }

            // Guard: no duplicate active (Pending or Approved) COC for the same employee + dateWorked.
            // Disapproved records are excluded so the employee can re-file after rejection.
            if (cocRepository.existsByEmployeeIdAndDateWorkedAndStatusNot(dto.getEmployeeId(), dto.getDateWorked(), "Disapproved")) {
                log.warn("COC creation rejected: active COC already exists for employeeId {} on dateWorked {}", dto.getEmployeeId(), dto.getDateWorked());
                return null;
            }

            CompensatoryOvertimeCredit entity = new CompensatoryOvertimeCredit();
            entity.setEmployeeId(dto.getEmployeeId());
            entity.setDateFiled(dto.getDateFiled());
            entity.setDateWorked(dto.getDateWorked());
            entity.setHoursWorked(dto.getHoursWorked());
            entity.setReason(dto.getReason());
            entity.setWorkType(dto.getWorkType());
            entity.setStatus("Pending");
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity = cocRepository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error creating COC for employeeId {}: ", dto.getEmployeeId(), ex);
            return null;
        }
    }

    @Override
    public List<CompensatoryOvertimeCreditDTO> getAll() throws Exception {
        return cocRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CompensatoryOvertimeCreditDTO> getAllByEmployeeId(Long employeeId) throws Exception {
        List<CompensatoryOvertimeCreditDTO> list = cocRepository
                .findByEmployeeIdOrderByDateFiledDesc(employeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());

        // Attach current balance to each record
        Double balance = getAvailableBalance(employeeId);
        list.forEach(dto -> dto.setCurrentBalance(balance));
        return list;
    }

    @Override
    public List<CompensatoryOvertimeCreditDTO> getPendingAll() throws Exception {
        return cocRepository.findByStatusOrderByDateFiledDesc("Pending")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CompensatoryOvertimeCreditDTO approve(Long cocId, Long approvedById, String remarks) throws Exception {
        return updateStatus(cocId, "Approved", approvedById, remarks);
    }

    @Transactional
    @Override
    public CompensatoryOvertimeCreditDTO disapprove(Long cocId, Long approvedById, String remarks) throws Exception {
        return updateStatus(cocId, "Disapproved", approvedById, remarks);
    }

    private CompensatoryOvertimeCreditDTO updateStatus(Long cocId, String newStatus, Long approvedById, String remarks) {
        try {
            Optional<CompensatoryOvertimeCredit> optional = cocRepository.findById(cocId);
            if (optional.isEmpty()) return null;
            CompensatoryOvertimeCredit entity = optional.get();
            entity.setStatus(newStatus);
            entity.setApprovedById(approvedById);
            entity.setApprovedAt(LocalDateTime.now());
            entity.setApprovalRemarks(remarks);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = cocRepository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error updating COC status for id {}: ", cocId, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public CompensatoryOvertimeCreditDTO update(Long cocId, CompensatoryOvertimeCreditDTO dto) throws Exception {
        try {
            Optional<CompensatoryOvertimeCredit> optional = cocRepository.findById(cocId);
            if (optional.isEmpty()) return null;
            CompensatoryOvertimeCredit entity = optional.get();
            entity.setDateFiled(dto.getDateFiled());
            entity.setDateWorked(dto.getDateWorked());
            entity.setHoursWorked(dto.getHoursWorked());
            entity.setReason(dto.getReason());
            entity.setWorkType(dto.getWorkType());
            entity.setUpdatedAt(LocalDateTime.now());
            entity = cocRepository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error updating COC id {}: ", cocId, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean delete(Long cocId) throws Exception {
        try {
            if (!cocRepository.existsById(cocId)) return false;
            cocRepository.deleteById(cocId);
            return true;
        } catch (Exception ex) {
            log.error("Error deleting COC id {}: ", cocId, ex);
            return false;
        }
    }

    /**
     * Available COC Balance =
     *   CocBeginningBalance.accumulatedHours (initial seed balance, if any)
     *   + SUM of approved COC hoursWorked
     *   - SUM of approved CTO hoursUsed
     */
    @Override
    public Double getAvailableBalance(Long employeeId) throws Exception {
        double beginningBalance = 0.0;
        Optional<CocBeginningBalance> begBal = cocBegBalRepository.findByEmployeeId(employeeId);
        if (begBal.isPresent() && begBal.get().getAccumulatedHours() != null) {
            beginningBalance = begBal.get().getAccumulatedHours();
        }
        Double earnedHours = cocRepository.sumApprovedHoursByEmployeeId(employeeId);
        Double usedHours = ctoRepository.sumApprovedHoursUsedByEmployeeId(employeeId);
        return beginningBalance + (earnedHours != null ? earnedHours : 0.0)
                - (usedHours != null ? usedHours : 0.0);
    }
}
