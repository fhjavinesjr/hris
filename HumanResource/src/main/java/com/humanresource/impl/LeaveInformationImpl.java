package com.humanresource.impl;

import com.humanresource.dtos.LeaveInformationDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.LeaveInformation;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.LeaveInformationRepository;
import com.humanresource.services.LeaveInformationService;
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
public class LeaveInformationImpl implements LeaveInformationService {

    private static final Logger log = LoggerFactory.getLogger(LeaveInformationImpl.class);

    private final LeaveInformationRepository repository;
    private final EmployeeRepository employeeRepository;

    public LeaveInformationImpl(LeaveInformationRepository repository,
                                 EmployeeRepository employeeRepository) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
    }

    private LeaveInformationDTO toDTO(LeaveInformation e) {
        LeaveInformationDTO dto = new LeaveInformationDTO();
        dto.setLeaveInformationId(e.getLeaveInformationId());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setSalaryPeriodSettingId(e.getSalaryPeriodSettingId());
        dto.setCutoffStartDate(e.getCutoffStartDate());
        dto.setCutoffEndDate(e.getCutoffEndDate());
        dto.setProcessDate(e.getProcessDate());
        dto.setProcessedById(e.getProcessedById());
        dto.setEarnedSl(e.getEarnedSl());
        dto.setEarnedVl(e.getEarnedVl());
        dto.setSickLeaveUsed(e.getSickLeaveUsed());
        dto.setVacationLeaveUsed(e.getVacationLeaveUsed());
        dto.setLeaveWithoutPaySl(e.getLeaveWithoutPaySl());
        dto.setLeaveWithoutPayVl(e.getLeaveWithoutPayVl());
        dto.setPreviousSickLeaveBalance(e.getPreviousSickLeaveBalance());
        dto.setPreviousVacationLeaveBalance(e.getPreviousVacationLeaveBalance());
        dto.setSickLeaveBalance(e.getSickLeaveBalance());
        dto.setVacationLeaveBalance(e.getVacationLeaveBalance());
        dto.setLateUndertimeMinutes(e.getLateUndertimeMinutes());
        dto.setLateUndertimeEquivalent(e.getLateUndertimeEquivalent());
        dto.setLateCount(e.getLateCount());
        dto.setUndertimeCount(e.getUndertimeCount());
        dto.setAbsentCount(e.getAbsentCount());
        dto.setLeaveParticulars(e.getLeaveParticulars());
        dto.setIsBegBalance(e.getIsBegBalance());
        dto.setIsLocked(e.getIsLocked());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());

        // Enrich with employee name
        employeeRepository.findById(e.getEmployeeId()).ifPresent(emp -> {
            dto.setEmployeeName(emp.getLastname() + ", " + emp.getFirstname());
            dto.setEmployeeNo(emp.getEmployeeNo());
        });

        return dto;
    }

    @Override
    public List<LeaveInformationDTO> getByEmployeeId(Long employeeId) throws Exception {
        return repository.findByEmployeeIdOrderByCutoffStartDateAsc(employeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<LeaveInformationDTO> getByPeriod(LocalDate cutoffStart, LocalDate cutoffEnd) throws Exception {
        return repository.findByCutoffStartDateAndCutoffEndDate(cutoffStart, cutoffEnd)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<LeaveInformationDTO> getBySalaryPeriodSettingId(Long salaryPeriodSettingId) throws Exception {
        return repository.findBySalaryPeriodSettingId(salaryPeriodSettingId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public LeaveInformationDTO lock(Long leaveInformationId) throws Exception {
        try {
            Optional<LeaveInformation> opt = repository.findById(leaveInformationId);
            if (opt.isEmpty()) return null;
            LeaveInformation entity = opt.get();
            entity.setIsLocked(true);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error locking LeaveInformation id {}: ", leaveInformationId, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public LeaveInformationDTO unlock(Long leaveInformationId) throws Exception {
        try {
            Optional<LeaveInformation> opt = repository.findById(leaveInformationId);
            if (opt.isEmpty()) return null;
            LeaveInformation entity = opt.get();
            entity.setIsLocked(false);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error unlocking LeaveInformation id {}: ", leaveInformationId, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean delete(Long leaveInformationId) throws Exception {
        try {
            Optional<LeaveInformation> opt = repository.findById(leaveInformationId);
            if (opt.isEmpty()) return false;
            if (Boolean.TRUE.equals(opt.get().getIsLocked())) {
                log.warn("Cannot delete locked LeaveInformation id {}", leaveInformationId);
                return false;
            }
            repository.deleteById(leaveInformationId);
            return true;
        } catch (Exception ex) {
            log.error("Error deleting LeaveInformation id {}: ", leaveInformationId, ex);
            return false;
        }
    }
}
