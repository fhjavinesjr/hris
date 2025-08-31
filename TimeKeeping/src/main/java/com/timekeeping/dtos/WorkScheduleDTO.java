package com.timekeeping.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

public class WorkScheduleDTO implements Serializable {

    private Long wsId;

    @NotBlank(message = "Employee No is mandatory")
    private String employeeNo;

    @NotBlank(message = "Time Shift Code is mandatory")
    private String tsCode;

    @NotNull(message = "Work Schedule Date/Time is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime wsDateTime;

    public WorkScheduleDTO() {

    }

    public WorkScheduleDTO(Long wsId, String employeeNo, String tsCode, LocalDateTime wsDateTime) {
        this.wsId = wsId;
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
