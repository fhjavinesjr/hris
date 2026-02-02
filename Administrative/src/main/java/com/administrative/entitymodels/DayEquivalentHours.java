package com.administrative.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "dayequivalenthours")
public class DayEquivalentHours implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dayEquivalentHoursId")
    private Long dayEquivalentHoursId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "effectivityDate")
    private LocalDateTime effectivityDate;

    @Column(name = "hours")
    private String hours;

    @Column(name = "hoursEquivalent")
    private String hoursEquivalent;

    public DayEquivalentHours() {

    }

    public DayEquivalentHours(Long dayEquivalentHoursId, LocalDateTime effectivityDate, String hours, String hoursEquivalent) {
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