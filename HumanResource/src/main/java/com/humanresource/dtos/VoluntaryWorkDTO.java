package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;

import java.io.Serializable;
import java.time.LocalDateTime;

public class VoluntaryWorkDTO implements Serializable {

    private Long voluntaryWorkId;

    private Long personalDataId;

    private String organizationName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime fromDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime toDate;

    private Long voluntaryHrs;

    private String positionTitle;

    public VoluntaryWorkDTO() {

    }

    public VoluntaryWorkDTO(Long voluntaryWorkId, Long personalDataId, String organizationName, LocalDateTime fromDate, LocalDateTime toDate, Long voluntaryHrs, String positionTitle) {
        this.voluntaryWorkId = voluntaryWorkId;
        this.personalDataId = personalDataId;
        this.organizationName = organizationName;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.voluntaryHrs = voluntaryHrs;
        this.positionTitle = positionTitle;
    }

    public Long getVoluntaryWorkId() {
        return voluntaryWorkId;
    }

    public void setVoluntaryWorkId(Long voluntaryWorkId) {
        this.voluntaryWorkId = voluntaryWorkId;
    }

    public Long getPersonalDataId() {
        return personalDataId;
    }

    public void setPersonalDataId(Long personalDataId) {
        this.personalDataId = personalDataId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDateTime fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDateTime getToDate() {
        return toDate;
    }

    public void setToDate(LocalDateTime toDate) {
        this.toDate = toDate;
    }

    public Long getVoluntaryHrs() {
        return voluntaryHrs;
    }

    public void setVoluntaryHrs(Long voluntaryHrs) {
        this.voluntaryHrs = voluntaryHrs;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }
}