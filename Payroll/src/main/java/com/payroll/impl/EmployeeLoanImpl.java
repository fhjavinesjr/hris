package com.payroll.impl;

import com.payroll.dtos.EmployeeLoanDTO;
import com.payroll.entitymodels.EmployeeLoan;
import com.payroll.repositories.EmployeeLoanRepository;
import com.payroll.services.EmployeeLoanService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeLoanImpl implements EmployeeLoanService {

    private final EmployeeLoanRepository repository;

    public EmployeeLoanImpl(EmployeeLoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public EmployeeLoanDTO createEmployeeLoan(EmployeeLoanDTO dto) throws Exception {
        EmployeeLoan entity = mapToEntity(dto);
        EmployeeLoan saved = repository.save(entity);
        return mapToDTO(saved);
    }

    @Override
    public List<EmployeeLoanDTO> getAllEmployeeLoans() throws Exception {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public EmployeeLoanDTO updateEmployeeLoan(Long id, EmployeeLoanDTO dto) throws Exception {
        EmployeeLoan existing = repository.findById(id)
                .orElseThrow(() -> new Exception("EmployeeLoan not found for ID: " + id));

        existing.setEmployeeNo(dto.getEmployeeNo());
        existing.setEmployeeName(dto.getEmployeeName());
        existing.setSalaryPeriod(dto.getSalaryPeriod());
        existing.setLoanType(dto.getLoanType());
        existing.setReference(dto.getReference());
        existing.setAmount(dto.getAmount());
        existing.setToPay(dto.getToPay());
        existing.setPaid(dto.getPaid());
        existing.setIsStopDeduction(dto.getIsStopDeduction());
        existing.setLoanStopDate(dto.getLoanStopDate());

        EmployeeLoan updated = repository.save(existing);
        return mapToDTO(updated);
    }

    @Override
    public Boolean deleteEmployeeLoan(Long id) throws Exception {
        repository.deleteById(id);
        return true;
    }

    private EmployeeLoanDTO mapToDTO(EmployeeLoan entity) {
        EmployeeLoanDTO dto = new EmployeeLoanDTO();
        dto.setId(entity.getId());
        dto.setEmployeeNo(entity.getEmployeeNo());
        dto.setEmployeeName(entity.getEmployeeName());
        dto.setSalaryPeriod(entity.getSalaryPeriod());
        dto.setLoanType(entity.getLoanType());
        dto.setReference(entity.getReference());
        dto.setAmount(entity.getAmount());
        dto.setToPay(entity.getToPay());
        dto.setPaid(entity.getPaid());
        dto.setIsStopDeduction(entity.getIsStopDeduction());
        dto.setLoanStopDate(entity.getLoanStopDate());
        return dto;
    }

    private EmployeeLoan mapToEntity(EmployeeLoanDTO dto) {
        EmployeeLoan entity = new EmployeeLoan();
        entity.setEmployeeNo(dto.getEmployeeNo());
        entity.setEmployeeName(dto.getEmployeeName());
        entity.setSalaryPeriod(dto.getSalaryPeriod());
        entity.setLoanType(dto.getLoanType());
        entity.setReference(dto.getReference());
        entity.setAmount(dto.getAmount());
        entity.setToPay(dto.getToPay());
        entity.setPaid(dto.getPaid());
        entity.setIsStopDeduction(dto.getIsStopDeduction());
        entity.setLoanStopDate(dto.getLoanStopDate());
        return entity;
    }
}
