package com.payroll.dtos;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of ALL data needed to compute payroll for every employee
 * in a single salary period.
 *
 * ─── Performance contract ───────────────────────────────────────────────────
 *  This object is built ONCE by {@link com.payroll.impl.PayrollBatchServiceImpl}
 *  before any employee is processed.  Every field is a pre-indexed Map so that
 *  per-employee access is O(1) – no further database queries are made during
 *  the parallel computation phase.
 *
 *  Old system:  each employee's computation made 20–50 individual DB queries.
 *  New system:  the entire batch makes ~10 bulk queries total, regardless of
 *               how many employees are in the batch.
 * ────────────────────────────────────────────────────────────────────────────
 */
public class PayrollDataSnapshot {

    // ── Employee catalogue ────────────────────────────────────────────────────
    /** key: employeeNo  →  payroll-relevant employee information */
    private Map<String, EmployeePayrollInfoDTO> employeeMap = Collections.emptyMap();

    // ── Attendance (from TimeKeeping service) ─────────────────────────────────
    /**
     * key: employeeNo  →  list of daily DTR records within the cutoff window.
     * Pre-indexed so the engine never calls the TimeKeeping service per employee.
     */
    private Map<String, List<DtrDailySummaryDTO>> dtrMap = Collections.emptyMap();

    // ── Leave (from HumanResource service) ───────────────────────────────────
    /**
     * key: employeeNo  →  all APPROVED leaves whose date falls in the cutoff window.
     */
    private Map<String, List<ApprovedLeaveDTO>> leavesMap = Collections.emptyMap();

    /**
     * key: employeeNo  →  Vacation Leave balance as of start of this period.
     * Used for the CSC rule: late/undertime charged to VL first, then salary.
     */
    private Map<String, Double> vlBalanceMap = Collections.emptyMap();

    /**
     * key: employeeNo  →  Sick Leave balance as of start of this period.
     */
    private Map<String, Double> slBalanceMap = Collections.emptyMap();

    // ── Calendar (from Administrative service) ────────────────────────────────
    /**
     * key: date  →  holiday details.
     * Loaded once for the entire cutoff window; shared across all employee threads.
     */
    private Map<LocalDate, HolidayDTO> holidayMap = Collections.emptyMap();

    // ── Allowances (from Payroll DB – earning_allowance table) ────────────────
    /**
     * key: employeeNo  →  list of applicable allowances for this period.
     * Includes PERA, Subsistence, Laundry, Hazard Pay, and any custom items.
     */
    private Map<String, List<AllowanceDTO>> allowancesMap = Collections.emptyMap();

    // ── Previous payroll details ──────────────────────────────────────────────
    /**
     * key: employeeNo  →  leave balances carried from previous payroll period.
     * Used to initialise VL/SL balance when vlBalanceMap has no explicit entry.
     */
    private Map<String, PreviousPeriodBalanceDTO> previousBalanceMap = Collections.emptyMap();

    // ── Statutory contribution rates (shared, immutable, loaded once) ─────────
    /** Semi-monthly PagIbig mandatory share (employee side). Typically ₱100. */
    private Double pagibigMandatoryAmount = 100.0;

    /** GSIS employee personal share rate (currently 9%). */
    private Double gsisPsRate = 0.09;

    /** GSIS employer share rate (currently 12%). */
    private Double gsisErRate = 0.12;

    /**
     * PhilHealth brackets: sorted list used to look up the employee's
     * contribution based on their basic monthly salary.
     */
    private List<PhilHealthBracketDTO> philHealthBrackets = Collections.emptyList();

    /**
     * Withholding tax table sorted by income bracket.
     * key: salaryType ("SEMI_MONTHLY" | "MONTHLY")  →  sorted bracket list
     */
    private Map<String, List<WHoldingTaxBracketDTO>> taxBrackets = Collections.emptyMap();

    /**
     * key: salaryGrade  →  hazard pay percentage of basic salary.
     * Used for DOH employees when no fixed hazard amount is in allowances.
     */
    private Map<Integer, Double> hazardPayRateByGrade = Collections.emptyMap();

    // ═════════════════════════════════════════════════════════════════════════
    //  Inner helper DTOs (kept here to minimise file count)
    // ═════════════════════════════════════════════════════════════════════════

    public static class PreviousPeriodBalanceDTO {
        private Double vlBalance = 0.0;
        private Double slBalance = 0.0;
        private Double vlDeductedDays = 0.0;  // days charged to leave in that period
        private Double earnedLeave = 0.0;

        public Double getVlBalance() { return vlBalance; }
        public void setVlBalance(Double vlBalance) { this.vlBalance = vlBalance; }
        public Double getSlBalance() { return slBalance; }
        public void setSlBalance(Double slBalance) { this.slBalance = slBalance; }
        public Double getVlDeductedDays() { return vlDeductedDays; }
        public void setVlDeductedDays(Double vlDeductedDays) { this.vlDeductedDays = vlDeductedDays; }
        public Double getEarnedLeave() { return earnedLeave; }
        public void setEarnedLeave(Double earnedLeave) { this.earnedLeave = earnedLeave; }
    }

