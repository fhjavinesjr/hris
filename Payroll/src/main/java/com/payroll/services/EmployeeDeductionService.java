package com.payroll.services;

import com.payroll.dtos.EmployeeDeductionDTO;

import java.util.List;

public interface EmployeeDeductionService {

    List<EmployeeDeductionDTO> createBulkEmployeeDeduction(List<EmployeeDeductionDTO> dtoList) throws Exception;

    List<EmployeeDeductionDTO> getAllEmployeeDeduction() throws Exception;

    EmployeeDeductionDTO updateEmployeeDeduction(Long id, EmployeeDeductionDTO dto) throws Exception;

    Boolean deleteEmployeeDeduction(Long id) throws Exception;
}
