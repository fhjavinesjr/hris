package com.administrative.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "dayequivalentminutes")
public class DayEquivalentMinutes implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dayEquivalentMinutesId")
    private Long dayEquivalentMinutesId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "effectivityDate")
    private LocalDateTime effectivityDate;

    @Column(name = "minutes")
    private String minutes;

    @Column(name = "minutesEquivalent")
    private String minutesEquivalent;

    public DayEquivalentMinutes() {

    }

    public DayEquivalentMinutes(Long dayEquivalentMinutesId, LocalDateTime effectivityDate, String minutes, String minutesEquivalent) {
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