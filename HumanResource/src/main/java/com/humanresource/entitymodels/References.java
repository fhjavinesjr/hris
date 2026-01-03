package com.humanresource.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "characterreferences")
public class References implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "referencesId")
    private Long referencesId;

    @Column(name = "personalDataId")
    private Long personalDataId;

    @Column(name = "refName")
    private String refName;

    @Column(name = "address")
    private String address;

    @Column(name = "contactNo")
    private String contactNo;

    public References() {

    }

    public References(Long referencesId, Long personalDataId, String refName, String address, String contactNo) {
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