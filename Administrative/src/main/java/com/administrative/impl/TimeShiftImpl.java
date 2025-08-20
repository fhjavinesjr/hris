package com.administrative.impl;

import com.administrative.dtos.TimeShiftDTO;
import com.administrative.entitymodels.TimeShift;
import com.administrative.repositories.TimeShiftRepository;
import com.administrative.services.TimeShiftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TimeShiftImpl implements TimeShiftService {

    private static final Logger log = LoggerFactory.getLogger(TimeShiftImpl.class);
    private final TimeShiftRepository timeShiftRepository;

    public TimeShiftImpl(TimeShiftRepository timeShiftRepository) {
        this.timeShiftRepository = timeShiftRepository;
    }

    @Override
    public TimeShiftDTO createTimeShift(TimeShiftDTO timeShiftDTO) throws Exception {
        try {
            TimeShift timeShift = new TimeShift(timeShiftDTO.getTsCode(), timeShiftDTO.getTimeIn(), timeShiftDTO.getBreakOut(), timeShiftDTO.getBreakIn(), timeShiftDTO.getTimeOut());
            timeShiftRepository.save(timeShift);

            return timeShiftDTO;
        } catch(Exception e) {
            log.error("Error failed save TimeShift: {}", e.getMessage());
            return null;
        }
    }
}
