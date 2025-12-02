package com.humanresource.services;

import com.humanresource.dtos.EmployeeAppointmentDTO;

import java.util.List;

public interface EmployeeAppointmentService {

    EmployeeAppointmentDTO createEmployeeAppointment(EmployeeAppointmentDTO employeeAppointmentDTO) throws Exception;

    List<EmployeeAppointmentDTO> getAllEmployeeAppointment() throws Exception;

    List<EmployeeAppointmentDTO> getAllEmployeeAppointmentByEmployeeId(Long employeeId) throws Exception;

    EmployeeAppointmentDTO getLatestEmployeeAppointmentByEmployeeId(Long employeeId) throws Exception;

    EmployeeAppointmentDTO getEmployeeAppointmentById(Long employeeAppointmentId) throws Exception;

    EmployeeAppointmentDTO updateEmployeeAppointment(Long employeeAppointmentId, EmployeeAppointmentDTO employeeAppointmentDTO) throws Exception;

    Boolean deleteEmployeeAppointment(Long employeeAppointmentId) throws Exception;

    List<EmployeeAppointmentDTO> getByJobPositionId(Long jobPositionId) throws Exception;

}
