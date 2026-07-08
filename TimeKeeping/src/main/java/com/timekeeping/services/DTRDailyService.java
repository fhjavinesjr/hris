package com.timekeeping.services;

import com.timekeeping.dtos.DTRDailyDTO;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DTRDailyService {
    DTRDailyDTO createOrUpdateDTRDaily(DTRDailyDTO dtrDailyDTO);
    List<DTRDailyDTO> getEmployeeDTRDaily(String employeeId, LocalDateTime fromDate, LocalDateTime toDate);
    
    /**
     * Fetch bulk DTR summaries for all employees within a date range.
     * Used by Payroll service for batch computation.
     * 
     * @param from start date (inclusive)
     * @param to end date (inclusive)
     * @return List of daily summaries for all employees (format compatible with DtrDailySummaryDTO)
     */
    List<Map<String, Object>> getBulkDtrSummary(LocalDate from, LocalDate to);

    void generateDtrReport(String employeeId, LocalDate fromDate, LocalDate toDate, OutputStream out) throws Exception;
}


