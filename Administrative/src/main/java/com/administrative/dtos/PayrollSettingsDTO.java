package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayrollSettingsDTO implements Serializable {

    private Long payrollSettingsId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime effectivityDate;

    private Integer cutoffDays;
    private Integer peraProrationDivisor;
    private Boolean autoComputeHazardPay;
    @DecimalMin(value = "0.0", inclusive = false, message = "Regular day multiplier must be greater than zero")
    private BigDecimal regularDayMultiplier;

    @DecimalMin(value = "0.0", inclusive = false, message = "Regular overtime multiplier must be greater than zero")
    private BigDecimal regularOvertimeMultiplier;

    public PayrollSettingsDTO() {}

    public PayrollSettingsDTO(Long payrollSettingsId, LocalDateTime effectivityDate,
                              Integer cutoffDays, Integer peraProrationDivisor,
                              Boolean autoComputeHazardPay) {
        this(payrollSettingsId, effectivityDate, cutoffDays, peraProrationDivisor,
                autoComputeHazardPay, null, null);
    }

    public PayrollSettingsDTO(Long payrollSettingsId, LocalDateTime effectivityDate,
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
