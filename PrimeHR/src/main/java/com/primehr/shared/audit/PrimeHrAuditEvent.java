package com.primehr.shared.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prime_audit_event")
public class PrimeHrAuditEvent {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "agency_id", length = 64, nullable = false, updatable = false)
    private String agencyId;

    @Column(name = "actor", length = 100, nullable = false, updatable = false)
    private String actor;

    @Column(name = "action", length = 50, nullable = false, updatable = false)
    private String action;

    @Column(name = "aggregate_type", length = 50, nullable = false, updatable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 36, nullable = false, updatable = false)
    private String aggregateId;

    @Column(name = "business_version", updatable = false)
    private Integer businessVersion;

    @Column(name = "record_version", updatable = false)
    private Long recordVersion;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "previous_state", length = 32000, updatable = false)
    private String previousState;

    @Column(name = "new_state", length = 32000, updatable = false)
    private String newState;

    @Nationalized
    @Column(name = "reason", length = 1000, updatable = false)
    private String reason;

    @Column(name = "source_module", length = 50, nullable = false, updatable = false)
    private String sourceModule;

    @Column(name = "correlation_id", length = 100, updatable = false)
    private String correlationId;

    protected PrimeHrAuditEvent() {
    }

    public PrimeHrAuditEvent(String agencyId, String actor, String action, String aggregateType,
                             String aggregateId, Integer businessVersion, Long recordVersion,
                             String previousState, String newState, String reason, String correlationId) {
        this.agencyId = required(agencyId, "agencyId");
        this.actor = required(actor, "actor");
        this.action = required(action, "action");
        this.aggregateType = required(aggregateType, "aggregateType");
        this.aggregateId = required(aggregateId, "aggregateId");
        this.businessVersion = businessVersion;
        this.recordVersion = recordVersion;
        this.previousState = previousState;
        this.newState = newState;
        this.reason = normalize(reason);
        this.sourceModule = "PrimeHR";
        this.correlationId = normalize(correlationId);
        this.occurredAt = Instant.now();
    }

    @PrePersist
    void assignId() {
        if (id == null) id = UUID.randomUUID().toString();
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getId() { return id; }
    public String getAgencyId() { return agencyId; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public Integer getBusinessVersion() { return businessVersion; }
    public Long getRecordVersion() { return recordVersion; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getPreviousState() { return previousState; }
    public String getNewState() { return newState; }
    public String getReason() { return reason; }
    public String getSourceModule() { return sourceModule; }
    public String getCorrelationId() { return correlationId; }
}
