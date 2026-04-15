package com.administrative.services;

import com.administrative.dtos.EmployeeRequestDTO;

import java.util.List;

public interface EmployeeRequestService {

    EmployeeRequestDTO createEmployeeRequest(EmployeeRequestDTO employeeRequestDTO) throws Exception;

    List<EmployeeRequestDTO> getAllEmployeeRequests() throws Exception;

    EmployeeRequestDTO getEmployeeRequestById(Long employeeRequestId) throws Exception;

    EmployeeRequestDTO updateEmployeeRequest(Long employeeRequestId, EmployeeRequestDTO employeeRequestDTO) throws Exception;

    Boolean deleteEmployeeRequest(Long employeeRequestId) throws Exception;

}
