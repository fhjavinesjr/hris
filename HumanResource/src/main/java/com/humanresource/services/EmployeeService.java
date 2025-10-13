package com.humanresource.services;

import com.humanresource.dtos.EmployeeDTO;

import java.util.List;
import java.util.Map;

public interface EmployeeService {

    String loginEmployee(String employeeNo, String employeePassword);

    EmployeeDTO createEmployee(EmployeeDTO employeeDTO) throws Exception;

    EmployeeDTO updateEmployee(Long employeeId, Map<String, Object> updates) throws Exception;

    EmployeeDTO getEmployeeDisplayById(Long employeeId) throws Exception;

    List<EmployeeDTO> getAllEmployeeNoAndName() throws Exception;

}
