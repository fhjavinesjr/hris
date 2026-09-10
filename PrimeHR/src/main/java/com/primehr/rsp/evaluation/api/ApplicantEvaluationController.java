package com.primehr.rsp.evaluation.api;

import com.primehr.rsp.evaluation.api.EvaluationExecutionDtos.ApplicantEvaluationStatus;
import com.primehr.rsp.evaluation.application.EvaluationExecutionService;
import com.primehr.security.AgencyScopeResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/primehr/applicant/v1/me/applications/{applicationId}/evaluation")
@ConditionalOnProperty(name="primehr.applicant.enabled",havingValue="true")
public class ApplicantEvaluationController {
    private final EvaluationExecutionService service;
    private final AgencyScopeResolver agency;
    public ApplicantEvaluationController(EvaluationExecutionService service,AgencyScopeResolver agency){this.service=service;this.agency=agency;}
    @GetMapping public ApplicantEvaluationStatus status(Authentication authentication,@PathVariable String applicationId){return service.applicantStatus(agency.resolveAgencyId(authentication),authentication.getName(),applicationId);}
}
