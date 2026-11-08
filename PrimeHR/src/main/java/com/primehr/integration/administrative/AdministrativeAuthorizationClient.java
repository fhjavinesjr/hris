package com.primehr.integration.administrative;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AdministrativeAuthorizationClient {
    private static final String COMPETENCY_FEATURE = "primehr.competency";
    private final RestClient restClient;

    public AdministrativeAuthorizationClient(
            @Qualifier("administrativeRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public EffectiveFeaturePermission resolve(String authorizationHeader) {
        return resolve(COMPETENCY_FEATURE, authorizationHeader);
    }

    public EffectiveFeaturePermission resolve(String featureKey, String authorizationHeader) {
        if (featureKey == null || featureKey.isBlank()) {
            throw new IllegalArgumentException("featureKey is required");
        }
        try {
            EffectiveFeaturePermission permission = restClient.get()
                    .uri(builder -> builder.path("/api/authorization/effective")
                            .queryParam("featureKey", featureKey).build())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(EffectiveFeaturePermission.class);
            if (permission == null || !featureKey.equals(permission.featureKey())) {
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

    public EffectiveFeaturePermission resolveEmployee(String featureKey, String employeeNo, String employeeRole,
                                                        String authorizationHeader) {
        try {
            EffectiveFeaturePermission permission = restClient.get()
                    .uri(builder -> builder.path("/api/authorization/effective/employee")
                            .queryParam("featureKey", featureKey).queryParam("employeeNo", employeeNo)
                            .queryParam("employeeRole", employeeRole).build())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader).retrieve()
                    .body(EffectiveFeaturePermission.class);
            if (permission == null || !featureKey.equals(permission.featureKey())) {
                throw new AuthorizationDependencyException("Administrative returned an invalid assignment permission", null);
            }
            return permission;
        } catch (AccessDeniedException | AuthorizationDependencyException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new AuthorizationDependencyException("Administrative assignment authorization is unavailable", exception);
        }
    }
}
