package com.primehr.rsp.applicant.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "rsp_applicant_communication", indexes =
        @Index(name = "ix_rsp_communication_application", columnList = "agency_id,application_id,occurred_at"))
public class ApplicantCommunication extends RspAuditedEntity {
    public enum Direction { SYSTEM_TO_APPLICANT, STAFF_TO_APPLICANT }

    @Column(name = "application_id", nullable = false, length = 36) private String applicationId;
    @Column(name = "applicant_id", nullable = false, length = 36) private String applicantId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Direction direction;
    @Column(nullable = false, length = 20) private String channel;
    @Column(nullable = false, length = 300) private String subject;
    @Column(nullable = false, length = 4000) private String body;
    @Column(nullable = false, length = 100) private String actor;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
    @Column(name = "read_at") private Instant readAt;
    @Column(name = "correlation_id", length = 100) private String correlationId;

    protected ApplicantCommunication() {}

    public ApplicantCommunication(String agencyId, String applicationId, String applicantId,
                                  Direction direction, String subject, String body, String actor,
                                  Instant occurredAt, String correlationId) {
        super(agencyId);
        this.applicationId = requiredText(applicationId, "applicationId");
        this.applicantId = requiredText(applicantId, "applicantId");
        this.direction = Objects.requireNonNull(direction, "direction");
        channel = "PORTAL";
        this.subject = requiredText(subject, "subject");
        this.body = requiredText(body, "body");
        this.actor = requiredText(actor, "actor");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        this.correlationId = optionalText(correlationId);
    }

    public void markRead(Instant at) { if (readAt == null) readAt = at; }
    public String getApplicationId() { return applicationId; }
    public String getApplicantId() { return applicantId; }
    public Direction getDirection() { return direction; }
    public String getChannel() { return channel; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getActor() { return actor; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getReadAt() { return readAt; }
    public String getCorrelationId() { return correlationId; }
}
