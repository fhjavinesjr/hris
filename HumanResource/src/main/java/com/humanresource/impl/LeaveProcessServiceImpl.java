package com.humanresource.impl;

import com.humanresource.dtos.LeaveInformationDTO;
import com.humanresource.dtos.LeaveProcessRequestDTO;
import com.humanresource.dtos.LeaveProcessResultDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.entitymodels.LeaveBeginningBalance;
import com.humanresource.entitymodels.LeaveInformation;
import com.humanresource.entitymodels.CompensatoryTimeOff;
import com.humanresource.entitymodels.OfficialEngagementApplication;
import com.humanresource.entitymodels.PassSlip;
import com.humanresource.entitymodels.TimeCorrection;
import com.humanresource.entitymodels.Separation;
import com.humanresource.repositories.EmployeeAppointmentRepository;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.LeaveApplicationRepository;
import com.humanresource.repositories.LeaveBeginningBalanceRepository;
import com.humanresource.repositories.LeaveInformationRepository;
import com.humanresource.repositories.CompensatoryTimeOffRepository;
import com.humanresource.repositories.OfficialEngagementApplicationRepository;
import com.humanresource.repositories.PassSlipRepository;
import com.humanresource.repositories.TimeCorrectionRepository;
import com.humanresource.repositories.SeparationRepository;
import com.humanresource.services.LeaveProcessService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Leave Process Computation Engine
 *
 * Computes one LeaveInformation row per employee per salary period.
 *
 * Algorithm (faithful port of old HRIS LeaveInformationAction.generate()):
 *   1.  Guard checks: skip Contractual, separated, no beginning balance, locked period
 *   2.  Resolve previous balance (from last LeaveInformation or from LeaveBeginningBalance)
 *   3.  Day-by-day loop through cutoffStartDate..cutoffEndDate:
 *         - Skip weekends and holidays
 *         - If DTR record exists → accrue late + undertime minutes
 *         - If no DTR → check approved leaves for that day
 *           (SL used, VL used, LWOP-SL, LWOP-VL, or absent)
 *   4.  Look up EarningLeave table for VL earn rate (defaults to 1.25 if not found)
 *         SL always earns 1.25 regardless of absences (CSC rule)
 *   5.  Compute Day Equivalent (late+UT total minutes → fractional VL deduction)
 *   6.  Compute new balances:
 *         newSL = prevSL + earnedSL - lwopSL - slUsed
 *           (unexcused absences do NOT deduct SL — CSC standard)
 *         newVL = prevVL + earnedVL - absentCount - lwopVL - vlUsed - dayEquivFraction
 *   7.  Save LeaveInformation row; leave isLocked = false
 *
 * Cross-module data (same hrisof database) is accessed via JdbcTemplate.
 */
@Service
public class LeaveProcessServiceImpl implements LeaveProcessService {

    private static final Logger log = LoggerFactory.getLogger(LeaveProcessServiceImpl.class);

    // CSC standard earning rate per semi-monthly period
    private static final double DEFAULT_EARN_RATE = 1.25;

    // Nature-of-appointment codes considered non-permanent / non-earning
    private static final Set<String> NON_EARNING_NATURES = Set.of(
            "CONTRACTUAL", "CONTRACT OF SERVICE", "COS", "JO", "JOB ORDER"
    );

    private final EmployeeRepository employeeRepository;
    private final EmployeeAppointmentRepository appointmentRepository;
    private final SeparationRepository separationRepository;
    private final LeaveBeginningBalanceRepository begBalanceRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveInformationRepository leaveInfoRepository;
    private final PassSlipRepository passSlipRepository;
    private final CompensatoryTimeOffRepository ctoRepository;
    private final OfficialEngagementApplicationRepository oeRepository;
    private final TimeCorrectionRepository tcRepository;
    private final JdbcTemplate jdbc;

