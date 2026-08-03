package com.primehr.shared.audit;

import java.time.Instant;

public record AuditEventResponse(
        String id, String actor, String action, String aggregateType, String aggregateId,
        Integer businessVersion, Long recordVersion, Instant occurredAt,
        String previousState, String newState, String reason, String correlationId
) {
    public static AuditEventResponse from(PrimeHrAuditEvent event) {
        return new AuditEventResponse(event.getId(), event.getActor(), event.getAction(),
                event.getAggregateType(), event.getAggregateId(), event.getBusinessVersion(),
                event.getRecordVersion(), event.getOccurredAt(), event.getPreviousState(),
                event.getNewState(), event.getReason(), event.getCorrelationId());
    }
}
