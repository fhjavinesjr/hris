package com.humanresource.entitymodels;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_mwe_status",
        uniqueConstraints = @UniqueConstraint(name = "uq_employee_mwe_status_from",
                columnNames = {"employeeId", "effectiveFrom"}),
        indexes = @Index(name = "idx_employee_mwe_status_effective",
                columnList = "employeeId,effectiveFrom,effectiveTo"))
public class EmployeeMweStatus {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long employeeId;
    @Column(nullable = false) private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    @Column(nullable = false) private Boolean minimumWageEarner;
    @Column(length = 30) private String regionCode;
    @Column(length = 500) private String remarks;
    @Column(nullable = false, length = 100) private String createdBy;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false, length = 100) private String updatedBy;
    @Column(nullable = false) private LocalDateTime updatedAt;

    public Long getId() { return id; } public void setId(Long value) { id = value; }
    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long value) { employeeId = value; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; } public void setEffectiveFrom(LocalDate value) { effectiveFrom = value; }
    public LocalDate getEffectiveTo() { return effectiveTo; } public void setEffectiveTo(LocalDate value) { effectiveTo = value; }
    public Boolean getMinimumWageEarner() { return minimumWageEarner; } public void setMinimumWageEarner(Boolean value) { minimumWageEarner = value; }
    public String getRegionCode() { return regionCode; } public void setRegionCode(String value) { regionCode = value; }
    public String getRemarks() { return remarks; } public void setRemarks(String value) { remarks = value; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String value) { createdBy = value; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String value) { updatedBy = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}