    public LeaveProcessServiceImpl(
            EmployeeRepository employeeRepository,
            EmployeeAppointmentRepository appointmentRepository,
            SeparationRepository separationRepository,
            LeaveBeginningBalanceRepository begBalanceRepository,
            LeaveApplicationRepository leaveApplicationRepository,
            LeaveInformationRepository leaveInfoRepository,
            PassSlipRepository passSlipRepository,
            CompensatoryTimeOffRepository ctoRepository,
            OfficialEngagementApplicationRepository oeRepository,
            TimeCorrectionRepository tcRepository,
            JdbcTemplate jdbc) {
        this.employeeRepository = employeeRepository;
        this.appointmentRepository = appointmentRepository;
        this.separationRepository = separationRepository;
        this.begBalanceRepository = begBalanceRepository;
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveInfoRepository = leaveInfoRepository;
        this.passSlipRepository = passSlipRepository;
        this.ctoRepository = ctoRepository;
        this.oeRepository = oeRepository;
        this.tcRepository = tcRepository;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public LeaveProcessResultDTO process(LeaveProcessRequestDTO req) throws Exception {
        List<String> skippedReasons = new ArrayList<>();
        List<LeaveInformationDTO> processedList = new ArrayList<>();

        LocalDate periodStart = req.getCutoffStartDate();
        LocalDate periodEnd = req.getCutoffEndDate();

        // Build the working set of employees
        List<Employee> employees;
        if ("EMPLOYEE".equalsIgnoreCase(req.getScope()) && req.getEmployeeId() != null) {
            employees = employeeRepository.findById(req.getEmployeeId())
                    .map(List::of).orElse(List.of());
        } else {
            employees = employeeRepository.findAll();
        }

        // Pre-load holiday dates for the period (JdbcTemplate cross-module query)
        Set<LocalDate> holidayDates = loadHolidayDates(periodStart, periodEnd);

        for (Employee emp : employees) {
            String empLabel = emp.getEmployeeNo() + " - " + emp.getLastname();
            try {
                LeaveInformationDTO result = processEmployee(
                        emp, periodStart, periodEnd,
                        req.getSalaryPeriodSettingId(), req.getProcessedById(),
                        holidayDates, skippedReasons, empLabel);
                if (result != null) {
                    processedList.add(result);
                }
            } catch (Exception ex) {
                log.error("Error processing employee {}: ", empLabel, ex);
                skippedReasons.add(empLabel + ": Unexpected error — " + ex.getMessage());
            }
        }

        return new LeaveProcessResultDTO(
                processedList.size(),
                skippedReasons.size(),
                skippedReasons,
                processedList);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Per-employee processing
    // ─────────────────────────────────────────────────────────────────────────

    private LeaveInformationDTO processEmployee(
            Employee emp,
            LocalDate periodStart, LocalDate periodEnd,
            Long salaryPeriodSettingId, Long processedById,
            Set<LocalDate> holidayDates,
            List<String> skippedReasons, String empLabel) {

        Long employeeId = emp.getEmployeeId();

        // Guard 1: Must have an active appointment
        EmployeeAppointment appt = appointmentRepository
                .findTop1ByEmployeeIdOrderByAssumptionToDutyDateDesc(employeeId);
        if (appt == null) {
            skippedReasons.add(empLabel + ": No active appointment found");
            return null;
        }

        // Guard 2: Skip contractual/COS employees — look up nature via JdbcTemplate
        String nature = loadNatureOfAppointment(appt.getNatureOfAppointmentId());
        if (nature != null && NON_EARNING_NATURES.contains(nature.toUpperCase())) {
            // Contractual employees do not earn VL/SL
            skippedReasons.add(empLabel + ": Contractual/COS — not eligible for leave earnings");
            return null;
        }

        // Guard 3: Employee must not be separated before the period ends
        List<Separation> separations = separationRepository.findByEmployeeId(employeeId);
        if (!separations.isEmpty()) {
            boolean separatedBeforePeriod = separations.stream()
                    .anyMatch(s -> s.getSeparationDate() != null &&
                              s.getSeparationDate().toLocalDate().isBefore(periodEnd));
            if (separatedBeforePeriod) {
                skippedReasons.add(empLabel + ": Employee is separated");
                return null;
            }
        }

        // Guard 4: Must have a SL beginning balance record
        Optional<LeaveBeginningBalance> slBegOpt =
                begBalanceRepository.findByEmployeeIdAndLeaveType(employeeId, "Sick Leave");
        Optional<LeaveBeginningBalance> vlBegOpt =
                begBalanceRepository.findByEmployeeIdAndLeaveType(employeeId, "Vacation Leave");
        if (slBegOpt.isEmpty() || vlBegOpt.isEmpty()) {
            skippedReasons.add(empLabel + ": No Leave Beginning Balance (SL/VL required)");
            return null;
        }

        LeaveBeginningBalance slBeg = slBegOpt.get();
        LeaveBeginningBalance vlBeg = vlBegOpt.get();

        // Guard 5: Beginning balance asOfDate must not be after the period end
        if (slBeg.getAsOfDate() != null && slBeg.getAsOfDate().isAfter(periodEnd)) {
            skippedReasons.add(empLabel + ": SL beginning balance date is after period end");
            return null;
        }

        // Guard 6: Period must not already be locked
        Optional<LeaveInformation> existingOpt = leaveInfoRepository
                .findByEmployeeIdAndCutoffStartDateAndCutoffEndDate(employeeId, periodStart, periodEnd);
        if (existingOpt.isPresent() && Boolean.TRUE.equals(existingOpt.get().getIsLocked())) {
            skippedReasons.add(empLabel + ": Period is locked — cannot re-process");
            return null;
        }

        // Guard 7: Cannot process a period if a later period already exists (no backfilling over future)
        if (leaveInfoRepository.existsByEmployeeIdAndCutoffEndDateGreaterThan(employeeId, periodEnd)) {
            skippedReasons.add(empLabel + ": A later period already exists — cannot backfill");
            return null;
        }

        // ── Resolve previous balances ────────────────────────────────────────
        double prevSL;
        double prevVL;

        // Look up the PREVIOUS period's balance, strictly before this period's end date.
        // Using cutoffEndDate < periodEnd avoids self-referencing the current period's
        // own stale record when reprocessing an already-computed period.
        Optional<LeaveInformation> lastPeriod =
                leaveInfoRepository.findTopByEmployeeIdAndCutoffEndDateBeforeOrderByCutoffEndDateDesc(
                        employeeId, periodEnd);

        if (lastPeriod.isPresent()) {
            prevSL = nvl(lastPeriod.get().getSickLeaveBalance());
            prevVL = nvl(lastPeriod.get().getVacationLeaveBalance());
        } else {
            // First ever period — use beginning balances
            prevSL = nvl(slBeg.getBalance());
            prevVL = nvl(vlBeg.getBalance());
        }

        // ── Pre-load approved leave applications for this employee ───────────
        List<LeaveApplication> approvedLeaves = leaveApplicationRepository
                .findByEmployeeId(employeeId).stream()
                .filter(la -> "Approved".equalsIgnoreCase(la.getApprovedStatus()))
                .collect(Collectors.toList());

        // ── Pre-load approved pass slips for this employee ───────────────────
        // Approved pass slips represent official-business absences; minutes covered
        // by a pass slip must not be charged to VL via the day equivalent deduction.
        List<PassSlip> approvedPassSlips = passSlipRepository
                .findByEmployeeIdAndPassSlipDateBetween(employeeId, periodStart, periodEnd)
                .stream()
                .filter(ps -> "Approved".equalsIgnoreCase(ps.getStatus()))
                .collect(Collectors.toList());

        // ── Pre-load approved CTOs (Compensatory Time Off) for this employee ──
        // An approved CTO means the employee uses accrued overtime credit to take a
        // day (or partial day) off. It prevents an absent mark; if hoursUsed < 8,
        // the shortfall is counted as undertime (same logic as old HRIS PayOff).
        List<CompensatoryTimeOff> approvedCtos = ctoRepository
                .findByEmployeeIdAndStatus(employeeId, "Approved")
                .stream()
                .filter(c -> !c.getDateOfOffset().isBefore(periodStart)
                          && !c.getDateOfOffset().isAfter(periodEnd))
                .collect(Collectors.toList());

        // ── Pre-load approved Official Engagement Applications for this employee ──
        // An approved OE (Official Business or Official Time) means the employee was
        // on authorized official work. Full-day OEs prevent an absent mark; partial-day
        // OEs offset late/undertime minutes in the same way as pass slips.
        List<OfficialEngagementApplication> approvedOEs = oeRepository
                .findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employeeId, periodEnd, periodStart)
                .stream()
                .filter(oe -> "Approved".equalsIgnoreCase(oe.getStatus()))
                .collect(Collectors.toList());

        // ── Pre-load approved Time Corrections for this employee ──────────────
        // An approved Time Correction means the employee's recorded time-in/out is
        // corrected. For absent days with a TC, the corrected times are used to
        // compute late and undertime instead of marking absent. For present days,
        // the corrected times override the DTR late/undertime.
        List<TimeCorrection> approvedTCs = tcRepository
                .findByEmployeeIdAndWorkDateBetween(employeeId, periodStart, periodEnd)
                .stream()
                .filter(tc -> "Approved".equalsIgnoreCase(tc.getStatus()))
                .collect(Collectors.toList());

        // ── Day-by-day computation loop ───────────────────────────────────────
        int totalLateMinutes = 0;
        int totalUndertimeMinutes = 0;
        int lateCount = 0;
        int undertimeCount = 0;
        double absentCount = 0.0;
        double slUsed = 0.0;
        double vlUsed = 0.0;
        double lwopSL = 0.0;
        double lwopVL = 0.0;
        StringBuilder particulars = new StringBuilder();

        // dtr_daily.employee_id stores the HR employee's numeric PK as a String
        // (the time-keeping frontend queries DTR using selectedEmployee.employeeId,
        //  which is the Long database ID — NOT the biometric device number)
        String dtrEmployeeId = String.valueOf(emp.getEmployeeId());

        LocalDate day = periodStart;
        while (!day.isAfter(periodEnd)) {
            DayOfWeek dow = day.getDayOfWeek();

            // Skip weekends
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                day = day.plusDays(1);
                continue;
            }

            // Skip holidays
            if (holidayDates.contains(day)) {
                day = day.plusDays(1);
                continue;
            }

            // Load employee's scheduled shift for this working day — used for schedule-aware
            // late/undertime computation in TC, OE offset, and any break-aware calculations.
            ScheduledShift scheduledShift = loadScheduledShiftForDay(dtrEmployeeId, day);

            // Check DTR record for this working day
            Map<String, Object> dtrRow = loadDtrDaily(dtrEmployeeId, day);

            if (dtrRow != null) {
                // Employee was present — accrue late and undertime
                int late = toInt(dtrRow.get("total_late_minutes"));
                int ut = toInt(dtrRow.get("total_undertime_minutes"));

                // Apply Time Correction override first (employee corrected their punch).
                // If an approved TC exists for this day, recompute late/UT from the
                // corrected times rather than the raw DTR values.
                TimeCorrection tc = findTCForDay(day, approvedTCs);
                if (tc != null) {
                    int[] corrected = computeTimeCorrectionLateUt(tc, scheduledShift);
                    late = corrected[0];
                    ut = corrected[1];
                    log.debug("TimeCorrection on {}: corrected late={}min, ut={}min", day, late, ut);
                }

                // Subtract approved pass slip minutes so official-business time is not
                // charged to VL. Offset is applied to late first, remainder to undertime.
                int passSlipOffset = computePassSlipOffset(day, approvedPassSlips);

                // Subtract approved Official Engagement minutes (same pattern as PassSlip).
                int oeOffset = computeOEOffset(day, approvedOEs, scheduledShift);
                int totalOffset = passSlipOffset + oeOffset;

                int netLate = Math.max(0, late - totalOffset);
                int remainingOffset = Math.max(0, totalOffset - late);
                int netUt = Math.max(0, ut - remainingOffset);

                if (totalOffset > 0) {
                    log.debug("Offset on {}: raw late={}, raw ut={}, passSlip={}, oe={} → netLate={}, netUt={}",
                            day, late, ut, passSlipOffset, oeOffset, netLate, netUt);
                }

                if (netLate > 0) lateCount++;
                if (netUt > 0) undertimeCount++;
                totalLateMinutes += netLate;
                totalUndertimeMinutes += netUt;
            } else {
                // No DTR record — check Time Correction first (employee corrected a missed punch).
                TimeCorrection tc = findTCForDay(day, approvedTCs);
                if (tc != null) {
                    int[] corrected = computeTimeCorrectionLateUt(tc, scheduledShift);
                    int tcLate = corrected[0];
                    int tcUt = corrected[1];
                    if (tcLate > 0) { lateCount++; totalLateMinutes += tcLate; }
                    if (tcUt > 0) { undertimeCount++; totalUndertimeMinutes += tcUt; }
                    appendParticular(particulars, day, "[TC]");
                    log.debug("TimeCorrection absent→present on {}: late={}min, ut={}min", day, tcLate, tcUt);
                } else {
                    // No DTR, no TC — check CTO first (mirrors old HRIS PayOff logic),
                    // then Official Engagement, then approved leaves, then mark absent.
                    CompensatoryTimeOff cto = findCtoForDay(day, approvedCtos);
                    if (cto != null) {
                        // CTO covers this day — employee is not absent.
                        int ctoUt = computeCtoUndertimeMinutes(cto);
                        if (ctoUt > 0) {
                            totalUndertimeMinutes += ctoUt;
                            undertimeCount++;
                            log.debug("CTO partial day on {}: hoursUsed={}, undertime added={} min",
                                    day, cto.getHoursUsed(), ctoUt);
                        }
                        appendParticular(particulars, day, "[CTO]");
                    } else if (isOEFullDay(day, approvedOEs, scheduledShift)) {
                        // Official Engagement covers the full work day — not absent.
                        appendParticular(particulars, day, "[OE]");
                    } else if (hasPassSlipForDay(day, approvedPassSlips)) {
                        // Approved Pass Slip covers this day — employee is not absent.
                        // Pass slips represent excused absences from the office (official or personal).
                        // On a no-DTR day, the pass slip is the only record of the employee's activity;
                        // we do not penalise them with an absent mark.
                        appendParticular(particulars, day, "[PS]");
                    } else {
                        // No CTO, no OE, no PassSlip — check approved leave applications
                        String leaveTag = resolveAbsenceType(day, approvedLeaves);
                        switch (leaveTag) {
                        case "SL":
                            slUsed += 1.0;
                            appendParticular(particulars, day, "[SL]");
                            break;
                        case "VL":
                            vlUsed += 1.0;
                            appendParticular(particulars, day, "[VL]");
                            break;
                        case "HALF_SL":
                            slUsed += 0.5;
                            appendParticular(particulars, day, "[SL-HALF]");
                            break;
                        case "HALF_VL":
                            vlUsed += 0.5;
                            appendParticular(particulars, day, "[VL-HALF]");
                            break;
                        case "LWOP_SL":
                            lwopSL += 1.0;
                            appendParticular(particulars, day, "[LWOP-SL]");
                            break;
                        case "LWOP_VL":
                            lwopVL += 1.0;
                            appendParticular(particulars, day, "[LWOP-VL]");
                            break;
                        case "CTO":
                            // CTO filed as a leave application (fallback) — no deduction
                            appendParticular(particulars, day, "[CTO]");
                            break;
                        default:
                            // ABSENT — no leave, no CTO, no excuse
                            absentCount += 1.0;
                            appendParticular(particulars, day, "[A]");
                            break;
                    }
                    } // end else (no OE)
                } // end else (no TC)
            } // end else (no DTR)

            day = day.plusDays(1);
        }

        // ── Compute earned leave rates ────────────────────────────────────────
        // SL: always 1.25 per period (CSC rule), absences do not reduce SL earning
        double earnedSL = DEFAULT_EARN_RATE;

        // VL: look up from EarningLeave table by absent days; falls back to 1.25
        double earnedVL = lookupVlEarnRate((int) Math.round(absentCount), periodEnd);

        // ── Compute Day Equivalent (late+UT deduction from VL) ───────────────
        int totalLateUt = totalLateMinutes + totalUndertimeMinutes;
        double dayEquivFraction = computeDayEquivalent(totalLateUt, periodEnd);

        // ── Compute new balances ─────────────────────────────────────────────
        //   newSL = prevSL + earnedSL - lwopSL - slUsed
        //     (unexcused absences are charged to VL, not SL — CSC standard)
        //   newVL = prevVL + earnedVL - absentCount - lwopVL - vlUsed - dayEquivFraction
        double newSL = prevSL + earnedSL - lwopSL - slUsed;
        double newVL = prevVL + earnedVL - absentCount - lwopVL - vlUsed - dayEquivFraction;

        // ── Build and save LeaveInformation row ──────────────────────────────
        LeaveInformation entity;
        if (existingOpt.isPresent()) {
            entity = existingOpt.get();
        } else {
            entity = new LeaveInformation();
            entity.setCreatedAt(LocalDateTime.now());
        }

        entity.setEmployeeId(employeeId);
        entity.setSalaryPeriodSettingId(salaryPeriodSettingId);
        entity.setCutoffStartDate(periodStart);
        entity.setCutoffEndDate(periodEnd);
        entity.setProcessDate(LocalDateTime.now());
        entity.setProcessedById(processedById);

        entity.setEarnedSl(round3(earnedSL));
        entity.setEarnedVl(round3(earnedVL));
        entity.setSickLeaveUsed(round3(slUsed));
        entity.setVacationLeaveUsed(round3(vlUsed));
        entity.setLeaveWithoutPaySl(round3(lwopSL));
        entity.setLeaveWithoutPayVl(round3(lwopVL));
        entity.setPreviousSickLeaveBalance(prevSL);
        entity.setPreviousVacationLeaveBalance(prevVL);
        entity.setSickLeaveBalance(round3(newSL));
        entity.setVacationLeaveBalance(round3(newVL));
        entity.setLateUndertimeMinutes(totalLateUt);
        entity.setLateUndertimeEquivalent(round3(dayEquivFraction));
        entity.setLateCount(lateCount);
        entity.setUndertimeCount(undertimeCount);
        entity.setAbsentCount(round3(absentCount));
        entity.setLeaveParticulars(particulars.toString());
        entity.setIsBegBalance(false);
        entity.setIsLocked(false);
        entity.setUpdatedAt(LocalDateTime.now());

        entity = leaveInfoRepository.save(entity);

        // Build response DTO with enriched employee info
        LeaveInformationDTO dto = toDTO(entity);
        dto.setEmployeeName(emp.getLastname() + ", " + emp.getFirstname());
        dto.setEmployeeNo(emp.getEmployeeNo());
        return dto;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Cross-module JdbcTemplate queries (same hrisof DB)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Loads all active holiday dates in the given period from the Administrative module's holiday table.
     */
    private Set<LocalDate> loadHolidayDates(LocalDate from, LocalDate to) {
        String sql = "SELECT holidayDate, observedDate FROM holiday " +
                     "WHERE isActive = 1 AND (" +
                     "  (holidayDate >= ? AND holidayDate <= ?) OR " +
                     "  (observedDate >= ? AND observedDate <= ?))";
        List<Map<String, Object>> rows = jdbc.queryForList(sql,
                from, to, from, to);

        return rows.stream()
                .flatMap(row -> {
                    List<LocalDate> dates = new ArrayList<>();
                    Object hd = row.get("holidayDate");
                    Object od = row.get("observedDate");
                    if (hd instanceof java.sql.Date) dates.add(((java.sql.Date) hd).toLocalDate());
                    if (od instanceof java.sql.Date) dates.add(((java.sql.Date) od).toLocalDate());
                    return dates.stream();
                })
                .collect(Collectors.toSet());
    }

    /**
     * Loads a single DTR daily row for an employee on a given date.
     * dtr_daily.employee_id stores the HR Employee's Long primary key as a String
     * (set by the time-keeping frontend using selectedEmployee.employeeId).
     */
    private Map<String, Object> loadDtrDaily(String employeeId, LocalDate date) {
        if (employeeId == null) return null;
        String sql = "SELECT total_late_minutes, total_undertime_minutes, attendance_status " +
                     "FROM dtr_daily WHERE employee_id = ? AND work_date = ?";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, employeeId, date);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Looks up the nature of appointment description by ID.
     */
    private String loadNatureOfAppointment(Integer natureOfAppointmentId) {
        if (natureOfAppointmentId == null) return null;
        String sql = "SELECT nature FROM natureofappointment WHERE natureofappointmentId = ?";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, natureOfAppointmentId);
        if (rows.isEmpty()) return null;
        Object nature = rows.get(0).get("nature");
        return nature != null ? nature.toString() : null;
    }

