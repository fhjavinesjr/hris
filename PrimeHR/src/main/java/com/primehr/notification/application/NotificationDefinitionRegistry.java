package com.primehr.notification.application;

import com.primehr.notification.domain.PrimeHrNotification.EventCode;
import com.primehr.notification.domain.PrimeHrNotification.Kind;
import com.primehr.notification.domain.PrimeHrNotification.RouteKey;
import com.primehr.notification.domain.PrimeHrNotification.SourceDomain;
import org.springframework.stereotype.Component;

@Component
public class NotificationDefinitionRegistry {
    public Definition resolve(SourceDomain domain, EventCode code) {
        if (domain == null || code == null) throw new IllegalArgumentException("Notification domain and event code are required");
        String label = switch (domain) {
            case COMPETENCY -> "Competency";
            case RSP -> "Recruitment, Selection and Placement";
            case PERFORMANCE -> "Performance Management";
            case LEARNING_DEVELOPMENT -> "Learning and Development";
            case REWARDS_RECOGNITION -> "Rewards and Recognition";
            case SUCCESSION -> "Career and Succession";
            case EVIDENCE -> "Evidence Center";
            case OPERATIONS -> "PRIME-HRM Operations";
        };
        RouteKey route = RouteKey.valueOf(domain.name());
        return switch (code) {
            case ACTION_ASSIGNED -> new Definition(Kind.ACTION_REQUIRED, route,
                    label + " action required", "A " + label + " workflow item requires your attention.");
            case RECORD_RETURNED -> new Definition(Kind.ACTION_REQUIRED, route,
                    label + " record returned", "A " + label + " record was returned and requires your attention.");
            case OUTCOME_AVAILABLE -> new Definition(Kind.INFORMATIONAL, route,
                    label + " update available", "A released " + label + " workflow update is available.");
            case DUE_REMINDER -> new Definition(Kind.ACTION_REQUIRED, route,
                    label + " due-date reminder", "A " + label + " workflow item is approaching its authoritative due date.");
        };
    }

    public record Definition(Kind kind, RouteKey routeKey, String title, String message) { }
}
