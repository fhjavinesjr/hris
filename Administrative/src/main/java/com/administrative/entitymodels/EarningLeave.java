package com.administrative.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "earningleave")
public class EarningLeave implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "earningLeaveId")
    private Long earningLeaveId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "effectivityDate")
    private LocalDateTime effectivityDate;

    @Column(name = "day")
    private String day;

    @Column(name = "earn")
    private String earn;

    public EarningLeave() {

    }

    public EarningLeave(Long earningLeaveId, LocalDateTime effectivityDate, String day, String earn) {
        this.earningLeaveId = earningLeaveId;
        this.effectivityDate = effectivityDate;
        this.day = day;
        this.earn = earn;
    }

    public Long getEarningLeaveId() {
        return earningLeaveId;
    }

    public void setEarningLeaveId(Long earningLeaveId) {
        this.earningLeaveId = earningLeaveId;
    }

    public LocalDateTime getEffectivityDate() {
        return effectivityDate;
    }

    public void setEffectivityDate(LocalDateTime effectivityDate) {
        this.effectivityDate = effectivityDate;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getEarn() {
        return earn;
    }

    public void setEarn(String earn) {
        this.earn = earn;
    }
}