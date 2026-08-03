package com.primehr.shared.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AgencyAuditableEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "agency_id", length = 64, nullable = false, updatable = false)
    private String agencyId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Version
    @Column(name = "record_version", nullable = false)
    private long version;

    @CreatedBy
    @Column(name = "created_by", length = 100, nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100, nullable = false)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AgencyAuditableEntity() {
    }

    protected AgencyAuditableEntity(String agencyId, boolean active, int displayOrder,
                                    LocalDate effectiveFrom, LocalDate effectiveTo) {
        this.agencyId = requireText(agencyId, "agencyId");
        if (displayOrder < 0) {
            throw new IllegalArgumentException("displayOrder cannot be negative");
        }
        validateEffectivity(effectiveFrom, effectiveTo);
        this.active = active;
        this.displayOrder = displayOrder;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    @PrePersist
    protected void assignStableIdentifier() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public boolean isEffectiveOn(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return active
                && (effectiveFrom == null || !effectiveFrom.isAfter(date))
                && (effectiveTo == null || !effectiveTo.isBefore(date));
    }

    protected static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    protected static void validateEffectivity(LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("effectiveTo cannot be before effectiveFrom");
        }
    }

    protected void updateDefinitionFields(int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (displayOrder < 0) {
            throw new IllegalArgumentException("displayOrder cannot be negative");
        }
        validateEffectivity(effectiveFrom, effectiveTo);
        this.displayOrder = displayOrder;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    protected void setDefinitionActive(boolean active) {
        this.active = active;
    }

    public String getId() { return id; }
    public String getAgencyId() { return agencyId; }
    public boolean isActive() { return active; }
    public int getDisplayOrder() { return displayOrder; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public long getVersion() { return version; }
    public String getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}
