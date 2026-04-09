package com.timekeeping.controllers;
import com.timekeeping.entitymodels.DTRRawLog;
import com.timekeeping.repositories.DTRRawLogRepository;
import com.timekeeping.services.DTRProcessingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
@RestController
@RequestMapping("/api/dtr-raw-log")
public class DTRRawLogController {
    private final DTRRawLogRepository rawLogRepository;
    private final DTRProcessingService dtrProcessingService;
    public DTRRawLogController(DTRRawLogRepository rawLogRepository, DTRProcessingService dtrProcessingService) {
        this.rawLogRepository = rawLogRepository;
        this.dtrProcessingService = dtrProcessingService;
    }
    @PostMapping
    public ResponseEntity<DTRRawLog> insertRawLog(@RequestBody DTRRawLog rawLog) {
        rawLog.setIsProcessed(false);
        rawLog.setDtrSegment(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(rawLogRepository.save(rawLog));
    }
    @GetMapping
    public ResponseEntity<List<DTRRawLog>> getRawLogs(
            @RequestParam String employeeId,
            @RequestParam @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime toDate) {
        return ResponseEntity.ok(
                rawLogRepository.findByEmployeeIdAndLogDatetimeBetweenOrderByLogDatetimeAsc(employeeId, fromDate, toDate));
    }
    @PostMapping("/process/{employeeId}")
    public ResponseEntity<String> processEmployee(@PathVariable String employeeId) {
        dtrProcessingService.processRawLogs(employeeId);
        return ResponseEntity.ok("Processing complete for: " + employeeId);
    }
    @PostMapping("/process/all")
    public ResponseEntity<String> processAll() {
        dtrProcessingService.processAllUnprocessedLogs();
        return ResponseEntity.ok("Batch processing complete.");
    }
}