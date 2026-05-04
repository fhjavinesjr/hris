package com.humanresource.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "time_correction")
public class TimeCorrection implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timeCorrectionId")
    private Long timeCorrectionId;

    @NotNull(message = "employeeId is mandatory")
    @Column(name = "employeeId", nullable = false)
    private Long employeeId;

    @NotNull(message = "dateFiled is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "dateFiled", nullable = false)
    private LocalDate dateFiled;

    @NotNull(message = "workDate is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "workDate", nullable = false)
    private LocalDate workDate; // The date being corrected

    @NotNull(message = "correctedTimeIn is mandatory")
    @Column(name = "correctedTimeIn", nullable = false)
    private LocalTime correctedTimeIn;

    @NotNull(message = "correctedTimeOut is mandatory")
    @Column(name = "correctedTimeOut", nullable = false)
    private LocalTime correctedTimeOut;

    @Column(name = "correctedBreakOut")
    private LocalTime correctedBreakOut;

    @Column(name = "correctedBreakIn")
    private LocalTime correctedBreakIn;

    @Column(name = "reason", length = 500)
    private String reason;

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

    public TimeCorrection() {}

    public Long getTimeCorrectionId() { return timeCorrectionId; }
    public void setTimeCorrectionId(Long id) { this.timeCorrectionId = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDate getDateFiled() { return dateFiled; }
    public void setDateFiled(LocalDate dateFiled) { this.dateFiled = dateFiled; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public LocalTime getCorrectedTimeIn() { return correctedTimeIn; }
    public void setCorrectedTimeIn(LocalTime correctedTimeIn) { this.correctedTimeIn = correctedTimeIn; }

    public LocalTime getCorrectedTimeOut() { return correctedTimeOut; }
    public void setCorrectedTimeOut(LocalTime correctedTimeOut) { this.correctedTimeOut = correctedTimeOut; }

    public LocalTime getCorrectedBreakOut() { return correctedBreakOut; }
    public void setCorrectedBreakOut(LocalTime correctedBreakOut) { this.correctedBreakOut = correctedBreakOut; }

    public LocalTime getCorrectedBreakIn() { return correctedBreakIn; }
    public void setCorrectedBreakIn(LocalTime correctedBreakIn) { this.correctedBreakIn = correctedBreakIn; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

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
