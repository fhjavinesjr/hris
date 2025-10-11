package com.timekeeping.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DTRDTO implements Serializable {

    private Long dtrId;

    @NotBlank(message = "Employee ID is mandatory")
    private String employeeId;

    @NotNull(message = "DTR Date is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime dtrDate;

    @NotBlank(message = "Work Date is mandatory")
    private String workDate;

    @NotNull(message = "Time In is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime timeIn;

    @NotNull(message = "Break Out is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime breakOut;

    @NotNull(message = "Break In is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime breakIn;

    @NotNull(message = "Time Out is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime timeOut;
    private Long lateMin;
    private Long underMin;

    public DTRDTO() {

    }

    public DTRDTO(Long dtrId, String employeeId, LocalDateTime dtrDate, String workDate, LocalTime timeIn, LocalTime breakOut, LocalTime breakIn, LocalTime timeOut, Long lateMin, Long underMin) {
        this.dtrId = dtrId;
        this.employeeId = employeeId;
        this.dtrDate = dtrDate;
        this.workDate = workDate;
        this.timeIn = timeIn;
        this.breakOut = breakOut;
        this.breakIn = breakIn;
        this.timeOut = timeOut;
        this.lateMin = lateMin;
        this.underMin = underMin;
    }

    public Long getDtrId() {
        return dtrId;
    }

    public void setDtrId(Long dtrId) {
        this.dtrId = dtrId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getDtrDate() {
        return dtrDate;
    }

    public void setDtrDate(LocalDateTime dtrDate) {
        this.dtrDate = dtrDate;
    }

    public String getWorkDate() {
        return workDate;
    }

    public void setWorkDate(String workDate) {
        this.workDate = workDate;
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

    public Long getLateMin() {
        return lateMin;
    }

    public void setLateMin(Long lateMin) {
        this.lateMin = lateMin;
    }

    public Long getUnderMin() {
        return underMin;
    }

    public void setUnderMin(Long underMin) {
        this.underMin = underMin;
    }
}
