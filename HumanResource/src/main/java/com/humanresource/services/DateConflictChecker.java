package com.humanresource.services;

import com.humanresource.repositories.CompensatoryTimeOffRepository;
import com.humanresource.repositories.LeaveApplicationRepository;
import com.humanresource.repositories.OfficialEngagementApplicationRepository;
import com.humanresource.repositories.PassSlipRepository;
import com.humanresource.repositories.TimeCorrectionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Centralised cross-type date-conflict checker.
 *
 * Any combination of Pass Slip, CTO, Official Engagement, Time Correction, and
 * Leave Application for the same employee cannot share overlapping dates while
 * they are in "Pending" or "Approved" status.
 *
 * Impls call {@link #checkSingleDate} or {@link #checkDateRange} BEFORE their
 * own try-catch so that the thrown {@link IllegalArgumentException} propagates
 * to the {@code GlobalExceptionHandler}, which returns HTTP 400 with the message.
 */
@Service
public class DateConflictChecker {

    private static final List<String> ACTIVE = List.of("Pending", "Approved");

    private final PassSlipRepository psRepo;
    private final CompensatoryTimeOffRepository ctoRepo;
    private final TimeCorrectionRepository tcRepo;
    private final OfficialEngagementApplicationRepository oeRepo;
    private final LeaveApplicationRepository leaveRepo;

    public DateConflictChecker(PassSlipRepository psRepo,
                               CompensatoryTimeOffRepository ctoRepo,
                               TimeCorrectionRepository tcRepo,
                               OfficialEngagementApplicationRepository oeRepo,
                               LeaveApplicationRepository leaveRepo) {
        this.psRepo = psRepo;
        this.ctoRepo = ctoRepo;
        this.tcRepo = tcRepo;
        this.oeRepo = oeRepo;
        this.leaveRepo = leaveRepo;
    }

    /**
     * Validates that no active (Pending/Approved) record of any of the 5 types
     * exists for {@code employeeId} on {@code date}.
     *
     * @throws IllegalArgumentException describing the first conflict found
     */
    public void checkSingleDate(Long employeeId, LocalDate date) {
        // Pass Slip
        boolean psConflict = psRepo
                .findByEmployeeIdAndPassSlipDateBetween(employeeId, date, date)
                .stream().anyMatch(ps -> ACTIVE.contains(ps.getStatus()));
        if (psConflict)
            throw new IllegalArgumentException(
                    "A Pass Slip is already filed for " + date + ". Please choose a different date.");

        // CTO
        boolean ctoConflict = ctoRepo
                .findByEmployeeIdAndDateOfOffsetBetween(employeeId, date, date)
                .stream().anyMatch(c -> ACTIVE.contains(c.getStatus()));
        if (ctoConflict)
            throw new IllegalArgumentException(
                    "A Compensatory Time Off is already filed for " + date + ". Please choose a different date.");

        // Time Correction
        boolean tcConflict = tcRepo
                .findByEmployeeIdAndWorkDateBetween(employeeId, date, date)
                .stream().anyMatch(tc -> ACTIVE.contains(tc.getStatus()));
        if (tcConflict)
            throw new IllegalArgumentException(
                    "A Time Correction is already filed for " + date + ". Please choose a different date.");

        // Official Engagement (range-based; a single day has startDate == endDate)
        boolean oeConflict = oeRepo
                .findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(employeeId, date, date)
                .stream().anyMatch(oe -> ACTIVE.contains(oe.getStatus()));
        if (oeConflict)
            throw new IllegalArgumentException(
                    "An Official Engagement is already filed that covers " + date + ". Please choose a different date.");

        // Leave Application (range-based)
        boolean leaveConflict = leaveRepo
                .findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(employeeId, date, date)
                .stream().anyMatch(l -> ACTIVE.contains(l.getStatus()));
        if (leaveConflict)
            throw new IllegalArgumentException(
                    "A Leave Application is already filed that covers " + date + ". Please choose a different date.");
    }

    /**
     * Validates that no active record overlaps the given date range [{@code startDate}, {@code endDate}].
     *
     * @throws IllegalArgumentException describing the first conflict found
     */
    public void checkDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        // Pass Slip (point-in-time; any passSlipDate within the range)
        boolean psConflict = psRepo
                .findByEmployeeIdAndPassSlipDateBetween(employeeId, startDate, endDate)
                .stream().anyMatch(ps -> ACTIVE.contains(ps.getStatus()));
        if (psConflict)
            throw new IllegalArgumentException(
                    "A Pass Slip is already filed within the selected dates. Please adjust the date range.");

        // CTO (point-in-time; any dateOfOffset within the range)
        boolean ctoConflict = ctoRepo
                .findByEmployeeIdAndDateOfOffsetBetween(employeeId, startDate, endDate)
                .stream().anyMatch(c -> ACTIVE.contains(c.getStatus()));
        if (ctoConflict)
            throw new IllegalArgumentException(
                    "A Compensatory Time Off is already filed within the selected dates. Please adjust the date range.");

        // Time Correction (point-in-time; any workDate within the range)
        boolean tcConflict = tcRepo
                .findByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate)
                .stream().anyMatch(tc -> ACTIVE.contains(tc.getStatus()));
        if (tcConflict)
            throw new IllegalArgumentException(
                    "A Time Correction is already filed within the selected dates. Please adjust the date range.");

        // Official Engagement (range overlap: OE.startDate <= endDate AND OE.endDate >= startDate)
        boolean oeConflict = oeRepo
                .findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(employeeId, endDate, startDate)
                .stream().anyMatch(oe -> ACTIVE.contains(oe.getStatus()));
        if (oeConflict)
            throw new IllegalArgumentException(
                    "An Official Engagement already covers some or all of the selected dates. Please adjust the date range.");

        // Leave Application (range overlap: Leave.startDate <= endDate AND Leave.endDate >= startDate)
        boolean leaveConflict = leaveRepo
                .findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(employeeId, endDate, startDate)
                .stream().anyMatch(l -> ACTIVE.contains(l.getStatus()));
        if (leaveConflict)
            throw new IllegalArgumentException(
                    "A Leave Application already covers some or all of the selected dates. Please adjust the date range.");
    }
}
