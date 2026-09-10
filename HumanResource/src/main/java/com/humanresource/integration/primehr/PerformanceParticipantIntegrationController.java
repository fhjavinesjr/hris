package com.humanresource.integration.primehr;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integration/v1/primehr/performance-participants")
public class PerformanceParticipantIntegrationController {
    private final AssessmentSubjectIntegrationService subjects;private final PrimeHrPerformanceParticipantAuthorization authorization;
    public PerformanceParticipantIntegrationController(AssessmentSubjectIntegrationService s,PrimeHrPerformanceParticipantAuthorization a){subjects=s;authorization=a;}
    @GetMapping public AssessmentSubjectPageResponse list(@RequestHeader(HttpHeaders.AUTHORIZATION)String token,
            @RequestParam(required=false)String search,@RequestParam(defaultValue="0")int page,
            @RequestParam(defaultValue="20")int size){authorization.requireAgencyWide(token);return subjects.list(search,page,size,true);}
    @GetMapping("/{employeeId}") public AssessmentSubjectResponse get(@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable Long employeeId){authorization.requireAgencyWide(token);return subjects.get(employeeId);}
    @GetMapping("/by-employee-no/{employeeNo}") public AssessmentSubjectResponse byEmployeeNo(@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable String employeeNo){authorization.requireAgencyWide(token);return subjects.getByEmployeeNo(employeeNo);}
}
