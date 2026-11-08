package com.primehr.integration.humanresource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class HumanResourceEmployeeDirectoryClient {
    private final RestClient restClient;

    public HumanResourceEmployeeDirectoryClient(
            @Qualifier("humanResourceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public HumanResourceEmployeeDirectoryEntry require(String employeeNo, String token) {
        try {
            List<HumanResourceEmployeeDirectoryEntry> values = restClient.get()
                    .uri("/api/employees/basicInfo")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (values == null) {
                throw new HumanResourceDependencyException(
                        "Assigned employee was not found in HumanResource", null);
            }
            return values.stream()
                    .filter(value -> value.employeeNo() != null
                            && value.employeeNo().equalsIgnoreCase(employeeNo))
                    .findFirst()
                    .orElseThrow(() -> new HumanResourceDependencyException(
                            "Assigned employee was not found in HumanResource", null));
        } catch (HumanResourceDependencyException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new HumanResourceDependencyException(
                    "HumanResource employee directory is unavailable", exception);
        }
    }
}
