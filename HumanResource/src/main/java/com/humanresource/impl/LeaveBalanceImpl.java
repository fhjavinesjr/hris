package com.humanresource.impl;

import com.humanresource.dtos.LeaveBalanceDTO;
import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.entitymodels.LeaveBeginningBalance;
import com.humanresource.entitymodels.LeaveInformation;
import com.humanresource.repositories.LeaveApplicationRepository;
import com.humanresource.repositories.LeaveBeginningBalanceRepository;
import com.humanresource.repositories.LeaveInformationRepository;
import com.humanresource.services.LeaveBalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Computes the employee's estimated current leave balance (Option A).
 *
 * Source of truth priority:
 *   1. Latest LeaveInformation record  → sickLeaveBalance / vacationLeaveBalance
 *   2. LeaveBeginningBalance           → fallback when no period has been processed yet
 *
 * After establishing the base balance, any leave applications whose inclusive dates
 * START after the last processed period end (or after the beginning balance asOfDate)
 * and whose status is NOT "Rejected" / "Cancelled" are treated as already-consuming
 * credit (conservative / safe-for-employee view).
 *
 * SPL and Forced Leave use their respective beginning balances minus approved
 * applications within the current calendar year (CSC grants 3 SPL days per year).
 */
@Service
public class LeaveBalanceImpl implements LeaveBalanceService {

    private static final Logger log = LoggerFactory.getLogger(LeaveBalanceImpl.class);

    // Leave types that consume VL credit (CSC: Forced Leave is mandatory VL use)
    private static final Set<String> VL_CONSUMING_TYPES = Set.of(
            "Vacation Leave", "Forced Leave"
    );

    // Statuses that mean the application is NOT consuming leave credit
    private static final Set<String> INACTIVE_STATUSES = Set.of(
            "Rejected", "Cancelled"
    );

    private final LeaveInformationRepository leaveInfoRepository;
    private final LeaveBeginningBalanceRepository begBalanceRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;

    public LeaveBalanceImpl(
            LeaveInformationRepository leaveInfoRepository,
            LeaveBeginningBalanceRepository begBalanceRepository,
            LeaveApplicationRepository leaveApplicationRepository) {
        this.leaveInfoRepository = leaveInfoRepository;
        this.begBalanceRepository = begBalanceRepository;
        this.leaveApplicationRepository = leaveApplicationRepository;
    }

