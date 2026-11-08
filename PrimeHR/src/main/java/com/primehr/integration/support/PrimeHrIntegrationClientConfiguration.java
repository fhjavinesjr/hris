package com.primehr.integration.support;

import com.primehr.config.PrimeHrProperties;
import com.primehr.shared.exception.ApplicationConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class PrimeHrIntegrationClientConfiguration {

    @Bean
    @Qualifier("administrativeRestClient")
    RestClient administrativeRestClient(PrimeHrProperties properties,
            @Value("${primehr.integration.safe-read-max-attempts:2}") int maximumAttempts,
            @Value("${primehr.integration.safe-read-retry-delay-millis:100}") long retryDelayMillis) {
        var source = properties.administrative();
        return client(source.baseUrl(), source.connectTimeoutMillis(), source.readTimeoutMillis(),
                "Administrative", "ADMINISTRATIVE_SERVICE_UNAVAILABLE", maximumAttempts,
                retryDelayMillis, true);
    }

    @Bean
    @Qualifier("humanResourceRestClient")
    RestClient humanResourceRestClient(PrimeHrProperties properties,
            @Value("${primehr.integration.safe-read-max-attempts:2}") int maximumAttempts,
            @Value("${primehr.integration.safe-read-retry-delay-millis:100}") long retryDelayMillis) {
        var source = properties.humanResource();
        return client(source.baseUrl(), source.connectTimeoutMillis(), source.readTimeoutMillis(),
                "HumanResource", "HUMAN_RESOURCE_SERVICE_UNAVAILABLE", maximumAttempts,
                retryDelayMillis, true);
    }

    @Bean
    @Qualifier("humanResourceHandoffRestClient")
    RestClient humanResourceHandoffRestClient(PrimeHrProperties properties) {
        var source = properties.humanResource();
        return client(source.baseUrl(), source.connectTimeoutMillis(), source.readTimeoutMillis(),
                "HumanResource", "HUMAN_RESOURCE_SERVICE_UNAVAILABLE", 1, 0, false);
    }

    private static RestClient client(String baseUrl, int connectTimeoutMillis, int readTimeoutMillis,
                                     String dependency, String unavailableCode, int maximumAttempts,
                                     long retryDelayMillis, boolean normalizeStatuses) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("The " + dependency + " API URL must be configured");
        }
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMillis)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMillis));
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory)
                .requestInterceptor(new CorrelationPropagationInterceptor())
                .requestInterceptor(new SafeReadRetryInterceptor(dependency, maximumAttempts,
                        Duration.ofMillis(Math.max(0, retryDelayMillis))));
        if (normalizeStatuses) {
            builder.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                throw statusException(dependency, unavailableCode,
                        response.getStatusCode().value());
            });
        }
        return builder.build();
    }

    static RuntimeException statusException(String dependency, String unavailableCode, int status) {
        if (status == 401 || status == 403) {
            return new AccessDeniedException(dependency + " denied the request");
        }
        if (status == 404) {
            return new ResourceNotFoundException(dependency + " source was not found");
        }
        if (status == 409) {
            return new ApplicationConflictException(dependency + " source changed or conflicts");
        }
        if (status == 429 || status >= 500) {
            return new ExternalDependencyException(dependency, unavailableCode, status,
                    dependency + " is temporarily unavailable", null);
        }
        return new ApplicationConflictException(
                dependency + " rejected the request (HTTP " + status + ")");
    }
}
