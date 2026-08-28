package com.humanresource.integration.primehr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Component
public class PrimeHrSubjectAuthorization {
    public static final String FEATURE_KEY = "primehr.assessment-administration";
    private final RestTemplate restTemplate;
    private final String effectiveAuthorizationUrl;

    public PrimeHrSubjectAuthorization(@Value("${primehr.administrative.base-url:}") String baseUrl,
            @Value("${primehr.administrative.connect-timeout-millis:3000}") int connectTimeoutMillis,
            @Value("${primehr.administrative.read-timeout-millis:5000}") int readTimeoutMillis) {
        String normalized = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
        if (normalized.isEmpty()) {
            restTemplate = null;
            effectiveAuthorizationUrl = null;
            return;
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.max(250, connectTimeoutMillis));
        factory.setReadTimeout(Math.max(250, readTimeoutMillis));
        restTemplate = new RestTemplate(factory);
        effectiveAuthorizationUrl = normalized
                + "/api/authorization/effective?featureKey=" + FEATURE_KEY;
    }

    public void requireAgencyWideAccess(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new AccessDeniedException("A bearer token is required");
        }
        if (restTemplate == null) throw unavailable(null);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
            ResponseEntity<AdministrativePermissionResponse> response = restTemplate.exchange(
                    effectiveAuthorizationUrl, HttpMethod.GET, new HttpEntity<>(headers),
                    AdministrativePermissionResponse.class);
            AdministrativePermissionResponse permission = response.getBody();
            if (permission == null || !FEATURE_KEY.equals(permission.featureKey())) throw unavailable(null);
            if (!permission.administrator() && (!permission.canAccess()
                    || !"AGENCY_WIDE".equals(permission.dataScope()))) {
                throw new AccessDeniedException("Assessment administration access is not permitted");
            }
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden exception) {
            throw new AccessDeniedException("Access denied", exception);
        } catch (AccessDeniedException | ResponseStatusException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    private static ResponseStatusException unavailable(Throwable cause) {
        return new ResponseStatusException(SERVICE_UNAVAILABLE,
                "Administrative authorization is unavailable", cause);
    }
}
