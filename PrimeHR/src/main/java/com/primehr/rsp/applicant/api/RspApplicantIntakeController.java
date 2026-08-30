package com.primehr.rsp.applicant.api;

import com.primehr.rsp.applicant.application.ApplicantApplicationService;
import com.primehr.security.*;
import com.primehr.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/primehr/v1/rsp/applications")
public class RspApplicantIntakeController {
    private final ApplicantApplicationService service;
    private final RspApplicantIntakePermissionGuard permission;
    private final AgencyScopeResolver agencyScope;

    public RspApplicantIntakeController(ApplicantApplicationService service,
                                        RspApplicantIntakePermissionGuard permission,
                                        AgencyScopeResolver agencyScope) {
        this.service = service; this.permission = permission; this.agencyScope = agencyScope;
    }

    @GetMapping
    public PageResponse<ApplicationDtos.Application> list(Authentication authentication,
                                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.staffApplications(agencyScope.resolveAgencyId(authentication), page, size);
    }

    @GetMapping("/{id}")
    public ApplicationDtos.Application get(Authentication authentication,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                           @PathVariable String id) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.staffApplication(agencyScope.resolveAgencyId(authentication), id);
    }

    @GetMapping("/{id}/communications")
    public List<ApplicationDtos.Communication> communications(Authentication authentication,
                                                               @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                               @PathVariable String id) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.staffCommunications(agencyScope.resolveAgencyId(authentication), id);
    }

    @PostMapping("/{id}/communications")
    public ApplicationDtos.Communication message(Authentication authentication,
                                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                  @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                                  @PathVariable String id,
                                                  @Valid @RequestBody ApplicationDtos.StaffMessage request) {
        permission.require(PrimeHrAction.ADD, token);
        return service.sendStaffMessage(agencyScope.resolveAgencyId(authentication), id, request,
                authentication.getName(), correlationId);
    }

    @GetMapping("/{id}/documents/{documentId}/content")
    public ResponseEntity<InputStreamResource> document(Authentication authentication,
                                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                         @PathVariable String id,
                                                         @PathVariable String documentId) {
        permission.require(PrimeHrAction.ACCESS, token);
        var value = service.staffDocument(agencyScope.resolveAgencyId(authentication), id, documentId,
                authentication.getName());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(value.mediaType()))
                .contentLength(value.size())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(value.filename()).build().toString())
                .body(new InputStreamResource(value.stream()));
    }
}
