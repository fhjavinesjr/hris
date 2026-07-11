package com.payroll.dtos;

public class PayslipLineDTO {
    private String source; // PAYROLL_DETAIL | ADJUSTMENT
    private String type;   // EARNING | DEDUCTION
    private String code;
    private String name;
    private Double amount;
    private Boolean taxable;
    private Boolean autoComputed;
    private Integer indexNo;

    public PayslipLineDTO() {}

    public PayslipLineDTO(String source, String type, String code, String name, Double amount,
                          Boolean taxable, Boolean autoComputed, Integer indexNo) {
        this.source = source;
        this.type = type;
        this.code = code;
        this.name = name;
        this.amount = amount;
        this.taxable = taxable;
        this.autoComputed = autoComputed;
        this.indexNo = indexNo;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Boolean getTaxable() { return taxable; }
    public void setTaxable(Boolean taxable) { this.taxable = taxable; }

    public Boolean getAutoComputed() { return autoComputed; }
    public void setAutoComputed(Boolean autoComputed) { this.autoComputed = autoComputed; }

    public Integer getIndexNo() { return indexNo; }
    public void setIndexNo(Integer indexNo) { this.indexNo = indexNo; }
}
