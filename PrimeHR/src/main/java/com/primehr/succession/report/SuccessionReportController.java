package com.primehr.succession.report;

import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.PrimeHrAction;
import com.primehr.security.SuccessionPlanningPermissionGuard;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/primehr/v1/succession/reports")
public class SuccessionReportController {
    private final SuccessionReportService service;
    private final SuccessionPlanningPermissionGuard permissions;
    private final AgencyScopeResolver agencies;

    public SuccessionReportController(SuccessionReportService service, SuccessionPlanningPermissionGuard permissions,
                                      AgencyScopeResolver agencies) {
        this.service = service;
        this.permissions = permissions;
        this.agencies = agencies;
    }

    @GetMapping(value = "/readiness/{id}.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> readiness(Authentication authentication, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                             @PathVariable String id) {
        var scope = permissions.require(SuccessionPlanningPermissionGuard.REPORT, PrimeHrAction.ACCESS, token);
        return pdf("succession-readiness-" + id, service.readiness(agencies.resolveAgencyId(authentication), id, authentication.getName(), scope));
    }

    @GetMapping(value = "/development/{id}.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> development(Authentication authentication, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                               @PathVariable String id) {
        var scope = permissions.require(SuccessionPlanningPermissionGuard.REPORT, PrimeHrAction.ACCESS, token);
        return pdf("succession-development-" + id, service.development(agencies.resolveAgencyId(authentication), id, authentication.getName(), scope));
    }

    @GetMapping(value = "/continuity/{id}.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> continuity(Authentication authentication, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                              @PathVariable String id) {
        var scope = permissions.require(SuccessionPlanningPermissionGuard.REPORT, PrimeHrAction.ACCESS, token);
        return pdf("succession-continuity-" + id, service.continuity(agencies.resolveAgencyId(authentication), id, authentication.getName(), scope));
    }

    @GetMapping(value = "/referrals/{id}.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> referral(Authentication authentication, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                            @PathVariable String id) {
        var scope = permissions.require(SuccessionPlanningPermissionGuard.REPORT, PrimeHrAction.ACCESS, token);
        return pdf("succession-rsp-referral-" + id, service.referral(agencies.resolveAgencyId(authentication), id, authentication.getName(), scope));
    }

    @GetMapping(value = "/coverage-register.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> register(Authentication authentication, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        var scope = permissions.require(SuccessionPlanningPermissionGuard.REPORT, PrimeHrAction.ACCESS, token);
        return pdf("succession-coverage-register", service.coverageRegister(agencies.resolveAgencyId(authentication), authentication.getName(), scope));
    }

    @GetMapping("/analytics")
    public SuccessionReportData.Analytics analytics(Authentication authentication, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        permissions.require(SuccessionPlanningPermissionGuard.ANALYTICS, PrimeHrAction.ACCESS, token);
        return service.analytics(agencies.resolveAgencyId(authentication), authentication.getName());
    }

    private static ResponseEntity<byte[]> pdf(String filename, byte[] content) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(filename + ".pdf").build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store").body(content);
    }
}
