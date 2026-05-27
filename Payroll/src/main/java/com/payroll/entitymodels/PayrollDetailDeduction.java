package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Single deduction line item belonging to a {@link PayrollDetail}.
 * e.g. "GSIS ₱1,080", "PhilHealth ₱450", "PagIbig ₱100", "Withholding Tax ₱500"
 */
@Entity
@Table(name = "payroll_detail_deduction",
       indexes = @Index(name = "idx_pdd_payroll_detail_id", columnList = "payroll_detail_id"))
public class PayrollDetailDeduction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_detail_id", nullable = false)
    private PayrollDetail payrollDetail;

    @Column(nullable = false, length = 150)
    private String deductionName;   // display name, e.g. "GSIS", "PhilHealth"

    @Column(nullable = false, length = 50)
    private String deductionCode;   // short code, e.g. "GSIS", "PHIC", "HDMF", "WTX", "LOAN"

    @Column(nullable = false)
    private Double amount = 0.0;

    /** Employer counterpart share, stored for reporting purposes. */
    private Double employerShare = 0.0;

    /** For loans/payables: total amount to pay. */
    private Double loanTotalAmount;

    /** For loans/payables: number of payments already made. */
    private Integer loanPaymentsMade;

    /** Reference/voucher number for loans or special deductions. */
    @Column(length = 100)
    private String reference;

    /** Whether the deduction amount is fixed per salary (manually set by admin). */
    private Boolean isFixedPerSalary = false;

    /** Sort order for payslip display. */
    private Integer indexNo = 0;

    public PayrollDetailDeduction() {}

    public PayrollDetailDeduction(PayrollDetail pd, String code, String name, Double amount, Double employerShare, int index) {
        this.payrollDetail = pd;
        this.deductionCode = code;
        this.deductionName = name;
        this.amount = amount;
        this.employerShare = employerShare;
        this.indexNo = index;
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PayrollDetail getPayrollDetail() { return payrollDetail; }
    public void setPayrollDetail(PayrollDetail payrollDetail) { this.payrollDetail = payrollDetail; }
    public String getDeductionName() { return deductionName; }
    public void setDeductionName(String deductionName) { this.deductionName = deductionName; }
    public String getDeductionCode() { return deductionCode; }
    public void setDeductionCode(String deductionCode) { this.deductionCode = deductionCode; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Double getEmployerShare() { return employerShare; }
    public void setEmployerShare(Double employerShare) { this.employerShare = employerShare; }
    public Double getLoanTotalAmount() { return loanTotalAmount; }
    public void setLoanTotalAmount(Double loanTotalAmount) { this.loanTotalAmount = loanTotalAmount; }
    public Integer getLoanPaymentsMade() { return loanPaymentsMade; }
    public void setLoanPaymentsMade(Integer loanPaymentsMade) { this.loanPaymentsMade = loanPaymentsMade; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Boolean getIsFixedPerSalary() { return isFixedPerSalary; }
    public void setIsFixedPerSalary(Boolean isFixedPerSalary) { this.isFixedPerSalary = isFixedPerSalary; }
    public Integer getIndexNo() { return indexNo; }
    public void setIndexNo(Integer indexNo) { this.indexNo = indexNo; }
}
