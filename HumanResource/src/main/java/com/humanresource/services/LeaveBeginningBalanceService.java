package com.humanresource.services;

import com.humanresource.dtos.LeaveBeginningBalanceDTO;

import java.util.List;

public interface LeaveBeginningBalanceService {
    List<LeaveBeginningBalanceDTO> saveAll(Long employeeId, List<LeaveBeginningBalanceDTO> dtoList) throws Exception;
    List<LeaveBeginningBalanceDTO> getAll() throws Exception;
    List<LeaveBeginningBalanceDTO> getAllByEmployeeId(Long employeeId) throws Exception;
    Boolean deleteById(Long id) throws Exception;
}
