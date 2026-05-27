package com.payroll.impl;

import com.payroll.dtos.AllowanceDTO;
import com.payroll.dtos.EarningAllowanceDTO;
import com.payroll.entitymodels.EarningAllowance;
import com.payroll.repositories.EarningAllowanceRepository;
import com.payroll.services.EarningAllowanceService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    @Override
    public List<AllowanceDTO> getBulkAllowancesForPayroll(LocalDate from, LocalDate to) throws Exception {
        try {
            // Fetch all earning allowances from the database
            // The payroll computation engine will filter based on effective dates
            List<EarningAllowance> entities = earningAllowanceRepository.findAll();
            List<AllowanceDTO> allowances = new ArrayList<>();

            for (EarningAllowance entity : entities) {
                AllowanceDTO dto = new AllowanceDTO();
                dto.setEmployeeNo(entity.getEmployeeNo());
                
                // Map allowanceType to code and name
                String allowanceType = entity.getAllowanceType() != null ? entity.getAllowanceType().toUpperCase() : "OTHER";
                dto.setAllowanceCode(allowanceType);
                dto.setAllowanceName(entity.getAllowanceType());
                
                // Map amounts
                dto.setAmountPerSalary(entity.getAmountPerSalary() != null ? entity.getAmountPerSalary() : 0.0);
                dto.setAmountPerDay(entity.getAmountDaily() != null ? entity.getAmountDaily() : 0.0);
                dto.setRatePerBasic(entity.getPercentage() != null ? entity.getPercentage() / 100.0 : 0.0);
                
                // Set type-specific flags based on allowanceType
                dto.setIsPera(allowanceType.contains("PERA"));
                dto.setIsSubsistence(allowanceType.contains("SUBSIST"));
                dto.setIsLaundry(allowanceType.contains("LAUNDRY"));
                dto.setIsHazardPay(allowanceType.contains("HAZARD"));
                
                // Default: non-taxable unless specified otherwise in the future
                dto.setIsTaxable(false);
                
                allowances.add(dto);
                log.debug("Converted allowance for {}: {} = {}", 
                    entity.getEmployeeNo(), allowanceType, entity.getAmountPerSalary());
            }
            
            log.info("Fetched {} earning allowances for payroll computation", allowances.size());
            return allowances;
        } catch (Exception e) {
            log.error("Error fetching bulk allowances: ", e);
            throw e;
        }
    }
}
