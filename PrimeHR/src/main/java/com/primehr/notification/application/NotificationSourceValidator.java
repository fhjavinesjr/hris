package com.primehr.notification.application;

import com.primehr.notification.domain.PrimeHrNotification;

public interface NotificationSourceValidator {
    boolean supports(PrimeHrNotification notification);
    Validation validate(PrimeHrNotification notification);

    record Validation(boolean available, long currentSourceVersion) {
        public static Validation available(long version) { return new Validation(true, version); }
        public static Validation unavailable(long version) { return new Validation(false, version); }
    }
}
