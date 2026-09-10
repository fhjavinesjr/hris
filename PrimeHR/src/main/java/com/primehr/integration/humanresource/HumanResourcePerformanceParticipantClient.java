package com.primehr.integration.humanresource;

public interface HumanResourcePerformanceParticipantClient {
    HumanResourceAssessmentSubject get(Long employeeId,String token);
    HumanResourceAssessmentSubject getByEmployeeNo(String employeeNo,String token);
}
