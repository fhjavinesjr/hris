package com.primehr.rsp.applicant.api;

import com.primehr.rsp.applicant.application.ApplicantApplicationService;
import com.primehr.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/primehr/applicant/v1/me/applications")
@ConditionalOnProperty(name = "primehr.applicant.enabled", havingValue = "true")
public class ApplicantApplicationController {
    private final ApplicantApplicationService service;

    public ApplicantApplicationController(ApplicantApplicationService service) { this.service = service; }

    @GetMapping
    public PageResponse<ApplicationDtos.Application> list(Authentication authentication,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return service.applicantApplications(authentication.getName(), page, size);
    }

    @PostMapping
    public ApplicationDtos.Application create(Authentication authentication,
                                               @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                               @Valid @RequestBody ApplicationDtos.Create request) {
        return service.create(authentication.getName(), request, correlationId);
    }

    @GetMapping("/{id}")
    public ApplicationDtos.Application get(Authentication authentication, @PathVariable String id) {
        return service.applicantApplication(authentication.getName(), id);
    }

    @PutMapping("/{id}")
    public ApplicationDtos.Application save(Authentication authentication, @PathVariable String id,
                                             @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                             @Valid @RequestBody ApplicationDtos.Save request) {
        return service.save(authentication.getName(), id, request, correlationId);
    }

    @PostMapping("/{id}/submit")
    public ApplicationDtos.Application submit(Authentication authentication, @PathVariable String id,
                                               @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                               @Valid @RequestBody ApplicationDtos.Submit request) {
        return service.submit(authentication.getName(), id, request, correlationId);
    }

    @PostMapping("/{id}/withdraw")
    public ApplicationDtos.Application withdraw(Authentication authentication, @PathVariable String id,
                                                 @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                                 @Valid @RequestBody ApplicationDtos.Withdraw request) {
        return service.withdraw(authentication.getName(), id, request, correlationId);
    }

    @GetMapping("/{id}/communications")
    public List<ApplicationDtos.Communication> communications(Authentication authentication,
                                                               @PathVariable String id) {
        return service.applicantCommunications(authentication.getName(), id);
    }
}
