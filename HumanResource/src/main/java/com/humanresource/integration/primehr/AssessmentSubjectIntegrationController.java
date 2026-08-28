package com.humanresource.integration.primehr;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integration/v1/primehr/assessment-subjects")
public class AssessmentSubjectIntegrationController {
    private final AssessmentSubjectIntegrationService subjects;
    private final PrimeHrSubjectAuthorization authorization;

    public AssessmentSubjectIntegrationController(AssessmentSubjectIntegrationService subjects,
                                                  PrimeHrSubjectAuthorization authorization) {
        this.subjects = subjects;
        this.authorization = authorization;
    }

    @GetMapping
    public AssessmentSubjectPageResponse list(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly) {
        authorization.requireAgencyWideAccess(token);
        return subjects.list(search, page, size, activeOnly);
    }

    @GetMapping("/{employeeId}")
    public AssessmentSubjectResponse get(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                         @PathVariable("employeeId") Long employeeId) {
        authorization.requireAgencyWideAccess(token);
        return subjects.get(employeeId);
    }
}
