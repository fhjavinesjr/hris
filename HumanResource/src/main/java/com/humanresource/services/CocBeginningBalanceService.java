package com.humanresource.services;

import com.humanresource.dtos.CocBeginningBalanceDTO;

public interface CocBeginningBalanceService {
    CocBeginningBalanceDTO save(Long employeeId, CocBeginningBalanceDTO dto) throws Exception;
    CocBeginningBalanceDTO getByEmployeeId(Long employeeId) throws Exception;
    Boolean deleteById(Long id) throws Exception;
}
