package com.payroll.entitymodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "bir_1604c_officer_review_audit")
public class Bir1604COfficerReviewAudit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Integer taxYear;
    @Column(nullable = false, length = 20) private String action;
    @Column(length = 2000) private String previousValues;
    @Column(nullable = false, length = 2000) private String newValues;
    @Column(nullable = false, length = 100) private String performedBy;
    @Column(nullable = false) private LocalDateTime performedAt;

    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public Integer getTaxYear() { return taxYear; } public void setTaxYear(Integer v) { taxYear = v; }
    public String getAction() { return action; } public void setAction(String v) { action = v; }
    public String getPreviousValues() { return previousValues; } public void setPreviousValues(String v) { previousValues = v; }
    public String getNewValues() { return newValues; } public void setNewValues(String v) { newValues = v; }
    public String getPerformedBy() { return performedBy; } public void setPerformedBy(String v) { performedBy = v; }
    public LocalDateTime getPerformedAt() { return performedAt; } public void setPerformedAt(LocalDateTime v) { performedAt = v; }
}
