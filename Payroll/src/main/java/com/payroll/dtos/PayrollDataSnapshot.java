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

    /** Leave days earned per cutoff period when the employee had no AWOL (typically 1.25). */
    private Double earnedLeavePerPeriod = 1.25;

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

    /**
     * key: employeeNo  →  hazard pay auto-compute settings for this employee.
     * Replaces the isDoh flag with a flexible, checkbox-based configuration.
     * If an employee has autoCompute=true, their hazard pay will be calculated automatically.
     */
    private Map<String, HazardPaySettingDTO> hazardPaySettingsMap = Collections.emptyMap();

    /**
     * Earned-leave lookup table from the EarningLeave administrative table.
     * key: numberOfAwolDays  →  earnedLeave days credited for that AWOL count.
     * An employee with 0 AWOL days earns {@link #earnedLeavePerPeriod} (typically 1.25).
     */
    private Map<Integer, Double> earnLeaveMap = Collections.emptyMap();

    /**
     * Active installment loans grouped by employee.
     * key: employeeNo  →  list of loans where isStopDeduction=false AND paid < toPay.
     */
    private Map<String, List<LoanDTO>> loansMap = Collections.emptyMap();

    /**
     * One-time or period entry deductions grouped by employee.
     * key: employeeNo  →  list of deductions for the current salary period.
     */
    private Map<String, List<EntryDeductionDTO>> entryDeductionsMap = Collections.emptyMap();

    /**
     * Special/one-time income entries grouped by employee.
     * key: employeeNo  →  list of income entries for the current month/year.
     */
    private Map<String, List<IncomeEntryDTO>> incomeEntriesMap = Collections.emptyMap();

    /**
     * Number of days used to prorate PERA per absent day.
     * Typically 22 (same as cutoffDays). Loaded from PayrollSettings.
     */
    private Integer peraProrationDivisor = 22;

    /**
     * Global flag: whether to auto-compute hazard pay for all employees.
     * Loaded from PayrollSettings.autoComputeHazardPay.
     * When true, hazard pay is calculated as (basicSalary × hazardPayRate) for applicable employees.
     * When false, hazard pay is only included if manually entered in earning_allowance table.
     */
    private Boolean autoComputeHazardPay = false;

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

    public static class LoanDTO {
        private String loanType;
        private String reference;
        private Double amount;
        private Integer toPay;   // total number of installments
        private Integer paid;    // installments already paid

        public String getLoanType() { return loanType; }
        public void setLoanType(String loanType) { this.loanType = loanType; }
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public Integer getToPay() { return toPay; }
        public void setToPay(Integer toPay) { this.toPay = toPay; }
        public Integer getPaid() { return paid; }
        public void setPaid(Integer paid) { this.paid = paid; }
    }

    public static class EntryDeductionDTO {
        private String deductionType;
        private Double amount;
        private String reference;
        private Boolean isFixed;

        public String getDeductionType() { return deductionType; }
        public void setDeductionType(String deductionType) { this.deductionType = deductionType; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
        public Boolean getIsFixed() { return isFixed; }
        public void setIsFixed(Boolean isFixed) { this.isFixed = isFixed; }
    }

    public static class IncomeEntryDTO {
        private String earningType;
        private String earningTypeName;
        private Double amount;
        private Boolean isTaxable;

        public String getEarningType() { return earningType; }
        public void setEarningType(String earningType) { this.earningType = earningType; }
        public String getEarningTypeName() { return earningTypeName; }
        public void setEarningTypeName(String earningTypeName) { this.earningTypeName = earningTypeName; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public Boolean getIsTaxable() { return isTaxable; }
        public void setIsTaxable(Boolean isTaxable) { this.isTaxable = isTaxable; }
    }

    /**
     * Hazard pay auto-compute setting for an employee.
     * Fetched from hazard_pay_settings table via Administrative service.
     */
    public static class HazardPaySettingDTO {
        private Boolean autoCompute = false;
        private Double percentage = 25.00;

        public Boolean getAutoCompute() { return autoCompute; }
        public void setAutoCompute(Boolean autoCompute) { this.autoCompute = autoCompute; }
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
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
    public Double getEarnedLeavePerPeriod() { return earnedLeavePerPeriod; }
    public void setEarnedLeavePerPeriod(Double earnedLeavePerPeriod) { this.earnedLeavePerPeriod = earnedLeavePerPeriod; }
    public List<PhilHealthBracketDTO> getPhilHealthBrackets() { return philHealthBrackets; }
    public void setPhilHealthBrackets(List<PhilHealthBracketDTO> philHealthBrackets) { this.philHealthBrackets = philHealthBrackets; }
    public Map<String, List<WHoldingTaxBracketDTO>> getTaxBrackets() { return taxBrackets; }
    public void setTaxBrackets(Map<String, List<WHoldingTaxBracketDTO>> taxBrackets) { this.taxBrackets = taxBrackets; }
    public Map<Integer, Double> getHazardPayRateByGrade() { return hazardPayRateByGrade; }
    public void setHazardPayRateByGrade(Map<Integer, Double> hazardPayRateByGrade) { this.hazardPayRateByGrade = hazardPayRateByGrade; }
    public Map<String, HazardPaySettingDTO> getHazardPaySettingsMap() { return hazardPaySettingsMap; }
    public void setHazardPaySettingsMap(Map<String, HazardPaySettingDTO> hazardPaySettingsMap) { this.hazardPaySettingsMap = hazardPaySettingsMap; }
    public Map<Integer, Double> getEarnLeaveMap() { return earnLeaveMap; }
    public void setEarnLeaveMap(Map<Integer, Double> earnLeaveMap) { this.earnLeaveMap = earnLeaveMap; }
    public Map<String, List<LoanDTO>> getLoansMap() { return loansMap; }
    public void setLoansMap(Map<String, List<LoanDTO>> loansMap) { this.loansMap = loansMap; }
    public Map<String, List<EntryDeductionDTO>> getEntryDeductionsMap() { return entryDeductionsMap; }
    public void setEntryDeductionsMap(Map<String, List<EntryDeductionDTO>> entryDeductionsMap) { this.entryDeductionsMap = entryDeductionsMap; }
    public Map<String, List<IncomeEntryDTO>> getIncomeEntriesMap() { return incomeEntriesMap; }
    public void setIncomeEntriesMap(Map<String, List<IncomeEntryDTO>> incomeEntriesMap) { this.incomeEntriesMap = incomeEntriesMap; }
    public Integer getPeraProrationDivisor() { return peraProrationDivisor; }
    public void setPeraProrationDivisor(Integer peraProrationDivisor) { this.peraProrationDivisor = peraProrationDivisor; }
    public Boolean getAutoComputeHazardPay() { return autoComputeHazardPay; }
    public void setAutoComputeHazardPay(Boolean autoComputeHazardPay) { this.autoComputeHazardPay = autoComputeHazardPay; }
}
