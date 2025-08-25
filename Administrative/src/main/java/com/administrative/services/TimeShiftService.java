package com.administrative.services;

import com.administrative.dtos.TimeShiftDTO;

import java.sql.Time;
import java.util.List;

public interface TimeShiftService {

    TimeShiftDTO createTimeShift(TimeShiftDTO timeShiftDTO) throws Exception;

    List<TimeShiftDTO> getAllTimeShift() throws Exception;

}
