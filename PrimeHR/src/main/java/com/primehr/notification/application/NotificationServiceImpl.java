package com.primehr.notification.application;

import com.primehr.notification.api.NotificationDtos.MarkAllReadResponse;
import com.primehr.notification.api.NotificationDtos.NotificationResponse;
import com.primehr.notification.api.NotificationDtos.TargetResolutionResponse;
import com.primehr.notification.domain.PrimeHrNotification;
import com.primehr.notification.infrastructure.PrimeHrNotificationRepository;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final PrimeHrNotificationRepository repository;
    private final NotificationDefinitionRegistry definitions;
    private final PrimeHrAuditService audit;
    private final NotificationSourceValidatorRegistry sourceValidators;

    public NotificationServiceImpl(PrimeHrNotificationRepository repository,
                                   NotificationDefinitionRegistry definitions,
                                   PrimeHrAuditService audit,
                                   NotificationSourceValidatorRegistry sourceValidators) {
        this.repository = repository;
        this.definitions = definitions;
        this.audit = audit;
        this.sourceValidators = sourceValidators;
    }

    @Override
    public PublishResult publish(PublishCommand command, String correlationId) {
        Objects.requireNonNull(command, "command");
        String recipientKey = PrimeHrNotification.recipientKey(command.recipientEmployeeNo());
        var existing = repository.findByAgencyIdAndRecipientKeyAndEventKey(
                command.agencyId(), recipientKey, command.eventKey());
        if (existing.isPresent()) {
            ensureSameEvent(existing.get(), command);
            return new PublishResult(response(existing.get()), true);
        }
        var definition = definitions.resolve(command.sourceDomain(), command.eventCode());
        var notification = new PrimeHrNotification(command.agencyId(), command.recipientEmployeeNo(),
                command.sourceDomain(), command.eventCode(), definition.kind(), command.sourceAggregateType(),
                command.sourceAggregateId(), command.sourceRecordVersion(), command.eventKey(),
                definition.routeKey(), definition.title(), definition.message(), command.dueAt());
        notification = repository.saveAndFlush(notification);
        audit.record(command.agencyId(), "PUBLISH_NOTIFICATION", "PRIMEHR_NOTIFICATION",
                notification.getId(), null, notification.getVersion(), null,
                auditState(notification), null, correlationId);
        return new PublishResult(response(notification), false);
    }

    @Override
    public int closeActions(CloseCommand command, String correlationId) {
        Objects.requireNonNull(command, "command");
        var active = repository.findByAgencyIdAndSourceDomainAndSourceAggregateTypeAndSourceAggregateIdAndKindAndActionState(
                command.agencyId(), command.sourceDomain(), command.sourceAggregateType(),
                command.sourceAggregateId(), PrimeHrNotification.Kind.ACTION_REQUIRED,
                PrimeHrNotification.ActionState.ACTIVE);
        for (var notification : active) {
            var before = auditState(notification);
            notification.close(command.sourceRecordVersion());
            repository.saveAndFlush(notification);
            audit.record(command.agencyId(), "CLOSE_NOTIFICATION_ACTION", "PRIMEHR_NOTIFICATION",
                    notification.getId(), null, notification.getVersion(), before,
                    auditState(notification), null, correlationId);
        }
        return active.size();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> feed(String agencyId, String recipientEmployeeNo,
                                                   boolean unreadOnly, boolean includeDismissed,
                                                   int page, int size) {
        if (page < 0) throw new IllegalArgumentException("page cannot be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
        return PageResponse.from(repository.feed(agencyId, PrimeHrNotification.recipientKey(recipientEmployeeNo),
                unreadOnly, includeDismissed, PageRequest.of(page, size)), this::response);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(String agencyId, String recipientEmployeeNo) {
        return repository.countByAgencyIdAndRecipientKeyAndReadAtIsNullAndDismissedAtIsNull(
                agencyId, PrimeHrNotification.recipientKey(recipientEmployeeNo));
    }

    @Override
    public NotificationResponse markRead(String agencyId, String recipientEmployeeNo, String id,
                                         long recordVersion, String correlationId) {
        var notification = owned(agencyId, recipientEmployeeNo, id);
        if (notification.getReadAt() != null) return response(notification);
        version(notification, recordVersion);
        var before = auditState(notification);
        notification.markRead(Instant.now());
        notification = repository.saveAndFlush(notification);
        audit.record(agencyId, "MARK_NOTIFICATION_READ", "PRIMEHR_NOTIFICATION", id, null,
                notification.getVersion(), before, auditState(notification), null, correlationId);
        return response(notification);
    }

    @Override
    public MarkAllReadResponse markAllRead(String agencyId, String recipientEmployeeNo, String correlationId) {
        Instant at = Instant.now();
        String recipientKey = PrimeHrNotification.recipientKey(recipientEmployeeNo);
        int updated = repository.markAllRead(agencyId, recipientKey, at, audit.currentActor());
        if (updated > 0) {
            String inboxId = UUID.nameUUIDFromBytes((agencyId + ":" + recipientKey)
                    .getBytes(StandardCharsets.UTF_8)).toString();
            audit.record(agencyId, "MARK_ALL_NOTIFICATIONS_READ", "PRIMEHR_NOTIFICATION_INBOX",
                    inboxId, null, null, null, new BulkAuditState(updated, at), null, correlationId);
        }
        return new MarkAllReadResponse(updated, at);
    }

    @Override
    public NotificationResponse dismiss(String agencyId, String recipientEmployeeNo, String id,
                                        long recordVersion, String correlationId) {
        var notification = owned(agencyId, recipientEmployeeNo, id);
        if (notification.getDismissedAt() != null) return response(notification);
        version(notification, recordVersion);
        var before = auditState(notification);
        notification.dismiss(Instant.now());
        notification = repository.saveAndFlush(notification);
        audit.record(agencyId, "DISMISS_NOTIFICATION", "PRIMEHR_NOTIFICATION", id, null,
                notification.getVersion(), before, auditState(notification), null, correlationId);
        return response(notification);
    }

    @Override
    public TargetResolutionResponse resolveTarget(String agencyId, String recipientEmployeeNo, String id,
                                                  String correlationId) {
        var notification = owned(agencyId, recipientEmployeeNo, id);
        var validation = sourceValidators.validate(notification);
        if (!validation.available() && notification.getKind() == PrimeHrNotification.Kind.ACTION_REQUIRED
                && notification.getActionState() == PrimeHrNotification.ActionState.ACTIVE) {
            var before = auditState(notification);
            notification.close(Math.max(validation.currentSourceVersion(), notification.getSourceRecordVersion()));
            notification = repository.saveAndFlush(notification);
            audit.record(agencyId, "CLOSE_STALE_NOTIFICATION_ACTION", "PRIMEHR_NOTIFICATION", id, null,
                    notification.getVersion(), before, auditState(notification), null, correlationId);
        }
        return new TargetResolutionResponse(validation.available(), notification.getRouteKey().name(),
                notification.getSourceAggregateType(), notification.getSourceAggregateId(),
                notification.getActionState().name());
    }

    private PrimeHrNotification owned(String agencyId, String recipient, String id) {
        return repository.findByIdAndAgencyIdAndRecipientKey(id, agencyId,
                PrimeHrNotification.recipientKey(recipient)).orElseThrow(
                () -> new ResourceNotFoundException("Notification was not found"));
    }

    private static void version(PrimeHrNotification value, long supplied) {
        if (value.getVersion() != supplied) throw new OptimisticConflictException("Notification changed; reload it");
    }

    private static void ensureSameEvent(PrimeHrNotification existing, PublishCommand command) {
        if (existing.getSourceDomain() != command.sourceDomain()
                || existing.getEventCode() != command.eventCode()
                || !existing.getSourceAggregateType().equalsIgnoreCase(command.sourceAggregateType())
                || !existing.getSourceAggregateId().equals(command.sourceAggregateId())
                || existing.getSourceRecordVersion() != command.sourceRecordVersion()) {
            throw new OptimisticConflictException("Notification event key is already used by another source revision");
        }
    }

    private NotificationResponse response(PrimeHrNotification value) {
        return new NotificationResponse(value.getId(), value.getVersion(), value.getSourceDomain().name(),
                value.getEventCode().name(), value.getKind().name(), value.getSourceAggregateType(),
                value.getSourceAggregateId(), value.getSourceRecordVersion(), value.getRouteKey().name(),
                value.getTitle(), value.getMessage(), value.getDueAt(), value.getActionState().name(),
                value.getReadAt(), value.getDismissedAt(), value.getCreatedAt());
    }

    private static AuditState auditState(PrimeHrNotification value) {
        return new AuditState(value.getSourceDomain().name(), value.getEventCode().name(),
                value.getActionState().name(), value.getReadAt(), value.getDismissedAt());
    }

    private record AuditState(String sourceDomain, String eventCode, String actionState,
                              Instant readAt, Instant dismissedAt) { }
    private record BulkAuditState(int updated, Instant readAt) { }
}
