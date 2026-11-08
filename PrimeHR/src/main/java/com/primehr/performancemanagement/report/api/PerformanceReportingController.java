package com.primehr.performancemanagement.report.api;

import com.primehr.performancemanagement.report.application.PerformanceReportingData.*;
import com.primehr.performancemanagement.report.application.PerformanceReportingService;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.PerformanceReportingPermissionGuard;
import com.primehr.shared.audit.PrimeHrAuditService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@RestController
public class PerformanceReportingController {
    private final PerformanceReportingService service;
    private final PerformanceReportingPermissionGuard permission;
    private final AgencyScopeResolver agencies;
    private final PrimeHrAuditService audit;

    public PerformanceReportingController(PerformanceReportingService service,
            PerformanceReportingPermissionGuard permission, AgencyScopeResolver agencies,
            PrimeHrAuditService audit) {
        this.service = service;
        this.permission = permission;
        this.agencies = agencies;
        this.audit = audit;
    }

    @GetMapping("/api/primehr/v1/performance-management/reports/register")
    public RegisterPage register(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestParam LocalDate from, @RequestParam LocalDate to,
            @RequestParam(defaultValue = "false") boolean includeHistory,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String cycleId,
            @RequestParam(required = false) String formType,
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String ratingLabel) {
        authorize(PerformanceReportingPermissionGuard.REGISTER, token, authentication, correlationId);
        return service.register(agencies.resolveAgencyId(authentication),
                query(from, to, includeHistory, page, size, cycleId, formType, businessUnitId, employeeNo,
                        ratingLabel), actor(authentication), correlationId);
    }

    @GetMapping("/api/primehr/v1/performance-management/reports/analytics")
    public Analytics analytics(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestParam LocalDate from, @RequestParam LocalDate to,
            @RequestParam(defaultValue = "false") boolean includeHistory,
            @RequestParam(required = false) String cycleId,
            @RequestParam(required = false) String formType,
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String ratingLabel) {
        authorize(PerformanceReportingPermissionGuard.ANALYTICS, token, authentication, correlationId);
        return service.analytics(agencies.resolveAgencyId(authentication),
                query(from, to, includeHistory, 0, 100, cycleId, formType, businessUnitId, employeeNo,
                        ratingLabel), actor(authentication), correlationId);
    }

    @GetMapping("/api/primehr/integration/v1/performance-outcomes")
    public OutcomePage outcomes(Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestParam Instant changedAfter, @RequestParam Instant changedThrough,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        authorize(PerformanceReportingPermissionGuard.OUTCOME_FEED, token, authentication, correlationId);
        return service.outcomes(agencies.resolveAgencyId(authentication),
                new OutcomeQuery(changedAfter, changedThrough, page, size), actor(authentication), correlationId);
    }

    private static RegisterQuery query(LocalDate from, LocalDate to, boolean includeHistory, int page, int size,
            String cycleId, String formType, Long businessUnitId, String employeeNo, String ratingLabel) {
        return new RegisterQuery(from, to, includeHistory, page, size, cycleId, formType, businessUnitId,
                employeeNo, ratingLabel);
    }

    private void authorize(String feature, String token, Authentication authentication, String correlationId) {
        try {
            permission.require(feature, token);
        } catch (AccessDeniedException exception) {
            String agency = agencies.resolveAgencyId(authentication);
            audit.record(agency, "DENY_SPMS_REPORTING", "SPMS_REPORT_REQUEST", agency, null, null, null,
                    Map.of("featureKey", feature, "outcome", "DENIED"), null, correlationId);
            throw exception;
        }
    }

    private static String actor(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Authenticated identity is required");
        }
        return authentication.getName();
    }
}
