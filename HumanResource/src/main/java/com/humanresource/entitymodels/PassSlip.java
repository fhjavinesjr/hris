package com.humanresource.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "pass_slip")
public class PassSlip implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "passSlipId")
    private Long passSlipId;

    @NotNull(message = "employeeId is mandatory")
    @Column(name = "employeeId", nullable = false)
    private Long employeeId;

    @NotNull(message = "dateFiled is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "dateFiled", nullable = false)
    private LocalDate dateFiled;

    @NotNull(message = "passSlipDate is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "passSlipDate", nullable = false)
    private LocalDate passSlipDate;

    @NotNull(message = "purpose is mandatory")
    @Column(name = "purpose", length = 50, nullable = false)
    private String purpose;

    @NotNull(message = "departureTime is mandatory")
    @Column(name = "departureTime", nullable = false)
    private LocalTime departureTime;

    @NotNull(message = "arrivalTime is mandatory")
    @Column(name = "arrivalTime", nullable = false)
    private LocalTime arrivalTime;

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

    @Column(name = "recommendationStatus", length = 50)
    private String recommendationStatus;

    @Column(name = "recommendedById")
    private Long recommendedById;

    @Column(name = "recommendationRemarks", length = 300)
    private String recommendationRemarks;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public PassSlip() {
    }

    public PassSlip(Long passSlipId, Long employeeId, LocalDate dateFiled, LocalDate passSlipDate, String purpose,
                    LocalTime departureTime, LocalTime arrivalTime, String details, String status,
                    Long approvedById, LocalDateTime approvedAt, String approvalRemarks,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.passSlipId = passSlipId;
        this.employeeId = employeeId;
        this.dateFiled = dateFiled;
        this.passSlipDate = passSlipDate;
        this.purpose = purpose;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.details = details;
        this.status = status;
        this.approvedById = approvedById;
        this.approvedAt = approvedAt;
        this.approvalRemarks = approvalRemarks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPassSlipId() { return passSlipId; }
    public void setPassSlipId(Long passSlipId) { this.passSlipId = passSlipId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDate getDateFiled() { return dateFiled; }
    public void setDateFiled(LocalDate dateFiled) { this.dateFiled = dateFiled; }

    public LocalDate getPassSlipDate() { return passSlipDate; }
    public void setPassSlipDate(LocalDate passSlipDate) { this.passSlipDate = passSlipDate; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }

    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }

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

    public String getRecommendationStatus() { return recommendationStatus; }
    public void setRecommendationStatus(String recommendationStatus) { this.recommendationStatus = recommendationStatus; }

    public Long getRecommendedById() { return recommendedById; }
    public void setRecommendedById(Long recommendedById) { this.recommendedById = recommendedById; }

    public String getRecommendationRemarks() { return recommendationRemarks; }
    public void setRecommendationRemarks(String recommendationRemarks) { this.recommendationRemarks = recommendationRemarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
