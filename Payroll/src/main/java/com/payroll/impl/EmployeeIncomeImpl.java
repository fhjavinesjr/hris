package com.payroll.impl;

import com.payroll.dtos.EmployeeIncomeDTO;
import com.payroll.entitymodels.EmployeeIncome;
import com.payroll.repositories.EmployeeIncomeRepository;
import com.payroll.services.EmployeeIncomeService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeIncomeImpl implements EmployeeIncomeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeIncomeImpl.class);
    private final EmployeeIncomeRepository repository;

    public EmployeeIncomeImpl(EmployeeIncomeRepository repository) {
        this.repository = repository;
    }

    private EmployeeIncome toEntity(EmployeeIncomeDTO dto) {
        EmployeeIncome e = new EmployeeIncome();
        e.setId(dto.getId());
        e.setEmployeeNo(dto.getEmployeeNo());
        e.setEmployeeName(dto.getEmployeeName());
        e.setMonth(dto.getMonth());
        e.setYear(dto.getYear());
        e.setEarningType(dto.getEarningType());
        e.setEarningTypeName(dto.getEarningTypeName());
        e.setAmount(dto.getAmount());
        e.setIsTaxable(dto.getIsTaxable());
        e.setRemarks(dto.getRemarks());
        return e;
    }

    private EmployeeIncomeDTO toDTO(EmployeeIncome e) {
        EmployeeIncomeDTO dto = new EmployeeIncomeDTO();
        dto.setId(e.getId());
        dto.setEmployeeNo(e.getEmployeeNo());
        dto.setEmployeeName(e.getEmployeeName());
        dto.setMonth(e.getMonth());
        dto.setYear(e.getYear());
        dto.setEarningType(e.getEarningType());
        dto.setEarningTypeName(e.getEarningTypeName());
        dto.setAmount(e.getAmount());
        dto.setIsTaxable(e.getIsTaxable());
        dto.setRemarks(e.getRemarks());
        return dto;
    }

    @Transactional
    @Override
    public List<EmployeeIncomeDTO> createBulkEmployeeIncome(List<EmployeeIncomeDTO> dtoList) throws Exception {
        try {
            List<EmployeeIncome> entities = dtoList.stream().map(this::toEntity).collect(Collectors.toList());
            entities = repository.saveAll(entities);
            return entities.stream().map(this::toDTO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error creating bulk EmployeeIncome: ", e);
            return null;
        }
    }

    @Override
    public List<EmployeeIncomeDTO> getAllEmployeeIncome() throws Exception {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EmployeeIncomeDTO> getByMonthAndYear(int month, int year) throws Exception {
        return repository.findByMonthAndYear(month, year).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EmployeeIncomeDTO updateEmployeeIncome(Long id, EmployeeIncomeDTO dto) throws Exception {
        try {
            return repository.findById(id).map(entity -> {
                entity.setEmployeeNo(dto.getEmployeeNo());
                entity.setEmployeeName(dto.getEmployeeName());
                entity.setMonth(dto.getMonth());
                entity.setYear(dto.getYear());
                entity.setEarningType(dto.getEarningType());
                entity.setEarningTypeName(dto.getEarningTypeName());
                entity.setAmount(dto.getAmount());
                entity.setIsTaxable(dto.getIsTaxable());
                entity.setRemarks(dto.getRemarks());
                repository.save(entity);
                return toDTO(entity);
            }).orElse(null);
        } catch (Exception e) {
            log.error("Error updating EmployeeIncome id={}: ", id, e);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean deleteEmployeeIncome(Long id) throws Exception {
        try {
            repository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("Error deleting EmployeeIncome id={}: ", id, e);
            return false;
        }
    }
}
