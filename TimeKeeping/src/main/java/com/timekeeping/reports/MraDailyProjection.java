package com.timekeeping.reports;

import com.timekeeping.dtos.DtrReportRow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One row per employee/work date, derived from the same rows printed by the DTR. */
public final class MraDailyProjection {
    private MraDailyProjection() { }

    public static List<Day> fromDtr(List<DtrReportRow> dtrRows, LocalDate monthStart, LocalDate monthEnd) {
        Map<LocalDate, Accumulator> days = new LinkedHashMap<>();
        for (DtrReportRow row : dtrRows) {
            if (row.getDtrDate() == null) continue;
            LocalDate date = row.getDtrDate().toLocalDate();
            if (date.isBefore(monthStart) || date.isAfter(monthEnd)) continue;
            Accumulator day = days.computeIfAbsent(date, Accumulator::new);
            day.accept(row);
        }
        return days.values().stream().map(Accumulator::finish).toList();
    }

    public record Day(LocalDate date, String status, String leaveType, int workMinutes,
                      int lateMinutes, int undertimeMinutes, int overtimeMinutes,
                      int absentMinutes, boolean overtimeApproved, String remarks) { }

    private static final class Accumulator {
        private final LocalDate date;
        private final List<String> remarks = new ArrayList<>();
        private String leaveType = "";
        private int workMinutes;
        private int lateMinutes;
        private int combinedDtrUndertimeMinutes;
        private int overtimeMinutes;
        private int absentMinutes;
        private boolean overtimeApproved;
        private boolean restDay;
        private boolean hasWorkDateRow;
        private boolean hasOvernightDeparture;

        private Accumulator(LocalDate date) { this.date = date; }

        private void accept(DtrReportRow row) {
            hasOvernightDeparture |= Boolean.TRUE.equals(row.getIsForNextDay());
            hasWorkDateRow |= !Boolean.TRUE.equals(row.getIsForNextDay());
            workMinutes = Math.max(workMinutes, number(row.getRegMin()));
            lateMinutes = Math.max(lateMinutes, number(row.getLateMin()));
            // The CSC DTR UNDERTIME column includes both late and distinct undertime.
            // Segment rows carry their own combined values; next-day departure rows
            // are display-only and must not be added again.
            if (!Boolean.TRUE.equals(row.getIsForNextDay())) {
                combinedDtrUndertimeMinutes += number(row.getUnderMinHours()) * 60 + number(row.getUnderMin());
            }
            overtimeMinutes = Math.max(overtimeMinutes,
                    (int) Math.round((row.getOtReg() == null ? 0d : row.getOtReg()) * 60));
            absentMinutes = Math.max(absentMinutes, number(row.getAbsentMin()));
            overtimeApproved |= !blank(row.getOt());
            restDay |= !blank(row.getDayOff());
            if (blank(leaveType) && !blank(row.getLeaveType())) leaveType = row.getLeaveType().trim();
            if (!blank(row.getRemarks()) && !remarks.contains(row.getRemarks().trim())) {
                remarks.add(row.getRemarks().trim());
            }
        }

        private Day finish() {
            boolean unfinishedDtr = remarks.stream().anyMatch(value ->
                    "IN PROGRESS".equalsIgnoreCase(value)
                            || "PENDING FINALIZATION".equalsIgnoreCase(value));
            boolean presentWithoutWork = hasWorkDateRow && workMinutes == 0
                    && remarks.stream().anyMatch(value -> "PRESENT".equalsIgnoreCase(value));
            String status = !blank(leaveType) && (workMinutes > 0 || absentMinutes > 0)
                    ? "LEAVE / DTR CONFLICT - REVIEW"
                    : !blank(leaveType) ? "LEAVE"
                    : absentMinutes > 0 && workMinutes > 0 ? "DTR CONFLICT - REVIEW"
                    : absentMinutes > 0 ? "ABSENT / FOR REVIEW"
                    : unfinishedDtr ? "DTR IN PROGRESS - REVIEW"
                    : presentWithoutWork ? "DTR WITHOUT WORK - REVIEW"
                    : workMinutes > 0 && restDay ? "REST DAY WORKED - REVIEW"
                    : workMinutes > 0 ? "PRESENT"
                    : hasOvernightDeparture && !hasWorkDateRow ? "OVERNIGHT DEPARTURE"
                    : restDay ? "REST DAY"
                    : remarks.isEmpty() ? "FOR REVIEW" : remarks.get(0).toUpperCase();
            if (remarks.stream().anyMatch(value -> "AWOL".equalsIgnoreCase(value))) {
                status = !blank(leaveType) || workMinutes > 0 ? "AWOL / DTR CONFLICT - REVIEW" : "AWOL";
            }
            int distinctUndertimeMinutes = Math.max(0, combinedDtrUndertimeMinutes - lateMinutes);
            return new Day(date, status, leaveType, workMinutes, lateMinutes,
                    distinctUndertimeMinutes, overtimeMinutes, absentMinutes,
                    overtimeApproved, String.join("; ", remarks));
        }
    }

    private static int number(Integer value) { return value == null ? 0 : value; }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
