package com.humanresource.integration.administrative;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

@Component
public class StaffOvertimeAuthorizationClient {
    public record SupervisedUnit(Long businessUnitId, String businessUnitName, String role,
                                 LocalDate effectiveFrom, LocalDate effectiveTo,
                                 List<Long> personnelEmployeeIds) {}

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public StaffOvertimeAuthorizationClient(
            @Value("${primehr.administrative.base-url:}") String baseUrl,
            @Value("${primehr.administrative.connect-timeout-millis:3000}") int connectTimeout,
            @Value("${primehr.administrative.read-timeout-millis:5000}") int readTimeout) {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.max(250, connectTimeout));
        factory.setReadTimeout(Math.max(250, readTimeout));
        this.restTemplate = new RestTemplate(factory);
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    public SupervisedUnit requireAuthorizedUnit(String bearerToken, Long businessUnitId,
                                                LocalDate fromDate, LocalDate toDate) {
        if (baseUrl.isBlank()) {
            throw new IllegalStateException("Administrative authorization service is not configured.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        String url = baseUrl + "/api/manage-personnel/supervised-units?fromDate=" + fromDate
                + "&toDate=" + toDate;
        try {
            ResponseEntity<List<SupervisedUnit>> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<SupervisedUnit>>() {});
            return response.getBody() == null ? null : response.getBody().stream()
                    .filter(unit -> businessUnitId.equals(unit.businessUnitId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "You are not the effective Head/OIC for the selected Business Unit and overtime dates."));
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to verify Head/OIC authority with Administrative.", ex);
        }
    }
}