    /**
     * Looks up VL earning rate from the earningleave table.
     * The 'day' column stores the number of absent days; 'earn' is the rate.
     * Returns DEFAULT_EARN_RATE (1.25) if no matching record is found.
     */
    private double lookupVlEarnRate(int absentDays, LocalDate cutoffEndDate) {
        try {
            String sql = "SELECT TOP 1 earn FROM earningleave " +
                         "WHERE day = ? AND effectivityDate <= ? " +
                         "ORDER BY effectivityDate DESC";
            List<Map<String, Object>> rows = jdbc.queryForList(sql,
                    String.valueOf(absentDays), cutoffEndDate.atStartOfDay());
            if (!rows.isEmpty() && rows.get(0).get("earn") != null) {
                return Double.parseDouble(rows.get(0).get("earn").toString());
            }
        } catch (Exception ex) {
            log.warn("EarningLeave lookup failed for absentDays={}, using default 1.25: {}", absentDays, ex.getMessage());
        }
        return DEFAULT_EARN_RATE;
    }

    /**
     * Converts total late+undertime minutes into a fractional day deduction using
     * the Administrative module's DayEquivalentHours and DayEquivalentMinutes tables.
     *
     * Example: 70 total minutes → 1 hour + 10 minutes remaining
     *   hourEquiv  = DayEquivalentHours[hours=1].hoursEquivalent
     *   minEquiv   = DayEquivalentMinutes[minutes=10].minutesEquivalent
     *   result     = hourEquiv + minEquiv
     */
    private double computeDayEquivalent(int totalMinutes, LocalDate cutoffEndDate) {
        if (totalMinutes <= 0) return 0.0;
        try {
            int hours = totalMinutes / 60;
            int remainingMinutes = totalMinutes % 60;

            double hourEquiv = 0.0;
            double minEquiv = 0.0;

            if (hours > 0) {
                // Primary: find the rate effective on or before the cutoff date
                String sqlH = "SELECT TOP 1 hoursEquivalent FROM dayequivalenthours " +
                              "WHERE hours = ? AND effectivityDate <= ? " +
                              "ORDER BY effectivityDate DESC";
                List<Map<String, Object>> hRows = jdbc.queryForList(sqlH,
                        String.valueOf(hours), cutoffEndDate.atStartOfDay());

                // Fallback: no rate was effective yet — use the earliest rate in the table
                if (hRows.isEmpty() || hRows.get(0).get("hoursEquivalent") == null) {
                    String sqlHFallback = "SELECT TOP 1 hoursEquivalent FROM dayequivalenthours " +
                                         "WHERE hours = ? ORDER BY effectivityDate ASC";
                    hRows = jdbc.queryForList(sqlHFallback, String.valueOf(hours));
                    if (!hRows.isEmpty()) {
                        log.debug("DayEquivalentHours: using earliest available rate for hours={} (no rate effective on {})",
                                hours, cutoffEndDate);
                    }
                }

                if (!hRows.isEmpty() && hRows.get(0).get("hoursEquivalent") != null) {
                    hourEquiv = Double.parseDouble(hRows.get(0).get("hoursEquivalent").toString());
                } else {
                    log.warn("DayEquivalentHours: no entry at all for hours='{}'. Table may be missing this value.", hours);
                }
            }

            if (remainingMinutes > 0) {
                // Primary: find the rate effective on or before the cutoff date
                String sqlM = "SELECT TOP 1 minutesEquivalent FROM dayequivalentminutes " +
                              "WHERE minutes = ? AND effectivityDate <= ? " +
                              "ORDER BY effectivityDate DESC";
                List<Map<String, Object>> mRows = jdbc.queryForList(sqlM,
                        String.valueOf(remainingMinutes), cutoffEndDate.atStartOfDay());

                // Fallback: no rate was effective yet — use the earliest rate in the table
                if (mRows.isEmpty() || mRows.get(0).get("minutesEquivalent") == null) {
                    String sqlMFallback = "SELECT TOP 1 minutesEquivalent FROM dayequivalentminutes " +
                                         "WHERE minutes = ? ORDER BY effectivityDate ASC";
                    mRows = jdbc.queryForList(sqlMFallback, String.valueOf(remainingMinutes));
                    if (!mRows.isEmpty()) {
                        log.debug("DayEquivalentMinutes: using earliest available rate for minutes={} (no rate effective on {})",
                                remainingMinutes, cutoffEndDate);
                    }
                }

                if (!mRows.isEmpty() && mRows.get(0).get("minutesEquivalent") != null) {
                    minEquiv = Double.parseDouble(mRows.get(0).get("minutesEquivalent").toString());
                } else {
                    log.warn("DayEquivalentMinutes: no entry at all for minutes='{}'. Table may be missing this value.", remainingMinutes);
                }
            }

            log.debug("DayEquivalent: totalMinutes={} → hours={} (equiv={}) + remainingMin={} (equiv={}) = {}",
                    totalMinutes, hours, hourEquiv, remainingMinutes, minEquiv, hourEquiv + minEquiv);

            return hourEquiv + minEquiv;
        } catch (Exception ex) {
            log.warn("DayEquivalent lookup failed for {} minutes, using 0.0: {}", totalMinutes, ex.getMessage());
            return 0.0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Leave Application matching
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Determines what type of absence covers a given day based on approved leaves.
     * Returns a string tag: SL, VL, HALF_SL, HALF_VL, LWOP_SL, LWOP_VL, CTO, or ABSENT.
     *
     * LeaveType classification:
     *   "Sick Leave"           → SL
     *   "Vacation Leave"       → VL
     *   "LWOP-SL"              → LWOP_SL
     *   "LWOP-VL" / "LWOP"    → LWOP_VL
     *   "Forced Leave"         → VL (charged to VL per CSC)
     *   "CTO" / "Compensatory" → CTO
     *   others (Maternity, Paternity, SPL, etc.) → treated as VL deduction
     */
    private String resolveAbsenceType(LocalDate day, List<LeaveApplication> approvedLeaves) {
        for (LeaveApplication la : approvedLeaves) {
            if (la.getStartDate() == null || la.getEndDate() == null) continue;
            if (!day.isBefore(la.getStartDate()) && !day.isAfter(la.getEndDate())) {
                String type = la.getLeaveType() != null ? la.getLeaveType().toUpperCase() : "";
                double noOfDays = la.getNoOfDays() != null ? la.getNoOfDays() : 1.0;
                boolean isHalf = noOfDays < 1.0;

                if (type.contains("SICK")) return isHalf ? "HALF_SL" : "SL";
                if (type.contains("VACATION") || type.contains("FORCED")) return isHalf ? "HALF_VL" : "VL";
                if (type.contains("LWOP-SL") || (type.contains("LWOP") && type.contains("SL"))) return "LWOP_SL";
                if (type.contains("LWOP")) return "LWOP_VL";
                if (type.contains("CTO") || type.contains("COMPENSATORY")) return "CTO";

                // Special leaves (SPL, Maternity, Paternity, Solo Parent) — not charged to SL/VL balance
                // Treated as present equivalent for SL/VL computation (no deduction)
                return "CTO"; // use CTO tag to mean "approved, no SL/VL deduction"
            }
        }
        return "ABSENT";
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helper utilities
    // ─────────────────────────────────────────────────────────────────────────

    private void appendParticular(StringBuilder sb, LocalDate date, String tag) {
        if (sb.length() > 0) sb.append("|");
        sb.append(date).append(" ").append(tag);
    }

    private double nvl(Double value) {
        return value != null ? value : 0.0;
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private int toInt(Object obj) {
        if (obj == null) return 0;
        return ((Number) obj).intValue();
    }

    /**
     * Finds the approved CTO record whose dateOfOffset matches the given day.
     * Returns null if none exists.
     */
    private CompensatoryTimeOff findCtoForDay(LocalDate day, List<CompensatoryTimeOff> ctos) {
        return ctos.stream()
                .filter(c -> day.equals(c.getDateOfOffset()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Computes undertime minutes for a partial-day CTO.
     *
     * A standard work day is 8 hours (480 minutes). If hoursUsed < 8, the employee
     * did not fully offset the day, and the shortfall counts as undertime — matching
     * the old HRIS PayOff logic: utMin += hoursOfWork - (payOffHour * 60).
     */
    private int computeCtoUndertimeMinutes(CompensatoryTimeOff cto) {
        if (cto.getHoursUsed() == null) return 0;
        double hoursUsed = cto.getHoursUsed();
        if (hoursUsed >= 8.0) return 0;
        return (int) Math.round((8.0 - hoursUsed) * 60);
    }

    /**
     * Computes the total approved pass slip minutes for a given working day.
     *
     * An approved pass slip means the employee was away on official business.
     * That time must not be charged to VL via the day equivalent deduction.
     * The offset is applied first to late minutes, then to undertime minutes.
     * Multiple pass slips on the same day are summed.
     */
    private int computePassSlipOffset(LocalDate day, List<PassSlip> passSlips) {
        return passSlips.stream()
                .filter(ps -> day.equals(ps.getPassSlipDate()))
                .mapToInt(ps -> {
                    if (ps.getDepartureTime() == null || ps.getArrivalTime() == null) return 0;
                    int minutes = (int) Duration.between(ps.getDepartureTime(), ps.getArrivalTime()).toMinutes();
                    return Math.max(0, minutes);
                })
                .sum();
    }

    /**
     * Computes the overlap in minutes between an Official Engagement's time range and
     * the standard working hours (08:00–17:00) for a given day. If the day falls
     * between startDate and endDate (exclusive), the full work day (480 min) is used.
     * Returns 0 if the day is outside the OE range.
     */
    private int computeOEOffset(LocalDate day, List<OfficialEngagementApplication> oes, ScheduledShift sched) {
        java.time.LocalTime workStart = sched != null ? sched.timeIn  : java.time.LocalTime.of(8, 0);
        java.time.LocalTime workEnd   = sched != null ? sched.timeOut : java.time.LocalTime.of(17, 0);
        int total = 0;
        for (OfficialEngagementApplication oe : oes) {
            if (day.isBefore(oe.getStartDate()) || day.isAfter(oe.getEndDate())) continue;
            java.time.LocalTime start = day.equals(oe.getStartDate()) ? oe.getStartTime() : workStart;
            java.time.LocalTime end   = day.equals(oe.getEndDate())   ? oe.getEndTime()   : workEnd;
            // Clamp to working hours
            start = start.isBefore(workStart) ? workStart : start;
            end   = end.isAfter(workEnd)      ? workEnd   : end;
            int minutes = (int) Duration.between(start, end).toMinutes();
            total += Math.max(0, minutes);
        }
        return total;
    }

    /**
     * Returns true if at least one approved OE covers the full work day (08:00–17:00)
     * for the given date. Used to suppress absent marks for no-DTR days.
     */
    private boolean isOEFullDay(LocalDate day, List<OfficialEngagementApplication> oes, ScheduledShift sched) {
        // Treat as full-day if OE covers all scheduled work minutes (fallback: 480 min = 8 hrs)
        int scheduledWorkMinutes = 480;
        if (sched != null) {
            scheduledWorkMinutes = (int) Duration.between(sched.timeIn, sched.timeOut).toMinutes();
            if (sched.breakOut != null && sched.breakIn != null) {
                scheduledWorkMinutes -= (int) Duration.between(sched.breakOut, sched.breakIn).toMinutes();
            }
        }
        return computeOEOffset(day, oes, sched) >= scheduledWorkMinutes;
    }

    /**
     * Returns true if at least one approved pass slip exists for the given date.
     * Used to suppress absent marks for no-DTR days where the employee filed a pass slip.
     * Pass slips are already filtered to Approved status when the list is built.
     */
    private boolean hasPassSlipForDay(LocalDate day, List<PassSlip> passSlips) {
        return passSlips.stream().anyMatch(ps -> day.equals(ps.getPassSlipDate()));
    }

    /**
     * Finds the first approved Time Correction whose workDate matches the given day.
     */
    private TimeCorrection findTCForDay(LocalDate day, List<TimeCorrection> tcs) {
        return tcs.stream()
                .filter(tc -> day.equals(tc.getWorkDate()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Resolves the employee's scheduled shift for a given working day via JdbcTemplate
     * (cross-module query: work_schedule in TimeKeeping, time_shift in Administrative).
     * Returns null if no schedule is found — callers fall back to 08:00/17:00.
     */
    private ScheduledShift loadScheduledShiftForDay(String dtrEmployeeId, LocalDate day) {
        try {
            String sql = "SELECT TOP 1 ts.timeIn, ts.timeOut, ts.breakOut, ts.breakIn " +
                         "FROM work_schedule ws " +
                         "JOIN time_shift ts ON ws.tsCode = ts.tsCode " +
                         "WHERE ws.employeeId = ? AND ws.wsDateTime >= ? AND ws.wsDateTime < ? " +
                         "  AND (ws.isDayOff IS NULL OR ws.isDayOff = 0) " +
                         "ORDER BY ws.wsId ASC";
            List<Map<String, Object>> rows = jdbc.queryForList(sql,
                    dtrEmployeeId, day.atStartOfDay(), day.plusDays(1).atStartOfDay());
            if (rows.isEmpty()) return null;
            Map<String, Object> row = rows.get(0);
            java.time.LocalTime ti = toLocalTimeFromDb(row.get("timeIn"));
            java.time.LocalTime to = toLocalTimeFromDb(row.get("timeOut"));
            if (ti == null || to == null) return null;
            return new ScheduledShift(ti, to,
                    toLocalTimeFromDb(row.get("breakOut")),
                    toLocalTimeFromDb(row.get("breakIn")));
        } catch (Exception ex) {
            log.warn("Could not load scheduled shift for employee {} on {}: {}", dtrEmployeeId, day, ex.getMessage());
            return null;
        }
    }

    private java.time.LocalTime toLocalTimeFromDb(Object dbVal) {
        if (dbVal == null) return null;
        if (dbVal instanceof java.time.LocalTime) return (java.time.LocalTime) dbVal;
        if (dbVal instanceof java.sql.Time) return ((java.sql.Time) dbVal).toLocalTime();
        return null;
    }

    /** Immutable holder for an employee's scheduled shift times on a given day. */
    private static class ScheduledShift {
        final java.time.LocalTime timeIn;
        final java.time.LocalTime timeOut;
        final java.time.LocalTime breakOut; // null when shift has no break
        final java.time.LocalTime breakIn;  // null when shift has no break
        ScheduledShift(java.time.LocalTime timeIn, java.time.LocalTime timeOut,
                       java.time.LocalTime breakOut, java.time.LocalTime breakIn) {
            this.timeIn   = timeIn;
            this.timeOut  = timeOut;
            this.breakOut = breakOut;
            this.breakIn  = breakIn;
        }
    }

    /**
     * Computes late and undertime minutes from a Time Correction's corrected times.
     * Uses the employee's actual scheduled shift (falls back to 08:00/17:00 if unavailable).
     * Business rules:
     *   Late      = (correctedTimeIn − schedIn) + max(0, correctedBreakIn − schedBreakIn)
     *   Undertime = max(0, schedBreakOut − correctedBreakOut) + (schedOut − correctedTimeOut)
     * Returns int[]{lateMinutes, undertimeMinutes}.
     */
    private int[] computeTimeCorrectionLateUt(TimeCorrection tc, ScheduledShift sched) {
        java.time.LocalTime schedIn  = sched != null ? sched.timeIn  : java.time.LocalTime.of(8, 0);
        java.time.LocalTime schedOut = sched != null ? sched.timeOut : java.time.LocalTime.of(17, 0);
        java.time.LocalTime schedBO  = sched != null ? sched.breakOut : null;
        java.time.LocalTime schedBI  = sched != null ? sched.breakIn  : null;
        int late = 0;
        int ut   = 0;
        // Late from clock-in
        if (tc.getCorrectedTimeIn() != null && tc.getCorrectedTimeIn().isAfter(schedIn)) {
            late += (int) Duration.between(schedIn, tc.getCorrectedTimeIn()).toMinutes();
        }
        // Undertime from early clock-out
        if (tc.getCorrectedTimeOut() != null && tc.getCorrectedTimeOut().isBefore(schedOut)) {
            ut += (int) Duration.between(tc.getCorrectedTimeOut(), schedOut).toMinutes();
        }
        // Break-aware: only applied when both corrected AND scheduled break times exist
        if (tc.getCorrectedBreakOut() != null && tc.getCorrectedBreakIn() != null
                && schedBO != null && schedBI != null) {
            // Early break departure → undertime
            if (tc.getCorrectedBreakOut().isBefore(schedBO)) {
                ut += (int) Duration.between(tc.getCorrectedBreakOut(), schedBO).toMinutes();
            }
            // Late break return → late
            if (tc.getCorrectedBreakIn().isAfter(schedBI)) {
                late += (int) Duration.between(schedBI, tc.getCorrectedBreakIn()).toMinutes();
            }
        }
        return new int[]{late, ut};
    }

    private LeaveInformationDTO toDTO(LeaveInformation e) {
        LeaveInformationDTO dto = new LeaveInformationDTO();
        dto.setLeaveInformationId(e.getLeaveInformationId());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setSalaryPeriodSettingId(e.getSalaryPeriodSettingId());
        dto.setCutoffStartDate(e.getCutoffStartDate());
        dto.setCutoffEndDate(e.getCutoffEndDate());
        dto.setProcessDate(e.getProcessDate());
        dto.setProcessedById(e.getProcessedById());
        dto.setEarnedSl(e.getEarnedSl());
        dto.setEarnedVl(e.getEarnedVl());
        dto.setSickLeaveUsed(e.getSickLeaveUsed());
        dto.setVacationLeaveUsed(e.getVacationLeaveUsed());
        dto.setLeaveWithoutPaySl(e.getLeaveWithoutPaySl());
        dto.setLeaveWithoutPayVl(e.getLeaveWithoutPayVl());
        dto.setPreviousSickLeaveBalance(e.getPreviousSickLeaveBalance());
        dto.setPreviousVacationLeaveBalance(e.getPreviousVacationLeaveBalance());
        dto.setSickLeaveBalance(e.getSickLeaveBalance());
        dto.setVacationLeaveBalance(e.getVacationLeaveBalance());
        dto.setLateUndertimeMinutes(e.getLateUndertimeMinutes());
        dto.setLateUndertimeEquivalent(e.getLateUndertimeEquivalent());
        dto.setLateCount(e.getLateCount());
        dto.setUndertimeCount(e.getUndertimeCount());
        dto.setAbsentCount(e.getAbsentCount());
        dto.setLeaveParticulars(e.getLeaveParticulars());
        dto.setIsBegBalance(e.getIsBegBalance());
        dto.setIsLocked(e.getIsLocked());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
