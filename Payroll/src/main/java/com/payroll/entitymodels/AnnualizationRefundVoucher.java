package com.payroll.entitymodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "annualization_refund_voucher",
        indexes = @Index(name = "idx_annual_refund_snapshot", columnList = "snapshotId"))
public class AnnualizationRefundVoucher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long snapshotId;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 100) private String voucherReference;
    @Column(nullable = false) private LocalDate paidDate;
    @Column(nullable = false, length = 100) private String recordedBy;
    @Column(nullable = false) private LocalDateTime recordedAt;
    private LocalDateTime voidedAt;
    @Column(length = 100) private String voidedBy;
    @Column(length = 500) private String voidReason;

    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public Long getSnapshotId() { return snapshotId; } public void setSnapshotId(Long v) { snapshotId = v; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal v) { amount = v; }
    public String getVoucherReference() { return voucherReference; } public void setVoucherReference(String v) { voucherReference = v; }
    public LocalDate getPaidDate() { return paidDate; } public void setPaidDate(LocalDate v) { paidDate = v; }
    public String getRecordedBy() { return recordedBy; } public void setRecordedBy(String v) { recordedBy = v; }
    public LocalDateTime getRecordedAt() { return recordedAt; } public void setRecordedAt(LocalDateTime v) { recordedAt = v; }
    public LocalDateTime getVoidedAt() { return voidedAt; } public void setVoidedAt(LocalDateTime v) { voidedAt = v; }
    public String getVoidedBy() { return voidedBy; } public void setVoidedBy(String v) { voidedBy = v; }
    public String getVoidReason() { return voidReason; } public void setVoidReason(String v) { voidReason = v; }
}
