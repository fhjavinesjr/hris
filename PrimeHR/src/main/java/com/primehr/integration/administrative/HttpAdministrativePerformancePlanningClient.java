package com.primehr.integration.administrative;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpAdministrativePerformancePlanningClient
        implements AdministrativePerformancePlanningClient {
    private final RestClient restClient;

    public HttpAdministrativePerformancePlanningClient(
            @Qualifier("administrativeRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public OrganizationTarget organization(String type, Long id, String authorization) {
        return get("/api/integration/v1/primehr/performance/organization-targets/{type}/{id}",
                authorization, OrganizationTarget.class, type, id);
    }

    @Override
    public PersonnelMembership membership(Long employeeId, String authorization) {
        return get("/api/integration/v1/primehr/performance/personnel-membership/{id}",
                authorization, PersonnelMembership.class, employeeId);
    }

    @Override
    public ApprovalRoute approvalRoute(Long businessUnitId, String requestCode, String authorization) {
        try {
            ApprovalRoute value = restClient.get()
                    .uri(builder -> builder.path("/api/integration/v1/primehr/performance/approval-routes")
                            .queryParam("businessUnitId", businessUnitId)
                            .queryParam("requestCode", requestCode).build())
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(ApprovalRoute.class);
            if (value == null) {
                throw new OrganizationDirectoryDependencyException(
                        "Administrative returned no approval route", null);
            }
            return value;
        } catch (RestClientException exception) {
            throw new OrganizationDirectoryDependencyException(
                    "Administrative performance approval route is unavailable", exception);
        }
    }

    private <T> T get(String path, String authorization, Class<T> type, Object... variables) {
        try {
            T value = restClient.get().uri(path, variables)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(type);
            if (value == null) {
                throw new OrganizationDirectoryDependencyException(
                        "Administrative returned no performance source", null);
            }
            return value;
        } catch (RestClientException exception) {
            throw new OrganizationDirectoryDependencyException(
                    "Administrative performance source is unavailable", exception);
        }
    }
}
