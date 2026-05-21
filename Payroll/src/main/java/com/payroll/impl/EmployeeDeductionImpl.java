package com.payroll.impl;

import com.payroll.dtos.EmployeeDeductionDTO;
import com.payroll.entitymodels.EmployeeDeduction;
import com.payroll.repositories.EmployeeDeductionRepository;
import com.payroll.services.EmployeeDeductionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeDeductionImpl implements EmployeeDeductionService {

    private final EmployeeDeductionRepository repository;

    public EmployeeDeductionImpl(EmployeeDeductionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<EmployeeDeductionDTO> createBulkEmployeeDeduction(List<EmployeeDeductionDTO> dtoList) throws Exception {
        List<EmployeeDeduction> entities = dtoList.stream().map(this::mapToEntity).collect(Collectors.toList());
        List<EmployeeDeduction> saved = repository.saveAll(entities);
        return saved.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDeductionDTO> getAllEmployeeDeduction() throws Exception {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public EmployeeDeductionDTO updateEmployeeDeduction(Long id, EmployeeDeductionDTO dto) throws Exception {
        EmployeeDeduction existing = repository.findById(id)
                .orElseThrow(() -> new Exception("EmployeeDeduction not found for ID: " + id));

        existing.setEmployeeNo(dto.getEmployeeNo());
        existing.setEmployeeName(dto.getEmployeeName());
        existing.setSalaryPeriod(dto.getSalaryPeriod());
        existing.setDeductionType(dto.getDeductionType());
        existing.setReferenceNo(dto.getReferenceNo());
        existing.setAmount(dto.getAmount());
        existing.setIsFixed(dto.getIsFixed());

        EmployeeDeduction updated = repository.save(existing);
        return mapToDTO(updated);
    }

    @Override
    public Boolean deleteEmployeeDeduction(Long id) throws Exception {
        repository.deleteById(id);
        return true;
    }

    private EmployeeDeductionDTO mapToDTO(EmployeeDeduction entity) {
        EmployeeDeductionDTO dto = new EmployeeDeductionDTO();
        dto.setId(entity.getId());
        dto.setEmployeeNo(entity.getEmployeeNo());
        dto.setEmployeeName(entity.getEmployeeName());
        dto.setSalaryPeriod(entity.getSalaryPeriod());
        dto.setDeductionType(entity.getDeductionType());
        dto.setReferenceNo(entity.getReferenceNo());
        dto.setAmount(entity.getAmount());
        dto.setIsFixed(entity.getIsFixed());
        return dto;
    }

    private EmployeeDeduction mapToEntity(EmployeeDeductionDTO dto) {
        EmployeeDeduction entity = new EmployeeDeduction();
        entity.setEmployeeNo(dto.getEmployeeNo());
        entity.setEmployeeName(dto.getEmployeeName());
        entity.setSalaryPeriod(dto.getSalaryPeriod());
        entity.setDeductionType(dto.getDeductionType());
        entity.setReferenceNo(dto.getReferenceNo());
        entity.setAmount(dto.getAmount());
        entity.setIsFixed(dto.getIsFixed());
        return entity;
    }
}
