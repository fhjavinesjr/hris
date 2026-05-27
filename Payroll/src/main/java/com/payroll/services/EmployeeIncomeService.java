package com.payroll.services;

import com.payroll.dtos.EmployeeIncomeDTO;

import java.util.List;

public interface EmployeeIncomeService {

    List<EmployeeIncomeDTO> createBulkEmployeeIncome(List<EmployeeIncomeDTO> dtoList) throws Exception;

    List<EmployeeIncomeDTO> getAllEmployeeIncome() throws Exception;

    List<EmployeeIncomeDTO> getByMonthAndYear(int month, int year) throws Exception;

    EmployeeIncomeDTO updateEmployeeIncome(Long id, EmployeeIncomeDTO dto) throws Exception;

    Boolean deleteEmployeeIncome(Long id) throws Exception;
}
