package com.timekeeping.services;

import com.timekeeping.dtos.AdmsDtrProcessResultDTO;

import java.time.LocalDateTime;

public interface AdmsDtrProcessingService {
    AdmsDtrProcessResultDTO processPendingPunches();

    /**
     * Immediately reconciles already-imported ADMS punches for one employee and
     * the selected DTR work-date range. This is called by DTR Search so the user
     * sees the latest staged punches without pressing a separate sync button.
     */
    AdmsDtrProcessResultDTO processEmployeePunches(
            String employeeId,
            LocalDateTime fromDate,
            LocalDateTime toDate);
}
