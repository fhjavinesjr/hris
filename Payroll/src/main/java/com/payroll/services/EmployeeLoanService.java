package com.payroll.services;

import com.payroll.dtos.EmployeeLoanDTO;

import java.util.List;

public interface EmployeeLoanService {

    EmployeeLoanDTO createEmployeeLoan(EmployeeLoanDTO dto) throws Exception;

    List<EmployeeLoanDTO> getAllEmployeeLoans() throws Exception;

    EmployeeLoanDTO updateEmployeeLoan(Long id, EmployeeLoanDTO dto) throws Exception;

    Boolean deleteEmployeeLoan(Long id) throws Exception;
}
