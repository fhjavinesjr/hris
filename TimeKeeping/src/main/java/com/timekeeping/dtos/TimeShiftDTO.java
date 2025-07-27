package com.timekeeping.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeShiftDTO implements Serializable {

    @NotNull(message = "Date From is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    private LocalDateTime dateFrom;

    @NotNull(message = "Date To is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    private LocalDateTime dateTo;

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

    private Boolean monRestDay;

    private Boolean tuesRestDay;

    private Boolean wedRestDay;

    private Boolean thursRestDay;

    private Boolean friRestDay;

    private Boolean satRestDay;

    private Boolean sunRestDay;

    private Boolean applyToAll;

    public TimeShiftDTO() {

    }

    public TimeShiftDTO(LocalDateTime dateFrom, LocalDateTime dateTo, LocalTime timeIn, LocalTime breakOut, LocalTime breakIn, LocalTime timeOut, Boolean monRestDay, Boolean tuesRestDay, Boolean wedRestDay, Boolean thursRestDay, Boolean friRestDay, Boolean satRestDay, Boolean sunRestDay, Boolean applyToAll) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.timeIn = timeIn;
        this.breakOut = breakOut;
        this.breakIn = breakIn;
        this.timeOut = timeOut;
        this.monRestDay = monRestDay;
        this.tuesRestDay = tuesRestDay;
        this.wedRestDay = wedRestDay;
        this.thursRestDay = thursRestDay;
        this.friRestDay = friRestDay;
        this.satRestDay = satRestDay;
        this.sunRestDay = sunRestDay;
        this.applyToAll = applyToAll;
    }

    public LocalDateTime getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDateTime dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDateTime getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDateTime dateTo) {
        this.dateTo = dateTo;
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

    public Boolean getMonRestDay() {
        return monRestDay;
    }

    public void setMonRestDay(Boolean monRestDay) {
        this.monRestDay = monRestDay;
    }

    public Boolean getTuesRestDay() {
        return tuesRestDay;
    }

    public void setTuesRestDay(Boolean tuesRestDay) {
        this.tuesRestDay = tuesRestDay;
    }

    public Boolean getWedRestDay() {
        return wedRestDay;
    }

    public void setWedRestDay(Boolean wedRestDay) {
        this.wedRestDay = wedRestDay;
    }

    public Boolean getThursRestDay() {
        return thursRestDay;
    }

    public void setThursRestDay(Boolean thursRestDay) {
        this.thursRestDay = thursRestDay;
    }

    public Boolean getFriRestDay() {
        return friRestDay;
    }

    public void setFriRestDay(Boolean friRestDay) {
        this.friRestDay = friRestDay;
    }

    public Boolean getSatRestDay() {
        return satRestDay;
    }

    public void setSatRestDay(Boolean satRestDay) {
        this.satRestDay = satRestDay;
    }

    public Boolean getSunRestDay() {
        return sunRestDay;
    }

    public void setSunRestDay(Boolean sunRestDay) {
        this.sunRestDay = sunRestDay;
    }

    public Boolean getApplyToAll() {
        return applyToAll;
    }

    public void setApplyToAll(Boolean applyToAll) {
        this.applyToAll = applyToAll;
    }
}
