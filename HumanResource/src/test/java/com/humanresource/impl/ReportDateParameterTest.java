package com.humanresource.impl;

import com.humanresource.entitymodels.Separation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportDateParameterTest {

    @Test
    void certificateValidityIsOneYearAfterTheFilingDate() {
        assertEquals(
                "07/23/2027",
                CompensatoryOvertimeCreditImpl.certificateValidUntil(LocalDate.of(2026, 7, 23))
        );
        assertEquals("", CompensatoryOvertimeCreditImpl.certificateValidUntil(null));
    }

    @Test
    void workingDaysMatchesTheExistingInclusiveCalendarDayRule() {
        assertEquals(
                3.0,
                LeaveFormReportServiceImpl.workingDaysApplied(
                        LocalDate.of(2026, 7, 21),
                        LocalDate.of(2026, 7, 23),
                        99.0
                )
        );
    }

    @Test
    void workingDaysUsesStoredFallbackForMissingOrInvalidRanges() {
        assertEquals(
                2.5,
                LeaveFormReportServiceImpl.workingDaysApplied(
                        null,
                        LocalDate.of(2026, 7, 23),
                        2.5
                )
        );
        assertEquals(
                4.0,
                LeaveFormReportServiceImpl.workingDaysApplied(
                        LocalDate.of(2026, 7, 24),
                        LocalDate.of(2026, 7, 23),
                        4.0
                )
        );
        assertEquals(
                0.0,
                LeaveFormReportServiceImpl.workingDaysApplied(null, null, null)
        );
    }

    @Test
    void inclusiveDatesMatchesTheExistingReportFormatAndFallback() {
        assertEquals(
                "07/21/2026 - 07/23/2026",
                LeaveFormReportServiceImpl.inclusiveDates(
                        LocalDate.of(2026, 7, 21),
                        LocalDate.of(2026, 7, 23),
                        LocalDate.of(2026, 7, 20)
                )
        );
        assertEquals(
                "07/20/2026",
                LeaveFormReportServiceImpl.inclusiveDates(
                        null,
                        null,
                        LocalDate.of(2026, 7, 20)
                )
        );
        assertEquals("", LeaveFormReportServiceImpl.inclusiveDates(null, null, null));
    }

    @Test
    void separationTextUsesTheLatestSeparationAndExistingDateFormat() {
        Separation older = separation(1L, LocalDateTime.of(2025, 12, 1, 8, 0));
        Separation latest = separation(2L, LocalDateTime.of(2026, 7, 23, 17, 0));

        assertEquals(
                "Separated effective 07/23/2026",
                LeaveFormReportServiceImpl.separationText(List.of(latest, older))
        );
        assertEquals("", LeaveFormReportServiceImpl.separationText(List.of()));
        assertEquals("", LeaveFormReportServiceImpl.separationText(null));
    }

    private static Separation separation(Long id, LocalDateTime date) {
        Separation separation = new Separation();
        separation.setSeparationId(id);
        separation.setSeparationDate(date);
        return separation;
    }
}
