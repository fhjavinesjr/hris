package com.payroll.entitymodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "tax_remittance_1601c_audit",
        indexes = @Index(name = "idx_1601c_audit_period", columnList = "taxYear,taxMonth,performedAt"))
public class TaxRemittance1601CAudit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Integer taxYear;
    @Column(nullable = false) private Integer taxMonth;
    @Column(nullable = false, length = 20) private String action;
    @Column(length = 2000) private String previousValues;
    @Column(nullable = false, length = 2000) private String newValues;
    @Column(nullable = false, length = 100) private String performedBy;
    @Column(nullable = false) private LocalDateTime performedAt;

    public Long getId() { return id; } public void setId(Long value) { id = value; }
    public Integer getTaxYear() { return taxYear; } public void setTaxYear(Integer value) { taxYear = value; }
    public Integer getTaxMonth() { return taxMonth; } public void setTaxMonth(Integer value) { taxMonth = value; }
    public String getAction() { return action; } public void setAction(String value) { action = value; }
    public String getPreviousValues() { return previousValues; } public void setPreviousValues(String value) { previousValues = value; }
    public String getNewValues() { return newValues; } public void setNewValues(String value) { newValues = value; }
    public String getPerformedBy() { return performedBy; } public void setPerformedBy(String value) { performedBy = value; }
    public LocalDateTime getPerformedAt() { return performedAt; } public void setPerformedAt(LocalDateTime value) { performedAt = value; }
}
