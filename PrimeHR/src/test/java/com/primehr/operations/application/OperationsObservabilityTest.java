package com.primehr.operations.application;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class OperationsObservabilityTest {
    @Test
    void metricsUseOnlyBoundedOperationalLabels() {
        var registry = new SimpleMeterRegistry();
        var observability = new OperationsObservability(registry);

        observability.reconciliation("unexpected employee value", "unexpected failure text", 4, 2,
                Duration.ofMillis(25));
        observability.dependency("https://private-host/employee/123", "raw exception text",
                Duration.ofMillis(10));

        assertThat(registry.get("primehr.operations.reconciliation.runs")
                .tags("trigger", "MANUAL", "outcome", "UNKNOWN").counter().count()).isEqualTo(1d);
        assertThat(registry.get("primehr.operations.dependency.checks")
                .tags("dependency", "UNKNOWN", "outcome", "UNKNOWN").counter().count()).isEqualTo(1d);
        assertThat(registry.getMeters().stream().flatMap(meter -> meter.getId().getTags().stream())
                .map(tag -> tag.getValue()).toList())
                .noneMatch(value -> value.contains("employee") || value.contains("private-host"));
    }
}
