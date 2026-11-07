package com.primehr.integration.administrative;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class HttpAdministrativeOrganizationScopeClient implements AdministrativeOrganizationScopeClient {
    private final RestClient restClient;

    public HttpAdministrativeOrganizationScopeClient(
            @Qualifier("administrativeRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Set<Long> businessUnitIdsForArea(Long areaId, String authorization) {
        if (areaId == null || areaId < 1) throw new IllegalArgumentException("areaId must be positive");
        try {
            List<BusinessUnit> units = restClient.get().uri("/api/businessUnits/get-all")
                    .header(HttpHeaders.AUTHORIZATION, authorization).retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (units == null) return Set.of();
            return units.stream().filter(unit -> areaId.equals(unit.areasId()))
                    .map(BusinessUnit::businessUnitsId).collect(Collectors.toUnmodifiableSet());
        } catch (RestClientException exception) {
            throw new OrganizationDirectoryDependencyException("Administrative organization directory is unavailable", exception);
        }
    }

    public record BusinessUnit(Long businessUnitsId, Long areasId) {}
}
