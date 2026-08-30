package com.primehr.rsp.domain;

import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "rsp_recruitment_plan", uniqueConstraints =
        @UniqueConstraint(name = "uk_rsp_plan_code", columnNames = {"agency_id", "code"}))
public class RecruitmentPlan extends RspAuditedEntity {
    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecruitmentPlanStatus status;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    protected RecruitmentPlan() {
    }

    public RecruitmentPlan(String agencyId, String code, String title, LocalDate periodStart,
                           LocalDate periodEnd, String description) {
        super(agencyId);
        this.code = text(code, "code").toUpperCase(Locale.ROOT);
        this.title = text(title, "title");
        applyPeriod(periodStart, periodEnd, description);
        this.status = RecruitmentPlanStatus.DRAFT;
    }

    public void update(String title, LocalDate periodStart, LocalDate periodEnd, String description) {
        requireEditable();
        this.title = text(title, "title");
        applyPeriod(periodStart, periodEnd, description);
    }

    public void submit(String actor, Instant at) {
        requireEditable();
        submittedBy = text(actor, "submitter");
        submittedAt = Objects.requireNonNull(at, "submittedAt");
        approvedBy = null;
        approvedAt = null;
        status = RecruitmentPlanStatus.SUBMITTED;
    }

    public void returnSubmission() {
        requireStatus(RecruitmentPlanStatus.SUBMITTED);
        status = RecruitmentPlanStatus.RETURNED;
        approvedBy = null;
        approvedAt = null;
    }

    public void approve(String actor, Instant at) {
        requireStatus(RecruitmentPlanStatus.SUBMITTED);
        approvedBy = text(actor, "approver");
        approvedAt = Objects.requireNonNull(at, "approvedAt");
        status = RecruitmentPlanStatus.APPROVED;
    }

    public void archive() {
        if (status != RecruitmentPlanStatus.DRAFT
                && status != RecruitmentPlanStatus.RETURNED
                && status != RecruitmentPlanStatus.APPROVED) {
            throw new IllegalLifecycleTransitionException(
                    "Only DRAFT, RETURNED, or APPROVED recruitment plans can be archived");
        }
        status = RecruitmentPlanStatus.ARCHIVED;
    }

    public void requireEditable() {
        if (status != RecruitmentPlanStatus.DRAFT && status != RecruitmentPlanStatus.RETURNED) {
            throw new IllegalLifecycleTransitionException(
                    "Only DRAFT or RETURNED recruitment plans can be changed");
        }
    }

    public boolean isApproved() {
        return status == RecruitmentPlanStatus.APPROVED;
    }

    private void applyPeriod(LocalDate start, LocalDate end, String description) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("planning period is required");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("periodEnd cannot be before periodStart");
        }
        this.periodStart = start;
        this.periodEnd = end;
        this.description = optional(description);
    }

    private void requireStatus(RecruitmentPlanStatus expected) {
        if (status != expected) {
            throw new IllegalLifecycleTransitionException(
                    "Only " + expected + " recruitment plans may perform this action");
        }
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public String getDescription() { return description; }
    public RecruitmentPlanStatus getStatus() { return status; }
    public String getSubmittedBy() { return submittedBy; }
    public Instant getSubmittedAt() { return submittedAt; }
    public String getApprovedBy() { return approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
}
