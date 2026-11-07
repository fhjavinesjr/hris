package com.primehr.operations.application;

import com.primehr.config.PrimeHrProperties;
import com.primehr.operations.api.OperationsDtos.DependencyStatus;
import com.primehr.operations.api.OperationsDtos.OperationsStatus;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class OperationsStatusService {
    private final HealthEndpoint health;
    private final PrimeHrProperties properties;
    private final OperationsObservability observability;
    private final HttpClient client;

    @Autowired
    public OperationsStatusService(HealthEndpoint health, PrimeHrProperties properties,
                                   OperationsObservability observability) {
        this(health, properties, observability, HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(properties.administrative().connectTimeoutMillis(),
                        properties.humanResource().connectTimeoutMillis())))
                .followRedirects(HttpClient.Redirect.NEVER).build());
    }

    OperationsStatusService(HealthEndpoint health, PrimeHrProperties properties,
                            OperationsObservability observability, HttpClient client) {
        this.health = health;
        this.properties = properties;
        this.observability = observability;
        this.client = client;
    }

    public OperationsStatus status() {
        String applicationStatus = health.health().getStatus().getCode();
        var dependencies = List.of(
                check("ADMINISTRATIVE", properties.administrative().baseUrl(),
                        properties.administrative().readTimeoutMillis()),
                check("HUMAN_RESOURCE", properties.humanResource().baseUrl(),
                        properties.humanResource().readTimeoutMillis()),
                configuredStorage());
        return new OperationsStatus(Instant.now(), applicationStatus, dependencies);
    }

    private DependencyStatus configuredStorage() {
        Instant started = Instant.now();
        String status = properties.storage().enabled() ? "UP" : "DISABLED";
        observability.dependency("DOCUMENT_STORAGE", status, Duration.between(started, Instant.now()));
        return new DependencyStatus("DOCUMENT_STORAGE", status, null, 0L);
    }

    private DependencyStatus check(String name, String baseUrl, int timeoutMillis) {
        Instant started = Instant.now();
        if (baseUrl == null || baseUrl.isBlank()) {
            Duration duration = Duration.between(started, Instant.now());
            observability.dependency(name, "NOT_CONFIGURED", duration);
            return new DependencyStatus(name, "NOT_CONFIGURED", null, duration.toMillis());
        }
        String status;
        Integer responseStatus = null;
        try {
            URI uri = URI.create(baseUrl + "/actuator/health/readiness");
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofMillis(timeoutMillis)).GET().build();
            responseStatus = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            status = responseStatus >= 200 && responseStatus < 300 ? "UP"
                    : responseStatus == 401 || responseStatus == 403 ? "UNAUTHORIZED" : "DOWN";
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            status = "UNAVAILABLE";
        } catch (RuntimeException | java.io.IOException exception) {
            status = "UNAVAILABLE";
        }
        Duration duration = Duration.between(started, Instant.now());
        observability.dependency(name, status, duration);
        return new DependencyStatus(name, status, responseStatus, duration.toMillis());
    }
}
