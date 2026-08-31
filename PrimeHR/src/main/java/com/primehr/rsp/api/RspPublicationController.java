package com.primehr.rsp.api;

import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.rsp.api.RspPublicationDtos.CreatePublication;
import com.primehr.rsp.api.RspPublicationDtos.PublicationResponse;
import com.primehr.rsp.api.RspPublicationDtos.PublicationTransition;
import com.primehr.rsp.api.RspPublicationDtos.UpdatePublication;
import com.primehr.rsp.application.RspPublicationService;
import com.primehr.rsp.report.VacancyNoticeReportService;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.PrimeHrAction;
import com.primehr.security.RspPublicationPermissionGuard;
import com.primehr.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/primehr/v1/rsp/vacancy-publications")
public class RspPublicationController {
    private final RspPublicationService service;
    private final RspPublicationPermissionGuard permission;
    private final AgencyScopeResolver agencyScope;
    private final VacancyNoticeReportService reportService;

    public RspPublicationController(RspPublicationService service,
                                    RspPublicationPermissionGuard permission,
                                    AgencyScopeResolver agencyScope,
                                    VacancyNoticeReportService reportService) {
        this.service = service;
        this.permission = permission;
        this.agencyScope = agencyScope;
        this.reportService = reportService;
    }

    @GetMapping
    public PageResponse<PublicationResponse> list(Authentication authentication,
                                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                                   @RequestParam(name = "size", defaultValue = "20") int size) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.list(agency(authentication), page, size);
    }

    @GetMapping("/{id}")
    public PublicationResponse get(Authentication authentication,
                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                   @PathVariable("id") String id) {
        permission.require(PrimeHrAction.ACCESS, token);
        return service.get(agency(authentication), id);
    }

    @GetMapping(value = "/{id}/notice.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> notice(Authentication authentication,
                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                         @PathVariable("id") String id) {
        permission.require(PrimeHrAction.ACCESS, token);
        String agencyId = agency(authentication);
        PublicationResponse publication = service.get(agencyId, id);
        String filename = "vacancy-notice-" + id.replaceAll("[^A-Za-z0-9._-]", "_") + ".pdf";
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(filename).build().toString())
                .body(reportService.generate(agencyId, publication));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublicationResponse create(Authentication authentication,
                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                      @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                      @Valid @RequestBody CreatePublication request) {
        permission.require(PrimeHrAction.ADD, token);
        return service.create(agency(authentication), request, token, correlationId);
    }

    @PutMapping("/{id}")
    public PublicationResponse update(Authentication authentication,
                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                      @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                      @PathVariable("id") String id,
                                      @Valid @RequestBody UpdatePublication request) {
        permission.require(PrimeHrAction.EDIT, token);
        return service.update(agency(authentication), id, request, token, correlationId);
    }

    @PostMapping("/{id}/submit")
    public PublicationResponse submit(Authentication authentication,
                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                      @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                      @PathVariable("id") String id,
                                      @Valid @RequestBody PublicationTransition request) {
        permission.require(PrimeHrAction.SUBMIT, token);
        return service.submit(agency(authentication), id, request, token, correlationId);
    }

    @PostMapping("/{id}/return")
    public PublicationResponse returnSubmission(Authentication authentication,
                                                @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                                @PathVariable("id") String id,
                                                @Valid @RequestBody PublicationTransition request) {
        permission.require(PrimeHrAction.APPROVE, token);
        return service.returnSubmission(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/{id}/approve")
    public PublicationResponse approve(Authentication authentication,
                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                       @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                       @PathVariable("id") String id,
                                       @Valid @RequestBody PublicationTransition request) {
        EffectiveFeaturePermission effective = permission.require(PrimeHrAction.APPROVE, token);
        return service.approve(agency(authentication), id, request, token,
                effective.administrator(), correlationId);
    }

    @PostMapping("/{id}/publish")
    public PublicationResponse publish(Authentication authentication,
                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                       @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                       @PathVariable("id") String id,
                                       @Valid @RequestBody PublicationTransition request) {
        EffectiveFeaturePermission effective = permission.require(PrimeHrAction.PUBLISH, token);
        return service.publish(agency(authentication), id, request, token,
                effective.administrator(), correlationId);
    }

    @PostMapping("/{id}/cancel")
    public PublicationResponse cancel(Authentication authentication,
                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                      @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                      @PathVariable("id") String id,
                                      @Valid @RequestBody PublicationTransition request) {
        permission.require(PrimeHrAction.ARCHIVE, token);
        return service.cancel(agency(authentication), id, request, correlationId);
    }

    @PostMapping("/{id}/close")
    public PublicationResponse close(Authentication authentication,
                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                     @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                     @PathVariable("id") String id,
                                     @Valid @RequestBody PublicationTransition request) {
        permission.require(PrimeHrAction.ARCHIVE, token);
        return service.close(agency(authentication), id, request, correlationId);
    }

    private String agency(Authentication authentication) {
        return agencyScope.resolveAgencyId(authentication);
    }
}
