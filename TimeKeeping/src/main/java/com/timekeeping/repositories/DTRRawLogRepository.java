package com.timekeeping.repositories;

import com.timekeeping.entitymodels.DTRRawLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DTRRawLogRepository extends JpaRepository<DTRRawLog, Long> {

    // All unprocessed logs for a specific employee, ordered by datetime ASC
    // Used by the processing service to pair TIME_IN / TIME_OUT in sequence
    List<DTRRawLog> findByEmployeeIdAndIsProcessedFalseOrderByLogDatetimeAsc(String employeeId);

    // Deterministic variant when multiple logs share the same timestamp
    List<DTRRawLog> findByEmployeeIdAndIsProcessedFalseOrderByLogDatetimeAscRawLogIdAsc(String employeeId);

    // All logs (processed or not) for an employee within a datetime range
    List<DTRRawLog> findByEmployeeIdAndLogDatetimeBetweenOrderByLogDatetimeAsc(
            String employeeId, LocalDateTime from, LocalDateTime to);

    // All logs linked to a specific processed segment (for audit/traceability)
    List<DTRRawLog> findByDtrSegment_DtrSegmentId(Long dtrSegmentId);

    // All employee IDs that still have unprocessed logs (used for batch processing)
    @Query("SELECT DISTINCT r.employeeId FROM DTRRawLog r WHERE r.isProcessed = false")
    List<String> findDistinctEmployeeIdsByIsProcessedFalse();
}

