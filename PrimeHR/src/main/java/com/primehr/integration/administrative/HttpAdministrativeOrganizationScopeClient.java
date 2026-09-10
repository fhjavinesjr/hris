package com.primehr.integration.administrative;

import com.primehr.config.PrimeHrProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class HttpAdministrativeOrganizationScopeClient implements AdministrativeOrganizationScopeClient {
    private final RestClient restClient;

    public HttpAdministrativeOrganizationScopeClient(PrimeHrProperties properties) {
        var configuration = properties.administrative();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(configuration.connectTimeoutMillis())).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofMillis(configuration.readTimeoutMillis()));
        restClient = RestClient.builder().baseUrl(configuration.baseUrl()).requestFactory(factory).build();
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
