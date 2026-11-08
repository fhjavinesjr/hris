package com.primehr.integration.administrative;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;

@Component
public class AdministrativeRspPositionSourceClient {
    private final RestClient restClient;

    public AdministrativeRspPositionSourceClient(
            @Qualifier("administrativeRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public AdministrativeRspPositionSource get(Long plantillaId, Long businessUnitId, LocalDate asOf,
                                                String authorization) {
        try {
            AdministrativeRspPositionSource source = restClient.get()
                    .uri(builder -> builder.path("/api/integration/v1/primehr/rsp/position-sources/{id}")
                            .queryParam("businessUnitId", businessUnitId)
                            .queryParam("asOf", asOf).build(plantillaId))
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(AdministrativeRspPositionSource.class);
            if (source == null || !plantillaId.equals(source.plantillaId())
                    || source.qualificationStandardId() == null
                    || source.qualificationStandardVersion() < 1
                    || blank(source.sourceFingerprint())) {
                throw new PositionTargetDependencyException(
                        "Administrative returned an invalid RSP source", null);
            }
            return source;
        } catch (AccessDeniedException | PositionTargetDependencyException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new PositionTargetDependencyException(
                    "Administrative RSP source is unavailable", exception);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
