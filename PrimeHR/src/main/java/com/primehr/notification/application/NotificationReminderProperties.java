package com.primehr.notification.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "primehr.notifications.reminders")
public record NotificationReminderProperties(Boolean enabled, Integer leadHours, Long fixedDelayMillis,
                                             Integer maximumBatchSize) {
    public NotificationReminderProperties {
        enabled = enabled != null && enabled;
        leadHours = leadHours == null ? 24 : Math.max(1, Math.min(720, leadHours));
        fixedDelayMillis = fixedDelayMillis == null ? 3_600_000L
                : Math.max(60_000L, fixedDelayMillis);
        maximumBatchSize = maximumBatchSize == null ? 100
                : Math.max(1, Math.min(500, maximumBatchSize));
    }
}
