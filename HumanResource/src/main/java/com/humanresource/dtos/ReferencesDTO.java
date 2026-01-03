package com.humanresource.dtos;

import java.io.Serializable;

public class ReferencesDTO implements Serializable {

    private Long referencesId;

    private Long personalDataId;

    private String refName;

    private String address;

    private String contactNo;

    public ReferencesDTO() {

    }

    public ReferencesDTO(Long referencesId, Long personalDataId, String refName, String address, String contactNo) {
        this.referencesId = referencesId;
        this.personalDataId = personalDataId;
        this.refName = refName;
        this.address = address;
        this.contactNo = contactNo;
    }

    public Long getReferencesId() {
        return referencesId;
    }

    public void setReferencesId(Long referencesId) {
        this.referencesId = referencesId;
    }

    public Long getPersonalDataId() {
        return personalDataId;
    }

    public void setPersonalDataId(Long personalDataId) {
        this.personalDataId = personalDataId;
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }
}