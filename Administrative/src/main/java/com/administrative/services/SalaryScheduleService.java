package com.administrative.services;

import com.administrative.dtos.SalaryScheduleDTO;

import java.util.List;

public interface SalaryScheduleService {

    List<SalaryScheduleDTO> createSalarySchedule(List<SalaryScheduleDTO> salaryScheduleDTOList) throws Exception;

    List<SalaryScheduleDTO> getAllSalarySchedule() throws Exception;

    SalaryScheduleDTO getSalaryScheduleById(Long SalaryScheduleId) throws Exception;

    SalaryScheduleDTO updateSalarySchedule(Long SalaryScheduleId, SalaryScheduleDTO salaryScheduleDTO) throws Exception;

    Boolean deleteSalarySchedule(Long SalaryScheduleId) throws Exception;

}
