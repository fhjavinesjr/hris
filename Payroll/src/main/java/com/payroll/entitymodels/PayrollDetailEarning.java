package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Single earning line item belonging to a {@link PayrollDetail}.
 * e.g. "Basic Salary ₱12,000", "PERA ₱2,000", "Hazard Pay ₱1,200"
 */
@Entity
@Table(name = "payroll_detail_earning",
       indexes = @Index(name = "idx_pde_payroll_detail_id", columnList = "payroll_detail_id"))
public class PayrollDetailEarning implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_detail_id", nullable = false)
    private PayrollDetail payrollDetail;

    @Column(nullable = false, length = 150)
    private String earningName;     // display name, e.g. "PERA", "Subsistence"

    @Column(nullable = false, length = 50)
    private String earningCode;     // short code, e.g. "PERA", "SUBSIST", "BASIC"

    @Column(nullable = false)
    private Double amount = 0.0;

    /** Whether this earning is included in the taxable income computation. */
    private Boolean isTaxable = true;

    /** Sort order for payslip display. */
    private Integer indexNo = 0;

    public PayrollDetailEarning() {}

    public PayrollDetailEarning(PayrollDetail pd, String code, String name, Double amount, Boolean isTaxable, int index) {
        this.payrollDetail = pd;
        this.earningCode = code;
        this.earningName = name;
        this.amount = amount;
        this.isTaxable = isTaxable;
        this.indexNo = index;
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PayrollDetail getPayrollDetail() { return payrollDetail; }
    public void setPayrollDetail(PayrollDetail payrollDetail) { this.payrollDetail = payrollDetail; }
    public String getEarningName() { return earningName; }
    public void setEarningName(String earningName) { this.earningName = earningName; }
    public String getEarningCode() { return earningCode; }
    public void setEarningCode(String earningCode) { this.earningCode = earningCode; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Boolean getIsTaxable() { return isTaxable; }
    public void setIsTaxable(Boolean isTaxable) { this.isTaxable = isTaxable; }
    public Integer getIndexNo() { return indexNo; }
    public void setIndexNo(Integer indexNo) { this.indexNo = indexNo; }
}
