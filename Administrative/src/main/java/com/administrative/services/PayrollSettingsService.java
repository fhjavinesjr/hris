package com.administrative.services;

import com.administrative.dtos.PayrollSettingsDTO;

import java.util.List;

public interface PayrollSettingsService {

    PayrollSettingsDTO createPayrollSettings(PayrollSettingsDTO dto) throws Exception;

    List<PayrollSettingsDTO> getAllPayrollSettings() throws Exception;

    PayrollSettingsDTO getCurrent() throws Exception;

    PayrollSettingsDTO updatePayrollSettings(Long id, PayrollSettingsDTO dto) throws Exception;

    Boolean deletePayrollSettings(Long id) throws Exception;

    Boolean updateHazardAutoCompute(Boolean autoCompute) throws Exception;
}
