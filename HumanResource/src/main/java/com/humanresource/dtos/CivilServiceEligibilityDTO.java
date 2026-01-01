package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CivilServiceEligibilityDTO implements Serializable {

    private Long civilServiceEligibilityId;

    @Column(name = "personalDataId")
    private Long personalDataId;

    @Column(name = "careerServiceName")
    private String careerServiceName;

    @Column(name = "civilServiceRating")
    private Double civilServiceRating;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "dateOfExamination")
    private LocalDateTime dateOfExamination;

    @Column(name = "placeOfExamination")
    private String placeOfExamination;

    @Column(name = "licenseNumber")
    private String licenseNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "licenseValidityDate")
    private LocalDateTime licenseValidityDate;

    public CivilServiceEligibilityDTO() {

    }

    public CivilServiceEligibilityDTO(Long civilServiceEligibilityId, Long personalDataId, String careerServiceName, Double civilServiceRating, LocalDateTime dateOfExamination, String placeOfExamination, String licenseNumber, LocalDateTime licenseValidityDate) {
        this.civilServiceEligibilityId = civilServiceEligibilityId;
        this.personalDataId = personalDataId;
        this.careerServiceName = careerServiceName;
        this.civilServiceRating = civilServiceRating;
        this.dateOfExamination = dateOfExamination;
        this.placeOfExamination = placeOfExamination;
        this.licenseNumber = licenseNumber;
        this.licenseValidityDate = licenseValidityDate;
    }

    public Long getCivilServiceEligibilityId() {
        return civilServiceEligibilityId;
    }

    public void setCivilServiceEligibilityId(Long civilServiceEligibilityId) {
        this.civilServiceEligibilityId = civilServiceEligibilityId;
    }

    public Long getPersonalDataId() {
        return personalDataId;
    }

    public void setPersonalDataId(Long personalDataId) {
        this.personalDataId = personalDataId;
    }

    public String getCareerServiceName() {
        return careerServiceName;
    }

    public void setCareerServiceName(String careerServiceName) {
        this.careerServiceName = careerServiceName;
    }

    public Double getCivilServiceRating() {
        return civilServiceRating;
    }

    public void setCivilServiceRating(Double civilServiceRating) {
        this.civilServiceRating = civilServiceRating;
    }

    public LocalDateTime getDateOfExamination() {
        return dateOfExamination;
    }

    public void setDateOfExamination(LocalDateTime dateOfExamination) {
        this.dateOfExamination = dateOfExamination;
    }

    public String getPlaceOfExamination() {
        return placeOfExamination;
    }

    public void setPlaceOfExamination(String placeOfExamination) {
        this.placeOfExamination = placeOfExamination;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public LocalDateTime getLicenseValidityDate() {
        return licenseValidityDate;
    }

    public void setLicenseValidityDate(LocalDateTime licenseValidityDate) {
        this.licenseValidityDate = licenseValidityDate;
    }
}