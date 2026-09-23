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
import java.util.Comparator;
import java.util.List;

@Component
public class StaffOvertimeAuthorizationClient {
    public record SupervisedUnit(Long businessUnitId, String businessUnitName, String role,
                                 LocalDate effectiveFrom, LocalDate effectiveTo,
                                 List<Long> personnelEmployeeIds) {}
    public record EmployeeRequest(Long employeeRequestId, String code, String name, Integer max) {}
    public record ApprovalStep(Long approvalWorkflowId, Long employeeId, Long businessUnitId,
                               Long employeeRequestId, Integer approvalLevel) {}

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

    public List<ApprovalStep> requireOvertimeApprovalRoute(String bearerToken, Long businessUnitId) {
        if (baseUrl.isBlank()) {
            throw new IllegalStateException("Administrative authorization service is not configured.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<List<EmployeeRequest>> requests = restTemplate.exchange(
                    baseUrl + "/api/employeeRequest/get-all", HttpMethod.GET, request,
                    new ParameterizedTypeReference<List<EmployeeRequest>>() {});
            EmployeeRequest overtimeRequest = requests.getBody() == null ? null : requests.getBody().stream()
                    .filter(value -> "Overtime Request".equalsIgnoreCase(value.name())
                            || "OVERTIME_REQUEST".equalsIgnoreCase(value.code()))
                    .findFirst()
                    .orElse(null);
            if (overtimeRequest == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The Overtime Request workflow type is not configured in Administrative.");
            }
            String url = baseUrl + "/api/approval-workflow/get-by-unit-and-request?businessUnitId="
                    + businessUnitId + "&employeeRequestId=" + overtimeRequest.employeeRequestId();
            ResponseEntity<List<ApprovalStep>> response = restTemplate.exchange(
                    url, HttpMethod.GET, request,
                    new ParameterizedTypeReference<List<ApprovalStep>>() {});
            List<ApprovalStep> route = response.getBody() == null ? List.of() : response.getBody().stream()
                    .sorted(Comparator.comparing(ApprovalStep::approvalLevel)
                            .thenComparing(ApprovalStep::approvalWorkflowId))
                    .toList();
            if (route.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No Overtime Request approval workflow is configured for the selected Business Unit.");
            }
            return route;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to resolve the Overtime Request approval workflow from Administrative.", ex);
        }
    }
}
