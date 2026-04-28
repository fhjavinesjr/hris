package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeCorrectionDTO implements Serializable {

    private Long timeCorrectionId;

    @NotNull(message = "employeeId is mandatory")
    private Long employeeId;

    @NotNull(message = "dateFiled is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateFiled;

    @NotNull(message = "workDate is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    @NotNull(message = "correctedTimeIn is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime correctedTimeIn;

    @NotNull(message = "correctedTimeOut is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime correctedTimeOut;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime correctedBreakOut;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime correctedBreakIn;

    private String reason;

    @NotNull(message = "status is mandatory")
    private String status;

    private Long approvedById;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime approvedAt;

    private String approvalRemarks;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime updatedAt;

    public TimeCorrectionDTO() {}

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
