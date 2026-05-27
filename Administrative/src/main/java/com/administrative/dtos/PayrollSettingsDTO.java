package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PayrollSettingsDTO implements Serializable {

    private Long payrollSettingsId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime effectivityDate;

    private Integer cutoffDays;
    private Integer peraProrationDivisor;
    private Boolean autoComputeHazardPay;

    public PayrollSettingsDTO() {}

    public PayrollSettingsDTO(Long payrollSettingsId, LocalDateTime effectivityDate,
                              Integer cutoffDays, Integer peraProrationDivisor,
                              Boolean autoComputeHazardPay) {
        this.payrollSettingsId = payrollSettingsId;
        this.effectivityDate = effectivityDate;
        this.cutoffDays = cutoffDays;
        this.peraProrationDivisor = peraProrationDivisor;
        this.autoComputeHazardPay = autoComputeHazardPay;
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
}
