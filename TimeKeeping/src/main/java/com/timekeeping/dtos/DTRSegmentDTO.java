package com.timekeeping.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalTime;

public class DTRSegmentDTO implements Serializable {
    private Long dtrSegmentId;
    private Integer segmentNo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime timeIn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime breakOut;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime breakIn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime timeOut;
    private Integer workMinutes;
    private Integer lateMinutes;
    private Integer undertimeMinutes;
    private Integer overtimeMinutes;

    public DTRSegmentDTO() {

    }

    public DTRSegmentDTO(Long dtrSegmentId, Integer segmentNo, LocalTime timeIn, LocalTime breakOut, LocalTime breakIn, LocalTime timeOut, Integer workMinutes, Integer lateMinutes, Integer undertimeMinutes, Integer overtimeMinutes) {
        this.dtrSegmentId = dtrSegmentId;
        this.segmentNo = segmentNo;
        this.timeIn = timeIn;
        this.breakOut = breakOut;
        this.breakIn = breakIn;
        this.timeOut = timeOut;
        this.workMinutes = workMinutes;
        this.lateMinutes = lateMinutes;
        this.undertimeMinutes = undertimeMinutes;
        this.overtimeMinutes = overtimeMinutes;
    }

    public Long getDtrSegmentId() {
        return dtrSegmentId;
    }

    public void setDtrSegmentId(Long dtrSegmentId) {
        this.dtrSegmentId = dtrSegmentId;
    }

    public Integer getSegmentNo() {
        return segmentNo;
    }

    public void setSegmentNo(Integer segmentNo) {
        this.segmentNo = segmentNo;
    }

    public LocalTime getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(LocalTime timeIn) {
        this.timeIn = timeIn;
    }

    public LocalTime getBreakOut() {
        return breakOut;
    }

    public void setBreakOut(LocalTime breakOut) {
        this.breakOut = breakOut;
    }

    public LocalTime getBreakIn() {
        return breakIn;
    }

    public void setBreakIn(LocalTime breakIn) {
        this.breakIn = breakIn;
    }

    public LocalTime getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(LocalTime timeOut) {
        this.timeOut = timeOut;
    }

    public Integer getWorkMinutes() {
        return workMinutes;
    }

    public void setWorkMinutes(Integer workMinutes) {
        this.workMinutes = workMinutes;
    }

    public Integer getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(Integer lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    public Integer getUndertimeMinutes() {
        return undertimeMinutes;
    }

    public void setUndertimeMinutes(Integer undertimeMinutes) {
        this.undertimeMinutes = undertimeMinutes;
    }

    public Integer getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public void setOvertimeMinutes(Integer overtimeMinutes) {
        this.overtimeMinutes = overtimeMinutes;
    }

    /**
     * Computed field — not stored in DB.
     * Tells the UI whether this segment crosses midnight (overnight shift).
     * Logic: in 24hr format, timeOut < timeIn means overnight.
     *   Example: 9U = 18:00 → 06:00 → isOvernightShift = true  → UI shows "+1 day"
     *   Example: 9K = 06:00 → 18:00 → isOvernightShift = false → UI shows nothing extra
     */
    public boolean isOvernightShift() {
        if (timeIn == null || timeOut == null) return false;
        return timeOut.isBefore(timeIn);
    }
}

