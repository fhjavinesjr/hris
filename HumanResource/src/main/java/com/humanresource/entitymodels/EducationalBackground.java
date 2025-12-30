package com.humanresource.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "educationalbackground")
public class EducationalBackground implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "educationalBackgroundId")
    private Long educationalBackgroundId;

    @NotNull(message = "Personal Data ID is mandatory")
    @Column(name = "personalDataId")
    private Long personalDataId;

    @NotBlank(message = "levelOfEducation is mandatory")
    @Column(name = "levelOfEducation")
    private String levelOfEducation;

    @NotBlank(message = "nameOfSchool is mandatory")
    @Column(name = "nameOfSchool")
    private String nameOfSchool;

    @NotBlank(message = "degreeCourse is mandatory")
    @Column(name = "degreeCourse")
    private String degreeCourse;

    @NotNull(message = "scoreGrade is mandatory")
    @Column(name = "scoreGrade")
    private Double scoreGrade;

    @NotNull(message = "yearGraduated is mandatory")
    @Column(name = "yearGraduated")
    private Long yearGraduated;

    @NotNull(message = "From Date is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "fromDate")
    private LocalDateTime fromDate;

    @NotNull(message = "To Date is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "toDate")
    private LocalDateTime toDate;

    @NotNull(message = "honorsReceived is mandatory")
    @Column(name = "honorsReceived")
    private String honorsReceived;

    public EducationalBackground() {

    }

    public EducationalBackground(Long educationalBackgroundId, Long personalDataId, String levelOfEducation, String nameOfSchool, String degreeCourse, Double scoreGrade, Long yearGraduated, LocalDateTime fromDate, LocalDateTime toDate, String honorsReceived) {
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