package com.timekeeping.services;

import com.timekeeping.dtos.DTRDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface DTRService {

    Boolean createEmployeeDTR(DTRDTO dtrdto) throws Exception;

    List<DTRDTO> getEmployeeDTR(String employeeId, LocalDateTime fromDate, LocalDateTime toDate) throws Exception;

}
