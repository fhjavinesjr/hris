package com.humanresource.entitymodels;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "personaldata")
public class PersonalData implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personalDataId")
    private Long personalDataId;

    @NotBlank(message = "Surname is mandatory")
    @Column(name = "surname")
    private String surname;

    @NotBlank(message = "firstname is mandatory")
    @Column(name = "firstname")
    private String firstname;

    @NotBlank(message = "middlename is mandatory")
    @Column(name = "middlename")
    private String middlename;

    @NotBlank(message = "extname is mandatory")
    @Column(name = "extname")
    private String extname;

    @NotNull(message = "Date of Birth is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "dob")
    private LocalDateTime dob;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "pob")
    private LocalDateTime pob;

    @NotNull(message = "sex_id is mandatory")
    @Column(name = "sex_id")
    private Integer sex_id;

    @NotNull(message = "civilStatus_id is mandatory")
    @Column(name = "civilStatus_id")
    private Integer civilStatus_id;

    @Column(name = "height")
    private Integer height;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "bloodType")
    private String bloodType;

    @Column(name = "gsisId")
    private String gsisId;

    @Column(name = "pagibigId")
    private String pagibigId;

    @Column(name = "philhealthNo")
    private String philhealthNo;

    @Column(name = "sssNo")
    private String sssNo;

    @Column(name = "tinNo")
    private String tinNo;

    @Column(name = "agencyEmpNo")
    private String agencyEmpNo;

    @Column(name = "citizenship")
    private String citizenship;

    @Column(name = "resAddress")
    private String resAddress;

    @Column(name = "resZip")
    private String resZip;

    @Column(name = "permAddress")
    private String permAddress;

    @Column(name = "permZip")
    private String permZip;

    @Column(name = "telNo")
    private String telNo;

    @NotBlank(message = "Mobile number is mandatory")
    @Column(name = "mobileNo")
    private String mobileNo;

    @NotBlank(message = "Email is mandatory")
    @Column(name = "email")
    private String email;

    @Column(name = "employeePicture")
    private String employeePicture;

    @Column(name = "employeeSignature")
    private String employeeSignature;

    @Column(name = "spouseSurname")
    private String spouseSurname;

    @Column(name = "spouseFirstname")
    private String spouseFirstname;

    @Column(name = "spouseMiddlename")
    private String spouseMiddlename;

    @Column(name = "spouseOccupation")
    private String spouseOccupation;

    @Column(name = "spouseEmployer")
    private String spouseEmployer;

    @Column(name = "spouseBusinessAddress")
    private String spouseBusinessAddress;

    @Column(name = "spouseTelNo")
    private String spouseTelNo;

    @Column(name = "fatherSurname")
    private String fatherSurname;

    @Column(name = "fatherFirstname")
    private String fatherFirstname;

    @Column(name = "fatherMiddlename")
    private String fatherMiddlename;

    @Column(name = "motherSurname")
    private String motherSurname;

    @Column(name = "motherFirstname")
    private String motherFirstname;

    @Column(name = "motherMiddlename")
    private String motherMiddlename;

    @Column(name = "govIdNumber")
    private String govIdNumber;

    @Column(name = "govIdType")
    private String govIdType;

    @Column(name = "govIdDate")
    private String govIdDate;

    @Column(name = "govIdPlace")
    private String govIdPlace;

    @Column(name = "skillOrHobby")
    private String skillOrHobby;

    @Column(name = "distinction")
    private String distinction;

    @Column(name = "association")
    private String association;

    @Column(name = "q34aDetails")
    private String q34aDetails;

    @Column(name = "q34bDetails")
    private String q34bDetails;

    @Column(name = "q35aDetails")
    private String q35aDetails;

    @Column(name = "q35bDetails")
    private String q35bDetails;

    @Column(name = "q35bDateFiled")
    private String q35bDateFiled;

    @Column(name = "q35bStatus")
    private String q35bStatus;

    @Column(name = "q36Details")
    private String q36Details;

    @Column(name = "q37aDetails")
    private String q37aDetails;

    @Column(name = "q37bDetails")
    private String q37bDetails;

    @Column(name = "q37cDetails")
    private String q37cDetails;

    @Column(name = "q38Details")
    private String q38Details;

    @Column(name = "q39aDetails")
    private String q39aDetails;

    @Column(name = "q39bDetails")
    private String q39bDetails;

    @Column(name = "q39cDetails")
    private String q39cDetails;

    @Column(name = "q42")
    private Boolean q42;

    public PersonalData() {

    }

    public PersonalData(Long personalDataId, String surname, String firstname, String middlename, String extname, LocalDateTime dob, LocalDateTime pob, Integer sex_id, Integer civilStatus_id, Integer height, Integer weight, String bloodType, String gsisId, String pagibigId, String philhealthNo, String sssNo, String tinNo, String agencyEmpNo, String citizenship, String resAddress, String resZip, String permAddress, String permZip, String telNo, String mobileNo, String email, String employeePicture, String employeeSignature, String spouseSurname, String spouseFirstname, String spouseMiddlename, String spouseOccupation, String spouseEmployer, String spouseBusinessAddress, String spouseTelNo, String fatherSurname, String fatherFirstname, String fatherMiddlename, String motherSurname, String motherFirstname, String motherMiddlename, String govIdNumber, String govIdType, String govIdDate, String govIdPlace, String skillOrHobby, String distinction, String association, String q34aDetails, String q34bDetails, String q35aDetails, String q35bDetails, String q35bDateFiled, String q35bStatus, String q36Details, String q37aDetails, String q37bDetails, String q37cDetails, String q38Details, String q39aDetails, String q39bDetails, String q39cDetails, Boolean q42) {
        this.personalDataId = personalDataId;
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

    public LocalDateTime getPob() {
        return pob;
    }

    public void setPob(LocalDateTime pob) {
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

    public String getEmployeePicture() {
        return employeePicture;
    }

    public void setEmployeePicture(String employeePicture) {
        this.employeePicture = employeePicture;
    }

    public String getEmployeeSignature() {
        return employeeSignature;
    }

    public void setEmployeeSignature(String employeeSignature) {
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

    public String getGovIdDate() {
        return govIdDate;
    }

    public void setGovIdDate(String govIdDate) {
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

    public String getQ35bDateFiled() {
        return q35bDateFiled;
    }

    public void setQ35bDateFiled(String q35bDateFiled) {
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