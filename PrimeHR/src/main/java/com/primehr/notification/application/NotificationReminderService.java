package com.primehr.notification.application;

import com.primehr.notification.domain.PrimeHrNotification;
import com.primehr.notification.infrastructure.PrimeHrNotificationRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class NotificationReminderService {
    private final PrimeHrNotificationRepository repository;
    private final NotificationService notifications;
    private final NotificationSourceValidatorRegistry validators;
    private final NotificationReminderProperties properties;
    private final MeterRegistry metrics;

    public NotificationReminderService(PrimeHrNotificationRepository repository,
                                       NotificationService notifications,
                                       NotificationSourceValidatorRegistry validators,
                                       NotificationReminderProperties properties,
                                       MeterRegistry metrics) {
        this.repository = repository;
        this.notifications = notifications;
        this.validators = validators;
        this.properties = properties;
        this.metrics = metrics;
    }

    @Transactional
    public int issueDueReminders(String agencyId, Instant now, String correlationId) {
        Instant through = now.plusSeconds(properties.leadHours() * 3_600L);
        var candidates = repository.reminderCandidates(agencyId, PrimeHrNotification.EventCode.ACTION_ASSIGNED,
                PrimeHrNotification.Kind.ACTION_REQUIRED, PrimeHrNotification.ActionState.ACTIVE, now, through,
                PageRequest.of(0, properties.maximumBatchSize())).getContent();
        int issued = 0;
        for (var source : candidates) {
            var validation = validators.validate(source);
            if (!validation.available()) {
                notifications.closeActions(new NotificationService.CloseCommand(agencyId,
                        source.getSourceDomain(), source.getSourceAggregateType(), source.getSourceAggregateId(),
                        Math.max(validation.currentSourceVersion(), source.getSourceRecordVersion())), correlationId);
                metrics.counter("primehr.notifications.reminders", "outcome", "stale").increment();
                continue;
            }
            String eventKey = "reminder:" + source.getId() + ":" + source.getDueAt().getEpochSecond();
            var result = notifications.publish(new NotificationService.PublishCommand(agencyId,
                    source.getRecipientEmployeeNo(), source.getSourceDomain(),
                    PrimeHrNotification.EventCode.DUE_REMINDER, source.getSourceAggregateType(),
                    source.getSourceAggregateId(), validation.currentSourceVersion(), eventKey,
                    source.getDueAt()), correlationId);
            metrics.counter("primehr.notifications.reminders", "outcome",
                    result.idempotentReplay() ? "replayed" : "issued").increment();
            if (!result.idempotentReplay()) issued++;
        }
        return issued;
    }
}
