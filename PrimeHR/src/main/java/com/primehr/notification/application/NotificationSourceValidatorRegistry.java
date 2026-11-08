package com.primehr.notification.application;

import com.primehr.notification.domain.PrimeHrNotification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationSourceValidatorRegistry {
    private final List<NotificationSourceValidator> validators;

    public NotificationSourceValidatorRegistry(List<NotificationSourceValidator> validators) {
        this.validators = List.copyOf(validators);
    }

    public NotificationSourceValidator.Validation validate(PrimeHrNotification notification) {
        return validators.stream().filter(value -> value.supports(notification)).findFirst()
                .map(value -> value.validate(notification))
                .orElseGet(() -> NotificationSourceValidator.Validation.unavailable(
                        notification.getSourceRecordVersion()));
    }
}
