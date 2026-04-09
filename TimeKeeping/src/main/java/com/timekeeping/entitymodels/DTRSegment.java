package com.timekeeping.entitymodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalTime;

@Entity
@Table(name = "dtr_segment")
public class DTRSegment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dtr_segment_id")
    private Long dtrSegmentId;

    // @JsonIgnore prevents circular serialization:
    // DTRSegment → DTRDaily → List<DTRSegment> → DTRDaily → ... (infinite loop)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dtr_daily_id", nullable = false)
    private DTRDaily dtrDaily;

    @Column(name = "segment_no", nullable = false)
    private Integer segmentNo;

    @Column(name = "time_in", nullable = false)
    private LocalTime timeIn;

    @Column(name = "break_out")
    private LocalTime breakOut;

    @Column(name = "break_in")
    private LocalTime breakIn;

    @Column(name = "time_out", nullable = false)
    private LocalTime timeOut;

    @Column(name = "work_minutes", nullable = false)
    private Integer workMinutes = 0;

    @Column(name = "late_minutes", nullable = false)
    private Integer lateMinutes = 0;

    @Column(name = "undertime_minutes", nullable = false)
    private Integer undertimeMinutes = 0;

    @Column(name = "overtime_minutes", nullable = false)
    private Integer overtimeMinutes = 0;

    // Getters and setters
    public Long getDtrSegmentId() { return dtrSegmentId; }
    public void setDtrSegmentId(Long dtrSegmentId) { this.dtrSegmentId = dtrSegmentId; }
    public DTRDaily getDtrDaily() { return dtrDaily; }
    public void setDtrDaily(DTRDaily dtrDaily) { this.dtrDaily = dtrDaily; }
    public Integer getSegmentNo() { return segmentNo; }
    public void setSegmentNo(Integer segmentNo) { this.segmentNo = segmentNo; }
    public LocalTime getTimeIn() { return timeIn; }
    public void setTimeIn(LocalTime timeIn) { this.timeIn = timeIn; }
    public LocalTime getBreakOut() { return breakOut; }
    public void setBreakOut(LocalTime breakOut) { this.breakOut = breakOut; }
    public LocalTime getBreakIn() { return breakIn; }
    public void setBreakIn(LocalTime breakIn) { this.breakIn = breakIn; }
    public LocalTime getTimeOut() { return timeOut; }
    public void setTimeOut(LocalTime timeOut) { this.timeOut = timeOut; }
    public Integer getWorkMinutes() { return workMinutes; }
    public void setWorkMinutes(Integer workMinutes) { this.workMinutes = workMinutes; }
    public Integer getLateMinutes() { return lateMinutes; }
    public void setLateMinutes(Integer lateMinutes) { this.lateMinutes = lateMinutes; }
    public Integer getUndertimeMinutes() { return undertimeMinutes; }
    public void setUndertimeMinutes(Integer undertimeMinutes) { this.undertimeMinutes = undertimeMinutes; }
    public Integer getOvertimeMinutes() { return overtimeMinutes; }
    public void setOvertimeMinutes(Integer overtimeMinutes) { this.overtimeMinutes = overtimeMinutes; }

    /**
     * Computed: no DB column needed.
     * In 24hr format, if timeOut < timeIn, the shift crosses midnight (overnight).
     * Example: 9U = 18:00 → 06:00 → 06:00 < 18:00 → overnight ✓
     * Example: 9K = 06:00 → 18:00 → 18:00 > 06:00 → same day ✓
     */
    @Transient
    public boolean isOvernightShift() {
        if (timeIn == null || timeOut == null) return false;
        return timeOut.isBefore(timeIn);
    }
}

