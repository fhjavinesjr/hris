package com.primehr.evidence.api;

import com.primehr.evidence.api.EvidenceReadinessDtos.SignOffInput;
import com.primehr.evidence.application.EvidenceReadinessData.*;
import com.primehr.evidence.application.EvidenceReadinessService;
import com.primehr.security.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/primehr/v1/evidence-readiness")
public class EvidenceReadinessController {
    private final EvidenceReadinessService service;
    private final EvidenceReportingPermissionGuard reporting;
    private final EvidenceWorkflowPermissionGuard workflow;
    private final AgencyScopeResolver agencies;

    public EvidenceReadinessController(EvidenceReadinessService service,
            EvidenceReportingPermissionGuard reporting, EvidenceWorkflowPermissionGuard workflow,
            AgencyScopeResolver agencies) {
        this.service = service;
        this.reporting = reporting;
        this.workflow = workflow;
        this.agencies = agencies;
    }

    @GetMapping("/cases/{id}/calculation")
    public Calculation calculate(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token, @PathVariable String id,
            @RequestParam(required = false) LocalDate asOf,
            @RequestParam(required = false) LocalDate expiringThrough) {
        return service.calculate(agency(authentication), id, asOf, expiringThrough, actor(authentication),
                reporting.requireReport(token));
    }

    @GetMapping("/cases/{id}/snapshots")
    public List<Snapshot> snapshots(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token, @PathVariable String id) {
        return service.snapshots(agency(authentication), id, actor(authentication), reporting.requireReport(token));
    }

    @PostMapping("/cases/{id}/snapshots")
    @ResponseStatus(HttpStatus.CREATED)
    public Snapshot signOff(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable String id, @Valid @RequestBody SignOffInput input) {
        var scope = workflow.require(EvidenceWorkflowPermissionGuard.VALIDATION, PrimeHrAction.FINALIZE, token);
        return service.signOff(agency(authentication), id, input, actor(authentication), scope, correlationId);
    }

    @GetMapping("/exceptions")
    public ExceptionView exceptions(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(required = false) LocalDate asOf,
            @RequestParam(required = false) LocalDate expiringThrough) {
        return service.exceptions(agency(authentication), asOf, expiringThrough, actor(authentication),
                reporting.requireReport(token));
    }

    @GetMapping(value = "/reports/cases/{id}/evidence-index.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> evidenceIndex(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token, @PathVariable String id) {
        byte[] content = service.evidenceIndex(agency(authentication), id, actor(authentication),
                reporting.requireReport(token));
        return pdf("internal-evidence-index-" + id, content);
    }

    @GetMapping(value = "/reports/snapshots/{id}/internal-assessment.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> internalAssessment(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token, @PathVariable String id) {
        byte[] content = service.internalAssessment(agency(authentication), id, actor(authentication),
                reporting.requireReport(token));
        return pdf("internal-readiness-assessment-" + id, content);
    }

    @GetMapping("/analytics")
    public Analytics analytics(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestParam LocalDate from, @RequestParam LocalDate to) {
        reporting.requireAnalytics(token);
        return service.analytics(agency(authentication), from, to, token, actor(authentication), correlationId);
    }

    private String agency(Authentication authentication) { return agencies.resolveAgencyId(authentication); }
    private static String actor(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Authenticated employee identity is required");
        }
        return authentication.getName();
    }
    private static ResponseEntity<byte[]> pdf(String filename, byte[] content) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(filename + ".pdf").build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store").body(content);
    }
}