    public static class PhilHealthBracketDTO {
        private Double salaryFrom;   // null = no lower bound
        private Double salaryTo;     // null = no upper bound (isAndUp)
        private Boolean isAndUp = false;
        private Double rate = 0.0;   // e.g. 0.05 for 5%
        private Double psFixed;      // fixed personal share (used when isAndUp=true or range edges)
        private Double esFixed;      // fixed employer share

        public Double getSalaryFrom() { return salaryFrom; }
        public void setSalaryFrom(Double salaryFrom) { this.salaryFrom = salaryFrom; }
        public Double getSalaryTo() { return salaryTo; }
        public void setSalaryTo(Double salaryTo) { this.salaryTo = salaryTo; }
        public Boolean getIsAndUp() { return isAndUp; }
        public void setIsAndUp(Boolean isAndUp) { this.isAndUp = isAndUp; }
        public Double getRate() { return rate; }
        public void setRate(Double rate) { this.rate = rate; }
        public Double getPsFixed() { return psFixed; }
        public void setPsFixed(Double psFixed) { this.psFixed = psFixed; }
        public Double getEsFixed() { return esFixed; }
        public void setEsFixed(Double esFixed) { this.esFixed = esFixed; }
    }

    public static class WHoldingTaxBracketDTO {
        private Double incomeFrom;
        private Double incomeTo;     // null = and above
        private Double baseTax;      // fixed tax amount at the lower bound
        private Double excessRate;   // rate applied to income above incomeFrom

        public Double getIncomeFrom() { return incomeFrom; }
        public void setIncomeFrom(Double incomeFrom) { this.incomeFrom = incomeFrom; }
        public Double getIncomeTo() { return incomeTo; }
        public void setIncomeTo(Double incomeTo) { this.incomeTo = incomeTo; }
        public Double getBaseTax() { return baseTax; }
        public void setBaseTax(Double baseTax) { this.baseTax = baseTax; }
        public Double getExcessRate() { return excessRate; }
        public void setExcessRate(Double excessRate) { this.excessRate = excessRate; }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public Map<String, EmployeePayrollInfoDTO> getEmployeeMap() { return employeeMap; }
    public void setEmployeeMap(Map<String, EmployeePayrollInfoDTO> employeeMap) { this.employeeMap = employeeMap; }
    public Map<String, List<DtrDailySummaryDTO>> getDtrMap() { return dtrMap; }
    public void setDtrMap(Map<String, List<DtrDailySummaryDTO>> dtrMap) { this.dtrMap = dtrMap; }
    public Map<String, List<ApprovedLeaveDTO>> getLeavesMap() { return leavesMap; }
    public void setLeavesMap(Map<String, List<ApprovedLeaveDTO>> leavesMap) { this.leavesMap = leavesMap; }
    public Map<String, Double> getVlBalanceMap() { return vlBalanceMap; }
    public void setVlBalanceMap(Map<String, Double> vlBalanceMap) { this.vlBalanceMap = vlBalanceMap; }
    public Map<String, Double> getSlBalanceMap() { return slBalanceMap; }
    public void setSlBalanceMap(Map<String, Double> slBalanceMap) { this.slBalanceMap = slBalanceMap; }
    public Map<LocalDate, HolidayDTO> getHolidayMap() { return holidayMap; }
    public void setHolidayMap(Map<LocalDate, HolidayDTO> holidayMap) { this.holidayMap = holidayMap; }
    public Map<String, List<AllowanceDTO>> getAllowancesMap() { return allowancesMap; }
    public void setAllowancesMap(Map<String, List<AllowanceDTO>> allowancesMap) { this.allowancesMap = allowancesMap; }
    public Map<String, PreviousPeriodBalanceDTO> getPreviousBalanceMap() { return previousBalanceMap; }
    public void setPreviousBalanceMap(Map<String, PreviousPeriodBalanceDTO> previousBalanceMap) { this.previousBalanceMap = previousBalanceMap; }
    public Double getPagibigMandatoryAmount() { return pagibigMandatoryAmount; }
    public void setPagibigMandatoryAmount(Double pagibigMandatoryAmount) { this.pagibigMandatoryAmount = pagibigMandatoryAmount; }
    public Double getGsisPsRate() { return gsisPsRate; }
    public void setGsisPsRate(Double gsisPsRate) { this.gsisPsRate = gsisPsRate; }
    public Double getGsisErRate() { return gsisErRate; }
    public void setGsisErRate(Double gsisErRate) { this.gsisErRate = gsisErRate; }
    public List<PhilHealthBracketDTO> getPhilHealthBrackets() { return philHealthBrackets; }
    public void setPhilHealthBrackets(List<PhilHealthBracketDTO> philHealthBrackets) { this.philHealthBrackets = philHealthBrackets; }
    public Map<String, List<WHoldingTaxBracketDTO>> getTaxBrackets() { return taxBrackets; }
    public void setTaxBrackets(Map<String, List<WHoldingTaxBracketDTO>> taxBrackets) { this.taxBrackets = taxBrackets; }
    public Map<Integer, Double> getHazardPayRateByGrade() { return hazardPayRateByGrade; }
    public void setHazardPayRateByGrade(Map<Integer, Double> hazardPayRateByGrade) { this.hazardPayRateByGrade = hazardPayRateByGrade; }
}