    @Override
    public LeaveBalanceDTO getCurrentBalance(Long employeeId) throws Exception {
        LeaveBalanceDTO result = new LeaveBalanceDTO();
        result.setEmployeeId(employeeId);

        // ── Step 1: Establish base VL / SL from the latest processed period ──────
        Optional<LeaveInformation> latestPeriodOpt =
                leaveInfoRepository.findTopByEmployeeIdAndCutoffEndDateBeforeOrderByCutoffEndDateDesc(
                        employeeId, LocalDate.now().plusYears(10)); // "before far future" = any record

        LocalDate cutoffAfter; // applications starting AFTER this date are "unposted"
        double baseVL;
        double baseSL;

        if (latestPeriodOpt.isPresent()) {
            LeaveInformation latest = latestPeriodOpt.get();
            baseVL = nvl(latest.getVacationLeaveBalance());
            baseSL = nvl(latest.getSickLeaveBalance());
            cutoffAfter = latest.getCutoffEndDate();
            result.setLastProcessedPeriodEnd(latest.getCutoffEndDate().toString());
        } else {
            // No period processed yet — fall back to beginning balances
            Optional<LeaveBeginningBalance> vlBeg =
                    begBalanceRepository.findByEmployeeIdAndLeaveType(employeeId, "Vacation Leave");
            Optional<LeaveBeginningBalance> slBeg =
                    begBalanceRepository.findByEmployeeIdAndLeaveType(employeeId, "Sick Leave");

            baseVL = vlBeg.map(b -> nvl(b.getBalance())).orElse(0.0);
            baseSL = slBeg.map(b -> nvl(b.getBalance())).orElse(0.0);

            // Use asOfDate of SL beginning balance as the cutoff, fallback to epoch
            cutoffAfter = slBeg.flatMap(b -> Optional.ofNullable(b.getAsOfDate()))
                    .orElse(LocalDate.of(2000, 1, 1));
            result.setLastProcessedPeriodEnd(null);
        }

        // ── Step 2: Collect all leave applications for this employee ─────────────
        List<LeaveApplication> allApps = leaveApplicationRepository.findByEmployeeId(employeeId);

        // ── Step 3: Deduct unposted VL / SL applications ────────────────────────
        // "Unposted" = startDate is AFTER the last posted period end AND not Rejected/Cancelled
        double unpostedVL = 0.0;
        double unpostedSL = 0.0;
        final LocalDate cutoffFinal = cutoffAfter;

        for (LeaveApplication app : allApps) {
            if (app.getStartDate() == null) continue;
            if (!app.getStartDate().isAfter(cutoffFinal)) continue;
            if (INACTIVE_STATUSES.contains(app.getStatus())) continue;

            double days = nvl(app.getNoOfDays());
            // If noOfDays is not stored, estimate from inclusive date range
            if (days <= 0 && app.getEndDate() != null) {
                days = estimateWorkingDays(app.getStartDate(), app.getEndDate());
            }

            String leaveType = app.getLeaveType();
            if ("Sick Leave".equalsIgnoreCase(leaveType)) {
                unpostedSL += days;
            } else if (VL_CONSUMING_TYPES.stream()
                    .anyMatch(t -> t.equalsIgnoreCase(leaveType))) {
                unpostedVL += days;
            }
        }

        result.setVacationLeaveBalance(round3(baseVL - unpostedVL));
        result.setSickLeaveBalance(round3(baseSL - unpostedSL));

        // ── Step 4: SPL — beginning balance minus approved SPL in current year ───
        int currentYear = LocalDate.now().getYear();
        Optional<LeaveBeginningBalance> splBeg =
                begBalanceRepository.findByEmployeeIdAndLeaveType(employeeId, "Special Privilege Leave");
        double splBase = splBeg.map(b -> nvl(b.getBalance())).orElse(0.0);

        double usedSPL = allApps.stream()
                .filter(a -> "Special Privilege Leave".equalsIgnoreCase(a.getLeaveType()))
                .filter(a -> !INACTIVE_STATUSES.contains(a.getStatus()))
                .filter(a -> "Approved".equalsIgnoreCase(a.getApprovedStatus()))
                .filter(a -> a.getStartDate() != null && a.getStartDate().getYear() == currentYear)
                .mapToDouble(a -> {
                    double d = nvl(a.getNoOfDays());
                    if (d <= 0 && a.getEndDate() != null) d = estimateWorkingDays(a.getStartDate(), a.getEndDate());
                    return d;
                })
                .sum();

        result.setSplBalance(round3(splBase - usedSPL));

        // ── Step 5: Forced Leave — beginning balance minus approved FL in current year ─
        Optional<LeaveBeginningBalance> flBeg =
                begBalanceRepository.findByEmployeeIdAndLeaveType(employeeId, "Forced Leave");
        double flBase = flBeg.map(b -> nvl(b.getBalance())).orElse(0.0);

        double usedFL = allApps.stream()
                .filter(a -> "Forced Leave".equalsIgnoreCase(a.getLeaveType()))
                .filter(a -> !INACTIVE_STATUSES.contains(a.getStatus()))
                .filter(a -> "Approved".equalsIgnoreCase(a.getApprovedStatus()))
                .filter(a -> a.getStartDate() != null && a.getStartDate().getYear() == currentYear)
                .mapToDouble(a -> {
                    double d = nvl(a.getNoOfDays());
                    if (d <= 0 && a.getEndDate() != null) d = estimateWorkingDays(a.getStartDate(), a.getEndDate());
                    return d;
                })
                .sum();

        result.setForcedLeaveBalance(round3(flBase - usedFL));

        log.debug("LeaveBalance for emp {}: VL={}, SL={}, SPL={}, FL={}, cutoffAfter={}",
                employeeId, result.getVacationLeaveBalance(), result.getSickLeaveBalance(),
                result.getSplBalance(), result.getForcedLeaveBalance(), cutoffFinal);

        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double nvl(Double v) {
        return v == null ? 0.0 : v;
    }

    private double round3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    /**
     * Rough estimate of working days between two dates (Mon–Fri only, no holiday awareness).
     * Used only when noOfDays is not stored on the application.
     */
    private double estimateWorkingDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) return 0;
        long days = 0;
        LocalDate d = start;
        while (!d.isAfter(end)) {
            java.time.DayOfWeek dow = d.getDayOfWeek();
            if (dow != java.time.DayOfWeek.SATURDAY && dow != java.time.DayOfWeek.SUNDAY) {
                days++;
            }
            d = d.plusDays(1);
        }
        return days;
    }
}
