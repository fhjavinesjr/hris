package com.timekeeping.services;

import com.timekeeping.dtos.WorkScheduleDTO;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkScheduleService {

    WorkScheduleDTO createWorkSchedule(WorkScheduleDTO workScheduleDTO) throws Exception;

    List<WorkScheduleDTO> getAllWorkSchedule(String employeeId, LocalDateTime monthStart, LocalDateTime monthEnd) throws Exception;

    WorkScheduleDTO updateWorkSchedule(Long wsId, WorkScheduleDTO workScheduleDTO) throws Exception;

    Boolean deleteWorkSchedule(Long wsId) throws Exception;

}
