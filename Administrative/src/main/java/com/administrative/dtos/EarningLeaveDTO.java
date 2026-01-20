package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EarningLeaveDTO implements Serializable {

    private Long earningLeaveId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime effectivityDate;
    private String day;
    private String earn;

    public EarningLeaveDTO() {

    }

    public EarningLeaveDTO(Long earningLeaveId, LocalDateTime effectivityDate, String day, String earn) {
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