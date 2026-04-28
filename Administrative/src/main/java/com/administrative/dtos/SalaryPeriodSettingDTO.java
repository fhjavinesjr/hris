package com.administrative.dtos;

import java.io.Serializable;

public class SalaryPeriodSettingDTO implements Serializable {

    private Long salaryPeriodSettingId;
    private String salaryType;
    private Integer nthOrder;
    private String periodContext;
    private Integer cutoffStartDay;
    private Integer cutoffStartMonthOffset;
    private Integer cutoffEndDay;
    private Integer cutoffEndMonthOffset;
    private Integer salaryReleaseStartDay;
    private Integer salaryReleaseEndDay;
    private Boolean isActive;

    public SalaryPeriodSettingDTO() {}

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
}
