package com.payroll.dtos;

import java.time.LocalDate;

/**
 * Holiday on a specific calendar date.
 * Fetched once per batch from the Administrative service.
 *
 * The Administrative service must expose:
 *   GET /api/holiday/range?from={cutoffStart}&to={cutoffEnd}
 * returning List&lt;HolidayDTO&gt;.
 */
public class HolidayDTO {

    private LocalDate date;

    /**
     * REGULAR | SPECIAL | WORK_SUSPENSION
     * REGULAR = paid, non-work; SPECIAL = partially paid; WORK_SUSPENSION = varies by area.
     */
    private String holidayType;

    private String name;

    // Getters / Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getHolidayType() { return holidayType; }
    public void setHolidayType(String holidayType) { this.holidayType = holidayType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
