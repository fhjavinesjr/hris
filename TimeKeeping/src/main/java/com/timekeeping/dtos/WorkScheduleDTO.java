package com.timekeeping.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

public class WorkScheduleDTO implements Serializable {

    private Long wsId;

    @NotBlank(message = "Employee ID is mandatory")
    private String employeeId;

    @NotBlank(message = "Time Shift Code is mandatory")
    private String tsCode;

    @NotNull(message = "Work Schedule Date/Time is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime wsDateTime;

    public WorkScheduleDTO() {

    }

    public WorkScheduleDTO(Long wsId, String employeeId, String tsCode, LocalDateTime wsDateTime) {
        this.wsId = wsId;
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
}