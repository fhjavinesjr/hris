package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public class DayEquivalentMinutesDTO implements Serializable {

    private Long dayEquivalentMinutesId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime effectivityDate;
    private String minutes;
    private String minutesEquivalent;

    public DayEquivalentMinutesDTO() {

    }

    public DayEquivalentMinutesDTO(Long dayEquivalentMinutesId, LocalDateTime effectivityDate, String minutes, String minutesEquivalent) {
        this.dayEquivalentMinutesId = dayEquivalentMinutesId;
        this.effectivityDate = effectivityDate;
        this.minutes = minutes;
        this.minutesEquivalent = minutesEquivalent;
    }

    public Long getDayEquivalentMinutesId() {
        return dayEquivalentMinutesId;
    }

    public void setDayEquivalentMinutesId(Long dayEquivalentMinutesId) {
        this.dayEquivalentMinutesId = dayEquivalentMinutesId;
    }

    public LocalDateTime getEffectivityDate() {
        return effectivityDate;
    }

    public void setEffectivityDate(LocalDateTime effectivityDate) {
        this.effectivityDate = effectivityDate;
    }

    public String getMinutes() {
        return minutes;
    }

    public void setMinutes(String minutes) {
        this.minutes = minutes;
    }

    public String getMinutesEquivalent() {
        return minutesEquivalent;
    }

    public void setMinutesEquivalent(String minutesEquivalent) {
        this.minutesEquivalent = minutesEquivalent;
    }
}