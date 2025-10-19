package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PersonalDataDTO implements Serializable {

    private Long personalDataId;
    private Long employeeId;

    @NotBlank(message = "Surname is mandatory")
    private String surname;

    @NotBlank(message = "firstname is mandatory")
    private String firstname;

    @NotBlank(message = "middlename is mandatory")
    private String middlename;

    private String extname;

    @NotNull(message = "Date of Birth is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime dob;

    private String pob;

    @NotNull(message = "sex_id is mandatory")
    private Integer sex_id;

    @NotNull(message = "civilStatus_id is mandatory")
    private Integer civilStatus_id;
    private Integer height;
    private Integer weight;
    private String bloodType;
    private String gsisId;
    private String pagibigId;
    private String philhealthNo;
    private String sssNo;
    private String tinNo;
    private String agencyEmpNo;
    private String citizenship;
    private String resAddress;
    private String resZip;
    private String permAddress;
    private String permZip;
    private String telNo;

    @NotBlank(message = "Mobile number is mandatory")
    private String mobileNo;

    @NotBlank(message = "Email is mandatory")
    private String email;

    //Spring is expecting the raw file bytes inside the JSON body — not multipart form-data
    @Lob
    private byte[] employeePicture;
    //Spring is expecting the raw file bytes inside the JSON body — not multipart form-data
    @Lob
    private byte[] employeeSignature;

    private String spouseSurname;
    private String spouseFirstname;
    private String spouseMiddlename;
    private String spouseOccupation;
    private String spouseEmployer;
    private String spouseBusinessAddress;
    private String spouseTelNo;
    private String fatherSurname;
    private String fatherFirstname;
    private String fatherMiddlename;
    private String motherSurname;
    private String motherFirstname;
    private String motherMiddlename;
    private String govIdNumber;
    private String govIdType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime govIdDate;

    private String govIdPlace;
    private String skillOrHobby;
    private String distinction;
    private String association;
    private String q34a;
    private String q34b;
    private String q35a;
    private String q35b;
    private String q36;
    private String q37a;
    private String q37b;
    private String q37c;
    private String q38;
    private String q39a;
    private String q39b;
    private String q39c;
    private String q34aDetails;
    private String q34bDetails;
    private String q35aDetails;
    private String q35bDetails;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime q35bDateFiled;

    private String q35bStatus;
    private String q36Details;
    private String q37aDetails;
    private String q37bDetails;
    private String q37cDetails;
    private String q38Details;
    private String q39aDetails;
    private String q39bDetails;
    private String q39cDetails;
    private Boolean q42;

    public PersonalDataDTO() {

    }

    public PersonalDataDTO(Long personalDataId, Long employeeId, String surname, String firstname, String middlename, String extname, LocalDateTime dob, String pob, Integer sex_id, Integer civilStatus_id, Integer height, Integer weight, String bloodType, String gsisId, String pagibigId, String philhealthNo, String sssNo, String tinNo, String agencyEmpNo, String citizenship, String resAddress, String resZip, String permAddress, String permZip, String telNo, String mobileNo, String email, byte[] employeePicture, byte[] employeeSignature, String spouseSurname, String spouseFirstname, String spouseMiddlename, String spouseOccupation, String spouseEmployer, String spouseBusinessAddress, String spouseTelNo, String fatherSurname, String fatherFirstname, String fatherMiddlename, String motherSurname, String motherFirstname, String motherMiddlename, String govIdNumber, String govIdType, LocalDateTime govIdDate, String govIdPlace, String skillOrHobby, String distinction, String association, String q34a, String q34b, String q35a, String q35b, String q36, String q37a, String q37b, String q37c, String q38, String q39a, String q39b, String q39c, String q34aDetails, String q34bDetails, String q35aDetails, String q35bDetails, LocalDateTime q35bDateFiled, String q35bStatus, String q36Details, String q37aDetails, String q37bDetails, String q37cDetails, String q38Details, String q39aDetails, String q39bDetails, String q39cDetails, Boolean q42) {
        this.personalDataId = personalDataId;
        this.employeeId = employeeId;
        this.surname = surname;
        this.firstname = firstname;
        this.middlename = middlename;
        this.extname = extname;
        this.dob = dob;
        this.pob = pob;
        this.sex_id = sex_id;
        this.civilStatus_id = civilStatus_id;
        this.height = height;
        this.weight = weight;
        this.bloodType = bloodType;
        this.gsisId = gsisId;
        this.pagibigId = pagibigId;
        this.philhealthNo = philhealthNo;
        this.sssNo = sssNo;
        this.tinNo = tinNo;
        this.agencyEmpNo = agencyEmpNo;
        this.citizenship = citizenship;
        this.resAddress = resAddress;
        this.resZip = resZip;
        this.permAddress = permAddress;
        this.permZip = permZip;
        this.telNo = telNo;
        this.mobileNo = mobileNo;
        this.email = email;
        this.employeePicture = employeePicture;
        this.employeeSignature = employeeSignature;
        this.spouseSurname = spouseSurname;
        this.spouseFirstname = spouseFirstname;
        this.spouseMiddlename = spouseMiddlename;
        this.spouseOccupation = spouseOccupation;
        this.spouseEmployer = spouseEmployer;
        this.spouseBusinessAddress = spouseBusinessAddress;
        this.spouseTelNo = spouseTelNo;
        this.fatherSurname = fatherSurname;
        this.fatherFirstname = fatherFirstname;
        this.fatherMiddlename = fatherMiddlename;
        this.motherSurname = motherSurname;
        this.motherFirstname = motherFirstname;
        this.motherMiddlename = motherMiddlename;
        this.govIdNumber = govIdNumber;
        this.govIdType = govIdType;
        this.govIdDate = govIdDate;
        this.govIdPlace = govIdPlace;
        this.skillOrHobby = skillOrHobby;
        this.distinction = distinction;
        this.association = association;
        this.q34a = q34a;
        this.q34b = q34b;
        this.q35a = q35a;
        this.q35b = q35b;
        this.q36 = q36;
        this.q37a = q37a;
        this.q37b = q37b;
        this.q37c = q37c;
        this.q38 = q38;
        this.q39a = q39a;
        this.q39b = q39b;
        this.q39c = q39c;
        this.q34aDetails = q34aDetails;
        this.q34bDetails = q34bDetails;
        this.q35aDetails = q35aDetails;
        this.q35bDetails = q35bDetails;
        this.q35bDateFiled = q35bDateFiled;
        this.q35bStatus = q35bStatus;
        this.q36Details = q36Details;
        this.q37aDetails = q37aDetails;
        this.q37bDetails = q37bDetails;
        this.q37cDetails = q37cDetails;
        this.q38Details = q38Details;
        this.q39aDetails = q39aDetails;
        this.q39bDetails = q39bDetails;
        this.q39cDetails = q39cDetails;
        this.q42 = q42;
    }

    public Long getPersonalDataId() {
        return personalDataId;
    }

    public void setPersonalDataId(Long personalDataId) {
        this.personalDataId = personalDataId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getExtname() {
        return extname;
    }

    public void setExtname(String extname) {
        this.extname = extname;
    }

    public LocalDateTime getDob() {
        return dob;
    }

    public void setDob(LocalDateTime dob) {
        this.dob = dob;
    }

    public String getPob() {
        return pob;
    }

    public void setPob(String pob) {
        this.pob = pob;
    }

    public Integer getSex_id() {
        return sex_id;
    }

    public void setSex_id(Integer sex_id) {
        this.sex_id = sex_id;
    }

    public Integer getCivilStatus_id() {
        return civilStatus_id;
    }

    public void setCivilStatus_id(Integer civilStatus_id) {
        this.civilStatus_id = civilStatus_id;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getGsisId() {
        return gsisId;
    }

    public void setGsisId(String gsisId) {
        this.gsisId = gsisId;
    }

    public String getPagibigId() {
        return pagibigId;
    }

    public void setPagibigId(String pagibigId) {
        this.pagibigId = pagibigId;
    }

    public String getPhilhealthNo() {
        return philhealthNo;
    }

    public void setPhilhealthNo(String philhealthNo) {
        this.philhealthNo = philhealthNo;
    }

    public String getSssNo() {
        return sssNo;
    }

    public void setSssNo(String sssNo) {
        this.sssNo = sssNo;
    }

    public String getTinNo() {
        return tinNo;
    }

    public void setTinNo(String tinNo) {
        this.tinNo = tinNo;
    }

    public String getAgencyEmpNo() {
        return agencyEmpNo;
    }

    public void setAgencyEmpNo(String agencyEmpNo) {
        this.agencyEmpNo = agencyEmpNo;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getResAddress() {
        return resAddress;
    }

    public void setResAddress(String resAddress) {
        this.resAddress = resAddress;
    }

    public String getResZip() {
        return resZip;
    }

    public void setResZip(String resZip) {
        this.resZip = resZip;
    }

    public String getPermAddress() {
        return permAddress;
    }

    public void setPermAddress(String permAddress) {
        this.permAddress = permAddress;
    }

    public String getPermZip() {
        return permZip;
    }

    public void setPermZip(String permZip) {
        this.permZip = permZip;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getEmployeePicture() {
        return employeePicture;
    }

    public void setEmployeePicture(byte[] employeePicture) {
        this.employeePicture = employeePicture;
    }

    public byte[] getEmployeeSignature() {
        return employeeSignature;
    }

    public void setEmployeeSignature(byte[] employeeSignature) {
        this.employeeSignature = employeeSignature;
    }

    public String getSpouseSurname() {
        return spouseSurname;
    }

    public void setSpouseSurname(String spouseSurname) {
        this.spouseSurname = spouseSurname;
    }

    public String getSpouseFirstname() {
        return spouseFirstname;
    }

    public void setSpouseFirstname(String spouseFirstname) {
        this.spouseFirstname = spouseFirstname;
    }

    public String getSpouseMiddlename() {
        return spouseMiddlename;
    }

    public void setSpouseMiddlename(String spouseMiddlename) {
        this.spouseMiddlename = spouseMiddlename;
    }

    public String getSpouseOccupation() {
        return spouseOccupation;
    }

    public void setSpouseOccupation(String spouseOccupation) {
        this.spouseOccupation = spouseOccupation;
    }

    public String getSpouseEmployer() {
        return spouseEmployer;
    }

    public void setSpouseEmployer(String spouseEmployer) {
        this.spouseEmployer = spouseEmployer;
    }

    public String getSpouseBusinessAddress() {
        return spouseBusinessAddress;
    }

    public void setSpouseBusinessAddress(String spouseBusinessAddress) {
        this.spouseBusinessAddress = spouseBusinessAddress;
    }

    public String getSpouseTelNo() {
        return spouseTelNo;
    }

    public void setSpouseTelNo(String spouseTelNo) {
        this.spouseTelNo = spouseTelNo;
    }

    public String getFatherSurname() {
        return fatherSurname;
    }

    public void setFatherSurname(String fatherSurname) {
        this.fatherSurname = fatherSurname;
    }

    public String getFatherFirstname() {
        return fatherFirstname;
    }

    public void setFatherFirstname(String fatherFirstname) {
        this.fatherFirstname = fatherFirstname;
    }

    public String getFatherMiddlename() {
        return fatherMiddlename;
    }

    public void setFatherMiddlename(String fatherMiddlename) {
        this.fatherMiddlename = fatherMiddlename;
    }

    public String getMotherSurname() {
        return motherSurname;
    }

    public void setMotherSurname(String motherSurname) {
        this.motherSurname = motherSurname;
    }

    public String getMotherFirstname() {
        return motherFirstname;
    }

    public void setMotherFirstname(String motherFirstname) {
        this.motherFirstname = motherFirstname;
    }

    public String getMotherMiddlename() {
        return motherMiddlename;
    }

    public void setMotherMiddlename(String motherMiddlename) {
        this.motherMiddlename = motherMiddlename;
    }

    public String getGovIdNumber() {
        return govIdNumber;
    }

    public void setGovIdNumber(String govIdNumber) {
        this.govIdNumber = govIdNumber;
    }

    public String getGovIdType() {
        return govIdType;
    }

    public void setGovIdType(String govIdType) {
        this.govIdType = govIdType;
    }

    public LocalDateTime getGovIdDate() {
        return govIdDate;
    }

    public void setGovIdDate(LocalDateTime govIdDate) {
        this.govIdDate = govIdDate;
    }

    public String getGovIdPlace() {
        return govIdPlace;
    }

    public void setGovIdPlace(String govIdPlace) {
        this.govIdPlace = govIdPlace;
    }

    public String getSkillOrHobby() {
        return skillOrHobby;
    }

    public void setSkillOrHobby(String skillOrHobby) {
        this.skillOrHobby = skillOrHobby;
    }

    public String getDistinction() {
        return distinction;
    }

    public void setDistinction(String distinction) {
        this.distinction = distinction;
    }

    public String getAssociation() {
        return association;
    }

    public void setAssociation(String association) {
        this.association = association;
    }

    public String getQ34a() {
        return q34a;
    }

    public void setQ34a(String q34a) {
        this.q34a = q34a;
    }

    public String getQ34b() {
        return q34b;
    }

    public void setQ34b(String q34b) {
        this.q34b = q34b;
    }

    public String getQ35a() {
        return q35a;
    }

    public void setQ35a(String q35a) {
        this.q35a = q35a;
    }

    public String getQ35b() {
        return q35b;
    }

    public void setQ35b(String q35b) {
        this.q35b = q35b;
    }

    public String getQ36() {
        return q36;
    }

    public void setQ36(String q36) {
        this.q36 = q36;
    }

    public String getQ37a() {
        return q37a;
    }

    public void setQ37a(String q37a) {
        this.q37a = q37a;
    }

    public String getQ37b() {
        return q37b;
    }

    public void setQ37b(String q37b) {
        this.q37b = q37b;
    }

    public String getQ37c() {
        return q37c;
    }

    public void setQ37c(String q37c) {
        this.q37c = q37c;
    }

    public String getQ38() {
        return q38;
    }

    public void setQ38(String q38) {
        this.q38 = q38;
    }

    public String getQ39a() {
        return q39a;
    }

    public void setQ39a(String q39a) {
        this.q39a = q39a;
    }

    public String getQ39b() {
        return q39b;
    }

    public void setQ39b(String q39b) {
        this.q39b = q39b;
    }

    public String getQ39c() {
        return q39c;
    }

    public void setQ39c(String q39c) {
        this.q39c = q39c;
    }

    public String getQ34aDetails() {
        return q34aDetails;
    }

    public void setQ34aDetails(String q34aDetails) {
        this.q34aDetails = q34aDetails;
    }

    public String getQ34bDetails() {
        return q34bDetails;
    }

    public void setQ34bDetails(String q34bDetails) {
        this.q34bDetails = q34bDetails;
    }

    public String getQ35aDetails() {
        return q35aDetails;
    }

    public void setQ35aDetails(String q35aDetails) {
        this.q35aDetails = q35aDetails;
    }

    public String getQ35bDetails() {
        return q35bDetails;
    }

    public void setQ35bDetails(String q35bDetails) {
        this.q35bDetails = q35bDetails;
    }

    public LocalDateTime getQ35bDateFiled() {
        return q35bDateFiled;
    }

    public void setQ35bDateFiled(LocalDateTime q35bDateFiled) {
        this.q35bDateFiled = q35bDateFiled;
    }

    public String getQ35bStatus() {
        return q35bStatus;
    }

    public void setQ35bStatus(String q35bStatus) {
        this.q35bStatus = q35bStatus;
    }

    public String getQ36Details() {
        return q36Details;
    }

    public void setQ36Details(String q36Details) {
        this.q36Details = q36Details;
    }

    public String getQ37aDetails() {
        return q37aDetails;
    }

    public void setQ37aDetails(String q37aDetails) {
        this.q37aDetails = q37aDetails;
    }

    public String getQ37bDetails() {
        return q37bDetails;
    }

    public void setQ37bDetails(String q37bDetails) {
        this.q37bDetails = q37bDetails;
    }

    public String getQ37cDetails() {
        return q37cDetails;
    }

    public void setQ37cDetails(String q37cDetails) {
        this.q37cDetails = q37cDetails;
    }

    public String getQ38Details() {
        return q38Details;
    }

    public void setQ38Details(String q38Details) {
        this.q38Details = q38Details;
    }

    public String getQ39aDetails() {
        return q39aDetails;
    }

    public void setQ39aDetails(String q39aDetails) {
        this.q39aDetails = q39aDetails;
    }

    public String getQ39bDetails() {
        return q39bDetails;
    }

    public void setQ39bDetails(String q39bDetails) {
        this.q39bDetails = q39bDetails;
    }

    public String getQ39cDetails() {
        return q39cDetails;
    }

    public void setQ39cDetails(String q39cDetails) {
        this.q39cDetails = q39cDetails;
    }

    public Boolean getQ42() {
        return q42;
    }

    public void setQ42(Boolean q42) {
        this.q42 = q42;
    }
}