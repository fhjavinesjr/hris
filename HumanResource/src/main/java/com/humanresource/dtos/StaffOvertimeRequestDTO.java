package com.humanresource.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class StaffOvertimeRequestDTO {
    private Long businessUnitId;
    private LocalDate dateFiled;
    private LocalDateTime dateTimeFrom;
    private LocalDateTime dateTimeTo;
    private String workType;
    private String dutyShiftCode;
    private String authorityReference;
    private Boolean emergencyPostFiling;
    private String emergencyJustification;
    private String purpose;
    private String expectedOutput;
    private List<StaffOvertimeParticipantDTO> participants;

    public Long getBusinessUnitId() { return businessUnitId; }
    public void setBusinessUnitId(Long businessUnitId) { this.businessUnitId = businessUnitId; }
    public LocalDate getDateFiled() { return dateFiled; }
    public void setDateFiled(LocalDate dateFiled) { this.dateFiled = dateFiled; }
    public LocalDateTime getDateTimeFrom() { return dateTimeFrom; }
    public void setDateTimeFrom(LocalDateTime dateTimeFrom) { this.dateTimeFrom = dateTimeFrom; }
    public LocalDateTime getDateTimeTo() { return dateTimeTo; }
    public void setDateTimeTo(LocalDateTime dateTimeTo) { this.dateTimeTo = dateTimeTo; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public String getDutyShiftCode() { return dutyShiftCode; }
    public void setDutyShiftCode(String dutyShiftCode) { this.dutyShiftCode = dutyShiftCode; }
    public String getAuthorityReference() { return authorityReference; }
    public void setAuthorityReference(String authorityReference) { this.authorityReference = authorityReference; }
    public Boolean getEmergencyPostFiling() { return emergencyPostFiling; }
    public void setEmergencyPostFiling(Boolean emergencyPostFiling) { this.emergencyPostFiling = emergencyPostFiling; }
    public String getEmergencyJustification() { return emergencyJustification; }
    public void setEmergencyJustification(String emergencyJustification) { this.emergencyJustification = emergencyJustification; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public List<StaffOvertimeParticipantDTO> getParticipants() { return participants; }
    public void setParticipants(List<StaffOvertimeParticipantDTO> participants) { this.participants = participants; }
}
