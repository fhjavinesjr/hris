package com.humanresource.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "official_engagement_application")
public class OfficialEngagementApplication implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "officialEngagementApplicationId")
    private Long officialEngagementApplicationId;

    @NotNull(message = "employeeId is mandatory")
    @Column(name = "employeeId", nullable = false)
    private Long employeeId;

    @NotNull(message = "dateFiled is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "dateFiled", nullable = false)
    private LocalDate dateFiled;

    @NotNull(message = "officialType is mandatory")
    @Column(name = "officialType", length = 50, nullable = false)
    private String officialType; // "Official Business" | "Official Time"

    @NotNull(message = "startDate is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "startDate", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "startTime is mandatory")
    @Column(name = "startTime", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "endDate is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "endDate", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "endTime is mandatory")
    @Column(name = "endTime", nullable = false)
    private LocalTime endTime;

    @Column(name = "details", length = 500)
    private String details;

    @NotNull(message = "status is mandatory")
    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "approvedById")
    private Long approvedById;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "approvedAt")
    private LocalDateTime approvedAt;

    @Column(name = "approvalRemarks", length = 300)
    private String approvalRemarks;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public OfficialEngagementApplication() {}

    public Long getOfficialEngagementApplicationId() { return officialEngagementApplicationId; }
    public void setOfficialEngagementApplicationId(Long id) { this.officialEngagementApplicationId = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDate getDateFiled() { return dateFiled; }
    public void setDateFiled(LocalDate dateFiled) { this.dateFiled = dateFiled; }

    public String getOfficialType() { return officialType; }
    public void setOfficialType(String officialType) { this.officialType = officialType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getApprovedById() { return approvedById; }
    public void setApprovedById(Long approvedById) { this.approvedById = approvedById; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getApprovalRemarks() { return approvalRemarks; }
    public void setApprovalRemarks(String approvalRemarks) { this.approvalRemarks = approvalRemarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
