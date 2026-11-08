package com.primehr.operations.application;

import com.primehr.config.PrimeHrProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;

import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OperationsStatusServiceTest {
    @Test
    void statusIsSanitizedAndDoesNotProbeMissingDependencyUrls() {
        HealthEndpoint health = mock(HealthEndpoint.class);
        OperationsObservability observability = mock(OperationsObservability.class);
        HttpClient client = mock(HttpClient.class);
        when(health.health()).thenReturn(Health.up().build());
        var properties = new PrimeHrProperties(null, null, null,
                new PrimeHrProperties.Administrative("", 500, 500),
                new PrimeHrProperties.HumanResource("", 500, 500), null,
                new PrimeHrProperties.Storage(false, "local", "", null, null, null, null, null), null);

        var response = new OperationsStatusService(health, properties, observability, client).status();

        assertThat(response.applicationStatus()).isEqualTo("UP");
        assertThat(response.dependencies()).extracting(value -> value.dependency() + ":" + value.status())
                .containsExactly("ADMINISTRATIVE:NOT_CONFIGURED", "HUMAN_RESOURCE:NOT_CONFIGURED",
                        "DOCUMENT_STORAGE:DISABLED");
        verifyNoInteractions(client);
    }
}
