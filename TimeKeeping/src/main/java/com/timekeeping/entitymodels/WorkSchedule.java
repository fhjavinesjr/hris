package com.timekeeping.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_schedule")
public class WorkSchedule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wsId")
    private Long wsId;

    @NotBlank(message = "Employee No is mandatory")
    @Column(name = "employeeNo")
    private String employeeNo;

    @NotBlank(message = "Time Shift Code is mandatory")
    @Column(name = "tsCode")
    private String tsCode;

    @NotBlank(message = "Work Schedule Date/Time is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    @Column(name = "wsDateTime")
    private LocalDateTime wsDateTime;

    public WorkSchedule() {

    }

    public WorkSchedule(String employeeNo, String tsCode, LocalDateTime wsDateTime) {
        this.employeeNo = employeeNo;
        this.tsCode = tsCode;
        this.wsDateTime = wsDateTime;
    }

    public Long getWsId() {
        return wsId;
    }

    public void setWsId(Long wsId) {
        this.wsId = wsId;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
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
}