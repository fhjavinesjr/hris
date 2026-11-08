package com.primehr.notification.application;

import com.primehr.config.PrimeHrProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Configuration
@EnableConfigurationProperties(NotificationReminderProperties.class)
class NotificationReminderPropertiesConfiguration { }

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "primehr.notifications.reminders.enabled", havingValue = "true")
class NotificationReminderSchedulingConfiguration { }

@Component
@ConditionalOnProperty(name = "primehr.notifications.reminders.enabled", havingValue = "true")
class NotificationReminderScheduler {
    private final NotificationReminderService service;
    private final PrimeHrProperties properties;

    NotificationReminderScheduler(NotificationReminderService service, PrimeHrProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${primehr.notifications.reminders.fixed-delay-millis:3600000}")
    void run() {
        service.issueDueReminders(properties.agency().id(), Instant.now(), null);
    }
}
