package com.humanresource.integration.primehr;

public interface AssessmentSubjectIntegrationService {
    AssessmentSubjectPageResponse list(String search, int page, int size, boolean activeOnly);
    AssessmentSubjectResponse get(Long employeeId);
}
