package com.primehr.notification;

import com.primehr.notification.application.*;
import com.primehr.notification.domain.PrimeHrNotification;
import com.primehr.notification.infrastructure.PrimeHrNotificationRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationReminderServiceTest {
    @Test
    void authoritativeDueReminderIsBoundedAndIdempotent() {
        var repository = mock(PrimeHrNotificationRepository.class);
        var notifications = mock(NotificationService.class);
        var validators = mock(NotificationSourceValidatorRegistry.class);
        var properties = new NotificationReminderProperties(true, 24, 60_000L, 25);
        var service = new NotificationReminderService(repository, notifications, validators,
                properties, new SimpleMeterRegistry());
        Instant now = Instant.parse("2026-11-07T00:00:00Z");
        var source = new PrimeHrNotification("A", "EMP-1", PrimeHrNotification.SourceDomain.EVIDENCE,
                PrimeHrNotification.EventCode.ACTION_ASSIGNED, PrimeHrNotification.Kind.ACTION_REQUIRED,
                "EVIDENCE_CORRECTIVE_ACTION", "action-1", 3, "assignment:1",
                PrimeHrNotification.RouteKey.EVIDENCE, "Action", "Message", now.plusSeconds(3_600));
        when(repository.reminderCandidates(eq("A"), eq(PrimeHrNotification.EventCode.ACTION_ASSIGNED),
                eq(PrimeHrNotification.Kind.ACTION_REQUIRED), eq(PrimeHrNotification.ActionState.ACTIVE),
                eq(now), eq(now.plusSeconds(86_400)), any()))
                .thenReturn(new PageImpl<>(List.of(source)));
        when(validators.validate(source)).thenReturn(NotificationSourceValidator.Validation.available(4));
        when(notifications.publish(any(), eq("correlation")))
                .thenReturn(new NotificationService.PublishResult(null, false));

        assertThat(service.issueDueReminders("A", now, "correlation")).isOne();
        var command = org.mockito.ArgumentCaptor.forClass(NotificationService.PublishCommand.class);
        verify(notifications).publish(command.capture(), eq("correlation"));
        assertThat(command.getValue().eventCode()).isEqualTo(PrimeHrNotification.EventCode.DUE_REMINDER);
        assertThat(command.getValue().sourceRecordVersion()).isEqualTo(4);
        assertThat(command.getValue().eventKey()).startsWith("reminder:");
    }

    @Test
    void staleCandidateIsClosedWithoutIssuingReminder() {
        var repository = mock(PrimeHrNotificationRepository.class);
        var notifications = mock(NotificationService.class);
        var validators = mock(NotificationSourceValidatorRegistry.class);
        var service = new NotificationReminderService(repository, notifications, validators,
                new NotificationReminderProperties(true, 1, 60_000L, 10), new SimpleMeterRegistry());
        Instant now = Instant.parse("2026-11-07T00:00:00Z");
        var source = new PrimeHrNotification("A", "EMP-1", PrimeHrNotification.SourceDomain.EVIDENCE,
                PrimeHrNotification.EventCode.ACTION_ASSIGNED, PrimeHrNotification.Kind.ACTION_REQUIRED,
                "EVIDENCE_CORRECTIVE_ACTION", "action-1", 3, "assignment:1",
                PrimeHrNotification.RouteKey.EVIDENCE, "Action", "Message", now.plusSeconds(60));
        when(repository.reminderCandidates(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(source)));
        when(validators.validate(source)).thenReturn(NotificationSourceValidator.Validation.unavailable(5));

        assertThat(service.issueDueReminders("A", now, null)).isZero();
        verify(notifications).closeActions(any(), isNull());
        verify(notifications, never()).publish(any(), any());
    }
}
