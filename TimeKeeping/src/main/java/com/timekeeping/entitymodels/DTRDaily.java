package com.timekeeping.entitymodels;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dtr_daily", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "work_date"}))
public class DTRDaily implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dtr_daily_id")
    private Long dtrDailyId;

    @Column(name = "employee_id", nullable = false, length = 100)
    private String employeeId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "total_work_minutes", nullable = false)
    private Integer totalWorkMinutes = 0;

    @Column(name = "total_late_minutes", nullable = false)
    private Integer totalLateMinutes = 0;

    @Column(name = "total_undertime_minutes", nullable = false)
    private Integer totalUndertimeMinutes = 0;

    @Column(name = "total_overtime_minutes", nullable = false)
    private Integer totalOvertimeMinutes = 0;

    @Column(name = "attendance_status", nullable = false, length = 50)
    private String attendanceStatus;

    @OneToMany(mappedBy = "dtrDaily", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DTRSegment> segments = new ArrayList<>();

    // Getters and setters
    public Long getDtrDailyId() { return dtrDailyId; }
    public void setDtrDailyId(Long dtrDailyId) { this.dtrDailyId = dtrDailyId; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
    public Integer getTotalWorkMinutes() { return totalWorkMinutes; }
    public void setTotalWorkMinutes(Integer totalWorkMinutes) { this.totalWorkMinutes = totalWorkMinutes; }
    public Integer getTotalLateMinutes() { return totalLateMinutes; }
    public void setTotalLateMinutes(Integer totalLateMinutes) { this.totalLateMinutes = totalLateMinutes; }
    public Integer getTotalUndertimeMinutes() { return totalUndertimeMinutes; }
    public void setTotalUndertimeMinutes(Integer totalUndertimeMinutes) { this.totalUndertimeMinutes = totalUndertimeMinutes; }
    public Integer getTotalOvertimeMinutes() { return totalOvertimeMinutes; }
    public void setTotalOvertimeMinutes(Integer totalOvertimeMinutes) { this.totalOvertimeMinutes = totalOvertimeMinutes; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    public List<DTRSegment> getSegments() { return segments; }
    public void setSegments(List<DTRSegment> segments) { this.segments = segments; }
}

