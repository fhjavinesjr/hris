package com.timekeeping.impl;

import com.timekeeping.dtos.WorkScheduleDTO;
import com.timekeeping.entitymodels.WorkSchedule;
import com.timekeeping.repositories.WorkScheduleRepository;
import com.timekeeping.services.WorkScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WorkScheduleImpl implements WorkScheduleService {

    private static final Logger log = LoggerFactory.getLogger(WorkScheduleImpl.class);
    private final WorkScheduleRepository workScheduleRepository;

    public WorkScheduleImpl(WorkScheduleRepository workScheduleRepository) {
        this.workScheduleRepository = workScheduleRepository;
    }

    @Override
    public WorkScheduleDTO createWorkSchedule(WorkScheduleDTO workScheduleDTO) throws Exception {
        try {
            WorkSchedule workSchedule = new WorkSchedule(workScheduleDTO.getEmployeeNo(), workScheduleDTO.getTsCode(), workScheduleDTO.getWsDateTime());
            workScheduleRepository.save(workSchedule);
            return workScheduleDTO;
        } catch (Exception e) {
            log.info("Creation of Work Schedule Failed: {}", e.getMessage());
            return null;
        }
    }
}