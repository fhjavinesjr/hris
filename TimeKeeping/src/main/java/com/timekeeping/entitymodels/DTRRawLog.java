package com.timekeeping.entitymodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "dtr_raw_log")
public class DTRRawLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "raw_log_id")
    private Long rawLogId;

    @Column(name = "employee_id", nullable = false, length = 100)
    private String employeeId;

    // Full date + time of the punch (e.g., 2026-04-15 06:00:00)
    // This is the key field that enables correct overnight shift detection
    @Column(name = "log_datetime", nullable = false)
    private LocalDateTime logDatetime;

    // TIME_IN | TIME_OUT | BREAK_OUT | BREAK_IN
    @Column(name = "log_type", nullable = false, length = 20)
    private String logType;

    // false = not yet paired into a DTRSegment; true = already processed
    @Column(name = "is_processed", nullable = false)
    private Boolean isProcessed = false;

    // @JsonIgnore prevents circular serialization:
    // DTRRawLog → DTRSegment → DTRDaily → List<DTRSegment> → ... (infinite loop)
    // NULL until processed by DTRProcessingService.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dtr_segment_id")
    private DTRSegment dtrSegment;

    public DTRRawLog() {}

    public Long getRawLogId() { return rawLogId; }
    public void setRawLogId(Long rawLogId) { this.rawLogId = rawLogId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public LocalDateTime getLogDatetime() { return logDatetime; }
    public void setLogDatetime(LocalDateTime logDatetime) { this.logDatetime = logDatetime; }

    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }

    public Boolean getIsProcessed() { return isProcessed; }
    public void setIsProcessed(Boolean isProcessed) { this.isProcessed = isProcessed; }

    public DTRSegment getDtrSegment() { return dtrSegment; }
    public void setDtrSegment(DTRSegment dtrSegment) { this.dtrSegment = dtrSegment; }
}

