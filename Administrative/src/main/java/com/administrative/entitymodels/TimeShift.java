package com.administrative.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalTime;

@Entity
@Table(name = "time_shift")
public class TimeShift implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timeShiftId")
    private Long timeShiftId;

    @NotBlank(message = "Code is mandatory")
    @Column(name = "tsCode", length = 100, unique = true, nullable = false)
    private String tsCode;

    @NotNull(message = "Time In is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "timeIn")
    private LocalTime timeIn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "breakOut")
    private LocalTime breakOut;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "breakIn")
    private LocalTime breakIn;

    @NotNull(message = "Time Out is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "timeOut")
    private LocalTime timeOut;

    public TimeShift() {

    }

    public TimeShift(String tsCode, LocalTime timeIn, LocalTime breakOut, LocalTime breakIn, LocalTime timeOut) {
        this.tsCode = tsCode;
        this.timeIn = timeIn;
        this.breakOut = breakOut;
        this.breakIn = breakIn;
        this.timeOut = timeOut;
    }

    public Long getTimeShiftId() {
        return timeShiftId;
    }

    public void setTimeShiftId(Long timeShiftId) {
        this.timeShiftId = timeShiftId;
    }

    public String getTsCode() {
        return tsCode;
    }

    public void setTsCode(String tsCode) {
        this.tsCode = tsCode;
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
}