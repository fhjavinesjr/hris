package com.humanresource.impl;

import com.humanresource.dtos.CocBeginningBalanceDTO;
import com.humanresource.entitymodels.CocBeginningBalance;
import com.humanresource.repositories.CocBeginningBalanceRepository;
import com.humanresource.services.CocBeginningBalanceService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class CocBeginningBalanceImpl implements CocBeginningBalanceService {

    private static final Logger log = LoggerFactory.getLogger(CocBeginningBalanceImpl.class);
    private final CocBeginningBalanceRepository repository;

    public CocBeginningBalanceImpl(CocBeginningBalanceRepository repository) {
        this.repository = repository;
    }

    private CocBeginningBalanceDTO toDTO(CocBeginningBalance entity) {
        CocBeginningBalanceDTO dto = new CocBeginningBalanceDTO();
        dto.setCocBeginningBalanceId(entity.getCocBeginningBalanceId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setAsOfDate(entity.getAsOfDate());
        dto.setAccumulatedHours(entity.getAccumulatedHours());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @Transactional
    @Override
    public CocBeginningBalanceDTO save(Long employeeId, CocBeginningBalanceDTO dto) throws Exception {
        try {
            dto.setEmployeeId(employeeId);
            Optional<CocBeginningBalance> existing = repository.findByEmployeeId(employeeId);

            CocBeginningBalance entity;
            if (existing.isPresent()) {
                entity = existing.get();
                entity.setAsOfDate(dto.getAsOfDate());
                entity.setAccumulatedHours(dto.getAccumulatedHours());
                entity.setUpdatedAt(LocalDateTime.now());
            } else {
                entity = new CocBeginningBalance(
                        null, employeeId, dto.getAsOfDate(), dto.getAccumulatedHours(),
                        LocalDateTime.now(), LocalDateTime.now());
            }
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (Exception e) {
            log.error("Error saving COC beginning balance for employeeId {}: ", employeeId, e);
            return null;
        }
    }

    @Override
    public CocBeginningBalanceDTO getByEmployeeId(Long employeeId) throws Exception {
        Optional<CocBeginningBalance> optional = repository.findByEmployeeId(employeeId);
        return optional.map(this::toDTO).orElse(null);
    }

    @Transactional
    @Override
    public Boolean deleteById(Long id) throws Exception {
        try {
            repository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("Error deleting COC beginning balance with id {}: ", id, e);
            return false;
        }
    }
}
