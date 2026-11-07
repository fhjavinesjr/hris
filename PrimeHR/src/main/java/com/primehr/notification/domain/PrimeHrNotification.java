package com.primehr.notification.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "primehr_notification", uniqueConstraints = @UniqueConstraint(
        name = "uk_notification_recipient_event", columnNames = {"agency_id", "recipient_key", "event_key"}))
public class PrimeHrNotification extends RspAuditedEntity {
    public enum SourceDomain {
        COMPETENCY, RSP, PERFORMANCE, LEARNING_DEVELOPMENT, REWARDS_RECOGNITION,
        SUCCESSION, EVIDENCE, OPERATIONS
    }
    public enum EventCode { ACTION_ASSIGNED, RECORD_RETURNED, OUTCOME_AVAILABLE, DUE_REMINDER }
    public enum Kind { ACTION_REQUIRED, INFORMATIONAL }
    public enum RouteKey {
        COMPETENCY, RSP, PERFORMANCE, LEARNING_DEVELOPMENT, REWARDS_RECOGNITION,
        SUCCESSION, EVIDENCE, OPERATIONS
    }
    public enum ActionState { ACTIVE, CLOSED }

    public static final String RETENTION_TAG = "PRIMEHR_NOTIFICATION";

    @Column(name = "recipient_employee_no", nullable = false, length = 100)
    private String recipientEmployeeNo;
    @Column(name = "recipient_key", nullable = false, length = 100)
    private String recipientKey;
    @Enumerated(EnumType.STRING)
    @Column(name = "source_domain", nullable = false, length = 40)
    private SourceDomain sourceDomain;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_code", nullable = false, length = 40)
    private EventCode eventCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Kind kind;
    @Column(name = "source_aggregate_type", nullable = false, length = 80)
    private String sourceAggregateType;
    @Column(name = "source_aggregate_id", nullable = false, length = 100)
    private String sourceAggregateId;
    @Column(name = "source_record_version", nullable = false)
    private long sourceRecordVersion;
    @Column(name = "event_key", nullable = false, length = 160)
    private String eventKey;
    @Enumerated(EnumType.STRING)
    @Column(name = "route_key", nullable = false, length = 40)
    private RouteKey routeKey;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, length = 500)
    private String message;
    @Column(name = "due_at")
    private Instant dueAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "action_state", nullable = false, length = 20)
    private ActionState actionState;
    @Column(name = "read_at")
    private Instant readAt;
    @Column(name = "dismissed_at")
    private Instant dismissedAt;
    @Column(name = "retention_tag", nullable = false, length = 100)
    private String retentionTag;

    protected PrimeHrNotification() { }

    public PrimeHrNotification(String agencyId, String recipientEmployeeNo, SourceDomain sourceDomain,
                               EventCode eventCode, Kind kind, String sourceAggregateType,
                               String sourceAggregateId, long sourceRecordVersion, String eventKey,
                               RouteKey routeKey, String title, String message, Instant dueAt) {
        super(agencyId);
        this.recipientEmployeeNo = requiredText(recipientEmployeeNo, "recipientEmployeeNo");
        this.recipientKey = recipientKey(recipientEmployeeNo);
        this.sourceDomain = Objects.requireNonNull(sourceDomain, "sourceDomain");
        this.eventCode = Objects.requireNonNull(eventCode, "eventCode");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.sourceAggregateType = controlledCode(sourceAggregateType, "sourceAggregateType", 80);
        this.sourceAggregateId = bounded(sourceAggregateId, "sourceAggregateId", 100);
        if (sourceRecordVersion < 0) throw new IllegalArgumentException("sourceRecordVersion cannot be negative");
        this.sourceRecordVersion = sourceRecordVersion;
        this.eventKey = controlledKey(eventKey);
        this.routeKey = Objects.requireNonNull(routeKey, "routeKey");
        this.title = bounded(title, "title", 200);
        this.message = bounded(message, "message", 500);
        this.dueAt = dueAt;
        this.actionState = ActionState.ACTIVE;
        this.retentionTag = RETENTION_TAG;
    }

    public void markRead(Instant at) {
        if (readAt == null) readAt = Objects.requireNonNull(at, "at");
    }

    public void dismiss(Instant at) {
        if (kind != Kind.INFORMATIONAL) throw new IllegalStateException("Action-required notifications cannot be dismissed");
        markRead(at);
        if (dismissedAt == null) dismissedAt = at;
    }

    public void close(long currentSourceVersion) {
        if (currentSourceVersion < sourceRecordVersion) throw new IllegalArgumentException("Source version cannot move backwards");
        sourceRecordVersion = currentSourceVersion;
        actionState = ActionState.CLOSED;
    }

    public static String recipientKey(String value) {
        return requiredText(value, "recipientEmployeeNo").toUpperCase(Locale.ROOT);
    }

    private static String controlledCode(String value, String field, int max) {
        String result = bounded(value, field, max).toUpperCase(Locale.ROOT);
        if (!result.matches("[A-Z][A-Z0-9_]*")) throw new IllegalArgumentException(field + " is not a controlled code");
        return result;
    }

    private static String controlledKey(String value) {
        String result = bounded(value, "eventKey", 160);
        if (!result.matches("[A-Za-z0-9._:-]+")) throw new IllegalArgumentException("eventKey contains unsupported characters");
        return result;
    }

    private static String bounded(String value, String field, int max) {
        String result = requiredText(value, field);
        if (result.length() > max) throw new IllegalArgumentException(field + " exceeds " + max + " characters");
        return result;
    }

    public String getRecipientEmployeeNo() { return recipientEmployeeNo; }
    public String getRecipientKey() { return recipientKey; }
    public SourceDomain getSourceDomain() { return sourceDomain; }
    public EventCode getEventCode() { return eventCode; }
    public Kind getKind() { return kind; }
    public String getSourceAggregateType() { return sourceAggregateType; }
    public String getSourceAggregateId() { return sourceAggregateId; }
    public long getSourceRecordVersion() { return sourceRecordVersion; }
    public String getEventKey() { return eventKey; }
    public RouteKey getRouteKey() { return routeKey; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Instant getDueAt() { return dueAt; }
    public ActionState getActionState() { return actionState; }
    public Instant getReadAt() { return readAt; }
    public Instant getDismissedAt() { return dismissedAt; }
    public String getRetentionTag() { return retentionTag; }
}
