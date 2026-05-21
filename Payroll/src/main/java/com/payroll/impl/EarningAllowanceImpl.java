package com.payroll.impl;

import com.payroll.dtos.EarningAllowanceDTO;
import com.payroll.entitymodels.EarningAllowance;
import com.payroll.repositories.EarningAllowanceRepository;
import com.payroll.services.EarningAllowanceService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EarningAllowanceImpl implements EarningAllowanceService {

    private static final Logger log = LoggerFactory.getLogger(EarningAllowanceImpl.class);
    private final EarningAllowanceRepository earningAllowanceRepository;

    public EarningAllowanceImpl(EarningAllowanceRepository earningAllowanceRepository) {
        this.earningAllowanceRepository = earningAllowanceRepository;
    }

    private EarningAllowance mapDtoToEntity(EarningAllowanceDTO dto) {
        EarningAllowance entity = new EarningAllowance();
        entity.setEmployeeNo(dto.getEmployeeNo());
        entity.setEmployeeName(dto.getEmployeeName());
        entity.setSalaryPeriod(dto.getSalaryPeriod());
        entity.setEffectiveUntil(dto.getEffectiveUntil());
        entity.setAllowanceType(dto.getAllowanceType());
        entity.setAmountPerSalary(dto.getAmountPerSalary());
        entity.setAmountDaily(dto.getAmountDaily());
        entity.setPercentage(dto.getPercentage());
        entity.setReason(dto.getReason());
        return entity;
    }

    private EarningAllowanceDTO mapEntityToDto(EarningAllowance entity) {
        EarningAllowanceDTO dto = new EarningAllowanceDTO();
        dto.setId(entity.getId());
        dto.setEmployeeNo(entity.getEmployeeNo());
        dto.setEmployeeName(entity.getEmployeeName());
        dto.setSalaryPeriod(entity.getSalaryPeriod());
        dto.setEffectiveUntil(entity.getEffectiveUntil());
        dto.setAllowanceType(entity.getAllowanceType());
        dto.setAmountPerSalary(entity.getAmountPerSalary());
        dto.setAmountDaily(entity.getAmountDaily());
        dto.setPercentage(entity.getPercentage());
        dto.setReason(entity.getReason());
        return dto;
    }

    @Transactional
    @Override
    public List<EarningAllowanceDTO> createBulkEarningAllowance(List<EarningAllowanceDTO> dtoList) throws Exception {
        try {
            List<EarningAllowanceDTO> savedList = new ArrayList<>();
            for (EarningAllowanceDTO dto : dtoList) {
                EarningAllowance saved = earningAllowanceRepository.save(mapDtoToEntity(dto));
                savedList.add(mapEntityToDto(saved));
            }
            return savedList;
        } catch (Exception e) {
            log.error("Error creating EarningAllowance: ", e);
            return null;
        }
    }

    @Override
    public List<EarningAllowanceDTO> getAllEarningAllowance() throws Exception {
        List<EarningAllowance> entities = earningAllowanceRepository.findAll();
        List<EarningAllowanceDTO> dtoList = new ArrayList<>();
        for (EarningAllowance entity : entities) {
            dtoList.add(mapEntityToDto(entity));
        }
        return dtoList;
    }

    @Transactional
    @Override
    public EarningAllowanceDTO updateEarningAllowance(Long id, EarningAllowanceDTO dto) throws Exception {
        try {
            EarningAllowance entity = earningAllowanceRepository.findById(id)
                    .orElseThrow(() -> new Exception("EarningAllowance not found with id: " + id));
            entity.setEmployeeNo(dto.getEmployeeNo());
            entity.setEmployeeName(dto.getEmployeeName());
            entity.setSalaryPeriod(dto.getSalaryPeriod());
            entity.setEffectiveUntil(dto.getEffectiveUntil());
            entity.setAllowanceType(dto.getAllowanceType());
            entity.setAmountPerSalary(dto.getAmountPerSalary());
            entity.setAmountDaily(dto.getAmountDaily());
            entity.setPercentage(dto.getPercentage());
            entity.setReason(dto.getReason());
            return mapEntityToDto(earningAllowanceRepository.save(entity));
        } catch (Exception e) {
            log.error("Error updating EarningAllowance: ", e);
            throw e;
        }
    }

    @Transactional
    @Override
    public Boolean deleteEarningAllowance(Long id) throws Exception {
        try {
            earningAllowanceRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("Error deleting EarningAllowance: ", e);
            return false;
        }
    }
}
