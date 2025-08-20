package com.administrative.services;

import com.administrative.dtos.TimeShiftDTO;

public interface TimeShiftService {

    TimeShiftDTO createTimeShift(TimeShiftDTO timeShiftDTO) throws Exception;

}
