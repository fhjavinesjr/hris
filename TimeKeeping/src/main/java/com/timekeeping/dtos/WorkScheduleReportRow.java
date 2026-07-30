package com.timekeeping.dtos;

import java.util.Date;

/**
 * Provider-neutral row consumed by {@code reports/works_schedule.jrxml}.
 */
public class WorkScheduleReportRow {
    private final String department;
    private final String fullname;
    private final Date dtrDate;
    private final String sched;
    private final String actual;
    private final String jobPosition;
    private final Integer salaryGrade;

    public WorkScheduleReportRow(
            String department,
            String fullname,
            Date dtrDate,
            String sched,
            String actual,
            String jobPosition,
            Integer salaryGrade) {
        this.department = department;
        this.fullname = fullname;
        this.dtrDate = dtrDate;
        this.sched = sched;
        this.actual = actual;
        this.jobPosition = jobPosition;
        this.salaryGrade = salaryGrade;
    }

    public String getDepartment() { return department; }
    public String getFullname() { return fullname; }
    public Date getDtrDate() { return dtrDate; }
    public String getSched() { return sched; }
    public String getActual() { return actual; }
    public String getJobPosition() { return jobPosition; }
    public Integer getSalaryGrade() { return salaryGrade; }
}
