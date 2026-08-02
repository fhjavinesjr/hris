package com.administrative.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payroll configuration constants — the institutional parameters that define
 * how salary is computed at ZCMC (or any configured organisation).
 *
 * Replaces the incorrectly-named PremiumMultiplier entity.
 * These constants are fetched by the Payroll batch service on every run.
 */
@Entity
@Table(name = "payroll_settings")
public class PayrollSettings implements Serializable {

    public static final BigDecimal DEFAULT_REGULAR_DAY_MULTIPLIER = new BigDecimal("1.0000");
    public static final BigDecimal DEFAULT_REGULAR_OVERTIME_MULTIPLIER = new BigDecimal("1.2500");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payrollSettingsId")
    private Long payrollSettingsId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "effectivityDate")
    private LocalDateTime effectivityDate;

    /**
     * Number of working days per salary period used as the salary rate divisor.
     * Standard for ZCMC and most Philippine government agencies: 22.
     * Formula: salaryPerDay = basicPerSalary / cutoffDays
     */
    @Column(name = "cutoffDays")
    private Integer cutoffDays;

    /**
     * Divisor used to prorate PERA per absent day.
     * Formula: peraDeduction = (peraAmount / peraProrationDivisor) × absentDays
     * Standard: 22 (same as cutoffDays but kept separate to allow independent change).
     */
    @Column(name = "peraProrationDivisor")
    private Integer peraProrationDivisor;

    /**
     * Global flag to enable/disable automatic hazard pay computation in payroll.
     * When true, hazard pay is computed using rates from hazard_pay table.
     * When false, hazard pay is only included if manually entered as an allowance.
     */
    @Column(name = "autoComputeHazardPay")
    private Boolean autoComputeHazardPay;

    /** Base multiplier applied to regular-day hours in premium-pay computations. */
    @Column(name = "regularDayMultiplier", precision = 10, scale = 4)
    private BigDecimal regularDayMultiplier;

    /** Multiplier applied to regular-day overtime hours in premium-pay computations. */
    @Column(name = "regularOvertimeMultiplier", precision = 10, scale = 4)
    private BigDecimal regularOvertimeMultiplier;

    public PayrollSettings() {}

    public PayrollSettings(Long payrollSettingsId, LocalDateTime effectivityDate,
                           Integer cutoffDays, Integer peraProrationDivisor,
                           Boolean autoComputeHazardPay) {
        this(payrollSettingsId, effectivityDate, cutoffDays, peraProrationDivisor,
                autoComputeHazardPay, DEFAULT_REGULAR_DAY_MULTIPLIER,
                DEFAULT_REGULAR_OVERTIME_MULTIPLIER);
    }

    public PayrollSettings(Long payrollSettingsId, LocalDateTime effectivityDate,
                           Integer cutoffDays, Integer peraProrationDivisor,
                           Boolean autoComputeHazardPay,
                           BigDecimal regularDayMultiplier,
                           BigDecimal regularOvertimeMultiplier) {
        this.payrollSettingsId = payrollSettingsId;
        this.effectivityDate = effectivityDate;
        this.cutoffDays = cutoffDays;
        this.peraProrationDivisor = peraProrationDivisor;
        this.autoComputeHazardPay = autoComputeHazardPay;
        this.regularDayMultiplier = regularDayMultiplier;
        this.regularOvertimeMultiplier = regularOvertimeMultiplier;
    }

    public Long getPayrollSettingsId() { return payrollSettingsId; }
    public void setPayrollSettingsId(Long payrollSettingsId) { this.payrollSettingsId = payrollSettingsId; }

    public LocalDateTime getEffectivityDate() { return effectivityDate; }
    public void setEffectivityDate(LocalDateTime effectivityDate) { this.effectivityDate = effectivityDate; }

    public Integer getCutoffDays() { return cutoffDays; }
    public void setCutoffDays(Integer cutoffDays) { this.cutoffDays = cutoffDays; }

    public Integer getPeraProrationDivisor() { return peraProrationDivisor; }
    public void setPeraProrationDivisor(Integer peraProrationDivisor) { this.peraProrationDivisor = peraProrationDivisor; }

    public Boolean getAutoComputeHazardPay() { return autoComputeHazardPay; }
    public void setAutoComputeHazardPay(Boolean autoComputeHazardPay) { this.autoComputeHazardPay = autoComputeHazardPay; }

    public BigDecimal getRegularDayMultiplier() { return regularDayMultiplier; }
    public void setRegularDayMultiplier(BigDecimal regularDayMultiplier) { this.regularDayMultiplier = regularDayMultiplier; }

    public BigDecimal getRegularOvertimeMultiplier() { return regularOvertimeMultiplier; }
    public void setRegularOvertimeMultiplier(BigDecimal regularOvertimeMultiplier) { this.regularOvertimeMultiplier = regularOvertimeMultiplier; }
}
