package com.payroll.dtos;

import java.time.LocalDate;

/**
 * One day's DTR summary for a single employee.
 * Fetched in bulk from the TimeKeeping service before computation.
 *
 * The TimeKeeping service must expose:
 *   GET /api/dtr/bulk-summary?from={cutoffStart}&to={cutoffEnd}
 * returning List&lt;DtrDailySummaryDTO&gt;.
 */
public class DtrDailySummaryDTO {

    private String employeeNo;
    private LocalDate dtrDate;

    /** true = employee clocked in and out (or has approved OB/TA/OT). */
    private Boolean present = false;

    /** true = this date is the employee's scheduled rest day. */
    private Boolean restDay = false;

    /** Raw late minutes recorded for this day (before VL credit check). */
    private Integer lateMinutes = 0;

    /** Raw undertime minutes recorded for this day (before VL credit check). */
    private Integer undertimeMinutes = 0;

    /**
     * true = employee has an Official Business order approved for this date.
     * Treated as present, no late/undertime.
     */
    private Boolean hasApprovedOb = false;

    /**
     * true = employee has an approved Overtime on this date.
     * Treated as present.
     */
    private Boolean hasApprovedOt = false;

    /**
     * true = employee has an approved Travel Authority for this date.
     * Treated as present.
     */
    private Boolean hasApprovedTa = false;

    // Getters / Setters
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public LocalDate getDtrDate() { return dtrDate; }
    public void setDtrDate(LocalDate dtrDate) { this.dtrDate = dtrDate; }
    public Boolean getPresent() { return present; }
    public void setPresent(Boolean present) { this.present = present; }
    public Boolean getRestDay() { return restDay; }
    public void setRestDay(Boolean restDay) { this.restDay = restDay; }
    public Integer getLateMinutes() { return lateMinutes; }
    public void setLateMinutes(Integer lateMinutes) { this.lateMinutes = lateMinutes; }
    public Integer getUndertimeMinutes() { return undertimeMinutes; }
    public void setUndertimeMinutes(Integer undertimeMinutes) { this.undertimeMinutes = undertimeMinutes; }
    public Boolean getHasApprovedOb() { return hasApprovedOb; }
    public void setHasApprovedOb(Boolean hasApprovedOb) { this.hasApprovedOb = hasApprovedOb; }
    public Boolean getHasApprovedOt() { return hasApprovedOt; }
    public void setHasApprovedOt(Boolean hasApprovedOt) { this.hasApprovedOt = hasApprovedOt; }
    public Boolean getHasApprovedTa() { return hasApprovedTa; }
    public void setHasApprovedTa(Boolean hasApprovedTa) { this.hasApprovedTa = hasApprovedTa; }
}
