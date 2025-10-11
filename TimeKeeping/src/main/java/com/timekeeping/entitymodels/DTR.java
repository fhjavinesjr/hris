package com.timekeeping.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "dtr")
public class DTR implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dtrId")
    private Long dtrId;

    @NotBlank(message = "Employee ID is mandatory")
    @Column(name = "employeeId", length = 100, nullable = false)
    private String employeeId;

    @NotNull(message = "DTR Date is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "dtrDate")
    private LocalDateTime dtrDate;

    @NotBlank(message = "Work Date is mandatory")
    @Column(name = "workDate")
    private String workDate;

    @NotNull(message = "Time In is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "timeIn")
    private LocalTime timeIn;

    @NotNull(message = "Break Out is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "breakOut")
    private LocalTime breakOut;

    @NotNull(message = "Break In is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "breakIn")
    private LocalTime breakIn;

    @NotNull(message = "Time Out is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "timeOut")
    private LocalTime timeOut;

    @NotNull(message = "Late Minutes is mandatory")
    @Column(name = "lateMin")
    private Long lateMin;

    @NotNull(message = "Undertime Minutes is mandatory")
    @Column(name = "underMin")
    private Long underMin;

    public DTR() {

    }

    public DTR(Long dtrId, String employeeId, LocalDateTime dtrDate, String workDate, LocalTime timeIn, LocalTime breakOut, LocalTime breakIn, LocalTime timeOut, Long lateMin, Long underMin) {
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
