package com.primehr.integration.humanresource;

import com.primehr.config.PrimeHrProperties;
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
public class HumanResourceAssessmentSubjectClient {
    private final RestClient restClient;

    public HumanResourceAssessmentSubjectClient(PrimeHrProperties properties) {
        PrimeHrProperties.HumanResource configuration = properties.humanResource();
        if (configuration.baseUrl().isBlank()) {
            throw new IllegalStateException("PRIMEHR_HUMAN_RESOURCE_API_URL must be configured");
        }
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(configuration.connectTimeoutMillis())).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofMillis(configuration.readTimeoutMillis()));
        restClient = RestClient.builder().baseUrl(configuration.baseUrl()).requestFactory(factory).build();
    }

    public HumanResourceAssessmentSubject get(Long employeeId, String authorizationHeader) {
        try {
            HumanResourceAssessmentSubject subject = restClient.get()
                    .uri("/api/integration/v1/primehr/assessment-subjects/{employeeId}", employeeId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader).retrieve()
                    .onStatus(status -> status.value() == 401 || status.value() == 403,
                            (request, response) -> { throw new AccessDeniedException("Access denied"); })
                    .onStatus(HttpStatusCode::isError,
                            (request, response) -> { throw new HumanResourceDependencyException(
                                    "HumanResource assessment subject is unavailable", null); })
                    .body(HumanResourceAssessmentSubject.class);
            if (subject == null || !employeeId.equals(subject.employeeId()) || !subject.eligible()) {
                throw new HumanResourceDependencyException("HumanResource returned an invalid subject", null);
            }
            return subject;
        } catch (AccessDeniedException | HumanResourceDependencyException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new HumanResourceDependencyException("HumanResource assessment subjects are unavailable", exception);
        }
    }
}
