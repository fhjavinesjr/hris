package com.humanresource.services;

import com.humanresource.dtos.EmployeeMweStatusDTO;
import java.util.List;

public interface EmployeeMweStatusService {
    List<EmployeeMweStatusDTO> getAll(String employeeNo);
    EmployeeMweStatusDTO create(EmployeeMweStatusDTO request, String actor);
    EmployeeMweStatusDTO update(Long id, EmployeeMweStatusDTO request, String actor);
    void delete(Long id);
}
