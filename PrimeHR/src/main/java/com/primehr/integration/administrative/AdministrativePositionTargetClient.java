package com.primehr.integration.administrative;

import com.primehr.config.PrimeHrProperties;
import com.primehr.positionprofile.domain.PositionTargetType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class AdministrativePositionTargetClient {
    private final RestClient restClient;

    public AdministrativePositionTargetClient(PrimeHrProperties properties) {
        PrimeHrProperties.Administrative configuration = properties.administrative();
        if (configuration.baseUrl().isBlank()) {
            throw new IllegalStateException("PRIMEHR_ADMINISTRATIVE_API_URL must be configured");
        }
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(configuration.connectTimeoutMillis())).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(client);
        requestFactory.setReadTimeout(Duration.ofMillis(configuration.readTimeoutMillis()));
        restClient = RestClient.builder().baseUrl(configuration.baseUrl())
                .requestFactory(requestFactory).build();
    }

    public AdministrativePositionTarget get(PositionTargetType type, Long id, String authorizationHeader) {
        try {
            AdministrativePositionTarget target = restClient.get()
                    .uri("/api/integration/v1/primehr/position-targets/{type}/{id}", type, id)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .onStatus(status -> status.value() == 401 || status.value() == 403,
                            (request, response) -> { throw new AccessDeniedException("Access denied"); })
                    .onStatus(status -> status.value() == 404,
                            (request, response) -> { throw new IllegalArgumentException(
                                    "The selected Administrative position target no longer exists"); })
                    .onStatus(HttpStatusCode::isError,
                            (request, response) -> { throw new PositionTargetDependencyException(
                                    "Administrative position targets are unavailable", null); })
                    .body(AdministrativePositionTarget.class);
            validate(target, type, id);
            return target;
        } catch (AccessDeniedException | IllegalArgumentException | PositionTargetDependencyException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new PositionTargetDependencyException("Administrative position targets are unavailable", exception);
        }
    }

    private static void validate(AdministrativePositionTarget target, PositionTargetType type, Long id) {
        if (target == null || target.type() != type || !id.equals(target.targetId())
                || target.jobPositionId() == null || blank(target.jobPositionName())
                || blank(target.sourceFingerprint()) || target.fetchedAt() == null) {
            throw new PositionTargetDependencyException(
                    "Administrative returned an invalid position target response", null);
        }
        if (type == PositionTargetType.JOB_POSITION
                && (target.plantillaId() != null || target.plantillaName() != null)) {
            throw new PositionTargetDependencyException(
                    "Administrative returned an invalid Job Position target", null);
        }
        if (type == PositionTargetType.PLANTILLA
                && (target.plantillaId() == null || blank(target.plantillaName()))) {
            throw new PositionTargetDependencyException(
                    "Administrative returned an invalid Plantilla target", null);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
