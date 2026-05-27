package com.humanresource.services;

import com.humanresource.dtos.EmployeeDTO;
import com.humanresource.dtos.EmployeePayrollInfoResponse;

import java.util.List;
import java.util.Map;

public interface EmployeeService {

    void installAuth() throws Exception;

    String loginEmployee(String employeeNo, String employeePassword);

    EmployeeDTO createEmployee(EmployeeDTO employeeDTO) throws Exception;

    EmployeeDTO updateEmployee(Long employeeId, Map<String, Object> updates) throws Exception;

    EmployeeDTO getEmployeeDisplayById(Long employeeId) throws Exception;

    List<EmployeeDTO> getAllEmployeeNoAndName() throws Exception;

    EmployeeDTO updateEmployeePassword(Long employeeId, Map<String, Object> updates) throws Exception;

    void adminResetPassword(Long employeeId, String newPassword) throws Exception;

    /**
     * Returns payroll-relevant info for all employees with an active appointment.
     * isExcludedFromPayroll is derived from natureofappointment.isContractual.
     *
     * @param departmentCode optional filter (null = all departments)
     * @param employeeNo     optional filter (null = all employees)
     */
    List<EmployeePayrollInfoResponse> getPayrollInfoBulk(String departmentCode, String employeeNo);
}
