package com.payroll.dtos;

/**
 * One line within a {@link PayrollAdjustmentDTO}.
 *
 * Amount sign convention:
 *   EARNING  + positive → extra pay added
 *   EARNING  + negative → pay clawback / correction
 *   DEDUCTION + positive → extra deduction (net decreases)
 *   DEDUCTION + negative → deduction reversal (net increases)
 */
public class PayrollAdjustmentLineDTO {

    private Long id;
    private String type;           // EARNING | DEDUCTION
    private String code;           // e.g. BACK_PAY, GSIS, PHIC, WTX
    private String name;           // display name
    private Double amount;
    private Boolean isTaxable;     // only meaningful for EARNING type
    private Boolean isAutoComputed; // true = system-generated cascade line

    public PayrollAdjustmentLineDTO() {}

    public PayrollAdjustmentLineDTO(String type, String code, String name,
                                    Double amount, Boolean isTaxable, Boolean isAutoComputed) {
        this.type = type;
        this.code = code;
        this.name = name;
        this.amount = amount;
        this.isTaxable = isTaxable;
        this.isAutoComputed = isAutoComputed;
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Boolean getIsTaxable() { return isTaxable; }
    public void setIsTaxable(Boolean isTaxable) { this.isTaxable = isTaxable; }
    public Boolean getIsAutoComputed() { return isAutoComputed; }
    public void setIsAutoComputed(Boolean isAutoComputed) { this.isAutoComputed = isAutoComputed; }
}
