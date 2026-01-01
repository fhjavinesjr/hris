package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LearningAndDevelopmentDTO implements Serializable {

    private Long learningAndDevelopmentId;

    private Long personalDataId;

    private String programName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime fromDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime toDate;

    private Long lndHrs;

    private String lndType;

    private String conductedBy;

    public LearningAndDevelopmentDTO() {

    }

    public LearningAndDevelopmentDTO(Long learningAndDevelopmentId, Long personalDataId, String programName, LocalDateTime fromDate, LocalDateTime toDate, Long lndHrs, String lndType, String conductedBy) {
        this.learningAndDevelopmentId = learningAndDevelopmentId;
        this.personalDataId = personalDataId;
        this.programName = programName;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.lndHrs = lndHrs;
        this.lndType = lndType;
        this.conductedBy = conductedBy;
    }

    public Long getLearningAndDevelopmentId() {
        return learningAndDevelopmentId;
    }

    public void setLearningAndDevelopmentId(Long learningAndDevelopmentId) {
        this.learningAndDevelopmentId = learningAndDevelopmentId;
    }

    public Long getPersonalDataId() {
        return personalDataId;
    }

    public void setPersonalDataId(Long personalDataId) {
        this.personalDataId = personalDataId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
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

    public Long getLndHrs() {
        return lndHrs;
    }

    public void setLndHrs(Long lndHrs) {
        this.lndHrs = lndHrs;
    }

    public String getLndType() {
        return lndType;
    }

    public void setLndType(String lndType) {
        this.lndType = lndType;
    }

    public String getConductedBy() {
        return conductedBy;
    }

    public void setConductedBy(String conductedBy) {
        this.conductedBy = conductedBy;
    }
}