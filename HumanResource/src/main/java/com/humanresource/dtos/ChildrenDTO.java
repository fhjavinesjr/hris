package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ChildrenDTO implements Serializable {

    private Long childrenId;

    @NotNull(message = "Personal Data ID is mandatory")
    private Long personalDataId;

    @NotBlank(message = "Children fullname is mandatory")
    private String childFullname;

    @NotNull(message = "Date of Birth is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime dob;

    public ChildrenDTO() {

    }

    public ChildrenDTO(Long childrenId, Long personalDataId, String childFullname, LocalDateTime dob) {
        this.childrenId = childrenId;
        this.personalDataId = personalDataId;
        this.childFullname = childFullname;
        this.dob = dob;
    }

    public Long getChildrenId() {
        return childrenId;
    }

    public void setChildrenId(Long childrenId) {
        this.childrenId = childrenId;
    }

    public Long getPersonalDataId() {
        return personalDataId;
    }

    public void setPersonalDataId(Long personalDataId) {
        this.personalDataId = personalDataId;
    }

    public String getChildFullname() {
        return childFullname;
    }

    public void setChildFullname(String childFullname) {
        this.childFullname = childFullname;
    }

    public LocalDateTime getDob() {
        return dob;
    }

    public void setDob(LocalDateTime dob) {
        this.dob = dob;
    }
}