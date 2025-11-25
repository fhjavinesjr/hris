package com.administrative.services;

import com.administrative.dtos.SalaryScheduleDTO;
import com.administrative.entitymodels.SalarySchedule;

import java.time.LocalDateTime;
import java.util.List;

public interface SalaryScheduleService {

    List<SalaryScheduleDTO> createSalarySchedule(List<SalaryScheduleDTO> salaryScheduleDTOList) throws Exception;

    List<SalaryScheduleDTO> getAllSalarySchedule() throws Exception;

    SalaryScheduleDTO getSalaryScheduleById(Long SalaryScheduleId) throws Exception;

    List<SalaryScheduleDTO> updateSalarySchedule(List<SalaryScheduleDTO> salaryScheduleDTOList) throws Exception;

    Boolean deleteSalarySchedule(LocalDateTime effectivityDate) throws Exception;

    SalaryScheduleDTO getSalaryScheduleByDateAssumptionAndSalaryGradeAndSalaryStep(LocalDateTime assumptionToDutyDate, Long salaryGrade, Long salaryStep) throws Exception;

}
