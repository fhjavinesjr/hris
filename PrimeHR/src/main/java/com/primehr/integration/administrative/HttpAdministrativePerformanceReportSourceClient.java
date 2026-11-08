package com.primehr.integration.administrative;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpAdministrativePerformanceReportSourceClient
        implements AdministrativePerformanceReportSourceClient {
    private final RestClient restClient;

    public HttpAdministrativePerformanceReportSourceClient(
            @Qualifier("administrativeRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Branding branding(String authorization) {
        return get("/api/integration/v1/primehr/report-sources/branding",
                authorization, Branding.class);
    }

    @Override
    public Position position(Long plantillaId, String authorization) {
        return get("/api/integration/v1/primehr/report-sources/positions/" + plantillaId,
                authorization, Position.class);
    }

    private <T> T get(String uri, String authorization, Class<T> type) {
        try {
            T value = restClient.get().uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(type);
            if (value == null) {
                throw new OrganizationDirectoryDependencyException(
                        "Administrative report source returned no data", null);
            }
            return value;
        } catch (RestClientException exception) {
            throw new OrganizationDirectoryDependencyException(
                    "Administrative report source is unavailable", exception);
        }
    }
}
