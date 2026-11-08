package com.primehr.integration.support;

import com.primehr.shared.exception.ApplicationConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeHrIntegrationClientConfigurationTest {
    @Test
    void normalizesDownstreamStatusFamilies() {
        assertThat(status(401)).isInstanceOf(AccessDeniedException.class);
        assertThat(status(403)).isInstanceOf(AccessDeniedException.class);
        assertThat(status(404)).isInstanceOf(ResourceNotFoundException.class);
        assertThat(status(409)).isInstanceOf(ApplicationConflictException.class);
        assertThat(status(422)).isInstanceOf(ApplicationConflictException.class);

        assertThat(status(429)).isInstanceOfSatisfying(ExternalDependencyException.class,
                exception -> {
                    assertThat(exception.code()).isEqualTo("DEPENDENCY_UNAVAILABLE");
                    assertThat(exception.downstreamStatus()).isEqualTo(429);
                });
        assertThat(status(503)).isInstanceOfSatisfying(ExternalDependencyException.class,
                exception -> assertThat(exception.downstreamStatus()).isEqualTo(503));
    }

    private static RuntimeException status(int value) {
        return PrimeHrIntegrationClientConfiguration.statusException(
                "TestDependency", "DEPENDENCY_UNAVAILABLE", value);
    }
}
