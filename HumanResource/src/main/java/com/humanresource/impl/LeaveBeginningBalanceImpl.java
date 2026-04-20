package com.humanresource.impl;

import com.humanresource.dtos.LeaveBeginningBalanceDTO;
import com.humanresource.entitymodels.LeaveBeginningBalance;
import com.humanresource.repositories.LeaveBeginningBalanceRepository;
import com.humanresource.services.LeaveBeginningBalanceService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class LeaveBeginningBalanceImpl implements LeaveBeginningBalanceService {

    private static final Logger log = LoggerFactory.getLogger(LeaveBeginningBalanceImpl.class);
    private final LeaveBeginningBalanceRepository repository;

    public LeaveBeginningBalanceImpl(LeaveBeginningBalanceRepository repository) {
        this.repository = repository;
    }

    private LeaveBeginningBalanceDTO toDTO(LeaveBeginningBalance entity) {
        LeaveBeginningBalanceDTO dto = new LeaveBeginningBalanceDTO();
        dto.setLeaveBeginningBalanceId(entity.getLeaveBeginningBalanceId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setAsOfDate(entity.getAsOfDate());
        dto.setLeaveType(entity.getLeaveType());
        dto.setBalance(entity.getBalance());
        dto.setLeaveTypesId(entity.getLeaveTypesId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @Transactional
    @Override
    public List<LeaveBeginningBalanceDTO> saveAll(Long employeeId, List<LeaveBeginningBalanceDTO> dtoList) throws Exception {
        try {
            List<LeaveBeginningBalanceDTO> result = new ArrayList<>();
            for (LeaveBeginningBalanceDTO dto : dtoList) {
                dto.setEmployeeId(employeeId);
                Optional<LeaveBeginningBalance> existing =
                        repository.findByEmployeeIdAndLeaveType(employeeId, dto.getLeaveType());

                LeaveBeginningBalance entity;
                if (existing.isPresent()) {
                    entity = existing.get();
                    entity.setAsOfDate(dto.getAsOfDate());
                    entity.setBalance(dto.getBalance());
                    entity.setLeaveTypesId(dto.getLeaveTypesId());
                    entity.setUpdatedAt(LocalDateTime.now());
                } else {
                    entity = new LeaveBeginningBalance(
                            null, employeeId, dto.getAsOfDate(), dto.getLeaveType(), dto.getBalance(),
                            dto.getLeaveTypesId(), LocalDateTime.now(), LocalDateTime.now());
                }
                entity = repository.save(entity);
                result.add(toDTO(entity));
            }
            return result;
        } catch (Exception e) {
            log.error("Error saving leave beginning balances: ", e);
            return null;
        }
    }

    @Override
    public List<LeaveBeginningBalanceDTO> getAll() throws Exception {
        List<LeaveBeginningBalanceDTO> result = new ArrayList<>();
        for (LeaveBeginningBalance entity : repository.findAll()) {
            result.add(toDTO(entity));
        }
        return result;
    }

    @Override
    public List<LeaveBeginningBalanceDTO> getAllByEmployeeId(Long employeeId) throws Exception {
        List<LeaveBeginningBalanceDTO> result = new ArrayList<>();
        for (LeaveBeginningBalance entity : repository.findByEmployeeId(employeeId)) {
            result.add(toDTO(entity));
        }
        return result;
    }

    @Transactional
    @Override
    public Boolean deleteById(Long id) throws Exception {
        try {
            repository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("Error deleting leave beginning balance with id {}: ", id, e);
            return false;
        }
    }
}
