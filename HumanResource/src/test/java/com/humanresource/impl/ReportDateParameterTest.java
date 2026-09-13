package com.humanresource.impl;

import com.humanresource.entitymodels.Separation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
    void leaveCreditCertificationUsesPreviousCalendarMonthEnd() {
        assertEquals(
                LocalDate.of(2019, 9, 30),
                LeaveFormReportServiceImpl.previousMonthEnd(LocalDate.of(2019, 10, 30))
        );
        assertEquals(
                LocalDate.of(2023, 2, 28),
                LeaveFormReportServiceImpl.previousMonthEnd(LocalDate.of(2023, 3, 1))
        );
        assertEquals(
                LocalDate.of(2023, 5, 31),
                LeaveFormReportServiceImpl.previousMonthEnd(LocalDate.of(2023, 6, 5))
        );
    }

    @Test
    void certificationReconstructsTopBalanceFromDashboardAndCurrentApplication() {
        assertEquals(46.666, LeaveFormReportServiceImpl.balanceBeforeApplication(45.666, 1.0));
        assertEquals(24.417, LeaveFormReportServiceImpl.balanceBeforeApplication(24.417, 0.0));
        assertEquals(45.666,
                LeaveFormReportServiceImpl.balanceBeforeApplication(45.666, 1.0) - 1.0);
    }

    @Test
    void mapsEveryCivilServiceFormSixLeaveTypeWithoutFallingBackToPaternity() {
        assertEquals(1, LeaveFormReportServiceImpl.leaveTypeCode("Vacation Leave"));
        assertEquals(5, LeaveFormReportServiceImpl.leaveTypeCode("Forced Leave"));
        assertEquals(2, LeaveFormReportServiceImpl.leaveTypeCode("Sick Leave"));
        assertEquals(4, LeaveFormReportServiceImpl.leaveTypeCode("Maternity Leave"));
        assertEquals(3, LeaveFormReportServiceImpl.leaveTypeCode("Paternity Leave"));
        assertEquals(6, LeaveFormReportServiceImpl.leaveTypeCode("Special Privilege Leave"));
        assertEquals(7, LeaveFormReportServiceImpl.leaveTypeCode("Solo Parent Leave"));
        assertEquals(10, LeaveFormReportServiceImpl.leaveTypeCode("Study Leave"));
        assertEquals(15, LeaveFormReportServiceImpl.leaveTypeCode("10-Day VAWC Leave"));
        assertEquals(8, LeaveFormReportServiceImpl.leaveTypeCode("Rehabilitation Privilege"));
        assertEquals(9, LeaveFormReportServiceImpl.leaveTypeCode("Special Leave Benefits for Women"));
        assertEquals(16, LeaveFormReportServiceImpl.leaveTypeCode("Special Emergency (Calamity) Leave"));
        assertEquals(17, LeaveFormReportServiceImpl.leaveTypeCode("Adoption Leave"));
        assertEquals(0, LeaveFormReportServiceImpl.leaveTypeCode("COVID-19 Treatment Leave"));
    }

    @Test
    void mapsConditionalFormDetailsAndPreservesLegacyPlainText() {
        Map<String, Object> vacation = new HashMap<>();
        LeaveFormReportServiceImpl.putLeaveDetailParameters(
                vacation, "Vacation Leave", "Abroad: Tokyo");
        assertEquals(1, vacation.get("VL_ABROAD"));
        assertEquals(0, vacation.get("VL_IN_COUNTRY"));
        assertEquals("Tokyo", vacation.get("VL_LOCATION"));

        Map<String, Object> sick = new HashMap<>();
        LeaveFormReportServiceImpl.putLeaveDetailParameters(
                sick, "Sick Leave", "Out Patient: Influenza");
        assertEquals(1, sick.get("SL_OUT_PATIENT"));
        assertEquals(0, sick.get("SL_IN_HOSPITAL"));
        assertEquals("Influenza", sick.get("SL_ILLNESS"));

        Map<String, Object> legacySick = new HashMap<>();
        LeaveFormReportServiceImpl.putLeaveDetailParameters(
                legacySick, "Sick Leave", "Legacy illness text");
        assertEquals(1, legacySick.get("SL_OUT_PATIENT"));
        assertEquals("Legacy illness text", legacySick.get("SL_ILLNESS"));

        Map<String, Object> study = new HashMap<>();
        LeaveFormReportServiceImpl.putLeaveDetailParameters(
                study, "Study Leave", "BAR/Board Examination Review");
        assertEquals(2, study.get("STUDY_LEAVE_CASE"));
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
