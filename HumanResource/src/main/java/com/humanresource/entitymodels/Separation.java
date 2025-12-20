package com.humanresource.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "separation")
public class Separation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "separationId")
    private Long separationId;

    @NotNull(message = "employeeId is mandatory")
    @Column(name = "employeeId", nullable = false)
    private Long employeeId;

    @NotNull(message = "Separation date is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "separationDate")
    private LocalDateTime separationDate;

    @NotNull(message = "natureOfSeparationId is mandatory")
    @Column(name = "natureOfSeparationId")
    private Integer natureOfSeparationId;

    @NotNull(message = "remarks is mandatory")
    @Column(name = "remarks", length = 100, nullable = false)
    private String remarks;

    @NotNull(message = "employeeInterviewerId is mandatory")
    @Column(name = "employeeInterviewerId", nullable = false)
    private Long employeeInterviewerId;

    @NotNull(message = "exitInterviewDate is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "exitInterviewDate")
    private LocalDateTime exitInterviewDate;

    @NotNull(message = "employeeIdProcessingBy is mandatory")
    @Column(name = "employeeIdProcessingBy", nullable = false)
    private Long employeeIdProcessingBy;

    @NotNull(message = "approvedById is mandatory")
    @Column(name = "approvedById", nullable = false)
    private Long approvedById;

    public Separation() {

    }

    public Separation(Long separationId, Long employeeId, LocalDateTime separationDate, Integer natureOfSeparationId, String remarks, Long employeeInterviewerId, LocalDateTime exitInterviewDate, Long employeeIdProcessingBy, Long approvedById) {
        this.separationId = separationId;
        this.employeeId = employeeId;
        this.separationDate = separationDate;
        this.natureOfSeparationId = natureOfSeparationId;
        this.remarks = remarks;
        this.employeeInterviewerId = employeeInterviewerId;
        this.exitInterviewDate = exitInterviewDate;
        this.employeeIdProcessingBy = employeeIdProcessingBy;
        this.approvedById = approvedById;
    }

    public Long getSeparationId() {
        return separationId;
    }

    public void setSeparationId(Long separationId) {
        this.separationId = separationId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getSeparationDate() {
        return separationDate;
    }

    public void setSeparationDate(LocalDateTime separationDate) {
        this.separationDate = separationDate;
    }

    public Integer getNatureOfSeparationId() {
        return natureOfSeparationId;
    }

    public void setNatureOfSeparationId(Integer natureOfSeparationId) {
        this.natureOfSeparationId = natureOfSeparationId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Long getEmployeeInterviewerId() {
        return employeeInterviewerId;
    }

    public void setEmployeeInterviewerId(Long employeeInterviewerId) {
        this.employeeInterviewerId = employeeInterviewerId;
    }

    public LocalDateTime getExitInterviewDate() {
        return exitInterviewDate;
    }

    public void setExitInterviewDate(LocalDateTime exitInterviewDate) {
        this.exitInterviewDate = exitInterviewDate;
    }

    public Long getEmployeeIdProcessingBy() {
        return employeeIdProcessingBy;
    }

    public void setEmployeeIdProcessingBy(Long employeeIdProcessingBy) {
        this.employeeIdProcessingBy = employeeIdProcessingBy;
    }

    public Long getApprovedById() {
        return approvedById;
    }

    public void setApprovedById(Long approvedById) {
        this.approvedById = approvedById;
    }
}