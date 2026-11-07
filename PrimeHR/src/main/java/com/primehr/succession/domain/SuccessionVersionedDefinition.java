package com.primehr.succession.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;

import java.time.*;
import java.util.Objects;

@MappedSuperclass
public abstract class SuccessionVersionedDefinition extends RspAuditedEntity {
    @Column(name = "definition_version", nullable = false) private int definitionVersion;
    @Column(name = "supersedes_id", length = 36) private String supersedesId;
    @Column(nullable = false, length = 200) private String title;
    @Column(length = 2000) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private SuccessionDefinitionStatus status;
    @Column(name = "effective_from") private LocalDate effectiveFrom;
    @Column(name = "effective_to") private LocalDate effectiveTo;
    @Column(name = "published_by", length = 100) private String publishedBy;
    @Column(name = "published_at") private Instant publishedAt;
    @Column(name = "retired_by", length = 100) private String retiredBy;
    @Column(name = "retired_at") private Instant retiredAt;
    @Column(name = "retirement_reason", length = 1000) private String retirementReason;

    protected SuccessionVersionedDefinition() { }
    protected SuccessionVersionedDefinition(String agencyId, int definitionVersion, String supersedesId,
                                             String title, String description) {
        super(agencyId);
        if (definitionVersion < 1) throw new IllegalArgumentException("definitionVersion must be positive");
        this.definitionVersion = definitionVersion;
        this.supersedesId = optionalText(supersedesId);
        this.status = SuccessionDefinitionStatus.DRAFT;
        changeDefinition(title, description);
    }
    protected final void changeDefinition(String title, String description) {
        requireDraft();
        this.title = requiredText(title, "title");
        this.description = optionalText(description);
    }
    public final void publish(LocalDate from, LocalDate to, String actor, Instant at) {
        requireDraft();
        if (from == null) throw new IllegalArgumentException("effectiveFrom is required");
        if (to != null && to.isBefore(from)) throw new IllegalArgumentException("effectiveTo cannot precede effectiveFrom");
        effectiveFrom = from; effectiveTo = to; publishedBy = requiredText(actor, "actor");
        publishedAt = Objects.requireNonNull(at, "publishedAt is required"); status = SuccessionDefinitionStatus.PUBLISHED;
    }
    public final void retire(String actor, String reason, Instant at) {
        if (status != SuccessionDefinitionStatus.PUBLISHED) throw new IllegalLifecycleTransitionException("Only a published definition may be retired");
        retiredBy = requiredText(actor, "actor"); retirementReason = requiredText(reason, "reason");
        retiredAt = Objects.requireNonNull(at, "retiredAt is required");
        LocalDate day = at.atZone(ZoneId.of("Asia/Manila")).toLocalDate();
        if (effectiveTo == null || effectiveTo.isAfter(day)) effectiveTo = day.isBefore(effectiveFrom) ? effectiveFrom : day;
        status = SuccessionDefinitionStatus.RETIRED;
    }
    public final void supersede(LocalDate successorFrom, String actor, Instant at) {
        if (status != SuccessionDefinitionStatus.PUBLISHED) throw new IllegalLifecycleTransitionException("Only a published definition may be superseded");
        if (successorFrom == null || !successorFrom.isAfter(effectiveFrom)) throw new IllegalArgumentException("A successor must become effective after its predecessor");
        effectiveTo = successorFrom.minusDays(1); retiredBy = requiredText(actor, "actor");
        retirementReason = "Superseded by a published revision"; retiredAt = Objects.requireNonNull(at); status = SuccessionDefinitionStatus.RETIRED;
    }
    public final boolean effectiveOn(LocalDate day) { return status == SuccessionDefinitionStatus.PUBLISHED && !day.isBefore(effectiveFrom) && (effectiveTo == null || !day.isAfter(effectiveTo)); }
    protected final void requireDraft() { if (status != SuccessionDefinitionStatus.DRAFT) throw new IllegalLifecycleTransitionException("Published definitions are immutable; create a revision"); }
    public int getDefinitionVersion() { return definitionVersion; } public String getSupersedesId() { return supersedesId; }
    public String getTitle() { return title; } public String getDescription() { return description; }
    public SuccessionDefinitionStatus getStatus() { return status; } public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; } public String getPublishedBy() { return publishedBy; }
    public Instant getPublishedAt() { return publishedAt; } public String getRetiredBy() { return retiredBy; }
    public Instant getRetiredAt() { return retiredAt; } public String getRetirementReason() { return retirementReason; }
}
