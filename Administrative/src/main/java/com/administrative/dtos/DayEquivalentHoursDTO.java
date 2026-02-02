package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public class DayEquivalentHoursDTO implements Serializable {

    private Long dayEquivalentHoursId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime effectivityDate;
    private String hours;
    private String hoursEquivalent;

    public DayEquivalentHoursDTO() {

    }

    public DayEquivalentHoursDTO(Long dayEquivalentHoursId, LocalDateTime effectivityDate, String hours, String hoursEquivalent) {
        this.dayEquivalentHoursId = dayEquivalentHoursId;
        this.effectivityDate = effectivityDate;
        this.hours = hours;
        this.hoursEquivalent = hoursEquivalent;
    }

    public Long getDayEquivalentHoursId() {
        return dayEquivalentHoursId;
    }

    public void setDayEquivalentHoursId(Long dayEquivalentHoursId) {
        this.dayEquivalentHoursId = dayEquivalentHoursId;
    }

    public LocalDateTime getEffectivityDate() {
        return effectivityDate;
    }

    public void setEffectivityDate(LocalDateTime effectivityDate) {
        this.effectivityDate = effectivityDate;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getHoursEquivalent() {
        return hoursEquivalent;
    }

    public void setHoursEquivalent(String hoursEquivalent) {
        this.hoursEquivalent = hoursEquivalent;
    }
}