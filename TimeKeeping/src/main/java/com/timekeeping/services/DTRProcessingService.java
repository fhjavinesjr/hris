package com.timekeeping.services;

public interface DTRProcessingService {

    // Process all unprocessed raw logs for a specific employee
    void processRawLogs(String employeeId);

    // Batch: process all employees that have unprocessed raw logs
    void processAllUnprocessedLogs();
}

