package com.administrative.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "salary_period_setting",
    uniqueConstraints = @UniqueConstraint(columnNames = {"salaryType", "nthOrder", "periodContext"})
)
public class SalaryPeriodSetting implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salaryPeriodSettingId")
    private Long salaryPeriodSettingId;

    // MONTHLY | SEMI_MONTHLY | WEEKLY | DAILY
    @NotBlank(message = "salaryType is mandatory")
    @Column(name = "salaryType", nullable = false, length = 20)
    private String salaryType;

    // 1 = 1st period, 2 = 2nd period of the month, etc.
    @NotNull(message = "nthOrder is mandatory")
    @Column(name = "nthOrder", nullable = false)
    private Integer nthOrder;

    // PAYROLL | LEAVE | BOTH
    @NotBlank(message = "periodContext is mandatory")
    @Column(name = "periodContext", nullable = false, length = 20)
    private String periodContext;

    // Day of month the cut-off starts (1-31)
    @NotNull(message = "cutoffStartDay is mandatory")
    @Column(name = "cutoffStartDay", nullable = false)
    private Integer cutoffStartDay;

    // Month offset: 0 = current month, -1 = previous month, -2 = two months ago
    @NotNull(message = "cutoffStartMonthOffset is mandatory")
    @Column(name = "cutoffStartMonthOffset", nullable = false)
    private Integer cutoffStartMonthOffset;

    // Day of month the cut-off ends (1-31)
    @NotNull(message = "cutoffEndDay is mandatory")
    @Column(name = "cutoffEndDay", nullable = false)
    private Integer cutoffEndDay;

    // Month offset: 0 = current month, -1 = previous month
    @NotNull(message = "cutoffEndMonthOffset is mandatory")
    @Column(name = "cutoffEndMonthOffset", nullable = false)
    private Integer cutoffEndMonthOffset;

    // Nullable for LEAVE context
    @Column(name = "salaryReleaseStartDay")
    private Integer salaryReleaseStartDay;

    @Column(name = "salaryReleaseEndDay")
    private Integer salaryReleaseEndDay;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive = true;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public SalaryPeriodSetting() {}

    public Long getSalaryPeriodSettingId() { return salaryPeriodSettingId; }
    public void setSalaryPeriodSettingId(Long salaryPeriodSettingId) { this.salaryPeriodSettingId = salaryPeriodSettingId; }

    public String getSalaryType() { return salaryType; }
    public void setSalaryType(String salaryType) { this.salaryType = salaryType; }

    public Integer getNthOrder() { return nthOrder; }
    public void setNthOrder(Integer nthOrder) { this.nthOrder = nthOrder; }

    public String getPeriodContext() { return periodContext; }
    public void setPeriodContext(String periodContext) { this.periodContext = periodContext; }

    public Integer getCutoffStartDay() { return cutoffStartDay; }
    public void setCutoffStartDay(Integer cutoffStartDay) { this.cutoffStartDay = cutoffStartDay; }

    public Integer getCutoffStartMonthOffset() { return cutoffStartMonthOffset; }
    public void setCutoffStartMonthOffset(Integer cutoffStartMonthOffset) { this.cutoffStartMonthOffset = cutoffStartMonthOffset; }

    public Integer getCutoffEndDay() { return cutoffEndDay; }
    public void setCutoffEndDay(Integer cutoffEndDay) { this.cutoffEndDay = cutoffEndDay; }

    public Integer getCutoffEndMonthOffset() { return cutoffEndMonthOffset; }
    public void setCutoffEndMonthOffset(Integer cutoffEndMonthOffset) { this.cutoffEndMonthOffset = cutoffEndMonthOffset; }

    public Integer getSalaryReleaseStartDay() { return salaryReleaseStartDay; }
    public void setSalaryReleaseStartDay(Integer salaryReleaseStartDay) { this.salaryReleaseStartDay = salaryReleaseStartDay; }

    public Integer getSalaryReleaseEndDay() { return salaryReleaseEndDay; }
    public void setSalaryReleaseEndDay(Integer salaryReleaseEndDay) { this.salaryReleaseEndDay = salaryReleaseEndDay; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
