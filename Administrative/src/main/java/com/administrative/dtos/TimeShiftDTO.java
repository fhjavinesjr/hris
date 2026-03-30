package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalTime;

public class TimeShiftDTO implements Serializable {

    private Long timeShiftId;

    @NotBlank(message = "Code is mandatory")
    private String tsCode;

    @NotBlank(message = "Name is mandatory")
    private String tsName;

    @NotNull(message = "Time In is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime timeIn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime breakOut;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime breakIn;

    @NotNull(message = "Time Out is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime timeOut;

    private Boolean tsFlexible;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime monInTimeLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime tueInTimeLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime wedInTimeLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime thuInTimeLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime friInTimeLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime satInTimeLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime sunInTimeLimit;

    public TimeShiftDTO() {

    }

    public TimeShiftDTO(Long timeShiftId, String tsCode, String tsName, LocalTime timeIn, LocalTime breakOut, LocalTime breakIn, LocalTime timeOut, Boolean tsFlexible, LocalTime monInTimeLimit, LocalTime tueInTimeLimit, LocalTime wedInTimeLimit, LocalTime thuInTimeLimit, LocalTime friInTimeLimit, LocalTime satInTimeLimit, LocalTime sunInTimeLimit) {
        this.timeShiftId = timeShiftId;
        this.tsCode = tsCode;
        this.tsName = tsName;
        this.timeIn = timeIn;
        this.breakOut = breakOut;
        this.breakIn = breakIn;
        this.timeOut = timeOut;
        this.tsFlexible = tsFlexible;
        this.monInTimeLimit = monInTimeLimit;
        this.tueInTimeLimit = tueInTimeLimit;
        this.wedInTimeLimit = wedInTimeLimit;
        this.thuInTimeLimit = thuInTimeLimit;
        this.friInTimeLimit = friInTimeLimit;
        this.satInTimeLimit = satInTimeLimit;
        this.sunInTimeLimit = sunInTimeLimit;
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

    public String getTsName() {
        return tsName;
    }

    public void setTsName(String tsName) {
        this.tsName = tsName;
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

    public Boolean getTsFlexible() {
        return tsFlexible;
    }

    public void setTsFlexible(Boolean tsFlexible) {
        this.tsFlexible = tsFlexible;
    }

    public LocalTime getMonInTimeLimit() {
        return monInTimeLimit;
    }

    public void setMonInTimeLimit(LocalTime monInTimeLimit) {
        this.monInTimeLimit = monInTimeLimit;
    }

    public LocalTime getTueInTimeLimit() {
        return tueInTimeLimit;
    }

    public void setTueInTimeLimit(LocalTime tueInTimeLimit) {
        this.tueInTimeLimit = tueInTimeLimit;
    }

    public LocalTime getWedInTimeLimit() {
        return wedInTimeLimit;
    }

    public void setWedInTimeLimit(LocalTime wedInTimeLimit) {
        this.wedInTimeLimit = wedInTimeLimit;
    }

    public LocalTime getThuInTimeLimit() {
        return thuInTimeLimit;
    }

    public void setThuInTimeLimit(LocalTime thuInTimeLimit) {
        this.thuInTimeLimit = thuInTimeLimit;
    }

    public LocalTime getFriInTimeLimit() {
        return friInTimeLimit;
    }

    public void setFriInTimeLimit(LocalTime friInTimeLimit) {
        this.friInTimeLimit = friInTimeLimit;
    }

    public LocalTime getSatInTimeLimit() {
        return satInTimeLimit;
    }

    public void setSatInTimeLimit(LocalTime satInTimeLimit) {
        this.satInTimeLimit = satInTimeLimit;
    }

    public LocalTime getSunInTimeLimit() {
        return sunInTimeLimit;
    }

    public void setSunInTimeLimit(LocalTime sunInTimeLimit) {
        this.sunInTimeLimit = sunInTimeLimit;
    }
}