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

    @NotBlank(message = "Name is mandatory")
    @Column(name = "tsName", length = 100, unique = true, nullable = false)
    private String tsName;

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

    @Column(name = "tsFlexible")
    private Boolean tsFlexible;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "monInTimeLimit")
    private LocalTime monInTimeLimit;

    @Column(name = "tueInTimeLimit")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime tueInTimeLimit;

    @Column(name = "wedInTimeLimit")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime wedInTimeLimit;

    @Column(name = "thuInTimeLimit")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime thuInTimeLimit;

    @Column(name = "friInTimeLimit")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime friInTimeLimit;

    @Column(name = "satInTimeLimit")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime satInTimeLimit;

    @Column(name = "sunInTimeLimit")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime sunInTimeLimit;

    public TimeShift() {

    }

    public TimeShift(Long timeShiftId, String tsCode, String tsName, LocalTime timeIn, LocalTime breakOut, LocalTime breakIn, LocalTime timeOut, Boolean tsFlexible, LocalTime monInTimeLimit, LocalTime tueInTimeLimit, LocalTime wedInTimeLimit, LocalTime thuInTimeLimit, LocalTime friInTimeLimit, LocalTime satInTimeLimit, LocalTime sunInTimeLimit) {
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

    public TimeShift(String tsCode, String tsName, LocalTime timeIn, LocalTime breakOut, LocalTime breakIn, LocalTime timeOut, Boolean tsFlexible, LocalTime monInTimeLimit, LocalTime tueInTimeLimit, LocalTime wedInTimeLimit, LocalTime thuInTimeLimit, LocalTime friInTimeLimit, LocalTime satInTimeLimit, LocalTime sunInTimeLimit) {
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