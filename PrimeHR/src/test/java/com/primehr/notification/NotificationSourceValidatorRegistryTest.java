package com.primehr.notification;

import com.primehr.notification.application.NotificationSourceValidatorRegistry;
import com.primehr.notification.domain.PrimeHrNotification;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationSourceValidatorRegistryTest {
    @Test
    void unregisteredSourceResolvesUnavailableWithoutExposingTheTarget() {
        var registry = new NotificationSourceValidatorRegistry(List.of());
        var notification = new PrimeHrNotification("A", "EMP-1",
                PrimeHrNotification.SourceDomain.RSP, PrimeHrNotification.EventCode.ACTION_ASSIGNED,
                PrimeHrNotification.Kind.ACTION_REQUIRED, "RSP_SCREENING_CASE", "case-1", 0,
                "event:1", PrimeHrNotification.RouteKey.RSP, "Title", "Message", null);

        assertThat(registry.validate(notification))
                .isEqualTo(new com.primehr.notification.application.NotificationSourceValidator.Validation(false, 0));
    }
}
