package com.payroll.services;

import com.payroll.dtos.AllowanceDTO;
import com.payroll.dtos.EarningAllowanceDTO;

import java.time.LocalDate;
import java.util.List;

public interface EarningAllowanceService {

    List<EarningAllowanceDTO> createBulkEarningAllowance(List<EarningAllowanceDTO> dtoList) throws Exception;

    List<EarningAllowanceDTO> getAllEarningAllowance() throws Exception;

    EarningAllowanceDTO updateEarningAllowance(Long id, EarningAllowanceDTO dto) throws Exception;

    Boolean deleteEarningAllowance(Long id) throws Exception;

    /**
     * Returns all earning allowances in AllowanceDTO format for payroll computation.
     * Filters allowances that are effective during the specified payroll period.
     *
     * @param from Start date of payroll period
     * @param to   End date of payroll period
     * @return List of allowances with proper type flags set for computation rules
     */
    List<AllowanceDTO> getBulkAllowancesForPayroll(LocalDate from, LocalDate to) throws Exception;
}
