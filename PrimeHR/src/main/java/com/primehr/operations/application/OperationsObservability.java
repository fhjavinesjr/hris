package com.primehr.operations.application;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class OperationsObservability {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationsObservability.class);
    private final MeterRegistry meters;

    public OperationsObservability(MeterRegistry meters) {
        this.meters = meters;
    }

    public void reconciliation(String trigger, String outcome, int checked, int findings, Duration duration) {
        String safeTrigger = "SCHEDULED".equals(trigger) ? "SCHEDULED" : "MANUAL";
        String safeOutcome = boundedOutcome(outcome);
        meters.counter("primehr.operations.reconciliation.runs", "trigger", safeTrigger,
                "outcome", safeOutcome).increment();
        meters.counter("primehr.operations.reconciliation.checked", "trigger", safeTrigger)
                .increment(Math.max(0, checked));
        meters.counter("primehr.operations.reconciliation.findings", "trigger", safeTrigger)
                .increment(Math.max(0, findings));
        Timer.builder("primehr.operations.reconciliation.duration")
                .tags("trigger", safeTrigger, "outcome", safeOutcome)
                .register(meters).record(duration);
        LOGGER.info("primehr_operation action=reconciliation trigger={} outcome={} checked={} findings={} durationMs={}",
                safeTrigger, safeOutcome, Math.max(0, checked), Math.max(0, findings), duration.toMillis());
    }

    public void dependency(String dependency, String outcome, Duration duration) {
        String safeDependency = switch (dependency) {
            case "ADMINISTRATIVE", "HUMAN_RESOURCE", "DATABASE", "DOCUMENT_STORAGE" -> dependency;
            default -> "UNKNOWN";
        };
        String safeOutcome = boundedOutcome(outcome);
        meters.counter("primehr.operations.dependency.checks", "dependency", safeDependency,
                "outcome", safeOutcome).increment();
        Timer.builder("primehr.operations.dependency.duration")
                .tags("dependency", safeDependency, "outcome", safeOutcome)
                .register(meters).record(duration);
        LOGGER.info("primehr_operation action=dependency_check dependency={} outcome={} durationMs={}",
                safeDependency, safeOutcome, duration.toMillis());
    }

    private static String boundedOutcome(String outcome) {
        return switch (outcome) {
            case "UP", "DOWN", "UNAVAILABLE", "UNAUTHORIZED", "NOT_CONFIGURED", "DISABLED",
                    "COMPLETED", "COMPLETED_WITH_FINDINGS", "FAILED" -> outcome;
            default -> "UNKNOWN";
        };
    }
}
