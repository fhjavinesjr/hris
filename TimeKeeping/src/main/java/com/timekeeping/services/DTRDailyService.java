package com.timekeeping.services;

import com.timekeeping.dtos.DTRDailyDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface DTRDailyService {
    DTRDailyDTO createOrUpdateDTRDaily(DTRDailyDTO dtrDailyDTO);
    List<DTRDailyDTO> getEmployeeDTRDaily(String employeeId, LocalDateTime fromDate, LocalDateTime toDate);
}

