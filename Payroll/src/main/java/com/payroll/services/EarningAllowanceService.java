package com.payroll.services;

import com.payroll.dtos.EarningAllowanceDTO;

import java.util.List;

public interface EarningAllowanceService {

    List<EarningAllowanceDTO> createBulkEarningAllowance(List<EarningAllowanceDTO> dtoList) throws Exception;

    List<EarningAllowanceDTO> getAllEarningAllowance() throws Exception;

    EarningAllowanceDTO updateEarningAllowance(Long id, EarningAllowanceDTO dto) throws Exception;

    Boolean deleteEarningAllowance(Long id) throws Exception;
}
