package com.primehr.integration.administrative;

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
public class AdministrativeAuthorizationClient {
    private static final String FEATURE = "primehr.competency";
    private final RestClient restClient;

    public AdministrativeAuthorizationClient(PrimeHrProperties properties) {
        PrimeHrProperties.Administrative configuration = properties.administrative();
        if (configuration.baseUrl().isBlank()) {
            throw new IllegalStateException("PRIMEHR_ADMINISTRATIVE_API_URL must be configured");
        }
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(configuration.connectTimeoutMillis()))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(client);
        requestFactory.setReadTimeout(Duration.ofMillis(configuration.readTimeoutMillis()));
        this.restClient = RestClient.builder().baseUrl(configuration.baseUrl())
                .requestFactory(requestFactory).build();
    }

    public EffectiveFeaturePermission resolve(String authorizationHeader) {
        try {
            EffectiveFeaturePermission permission = restClient.get()
                    .uri(builder -> builder.path("/api/authorization/effective")
                            .queryParam("featureKey", FEATURE).build())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .onStatus(status -> status.value() == 401 || status.value() == 403,
                            (request, response) -> { throw new AccessDeniedException("Access denied"); })
                    .onStatus(HttpStatusCode::isError,
                            (request, response) -> { throw new AuthorizationDependencyException(
                                    "Administrative authorization is unavailable", null); })
                    .body(EffectiveFeaturePermission.class);
            if (permission == null || !FEATURE.equals(permission.featureKey())) {
                throw new AuthorizationDependencyException("Administrative returned an invalid permission response", null);
            }
            return permission;
        } catch (AccessDeniedException exception) {
            throw exception;
        } catch (AuthorizationDependencyException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new AuthorizationDependencyException("Administrative authorization is unavailable", exception);
        }
    }
}
