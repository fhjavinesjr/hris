package com.timekeeping.reports;

import com.timekeeping.dtos.DtrReportRow;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MraDailyProjectionTest {
    @Test
    void combinesSegmentsOnceAndKeepsDistinctUndertime() {
        LocalDate day = LocalDate.of(2026, 10, 1);
        List<MraDailyProjection.Day> result = MraDailyProjection.fromDtr(List.of(
                row(day, 480, 15, 20, 1d, "Present", ""),
                row(day, 480, 15, 10, 1d, "Present", "")
        ), day, day);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).workMinutes()).isEqualTo(480);
        assertThat(result.get(0).lateMinutes()).isEqualTo(15);
        assertThat(result.get(0).undertimeMinutes()).isEqualTo(15);
        assertThat(result.get(0).overtimeMinutes()).isEqualTo(60);
        assertThat(result.get(0).status()).isEqualTo("PRESENT");
    }

    @Test
    void doesNotCountDtrCombinedLateAsDistinctUndertime() {
        LocalDate day = LocalDate.of(2026, 9, 11);
        MraDailyProjection.Day result = MraDailyProjection.fromDtr(List.of(
                row(day, 111, 428, 428, 0d, "Present", "")
        ), day, day).get(0);
        assertThat(result.lateMinutes()).isEqualTo(428);
        assertThat(result.undertimeMinutes()).isZero();
    }

    @Test
    void flagsRecordedWorkOnRestDayAndUnfinishedDtr() {
        LocalDate day = LocalDate.of(2026, 9, 5);
        DtrReportRow restDayWork = row(day, 480, 0, 0, 0d, "Present", "", "REST DAY");
        DtrReportRow unfinished = row(day.plusDays(1), 480, 20, 20, 0d, "IN PROGRESS", "");
        List<MraDailyProjection.Day> result = MraDailyProjection.fromDtr(
                List.of(restDayWork, unfinished), day, day.plusDays(1));
        assertThat(result.get(0).status()).isEqualTo("REST DAY WORKED - REVIEW");
        assertThat(result.get(1).status()).isEqualTo("DTR IN PROGRESS - REVIEW");
    }

    @Test
    void doesNotCallZeroWorkedMinutesPresent() {
        LocalDate day = LocalDate.of(2026, 9, 30);
        MraDailyProjection.Day result = MraDailyProjection.fromDtr(List.of(
                row(day, 0, 0, 0, 0d, "Present", "")
        ), day, day).get(0);
        assertThat(result.status()).isEqualTo("DTR WITHOUT WORK - REVIEW");
    }

    @Test
    void overnightDepartureIsNotAnExtraPresentDayOrFalseDtrError() {
        LocalDate day = LocalDate.of(2026, 10, 2);
        DtrReportRow departure = new DtrReportRow(Date.valueOf(day), "Sample Employee",
                null, null, null, null, 0, 0, 0d, 0d, 0d, 0d, 0d,
                0, 0, 0, "", "", null, null, "", 0, "Present", "", null,
                true, "", "Friday");
        MraDailyProjection.Day result = MraDailyProjection.fromDtr(
                List.of(departure), day, day).get(0);
        assertThat(result.status()).isEqualTo("OVERNIGHT DEPARTURE");
    }

    @Test
    void neverInventsAwolFromUnknownDay() {
        LocalDate day = LocalDate.of(2026, 10, 2);
        MraDailyProjection.Day unknown = MraDailyProjection.fromDtr(List.of(
                row(day, 0, 0, 0, 0d, "NO SCHEDULE / NO DTR - REVIEW", "")
        ), day, day).get(0);
        assertThat(unknown.status()).contains("REVIEW");
        assertThat(unknown.absentMinutes()).isZero();
        MraDailyProjection.Day awol = MraDailyProjection.fromDtr(List.of(
                row(day, 0, 0, 0, 0d, "AWOL", "")
        ), day, day).get(0);
        assertThat(awol.status()).isEqualTo("AWOL");
    }

    private DtrReportRow row(LocalDate day, int work, int late, int under,
                             double overtimeHours, String remarks, String leave) {
        return row(day, work, late, under, overtimeHours, remarks, leave, "");
    }

    private DtrReportRow row(LocalDate day, int work, int late, int under,
                             double overtimeHours, String remarks, String leave, String dayOff) {
        return new DtrReportRow(Date.valueOf(day), "Sample Employee", null, null, null, null,
                work, 0, 0d, overtimeHours, 0d, overtimeHours, 0d,
                late, under, 0, "", overtimeHours > 0 ? "OT" : "", null, null,
                dayOff, 0, remarks, leave, null, false, "", "Thursday");
    }
}
