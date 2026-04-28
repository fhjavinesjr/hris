package com.humanresource.services;

import com.humanresource.dtos.LeaveInformationDTO;

import java.time.LocalDate;
import java.util.List;

public interface LeaveInformationService {

    List<LeaveInformationDTO> getByEmployeeId(Long employeeId) throws Exception;

    List<LeaveInformationDTO> getByPeriod(LocalDate cutoffStart, LocalDate cutoffEnd) throws Exception;

    List<LeaveInformationDTO> getBySalaryPeriodSettingId(Long salaryPeriodSettingId) throws Exception;

    LeaveInformationDTO lock(Long leaveInformationId) throws Exception;

    LeaveInformationDTO unlock(Long leaveInformationId) throws Exception;

    Boolean delete(Long leaveInformationId) throws Exception;
}
