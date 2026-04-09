package com.timekeeping.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class DTRDailyDTO implements Serializable {
    private Long dtrDailyId;
    private String employeeId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime workDate;
    private Integer totalWorkMinutes;
    private Integer totalLateMinutes;
    private Integer totalUndertimeMinutes;
    private Integer totalOvertimeMinutes;
    private String attendanceStatus;
    private List<DTRSegmentDTO> segments;

    public DTRDailyDTO() {

    }

    public DTRDailyDTO(Long dtrDailyId, String employeeId, LocalDateTime workDate, Integer totalWorkMinutes, Integer totalLateMinutes, Integer totalUndertimeMinutes, Integer totalOvertimeMinutes, String attendanceStatus, List<DTRSegmentDTO> segments) {
        this.dtrDailyId = dtrDailyId;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.totalWorkMinutes = totalWorkMinutes;
        this.totalLateMinutes = totalLateMinutes;
        this.totalUndertimeMinutes = totalUndertimeMinutes;
        this.totalOvertimeMinutes = totalOvertimeMinutes;
        this.attendanceStatus = attendanceStatus;
        this.segments = segments;
    }

    public Long getDtrDailyId() {
        return dtrDailyId;
    }

    public void setDtrDailyId(Long dtrDailyId) {
        this.dtrDailyId = dtrDailyId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDateTime workDate) {
        this.workDate = workDate;
    }

    public Integer getTotalWorkMinutes() {
        return totalWorkMinutes;
    }

    public void setTotalWorkMinutes(Integer totalWorkMinutes) {
        this.totalWorkMinutes = totalWorkMinutes;
    }

    public Integer getTotalLateMinutes() {
        return totalLateMinutes;
    }

    public void setTotalLateMinutes(Integer totalLateMinutes) {
        this.totalLateMinutes = totalLateMinutes;
    }

    public Integer getTotalUndertimeMinutes() {
        return totalUndertimeMinutes;
    }

    public void setTotalUndertimeMinutes(Integer totalUndertimeMinutes) {
        this.totalUndertimeMinutes = totalUndertimeMinutes;
    }

    public Integer getTotalOvertimeMinutes() {
        return totalOvertimeMinutes;
    }

    public void setTotalOvertimeMinutes(Integer totalOvertimeMinutes) {
        this.totalOvertimeMinutes = totalOvertimeMinutes;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public List<DTRSegmentDTO> getSegments() {
        return segments;
    }

    public void setSegments(List<DTRSegmentDTO> segments) {
        this.segments = segments;
    }
}

