package com.primehr.integration.humanresource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HumanResourceAssessmentSubjectClient {
    private final RestClient restClient;

    public HumanResourceAssessmentSubjectClient(
            @Qualifier("humanResourceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public HumanResourceAssessmentSubject get(Long employeeId, String authorization) {
        try {
            HumanResourceAssessmentSubject subject = restClient.get()
                    .uri("/api/integration/v1/primehr/assessment-subjects/{employeeId}", employeeId)
                    .header(HttpHeaders.AUTHORIZATION, authorization).retrieve()
                    .body(HumanResourceAssessmentSubject.class);
            if (subject == null || !employeeId.equals(subject.employeeId()) || !subject.eligible()) {
                throw new HumanResourceDependencyException(
                        "HumanResource returned an invalid subject", null);
            }
            return subject;
        } catch (AccessDeniedException | HumanResourceDependencyException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new HumanResourceDependencyException(
                    "HumanResource assessment subjects are unavailable", exception);
        }
    }
}
