package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Lob;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SettingsDTO implements Serializable {

    private Long settingsId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime systemStartDate;

    private String companyName;

    private String shortName;

    private String city;

    private String address;

    private String isoNo;

    private String zipCode;

    private String telMobileNo;

    private String emailAddress;

    private String tinNo;

    private String pagIbigNo;

    private String philHealthNo;

    private Boolean hospitalAgency;

    //Spring is expecting the raw file bytes inside the JSON body — not multipart form-data
    @Lob
    private byte[] leftHeaderLogo;

    //Spring is expecting the raw file bytes inside the JSON body — not multipart form-data
    @Lob
    private byte[] mainLogo;

    //Spring is expecting the raw file bytes inside the JSON body — not multipart form-data
    @Lob
    private byte[] rightHeaderLogo;

    public SettingsDTO() {

    }

    public SettingsDTO(Long settingsId, LocalDateTime systemStartDate, String companyName, String shortName, String city, String address, String isoNo, String zipCode, String telMobileNo, String emailAddress, String tinNo, String pagIbigNo, String philHealthNo, Boolean hospitalAgency, byte[] leftHeaderLogo, byte[] mainLogo, byte[] rightHeaderLogo) {
        this.settingsId = settingsId;
        this.systemStartDate = systemStartDate;
        this.companyName = companyName;
        this.shortName = shortName;
        this.city = city;
        this.address = address;
        this.isoNo = isoNo;
        this.zipCode = zipCode;
        this.telMobileNo = telMobileNo;
        this.emailAddress = emailAddress;
        this.tinNo = tinNo;
        this.pagIbigNo = pagIbigNo;
        this.philHealthNo = philHealthNo;
        this.hospitalAgency = hospitalAgency;
        this.leftHeaderLogo = leftHeaderLogo;
        this.mainLogo = mainLogo;
        this.rightHeaderLogo = rightHeaderLogo;
    }

    public Long getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(Long settingsId) {
        this.settingsId = settingsId;
    }

    public LocalDateTime getSystemStartDate() {
        return systemStartDate;
    }

    public void setSystemStartDate(LocalDateTime systemStartDate) {
        this.systemStartDate = systemStartDate;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIsoNo() {
        return isoNo;
    }

    public void setIsoNo(String isoNo) {
        this.isoNo = isoNo;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getTelMobileNo() {
        return telMobileNo;
    }

    public void setTelMobileNo(String telMobileNo) {
        this.telMobileNo = telMobileNo;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getTinNo() {
        return tinNo;
    }

    public void setTinNo(String tinNo) {
        this.tinNo = tinNo;
    }

    public String getPagIbigNo() {
        return pagIbigNo;
    }

    public void setPagIbigNo(String pagIbigNo) {
        this.pagIbigNo = pagIbigNo;
    }

    public String getPhilHealthNo() {
        return philHealthNo;
    }

    public void setPhilHealthNo(String philHealthNo) {
        this.philHealthNo = philHealthNo;
    }

    public byte[] getLeftHeaderLogo() {
        return leftHeaderLogo;
    }

    public void setLeftHeaderLogo(byte[] leftHeaderLogo) {
        this.leftHeaderLogo = leftHeaderLogo;
    }

    public byte[] getMainLogo() {
        return mainLogo;
    }

    public void setMainLogo(byte[] mainLogo) {
        this.mainLogo = mainLogo;
    }

    public byte[] getRightHeaderLogo() {
        return rightHeaderLogo;
    }

    public void setRightHeaderLogo(byte[] rightHeaderLogo) {
        this.rightHeaderLogo = rightHeaderLogo;
    }

    public Boolean getHospitalAgency() {
        return hospitalAgency;
    }

    public void setHospitalAgency(Boolean hospitalAgency) {
        this.hospitalAgency = hospitalAgency;
    }
}