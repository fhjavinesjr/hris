package com.payroll.impl;

import com.payroll.dtos.*;
import com.payroll.entitymodels.PayrollDetail;
import com.payroll.entitymodels.PayrollDetailDeduction;
import com.payroll.entitymodels.PayrollDetailEarning;
import com.payroll.entitymodels.PayrollStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure, stateless, thread-safe payroll computation engine.
 *
 * ─── Design principles ────────────────────────────────────────────────────────
 *  • Zero database access — all data comes from {@link PayrollDataSnapshot}.
 *  • No shared mutable state — every call to {@link #compute} is independent.
 *  • All formulas derived from the legacy PayrollComputation.java logic.
 *  • CSC Rule applied: late/undertime minutes are charged to VL credits first;
 *    only when credits are exhausted does a salary deduction apply.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Component
public class PayrollComputationEngine {

    private static final Logger log = LoggerFactory.getLogger(PayrollComputationEngine.class);

    /** Standard work minutes per day (8 hours). */
    private static final int WORK_MINUTES_PER_DAY = 480;

    /** Days per month standard for ZCMC salary-rate calculation. */
    private static final int STANDARD_DAYS_PER_MONTH = 22;

    /** Leave earned per cutoff period when employee had no AWOL. */
    private static final double STANDARD_EARNED_LEAVE = 1.25;

    /** Subsistence daily deduction rate per absent+leave day. */
    private static final double SUBSISTENCE_DAILY_RATE = 50.0;

    // ─────────────────────────────────────────────────────────────────────────
    //  Entry point
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Compute full payroll for one employee using pre-fetched snapshot data.
     * This method is designed to be called from multiple threads concurrently.
     *
     * @param emp     Employee information
     * @param request Salary period and computation parameters
     * @param snap    Pre-loaded batch data (read-only, shared across threads)
     * @return Fully populated {@link PayrollDetail} ready for persistence
     */
    public PayrollDetail compute(EmployeePayrollInfoDTO emp,
                                 PayrollComputationRequest request,
                                 PayrollDataSnapshot snap) {
        try {
            // ── Step 1: Attendance aggregation ────────────────────────────────
            AttendanceSummary attendance = aggregateAttendance(emp, request, snap);

            // ── Step 2: Salary rates ──────────────────────────────────────────
            int cutoffDays = request.getCutoffDays() != null ? request.getCutoffDays() : STANDARD_DAYS_PER_MONTH;
            double basicPerSalary = emp.getBasicPerSalary();
            if (Boolean.TRUE.equals(emp.getIsPartTime())) {
                basicPerSalary = basicPerSalary / 2;
            }
            double salaryPerDay    = cutoffDays > 0 ? roundOff(basicPerSalary / cutoffDays) : 0;
            double salaryPerMinute = roundOff(salaryPerDay / 8 / 60);

            // ── Step 3: Absent deduction ──────────────────────────────────────
            double absentDeduction = roundOff(salaryPerDay * attendance.absentDays);

            // ── Step 4: Earned leave for this period ──────────────────────────
            double earnedLeave = attendance.awolDays > 0 ? 0.0 : STANDARD_EARNED_LEAVE;

            // ── Step 5: Late / undertime — CSC VL-first rule ──────────────────
            LateUndertimeResult lateResult = applyLateUndertimeCscRule(
                    emp, attendance, salaryPerMinute, earnedLeave, snap);

            // ── Step 6: Actual basic (net base after deductions) ──────────────
            double actualBasic = roundOff(basicPerSalary
                    - absentDeduction
                    - lateResult.lateValue
                    - lateResult.undertimeValue);
            if (actualBasic < 0) actualBasic = 0;

            // ── Step 7: Earnings lines ────────────────────────────────────────
            List<PayrollDetailEarning> earningLines = new ArrayList<>();
            double grossAmount = buildEarnings(emp, attendance, actualBasic, basicPerSalary,
                    earnedLeave, request, snap, earningLines);

            // ── Step 8: Mandatory deductions (GSIS, PhilHealth, PagIbig) ─────
            List<PayrollDetailDeduction> deductionLines = new ArrayList<>();
            double taxableIncome = actualBasic;   // adjusted during mandatory deductions
            taxableIncome = buildMandatoryDeductions(emp, basicPerSalary, taxableIncome, snap,
                    deductionLines, request.getSalaryType());

            // ── Step 9: Withholding tax ───────────────────────────────────────
            double wTax = computeWithholdingTax(taxableIncome, request.getSalaryType(), snap);
            if (wTax > 0) {
                deductionLines.add(new PayrollDetailDeduction(null,
                        "WTX", "Withholding Tax", roundOff(wTax), 0.0, deductionLines.size()));
            }

            // ── Step 10: Total deductions (mandatory already summed) ──────────
            double totalDeduction = deductionLines.stream()
                    .mapToDouble(d -> d.getAmount() != null ? d.getAmount() : 0)
                    .sum();

            double netAmount = roundOff(grossAmount - totalDeduction);
            if (netAmount < 0) netAmount = 0;

            // ── Step 11: Leave balances ───────────────────────────────────────
            LeaveBalanceResult leaveBalance = computeLeaveBalances(emp, attendance, earnedLeave,
                    lateResult.vlDeductedDays, snap);

            // ── Step 12: Assemble PayrollDetail ──────────────────────────────
            return buildPayrollDetail(emp, request, attendance, lateResult,
                    earnedLeave, absentDeduction, actualBasic, basicPerSalary,
                    salaryPerDay, salaryPerMinute, cutoffDays, grossAmount,
                    totalDeduction, netAmount, taxableIncome, wTax,
                    leaveBalance, earningLines, deductionLines);

        } catch (Exception ex) {
            log.error("[PayrollEngine] FAILED for employee {} — {}", emp.getEmployeeNo(), ex.getMessage(), ex);
            throw new PayrollComputationException(emp.getEmployeeNo(), ex);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Step 1 — Attendance aggregation
    // ─────────────────────────────────────────────────────────────────────────

    private AttendanceSummary aggregateAttendance(EmployeePayrollInfoDTO emp,
                                                   PayrollComputationRequest req,
                                                   PayrollDataSnapshot snap) {
        AttendanceSummary s = new AttendanceSummary();

        // Index approved leave dates for O(1) lookup
        List<ApprovedLeaveDTO> leaves = snap.getLeavesMap()
                .getOrDefault(emp.getEmployeeNo(), Collections.emptyList());

        // key: date → leave record (first record wins for that date)
        Map<LocalDate, ApprovedLeaveDTO> leaveByDate = new LinkedHashMap<>();
        for (ApprovedLeaveDTO l : leaves) {
            leaveByDate.putIfAbsent(l.getLeaveDate(), l);
        }

        List<DtrDailySummaryDTO> dtrs = snap.getDtrMap()
                .getOrDefault(emp.getEmployeeNo(), Collections.emptyList());

        // Index DTR by date for O(1) lookup
        Map<LocalDate, DtrDailySummaryDTO> dtrByDate = new LinkedHashMap<>();
        for (DtrDailySummaryDTO dtr : dtrs) {
            dtrByDate.put(dtr.getDtrDate(), dtr);
        }

        // Walk every calendar day in the cutoff window
        LocalDate cursor = req.getCutoffStartDate();
        LocalDate cutoffEnd = req.getCutoffEndDate();
        StringBuilder awolDates = new StringBuilder();

        while (!cursor.isAfter(cutoffEnd)) {
            boolean isHoliday = snap.getHolidayMap().containsKey(cursor);
            DtrDailySummaryDTO dtr = dtrByDate.get(cursor);
            ApprovedLeaveDTO leave = leaveByDate.get(cursor);

            boolean isRestDay = dtr != null && Boolean.TRUE.equals(dtr.getRestDay());

            if (!isRestDay && !isHoliday) {
                s.workDays++;
            }

            if (dtr != null && Boolean.TRUE.equals(dtr.getPresent())) {
                // Employee clocked in
                if (leave != null) {
                    // Approved leave day — treat as present, count leave type
                    if ("HALFDAYAM".equals(leave.getWorkDayType()) || "HALFDAYPM".equals(leave.getWorkDayType())) {
                        if (Boolean.TRUE.equals(leave.getWithPay())) {
                            s.workDaysPresent += 0.5;
                        } else {
                            s.workDaysPresent += 0.5;
                            s.halfDayWithoutPay += 0.5;
                        }
                    } else {
                        s.workDaysPresent++;
                    }
                    // Tally leave by type
                    tallyLeaveUsed(s, leave);

                } else if (dtrHasSpecialApproval(dtr)) {
                    // OB / OT / TA approved — treat as full present
                    s.workDaysPresent++;
                } else {
                    // Regular work day
                    if (!isRestDay && !isHoliday) {
                        s.workDaysPresent++;
                        s.lateMinutes += safeInt(dtr.getLateMinutes());
                        s.undertimeMinutes += safeInt(dtr.getUndertimeMinutes());
                    }
                }

            } else if (isRestDay || isHoliday) {
                // Day off or holiday — counts as present for entitlement purposes
                if (isHoliday && !isRestDay) {
                    s.workDaysPresent++;
                }
            } else {
                // No DTR and not a day-off/holiday
                if (leave != null && Boolean.TRUE.equals(leave.getWithPay())) {
                    // Approved leave without clock-in (e.g. whole-day leave filed)
                    s.workDaysPresent++;
                    tallyLeaveUsed(s, leave);
                } else if (leave != null && !Boolean.TRUE.equals(leave.getWithPay())) {
                    // Without-pay leave = AWOL for salary deduction
                    s.awolDays++;
                    appendDate(awolDates, cursor);
                } else {
                    // Genuine AWOL
                    s.awolDays++;
                    s.absentDays++;
                    appendDate(awolDates, cursor);
                }
            }

            cursor = cursor.plusDays(1);
        }

        s.absentParticulars = awolDates.toString().trim();
        s.absentDays = Math.max(0, s.absentDays + s.halfDayWithoutPay);
        return s;
    }

    private boolean dtrHasSpecialApproval(DtrDailySummaryDTO dtr) {
        return Boolean.TRUE.equals(dtr.getHasApprovedOb())
                || Boolean.TRUE.equals(dtr.getHasApprovedOt())
                || Boolean.TRUE.equals(dtr.getHasApprovedTa());
    }

    private void tallyLeaveUsed(AttendanceSummary s, ApprovedLeaveDTO leave) {
        double days = leave.getNoOfDaysApplied() != null ? leave.getNoOfDaysApplied() : 1.0;
        switch (leave.getLeaveType() != null ? leave.getLeaveType() : "") {
            case "VL" -> s.vlUsed += days;
            case "SL" -> s.slUsed += days;
            case "CL" -> s.clUsed += days;
            case "RL" -> s.rlUsed += days;
        }
        s.leaveCount++;
    }

    private void appendDate(StringBuilder sb, LocalDate d) {
        if (!sb.isEmpty()) sb.append(", ");
        sb.append(d.getMonth().name(), 0, 3).append(" ").append(d.getDayOfMonth());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Step 5 — CSC late / undertime rule
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * CSC Rule (Civil Service Commission):
     *   Tardiness and undertime minutes are first charged to the employee's
     *   Vacation Leave (VL) credits.  A salary deduction only applies to the
     *   minutes that exceed the remaining VL balance.
     *
     * @return object carrying lateValue, undertimeValue, and vlDeductedDays
     */
    private LateUndertimeResult applyLateUndertimeCscRule(
            EmployeePayrollInfoDTO emp,
            AttendanceSummary attendance,
            double salaryPerMinute,
            double earnedLeave,
            PayrollDataSnapshot snap) {

        LateUndertimeResult r = new LateUndertimeResult();
        int totalMinutes = attendance.lateMinutes + attendance.undertimeMinutes;
        if (totalMinutes == 0) return r;

        // VL balance: use explicit balance map, fall back to previous period, then to 0
        double vlBalance = snap.getVlBalanceMap().getOrDefault(emp.getEmployeeNo(), -1.0);
        if (vlBalance < 0) {
            PayrollDataSnapshot.PreviousPeriodBalanceDTO prev =
                    snap.getPreviousBalanceMap().get(emp.getEmployeeNo());
            vlBalance = (prev != null) ? Math.max(prev.getVlBalance(), 0) : 0.0;
        }
        // Add leave earned this period (accrued at start of period)
        double effectiveVlBalance = vlBalance + earnedLeave;

        double lateUndertimeDays = totalMinutes / (double) WORK_MINUTES_PER_DAY;

        if (effectiveVlBalance > 0) {
            if (lateUndertimeDays <= effectiveVlBalance) {
                // All minutes charged to VL — no salary deduction
                r.vlDeductedDays = lateUndertimeDays;
                r.lateValue      = 0.0;
                r.undertimeValue = 0.0;
            } else {
                // Partial: charge effectiveVlBalance days to VL, rest to salary
                r.vlDeductedDays = effectiveVlBalance;
                double remainingDays    = lateUndertimeDays - effectiveVlBalance;
                double remainingMinutes = remainingDays * WORK_MINUTES_PER_DAY;

                // Distribute remaining minutes proportionally between late and undertime
                double lateRatio = totalMinutes > 0
                        ? (double) attendance.lateMinutes / totalMinutes : 0.5;
                r.lateValue      = roundOff(salaryPerMinute * remainingMinutes * lateRatio);
                r.undertimeValue = roundOff(salaryPerMinute * remainingMinutes * (1 - lateRatio));
            }
        } else {
            // No VL credits — full salary deduction
            r.vlDeductedDays = 0.0;
            r.lateValue      = roundOff(salaryPerMinute * attendance.lateMinutes);
            r.undertimeValue = roundOff(salaryPerMinute * attendance.undertimeMinutes);
        }
        return r;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Step 7 — Earnings lines
    // ─────────────────────────────────────────────────────────────────────────

    private double buildEarnings(EmployeePayrollInfoDTO emp,
                                  AttendanceSummary attendance,
                                  double actualBasic,
                                  double basicPerSalary,
                                  double earnedLeave,
                                  PayrollComputationRequest req,
                                  PayrollDataSnapshot snap,
                                  List<PayrollDetailEarning> lines) {
        double gross = 0;

        // Basic salary
        PayrollDetailEarning basicLine = new PayrollDetailEarning(
                null, "BASIC", "Basic Salary", roundOff(actualBasic), true, 0);
        lines.add(basicLine);
        gross += basicLine.getAmount();

        // Allowances from snapshot
        List<AllowanceDTO> allowances = snap.getAllowancesMap()
                .getOrDefault(emp.getEmployeeNo(), Collections.emptyList());

        boolean hasHazardInAllowances = false;

        for (AllowanceDTO a : allowances) {
            double amount = computeAllowanceAmount(a, attendance, basicPerSalary, emp);
            if (Boolean.TRUE.equals(a.getIsHazardPay())) {
                hasHazardInAllowances = true;
            }
            PayrollDetailEarning line = new PayrollDetailEarning(
                    null, a.getAllowanceCode(), a.getAllowanceName(),
                    roundOff(amount), Boolean.TRUE.equals(a.getIsTaxable()), lines.size());
            lines.add(line);
            gross += line.getAmount();
        }

        // DOH hazard pay from grade table (when not already in allowances)
        if (Boolean.TRUE.equals(emp.getIsDoh())
                && !Boolean.TRUE.equals(emp.getNoHazardPay())
                && !hasHazardInAllowances) {
            Double hazardRate = snap.getHazardPayRateByGrade().get(emp.getSalaryGrade());
            if (hazardRate != null && hazardRate > 0) {
                double hazardAmount = 0;
                // Zeroed if >= 11 absent days or >= 11 leave days
                if (attendance.absentDays < 11 && attendance.leaveCount < 11) {
                    hazardAmount = roundOff(basicPerSalary * (hazardRate / 100));
                }
                if (Boolean.TRUE.equals(emp.getIsPartTime())) hazardAmount /= 2;
                PayrollDetailEarning hazardLine = new PayrollDetailEarning(
                        null, "HAZARD", "Hazard Pay", hazardAmount, false, lines.size());
                lines.add(hazardLine);
                gross += hazardAmount;
            }
        }

        return roundOff(gross);
    }

    /**
     * Compute individual allowance amount using ZCMC formulas (from legacy PayrollComputation.java).
     */
    private double computeAllowanceAmount(AllowanceDTO a,
                                           AttendanceSummary attendance,
                                           double basicPerSalary,
                                           EmployeePayrollInfoDTO emp) {
        double base = a.getAmountPerSalary() != null ? a.getAmountPerSalary() : 0;
        boolean partTime = Boolean.TRUE.equals(emp.getIsPartTime());

        if (Boolean.TRUE.equals(a.getIsPera())) {
            // PERA: deducted proportionally per absent day
            double amount = partTime ? base / 2 : base;
            if (attendance.absentDays > 0) {
                amount = amount - roundOff(roundOff(amount / STANDARD_DAYS_PER_MONTH) * attendance.absentDays);
            }
            return Math.max(0, amount);

        } else if (Boolean.TRUE.equals(a.getIsSubsistence())) {
            // Subsistence: ₱50 deducted per (absent + leave) day
            double amount = partTime ? base / 2 : base;
            double dailyRate = partTime ? 25.0 : SUBSISTENCE_DAILY_RATE;
            double countAbsLeave = attendance.absentDays + attendance.leaveCount;
            if (countAbsLeave >= STANDARD_DAYS_PER_MONTH) {
                // When absent almost entire period, pay only for days present
                return Math.max(0, dailyRate * attendance.workDaysPresent);
            }
            return Math.max(0, amount - (dailyRate * countAbsLeave));

        } else if (Boolean.TRUE.equals(a.getIsLaundry())) {
            // Laundry: reduced proportionally per (absent + leave) day
            double amount = partTime ? base / 2 : base;
            double countAbsLeave = attendance.absentDays + attendance.leaveCount;
            double reduced = amount - ((amount / STANDARD_DAYS_PER_MONTH) * countAbsLeave);
            if (countAbsLeave >= STANDARD_DAYS_PER_MONTH) {
                reduced = (amount / STANDARD_DAYS_PER_MONTH) * attendance.workDaysPresent;
            }
            return Math.max(0, reduced);

        } else if (Boolean.TRUE.equals(a.getIsHazardPay())) {
            // Hazard pay: zero if >= 11 absent or >= 11 leave days
            if (attendance.absentDays >= 11 || attendance.leaveCount >= 11) return 0.0;
            double amount;
            if (a.getAmountPerDay() != null && a.getAmountPerDay() > 0) {
                amount = a.getAmountPerDay() * 30;
            } else if (a.getRatePerBasic() != null && a.getRatePerBasic() > 0) {
                amount = basicPerSalary * (a.getRatePerBasic() / 100);
            } else {
                amount = base;
            }
            return partTime ? amount / 2 : amount;

        } else {
            // Fixed allowance
            return partTime ? base / 2 : base;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Step 8 — Mandatory deductions
    // ─────────────────────────────────────────────────────────────────────────

    private double buildMandatoryDeductions(EmployeePayrollInfoDTO emp,
                                             double basicPerSalary,
                                             double taxableIncome,
                                             PayrollDataSnapshot snap,
                                             List<PayrollDetailDeduction> lines,
                                             String salaryType) {
        // GSIS: 9% of basicPerSalary (rounded to nearest centavo)
        double gsisAmount = roundOff(Math.round(basicPerSalary * snap.getGsisPsRate() * 100.0) / 100.0);
        double gsisEr     = roundOff(basicPerSalary * snap.getGsisErRate());
        lines.add(new PayrollDetailDeduction(null, "GSIS", "GSIS", gsisAmount, gsisEr, lines.size()));
        taxableIncome -= gsisAmount;

        // PhilHealth
        double[] philHealth = computePhilHealth(basicPerSalary, snap.getPhilHealthBrackets());
        if (philHealth[0] > 0) {
            lines.add(new PayrollDetailDeduction(null, "PHIC", "PhilHealth", roundOff(philHealth[0]),
                    roundOff(philHealth[1]), lines.size()));
            taxableIncome -= philHealth[0];
        }

        // PagIbig (mandatory + preferred additional)
        double pagibig = snap.getPagibigMandatoryAmount()
                + (emp.getPagibigPreferred() != null ? emp.getPagibigPreferred() : 0.0);
        lines.add(new PayrollDetailDeduction(null, "HDMF", "PagIbig", pagibig, 100.0, lines.size()));
        taxableIncome -= pagibig;

        return taxableIncome;
    }

    /**
     * Returns [employeeShare, employerShare] based on PhilHealth bracket table.
     * Formula: basicPerSalary × rate × 0.01 / 2  (for ranged brackets)
     * or fixed PS/ES for min/max cap brackets.
     */
    private double[] computePhilHealth(double basicPerSalary,
                                        List<PayrollDataSnapshot.PhilHealthBracketDTO> brackets) {
        for (PayrollDataSnapshot.PhilHealthBracketDTO b : brackets) {
            boolean inRange = (b.getSalaryFrom() == null || basicPerSalary >= b.getSalaryFrom())
                    && (b.getIsAndUp() != null && b.getIsAndUp()
                        || (b.getSalaryTo() != null && basicPerSalary <= b.getSalaryTo()));

            if (!inRange) continue;

            if (Boolean.TRUE.equals(b.getIsAndUp()) || b.getSalaryFrom() == null || b.getSalaryTo() == null) {
                // Fixed cap bracket
                double ps = b.getPsFixed() != null ? b.getPsFixed() : 0;
                double es = b.getEsFixed() != null ? b.getEsFixed() : 0;
                return new double[]{ps, es};
            } else {
                // Ranged bracket: rate-based
                double ps = roundOff(basicPerSalary * b.getRate() * 0.010) / 2;
                double es = roundOff((roundOff(basicPerSalary * b.getRate()) * 0.010) / 2);
                return new double[]{ps, es};
            }
        }
        return new double[]{0, 0};
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Step 9 — Withholding tax
    // ─────────────────────────────────────────────────────────────────────────

    private double computeWithholdingTax(double taxableIncome,
                                          String salaryType,
                                          PayrollDataSnapshot snap) {
        if (taxableIncome <= 0) return 0;
        List<PayrollDataSnapshot.WHoldingTaxBracketDTO> brackets =
                snap.getTaxBrackets().getOrDefault(salaryType, Collections.emptyList());

        for (PayrollDataSnapshot.WHoldingTaxBracketDTO b : brackets) {
            boolean inRange = taxableIncome >= b.getIncomeFrom()
                    && (b.getIncomeTo() == null || taxableIncome <= b.getIncomeTo());
            if (inRange) {
                double tax = (b.getBaseTax() != null ? b.getBaseTax() : 0)
                        + ((taxableIncome - b.getIncomeFrom()) * (b.getExcessRate() != null ? b.getExcessRate() : 0));
                return roundOff(tax);
            }
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Step 11 — Leave balances
    // ─────────────────────────────────────────────────────────────────────────

    private LeaveBalanceResult computeLeaveBalances(EmployeePayrollInfoDTO emp,
                                                     AttendanceSummary attendance,
                                                     double earnedLeave,
                                                     double vlDeductedDays,
                                                     PayrollDataSnapshot snap) {
        LeaveBalanceResult r = new LeaveBalanceResult();

        // Starting VL balance
        double prevVl = snap.getVlBalanceMap().getOrDefault(emp.getEmployeeNo(), -1.0);
        if (prevVl < 0) {
            PayrollDataSnapshot.PreviousPeriodBalanceDTO prev =
                    snap.getPreviousBalanceMap().get(emp.getEmployeeNo());
            prevVl = (prev != null) ? Math.max(prev.getVlBalance(), 0) : 0.0;
        }

        double prevSl = snap.getSlBalanceMap().getOrDefault(emp.getEmployeeNo(), 0.0);

        // VL balance: previous + earned - VL taken (leave used) - VL charged for late/undertime
        r.vlBalance = Math.max(prevVl + earnedLeave - attendance.vlUsed - vlDeductedDays, 0);
        // SL balance: previous + earned (same earn rate) - SL taken
        r.slBalance = Math.max(prevSl + earnedLeave - attendance.slUsed, 0);
        return r;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Step 12 — Assemble PayrollDetail entity
    // ─────────────────────────────────────────────────────────────────────────

    private PayrollDetail buildPayrollDetail(EmployeePayrollInfoDTO emp,
                                              PayrollComputationRequest req,
                                              AttendanceSummary attendance,
                                              LateUndertimeResult lateResult,
                                              double earnedLeave,
                                              double absentDeduction,
                                              double actualBasic,
                                              double basicPerSalary,
                                              double salaryPerDay,
                                              double salaryPerMinute,
                                              int cutoffDays,
                                              double grossAmount,
                                              double totalDeduction,
                                              double netAmount,
                                              double taxableIncome,
                                              double wTax,
                                              LeaveBalanceResult leaveBalance,
                                              List<PayrollDetailEarning> earningLines,
                                              List<PayrollDetailDeduction> deductionLines) {

        PayrollDetail pd = new PayrollDetail();

        // Identity
        pd.setEmployeeNo(emp.getEmployeeNo());
        pd.setEmployeeName(emp.getFullName());
        pd.setDepartment(emp.getDepartment());
        pd.setSalaryGrade(emp.getSalaryGrade() != null ? emp.getSalaryGrade() : 0);
        pd.setSalaryStep(emp.getSalaryStep() != null ? emp.getSalaryStep() : 0);

        // Period
        pd.setSalaryPeriodKey(req.getSalaryPeriodKey());
        pd.setCutoffStartDate(req.getCutoffStartDate());
        pd.setCutoffEndDate(req.getCutoffEndDate());
        pd.setSalaryDate(req.getSalaryDate());

        // Rates
        pd.setBasicPerSalary(basicPerSalary);
        pd.setSalaryPerDay(salaryPerDay);
        pd.setSalaryPerMinute(salaryPerMinute);
        pd.setCutoffDays(cutoffDays);

        // Attendance
        pd.setWorkDays(attendance.workDays);
        pd.setWorkDaysPresent(attendance.workDaysPresent);
        pd.setAbsentDays(attendance.absentDays);
        pd.setAbsentParticulars(attendance.absentParticulars);

        // Late / undertime
        pd.setLateMinutes(attendance.lateMinutes);
        pd.setLateValue(lateResult.lateValue);
        pd.setUndertimeMinutes(attendance.undertimeMinutes);
        pd.setUndertimeValue(lateResult.undertimeValue);
        pd.setVlDeductedDays(lateResult.vlDeductedDays);

        // Leave
        pd.setEarnedLeave(earnedLeave);
        pd.setVacationLeaveUsed(attendance.vlUsed);
        pd.setSickLeaveUsed(attendance.slUsed);
        pd.setForceLeaveUsed(attendance.clUsed);
        pd.setVlBalance(leaveBalance.vlBalance);
        pd.setSlBalance(leaveBalance.slBalance);

        // Amounts
        pd.setActualBasic(actualBasic);
        pd.setGrossAmount(grossAmount);
        pd.setTotalDeduction(roundOff(totalDeduction));
        pd.setNetAmount(netAmount);

        // Tax
        pd.setTaxableIncome(taxableIncome);
        pd.setTaxAmount(wTax);

        // Status
        pd.setStatus(PayrollStatus.COMPUTED);
        pd.setIsLocked(false);
        pd.setComputedAt(LocalDateTime.now());

        // Link child records to this PayrollDetail
        for (PayrollDetailEarning e : earningLines) {
            e.setPayrollDetail(pd);
        }
        for (PayrollDetailDeduction d : deductionLines) {
            d.setPayrollDetail(pd);
        }
        pd.setEarnings(earningLines);
        pd.setDeductions(deductionLines);

        return pd;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Utility helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Rounds to 2 decimal places (standard currency rounding). */
    static double roundOff(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static int safeInt(Integer v) {
        return v != null ? v : 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Internal value objects (package-private for testability)
    // ─────────────────────────────────────────────────────────────────────────

    static class AttendanceSummary {
        int workDays        = 0;
        double workDaysPresent  = 0;
        double absentDays   = 0;
        int awolDays        = 0;
        int lateMinutes     = 0;
        int undertimeMinutes = 0;
        int leaveCount      = 0;
        double vlUsed       = 0;
        double slUsed       = 0;
        double clUsed       = 0;
        double rlUsed       = 0;
        double halfDayWithoutPay = 0;
        String absentParticulars = "";
    }

    static class LateUndertimeResult {
        double lateValue      = 0.0;
        double undertimeValue = 0.0;
        double vlDeductedDays = 0.0;
    }

    static class LeaveBalanceResult {
        double vlBalance = 0.0;
        double slBalance = 0.0;
    }

    /**
     * Thrown when computation for a single employee fails.
     * Allows the batch service to continue with remaining employees
     * and record the failure without stopping the entire batch.
     */
    public static class PayrollComputationException extends RuntimeException {
        private final String employeeNo;

        public PayrollComputationException(String employeeNo, Throwable cause) {
            super("Payroll computation failed for employee: " + employeeNo, cause);
            this.employeeNo = employeeNo;
        }

        public String getEmployeeNo() { return employeeNo; }
    }
}
