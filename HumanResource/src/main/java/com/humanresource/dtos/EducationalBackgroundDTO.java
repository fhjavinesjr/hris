package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EducationalBackgroundDTO implements Serializable {

    private Long educationalBackgroundId;

    @NotNull(message = "Personal Data ID is mandatory")
    private Long personalDataId;

    @NotBlank(message = "levelOfEducation is mandatory")
    private String levelOfEducation;

    @NotBlank(message = "nameOfSchool is mandatory")
    private String nameOfSchool;

    @NotBlank(message = "degreeCourse is mandatory")
    private String degreeCourse;

    @NotNull(message = "scoreGrade is mandatory")
    private Double scoreGrade;

    @NotNull(message = "yearGraduated is mandatory")
    private Long yearGraduated;

    @NotNull(message = "From Date is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime fromDate;

    @NotNull(message = "To Date is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime toDate;

    @NotNull(message = "honorsReceived is mandatory")
    private String honorsReceived;

    public EducationalBackgroundDTO() {

    }

    public EducationalBackgroundDTO(Long educationalBackgroundId, Long personalDataId, String levelOfEducation, String nameOfSchool, String degreeCourse, Double scoreGrade, Long yearGraduated, LocalDateTime fromDate, LocalDateTime toDate, String honorsReceived) {
        this.educationalBackgroundId = educationalBackgroundId;
        this.personalDataId = personalDataId;
        this.levelOfEducation = levelOfEducation;
        this.nameOfSchool = nameOfSchool;
        this.degreeCourse = degreeCourse;
        this.scoreGrade = scoreGrade;
        this.yearGraduated = yearGraduated;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.honorsReceived = honorsReceived;
    }

    public Long getEducationalBackgroundId() {
        return educationalBackgroundId;
    }

    public void setEducationalBackgroundId(Long educationalBackgroundId) {
        this.educationalBackgroundId = educationalBackgroundId;
    }

    public Long getPersonalDataId() {
        return personalDataId;
    }

    public void setPersonalDataId(Long personalDataId) {
        this.personalDataId = personalDataId;
    }

    public String getLevelOfEducation() {
        return levelOfEducation;
    }

    public void setLevelOfEducation(String levelOfEducation) {
        this.levelOfEducation = levelOfEducation;
    }

    public String getNameOfSchool() {
        return nameOfSchool;
    }

    public void setNameOfSchool(String nameOfSchool) {
        this.nameOfSchool = nameOfSchool;
    }

    public String getDegreeCourse() {
        return degreeCourse;
    }

    public void setDegreeCourse(String degreeCourse) {
        this.degreeCourse = degreeCourse;
    }

    public Double getScoreGrade() {
        return scoreGrade;
    }

    public void setScoreGrade(Double scoreGrade) {
        this.scoreGrade = scoreGrade;
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

    public Long getYearGraduated() {
        return yearGraduated;
    }

    public void setYearGraduated(Long yearGraduated) {
        this.yearGraduated = yearGraduated;
    }

    public String getHonorsReceived() {
        return honorsReceived;
    }

    public void setHonorsReceived(String honorsReceived) {
        this.honorsReceived = honorsReceived;
    }
}