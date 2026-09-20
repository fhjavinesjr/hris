package com.humanresource.dtos;

public class StaffOvertimeParticipantDTO {
    private Long employeeId;
    private Integer breakMinutes;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Integer getBreakMinutes() { return breakMinutes; }
    public void setBreakMinutes(Integer breakMinutes) { this.breakMinutes = breakMinutes; }
}
