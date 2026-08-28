package com.primehr.assessment.domain;

import com.primehr.shared.audit.AgencyAuditableEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "prime_assessment_cycle", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_assessment_cycle_code", columnNames = {"agency_id", "code"}))
public class AssessmentCycle extends AgencyAuditableEntity {
    @Column(name = "code", length = 100, nullable = false, updatable = false)
    private String code;
    @Nationalized
    @Column(name = "name", length = 200, nullable = false)
    private String name;
    @Nationalized
    @Column(name = "description", length = 2000)
    private String description;
    @Column(name = "status", length = 30, nullable = false)
    private String status;
    @Column(name = "opened_by", length = 100)
    private String openedBy;
    @Column(name = "opened_at")
    private Instant openedAt;
    @Column(name = "closed_by", length = 100)
    private String closedBy;
    @Column(name = "closed_at")
    private Instant closedAt;

    protected AssessmentCycle() { }

    public AssessmentCycle(String agencyId, String code, String name, String description,
                           LocalDate effectiveFrom, LocalDate effectiveTo) {
        super(agencyId, false, 0, effectiveFrom, effectiveTo);
        this.code = requireText(code, "code").toUpperCase(java.util.Locale.ROOT);
        this.name = requireText(name, "name");
        this.description = normalize(description);
        this.status = AssessmentCycleStatus.DRAFT.name();
    }

    public void updateDraft(String name, String description, LocalDate effectiveFrom, LocalDate effectiveTo) {
        requireDraft();
        this.name = requireText(name, "name");
        this.description = normalize(description);
        updateDefinitionFields(0, effectiveFrom, effectiveTo);
    }

    public void archiveDraft() {
        requireDraft();
        status = AssessmentCycleStatus.ARCHIVED.name();
        setDefinitionActive(false);
    }

    public void open(String actor, Instant at) {
        requireDraft();
        status = AssessmentCycleStatus.OPEN.name();
        setDefinitionActive(true);
        openedBy = requireText(actor, "opener");
        openedAt = java.util.Objects.requireNonNull(at, "openedAt");
    }

    public void close(String actor, Instant at) {
        if (getStatus() != AssessmentCycleStatus.OPEN) {
            throw new IllegalLifecycleTransitionException("Only OPEN assessment cycles may be closed");
        }
        status = AssessmentCycleStatus.CLOSED.name();
        setDefinitionActive(false);
        closedBy = requireText(actor, "closer");
        closedAt = java.util.Objects.requireNonNull(at, "closedAt");
    }

    public void requireDraft() {
        if (getStatus() != AssessmentCycleStatus.DRAFT) throw new IllegalLifecycleTransitionException(
                "Only DRAFT assessment cycles may be changed in Phase 3.1");
    }

    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public AssessmentCycleStatus getStatus() { return AssessmentCycleStatus.valueOf(status); }
    public String getOpenedBy() { return openedBy; }
    public Instant getOpenedAt() { return openedAt; }
    public String getClosedBy() { return closedBy; }
    public Instant getClosedAt() { return closedAt; }
}
