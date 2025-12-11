package com.humanresource.dtos;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EmployeeDTO implements Serializable {

    private Long employeeId;

    @NotBlank(message = "Employee No is mandatory")
    private String employeeNo;

    private String employeePassword;

    @NotBlank(message = "biometricNo is mandatory")
    private String biometricNo;

    @NotBlank(message = "User Role is mandatory")
    private String role;

    private String firstname;

    private String lastname;

    private String suffix;

    private String email;

    private String position;

    private String shortJobDesc;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String fullName;

    public EmployeeDTO() {

    }

    public EmployeeDTO(String employeeNo, String biometricNo, String employeePassword, String firstname, String lastname, String suffix, String email, String position, String shortJobDesc, String role) {
        this.employeeNo = employeeNo;
        this.biometricNo = biometricNo;
        this.employeePassword = employeePassword;
        this.firstname = firstname;
        this.lastname = lastname;
        this.suffix = suffix;
        this.email = email;
        this.position = position;
        this.shortJobDesc = shortJobDesc;
        this.role = role;
    }

    public EmployeeDTO(String employeeNo, String biometricNo, String employeePassword, String firstname, String lastname, String suffix, String email, String position, String shortJobDesc, LocalDateTime createdAt, LocalDateTime updatedAt, String role) {
        this.employeeNo = employeeNo;
        this.biometricNo = biometricNo;
        this.employeePassword = employeePassword;
        this.firstname = firstname;
        this.lastname = lastname;
        this.suffix = suffix;
        this.email = email;
        this.position = position;
        this.shortJobDesc = shortJobDesc;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.role = role;
    }

    public EmployeeDTO(Long employeeId, String employeeNo, String biometricNo, String firstname, String lastname, String suffix, String role) {
        this.employeeId = employeeId;
        this.employeeNo = employeeNo;
        this.biometricNo = biometricNo;
        this.firstname = firstname;
        this.lastname = lastname;
        this.suffix = suffix;
        this.fullName = getFullName();
        this.role = role;
    }

    public EmployeeDTO(String employeeNo, String biometricNo, String firstname, String lastname, String suffix, String email, String position, String shortJobDesc, LocalDateTime createdAt, String role, LocalDateTime updatedAt) {
        this.employeeNo = employeeNo;
        this.biometricNo = biometricNo;
        this.firstname = firstname;
        this.lastname = lastname;
        this.suffix = suffix;
        this.email = email;
        this.position = position;
        this.shortJobDesc = shortJobDesc;
        this.createdAt = createdAt;
        this.role = role;
        this.updatedAt = updatedAt;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getEmployeePassword() {
        return employeePassword;
    }

    public void setEmployeePassword(String employeePassword) {
        this.employeePassword = employeePassword;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getShortJobDesc() {
        return shortJobDesc;
    }

    public void setShortJobDesc(String shortJobDesc) {
        this.shortJobDesc = shortJobDesc;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder();

        if (firstname != null && !firstname.isEmpty()) {
            fullName.append(firstname.trim());
        }

        if (lastname != null && !lastname.isEmpty()) {
            fullName.append(" ").append(lastname.trim());
        }

        if (suffix != null && !suffix.isEmpty()) {
            fullName.append(" ").append(suffix.trim());
        }

        return fullName.toString().trim();
    }

    public String getBiometricNo() {
        return biometricNo;
    }

    public void setBiometricNo(String biometricNo) {
        this.biometricNo = biometricNo;
    }
}