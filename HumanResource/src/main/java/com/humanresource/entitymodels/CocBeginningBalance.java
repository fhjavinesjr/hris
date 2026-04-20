package com.humanresource.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coc_beginning_balance")
public class CocBeginningBalance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cocBeginningBalanceId")
    private Long cocBeginningBalanceId;

    @NotNull(message = "employeeId is mandatory")
    @Column(name = "employeeId", nullable = false, unique = true)
    private Long employeeId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "asOfDate")
    private LocalDate asOfDate;

    @Column(name = "accumulatedHours")
    private Double accumulatedHours;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public CocBeginningBalance() {
    }

    public CocBeginningBalance(Long cocBeginningBalanceId, Long employeeId, LocalDate asOfDate,
                                Double accumulatedHours, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.cocBeginningBalanceId = cocBeginningBalanceId;
        this.employeeId = employeeId;
        this.asOfDate = asOfDate;
        this.accumulatedHours = accumulatedHours;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getCocBeginningBalanceId() {
        return cocBeginningBalanceId;
    }

    public void setCocBeginningBalanceId(Long cocBeginningBalanceId) {
        this.cocBeginningBalanceId = cocBeginningBalanceId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getAsOfDate() {
        return asOfDate;
    }

    public void setAsOfDate(LocalDate asOfDate) {
        this.asOfDate = asOfDate;
    }

    public Double getAccumulatedHours() {
        return accumulatedHours;
    }

    public void setAccumulatedHours(Double accumulatedHours) {
        this.accumulatedHours = accumulatedHours;
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
