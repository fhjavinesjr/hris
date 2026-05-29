package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * One line item in a payroll adjustment — can be a manual entry or a system-computed cascade.
 *
 * Amount sign convention:
 *   Positive (+) = adds money (extra earning, reduced deduction)
 *   Negative (−) = removes money (correction earning, extra deduction)
 *
 * For EARNING lines:  amount > 0 means additional pay, amount < 0 means correction/clawback.
 * For DEDUCTION lines: amount > 0 means extra deduction, amount < 0 means deduction reversal.
 */
@Entity
@Table(name = "payroll_adjustment_line",
       indexes = @Index(name = "idx_pal_header_id", columnList = "header_id"))
public class PayrollAdjustmentLine implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "header_id", nullable = false)
    private PayrollAdjustmentHeader header;

    /**
     * EARNING  — an addition or correction to earnings (basic, back pay, allowance, etc.)
     * DEDUCTION — a correction/addition to deductions (GSIS, PHIC, WTX, loan, etc.)
     */
    @Column(nullable = false, length = 20)
    private String type;   // EARNING | DEDUCTION

    /** Short code matching the corresponding earning/deduction code. e.g. BACK_PAY, GSIS, WTX */
    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;   // display name, e.g. "Back Pay", "GSIS (cascade)", "WTX Adjustment"

    /**
     * Delta amount. Sign convention described in class Javadoc.
     * For DEDUCTION lines the stored amount represents the employee-side impact on net pay
     * (positive = net decreases, negative = net increases).
     */
    @Column(nullable = false)
    private Double amount = 0.0;

    /**
     * Applicable only for EARNING type.
     * true  → this earning is taxable; cascade engine will adjust GSIS, PHIC, WTX.
     * false → non-taxable earning; no cascade computed.
     * null  → not applicable (DEDUCTION type).
     */
    private Boolean isTaxable;

    /**
     * true  → line was auto-computed by the cascade engine (GSIS/PHIC/WTX deltas).
     * false → line was manually entered by the officer.
     */
    @Column(nullable = false)
    private Boolean isAutoComputed = false;

    /** Display order on the payslip/adjustment view. */
    @Column(nullable = false)
    private Integer indexNo = 0;

    public PayrollAdjustmentLine() {}

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PayrollAdjustmentHeader getHeader() { return header; }
    public void setHeader(PayrollAdjustmentHeader header) { this.header = header; }
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
    public Integer getIndexNo() { return indexNo; }
    public void setIndexNo(Integer indexNo) { this.indexNo = indexNo; }
}
