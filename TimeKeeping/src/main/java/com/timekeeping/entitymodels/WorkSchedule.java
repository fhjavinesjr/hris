package com.timekeeping.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_schedule")
public class WorkSchedule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wsId")
    private Long wsId;

    @NotBlank(message = "Employee ID is mandatory")
    @Column(name = "employeeId")
    private String employeeId;

    @Column(name = "tsCode")
    private String tsCode;

    @Column(name = "isDayOff", nullable = false)
    private Boolean isDayOff = false;

    @NotNull(message = "Work Schedule Date/Time is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "wsDateTime")
    private LocalDateTime wsDateTime;

    public WorkSchedule() {

    }

    public WorkSchedule(String employeeId, String tsCode, LocalDateTime wsDateTime) {
        this.employeeId = employeeId;
        this.tsCode = tsCode;
        this.wsDateTime = wsDateTime;
    }

    public Long getWsId() {
        return wsId;
    }

    public void setWsId(Long wsId) {
        this.wsId = wsId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getTsCode() {
        return tsCode;
    }

    public void setTsCode(String tsCode) {
        this.tsCode = tsCode;
    }

    public LocalDateTime getWsDateTime() {
        return wsDateTime;
    }

    public void setWsDateTime(LocalDateTime wsDateTime) {
        this.wsDateTime = wsDateTime;
    }

    public Boolean getIsDayOff() {
        return isDayOff;
    }

    public void setIsDayOff(Boolean isDayOff) {
        this.isDayOff = isDayOff;
    }
}