package com.timekeeping.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
public class Employee implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employeeId")
    private Long employeeId;

    @NotBlank(message = "Employee No is mandatory")
    @Column(name = "employeeNo", length = 100, unique = true, nullable = false)
    private String employeeNo;

    @NotBlank(message = "Employee Password is mandatory")
    @Column(name = "employeePassword")
    private String employeePassword;

    @NotBlank(message = "User Role is mandatory")
    @Column(name = "userRole")
    private String role;

    @NotBlank(message = "Firstname is mandatory")
    @Column(name = "firstname")
    private String firstname;

    @NotBlank(message = "Lastname is mandatory")
    @Column(name = "lastname")
    private String lastname;

    @Column(name = "suffix")
    private String suffix;

    @Email(message = "Email should be valid")
    @NotNull(message = "Email is mandatory")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Position is mandatory")
    @Column(name = "position")
    private String position;

    @Column(name = "shortJobDesc")
    private String shortJobDesc;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public Employee() {

    }

    public Employee(String employeeNo, String employeePassword, String role, String firstname, String lastname, String suffix, String email, String position, String shortJobDesc, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.employeeNo = employeeNo;
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

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
}