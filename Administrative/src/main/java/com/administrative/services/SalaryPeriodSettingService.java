package com.administrative.services;

import com.administrative.dtos.SalaryPeriodSettingDTO;

import java.util.List;

public interface SalaryPeriodSettingService {

    SalaryPeriodSettingDTO create(SalaryPeriodSettingDTO dto) throws Exception;

    List<SalaryPeriodSettingDTO> getAll() throws Exception;

    /**
     * Returns all active settings whose periodContext matches the requested context,
     * plus any settings with context "BOTH".
     *
     * @param context  "PAYROLL" or "LEAVE"
     */
    List<SalaryPeriodSettingDTO> getByContext(String context) throws Exception;

    SalaryPeriodSettingDTO update(Long id, SalaryPeriodSettingDTO dto) throws Exception;

    Boolean delete(Long id) throws Exception;
}
