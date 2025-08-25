package com.timekeeping.services;

import com.timekeeping.dtos.WorkScheduleDTO;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkScheduleService {

    WorkScheduleDTO createWorkSchedule(WorkScheduleDTO workScheduleDTO) throws Exception;

}
