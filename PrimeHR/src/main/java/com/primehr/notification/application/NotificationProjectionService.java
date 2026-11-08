package com.primehr.notification.application;

import com.primehr.notification.domain.PrimeHrNotification.EventCode;
import com.primehr.notification.domain.PrimeHrNotification.SourceDomain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class NotificationProjectionService {
    private final NotificationService notifications;

    public NotificationProjectionService(NotificationService notifications) {
        this.notifications = notifications;
    }

    public void assign(Projection command, String correlationId) {
        publish(command, EventCode.ACTION_ASSIGNED, correlationId);
    }

    public void returned(Projection command, String correlationId) {
        close(command, correlationId);
        publish(command, EventCode.RECORD_RETURNED, correlationId);
    }

    public void outcome(Projection command, String correlationId) {
        close(command, correlationId);
        publish(command, EventCode.OUTCOME_AVAILABLE, correlationId);
    }

    public void replaceAssignment(Projection command, String correlationId) {
        close(command, correlationId);
        publish(command, EventCode.ACTION_ASSIGNED, correlationId);
    }

    public int close(Projection command, String correlationId) {
        return notifications.closeActions(new NotificationService.CloseCommand(command.agencyId(),
                command.sourceDomain(), command.sourceAggregateType(), command.sourceAggregateId(),
                command.sourceRecordVersion()), correlationId);
    }

    private void publish(Projection command, EventCode eventCode, String correlationId) {
        String eventKey = String.join(":", "projection", command.sourceDomain().name().toLowerCase(Locale.ROOT),
                command.sourceAggregateType().toLowerCase(Locale.ROOT), command.sourceAggregateId(),
                Long.toString(command.sourceRecordVersion()), eventCode.name().toLowerCase(Locale.ROOT),
                recipientKey(command.recipientEmployeeNo()));
        notifications.publish(new NotificationService.PublishCommand(command.agencyId(),
                command.recipientEmployeeNo(), command.sourceDomain(), eventCode,
                command.sourceAggregateType(), command.sourceAggregateId(), command.sourceRecordVersion(),
                eventKey, command.dueAt()), correlationId);
    }

    private static String recipientKey(String employeeNo) {
        String normalized = employeeNo == null ? "" : employeeNo.trim().toUpperCase(Locale.ROOT);
        return UUID.nameUUIDFromBytes(normalized.getBytes(StandardCharsets.UTF_8)).toString();
    }

    public record Projection(String agencyId, String recipientEmployeeNo, SourceDomain sourceDomain,
                             String sourceAggregateType, String sourceAggregateId,
                             long sourceRecordVersion, Instant dueAt) { }
}
