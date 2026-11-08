package com.primehr.integration.humanresource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HumanResourcePlantillaOccupancyClient {
    private final RestClient restClient;

    public HumanResourcePlantillaOccupancyClient(
            @Qualifier("humanResourceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public HumanResourcePlantillaOccupancy get(Long plantillaId, String authorization) {
        try {
            HumanResourcePlantillaOccupancy occupancy = restClient.get()
                    .uri("/api/integration/v1/primehr/plantilla-occupancy/{id}", plantillaId)
                    .header(HttpHeaders.AUTHORIZATION, authorization).retrieve()
                    .body(HumanResourcePlantillaOccupancy.class);
            if (occupancy == null || !plantillaId.equals(occupancy.plantillaId())
                    || occupancy.sourceFingerprint() == null || occupancy.fetchedAt() == null
                    || occupancy.occupied() && (occupancy.activeAppointmentId() == null
                    || occupancy.assumptionToDutyDate() == null)) {
                throw new HumanResourceDependencyException(
                        "HumanResource returned invalid Plantilla occupancy", null);
            }
            return occupancy;
        } catch (AccessDeniedException | HumanResourceDependencyException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new HumanResourceDependencyException(
                    "HumanResource Plantilla occupancy is unavailable", exception);
        }
    }
}
