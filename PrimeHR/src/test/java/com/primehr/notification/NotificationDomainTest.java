package com.primehr.notification;

import com.primehr.notification.domain.PrimeHrNotification;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.primehr.notification.domain.PrimeHrNotification.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationDomainTest {
    @Test
    void informationalNotificationCanBeReadAndDismissed() {
        var value = notification(Kind.INFORMATIONAL);
        Instant at = Instant.parse("2026-11-06T12:00:00Z");
        value.dismiss(at);
        assertThat(value.getReadAt()).isEqualTo(at);
        assertThat(value.getDismissedAt()).isEqualTo(at);
        assertThat(value.getRetentionTag()).isEqualTo(PrimeHrNotification.RETENTION_TAG);
    }

    @Test
    void actionRequiredNotificationCannotBeDismissed() {
        assertThatThrownBy(() -> notification(Kind.ACTION_REQUIRED).dismiss(Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be dismissed");
    }

    @Test
    void unsafeSourceAndEventKeysAreRejected() {
        assertThatThrownBy(() -> new PrimeHrNotification("A", "EMP-1", SourceDomain.PERFORMANCE,
                EventCode.ACTION_ASSIGNED, Kind.ACTION_REQUIRED, "bad type", "id", 1,
                "unsafe key with spaces", RouteKey.PERFORMANCE, "Title", "Message", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static PrimeHrNotification notification(Kind kind) {
        return new PrimeHrNotification("A", "emp-1", SourceDomain.PERFORMANCE,
                kind == Kind.INFORMATIONAL ? EventCode.OUTCOME_AVAILABLE : EventCode.ACTION_ASSIGNED,
                kind, "SPMS_RATING_CASE", "case-1", 2, "performance:case-1:2",
                RouteKey.PERFORMANCE, "Safe title", "Safe message", null);
    }
}
