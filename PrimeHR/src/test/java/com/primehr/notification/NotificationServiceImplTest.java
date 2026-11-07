package com.primehr.notification;

import com.primehr.notification.application.NotificationDefinitionRegistry;
import com.primehr.notification.application.NotificationService.PublishCommand;
import com.primehr.notification.application.NotificationServiceImpl;
import com.primehr.notification.domain.PrimeHrNotification;
import com.primehr.notification.infrastructure.PrimeHrNotificationRepository;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.OptimisticConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

import static com.primehr.notification.domain.PrimeHrNotification.EventCode.ACTION_ASSIGNED;
import static com.primehr.notification.domain.PrimeHrNotification.SourceDomain.PERFORMANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {
    private PrimeHrNotificationRepository repository;
    private PrimeHrAuditService audit;
    private com.primehr.notification.application.NotificationSourceValidatorRegistry validators;
    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(PrimeHrNotificationRepository.class);
        audit = mock(PrimeHrAuditService.class);
        validators = mock(com.primehr.notification.application.NotificationSourceValidatorRegistry.class);
        service = new NotificationServiceImpl(repository, new NotificationDefinitionRegistry(),
                audit, validators);
    }

    @Test
    void publishUsesSafeDefinitionAndIsIdempotent() {
        PublishCommand command = command("event:1", 2);
        when(repository.findByAgencyIdAndRecipientKeyAndEventKey("A", "EMP-1", "event:1"))
                .thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.publish(command, "correlation");
        ArgumentCaptor<PrimeHrNotification> captor = ArgumentCaptor.forClass(PrimeHrNotification.class);
        verify(repository).saveAndFlush(captor.capture());
        PrimeHrNotification saved = captor.getValue();
        assertThat(created.idempotentReplay()).isFalse();
        assertThat(saved.getTitle()).isEqualTo("Performance Management action required");
        assertThat(saved.getMessage()).doesNotContain("case-1", "EMP-1");

        reset(repository);
        when(repository.findByAgencyIdAndRecipientKeyAndEventKey("A", "EMP-1", "event:1"))
                .thenReturn(Optional.of(saved));
        assertThat(service.publish(command, "correlation").idempotentReplay()).isTrue();
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void eventKeyCannotBeReusedForAnotherSourceRevision() {
        var existing = new PrimeHrNotification("A", "EMP-1", PERFORMANCE, ACTION_ASSIGNED,
                PrimeHrNotification.Kind.ACTION_REQUIRED, "SPMS_RATING_CASE", "case-1", 1,
                "event:1", PrimeHrNotification.RouteKey.PERFORMANCE, "Title", "Message", null);
        when(repository.findByAgencyIdAndRecipientKeyAndEventKey("A", "EMP-1", "event:1"))
                .thenReturn(Optional.of(existing));
        assertThatThrownBy(() -> service.publish(command("event:1", 2), null))
                .isInstanceOf(OptimisticConflictException.class);
    }

    @Test
    void invalidPageSizeIsRejectedBeforeRepositoryAccess() {
        assertThatThrownBy(() -> service.feed("A", "EMP-1", false, false, 0, 101))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void repeatedReadOfOwnedNotificationIsIdempotent() {
        var notification = notification(PrimeHrNotification.Kind.INFORMATIONAL);
        notification.markRead(Instant.now());
        when(repository.findByIdAndAgencyIdAndRecipientKey("notice-1", "A", "EMP-1"))
                .thenReturn(Optional.of(notification));

        var response = service.markRead("A", "EMP-1", "notice-1", 99, null);

        assertThat(response.readAt()).isNotNull();
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test
    void repeatedDismissOfOwnedNotificationIsIdempotent() {
        var notification = notification(PrimeHrNotification.Kind.INFORMATIONAL);
        notification.dismiss(Instant.now());
        when(repository.findByIdAndAgencyIdAndRecipientKey("notice-1", "A", "EMP-1"))
                .thenReturn(Optional.of(notification));

        var response = service.dismiss("A", "EMP-1", "notice-1", 99, null);

        assertThat(response.dismissedAt()).isNotNull();
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test
    void markAllReadUsesAuthenticatedActorForAuditMetadata() {
        when(audit.currentActor()).thenReturn("EMP-1");
        when(repository.markAllRead(eq("A"), eq("EMP-1"), any(), eq("EMP-1"))).thenReturn(0);

        service.markAllRead("A", "EMP-1", null);

        verify(repository).markAllRead(eq("A"), eq("EMP-1"), any(), eq("EMP-1"));
    }

    @Test
    void closeActionsClosesEveryActiveActionForTheExactSource() {
        var first = notification(PrimeHrNotification.Kind.ACTION_REQUIRED);
        var second = new PrimeHrNotification("A", "EMP-2", PERFORMANCE, ACTION_ASSIGNED,
                PrimeHrNotification.Kind.ACTION_REQUIRED, "SPMS_RATING_CASE", "case-1", 1,
                "event:2", PrimeHrNotification.RouteKey.PERFORMANCE, "Title", "Message", null);
        when(repository.findByAgencyIdAndSourceDomainAndSourceAggregateTypeAndSourceAggregateIdAndKindAndActionState(
                "A", PERFORMANCE, "SPMS_RATING_CASE", "case-1", PrimeHrNotification.Kind.ACTION_REQUIRED,
                PrimeHrNotification.ActionState.ACTIVE)).thenReturn(List.of(first, second));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        int closed = service.closeActions(new com.primehr.notification.application.NotificationService.CloseCommand(
                "A", PERFORMANCE, "SPMS_RATING_CASE", "case-1", 3), "correlation");

        assertThat(closed).isEqualTo(2);
        assertThat(first.getActionState()).isEqualTo(PrimeHrNotification.ActionState.CLOSED);
        assertThat(first.getSourceRecordVersion()).isEqualTo(3);
        assertThat(second.getActionState()).isEqualTo(PrimeHrNotification.ActionState.CLOSED);
        verify(repository, times(2)).saveAndFlush(any());
    }

    @Test
    void targetResolutionClosesAStaleActionWithoutExposingAnotherRecipient() {
        var notification = notification(PrimeHrNotification.Kind.ACTION_REQUIRED);
        when(repository.findByIdAndAgencyIdAndRecipientKey("notice-1", "A", "EMP-1"))
                .thenReturn(Optional.of(notification));
        when(validators.validate(notification)).thenReturn(
                com.primehr.notification.application.NotificationSourceValidator.Validation.unavailable(3));
        when(repository.saveAndFlush(notification)).thenReturn(notification);

        var target = service.resolveTarget("A", "EMP-1", "notice-1", "correlation");

        assertThat(target.available()).isFalse();
        assertThat(target.actionState()).isEqualTo("CLOSED");
        assertThat(notification.getSourceRecordVersion()).isEqualTo(3);
        verify(repository).findByIdAndAgencyIdAndRecipientKey("notice-1", "A", "EMP-1");
    }

    private static PrimeHrNotification notification(PrimeHrNotification.Kind kind) {
        var notification = new PrimeHrNotification("A", "EMP-1", PERFORMANCE, ACTION_ASSIGNED,
                kind, "SPMS_RATING_CASE", "case-1", 1, "event:1",
                PrimeHrNotification.RouteKey.PERFORMANCE, "Title", "Message", null);
        setId(notification, "notice-1");
        return notification;
    }

    private static void setId(PrimeHrNotification notification, String id) {
        try {
            var field = notification.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(notification, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static PublishCommand command(String eventKey, long version) {
        return new PublishCommand("A", "emp-1", PERFORMANCE, ACTION_ASSIGNED,
                "SPMS_RATING_CASE", "case-1", version, eventKey, null);
    }
}
