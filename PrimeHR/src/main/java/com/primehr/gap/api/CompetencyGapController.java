package com.primehr.gap.api;

import com.primehr.gap.api.CompetencyGapDtos.*;
import com.primehr.gap.application.CompetencyGapService;
import com.primehr.gap.report.CompetencyGapReportService;
import com.primehr.gap.domain.GapClassification;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.security.*;
import com.primehr.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/primehr/v1/competency-gaps")
public class CompetencyGapController {
    private final CompetencyGapService service;
    private final CompetencyGapReportService reportService;
    private final GapPermissionGuard permission;
    private final AgencyScopeResolver agencyScope;

    public CompetencyGapController(CompetencyGapService service, CompetencyGapReportService reportService,
                                   GapPermissionGuard permission,
                                   AgencyScopeResolver agencyScope) {
        this.service = service;
        this.reportService = reportService;
        this.permission = permission;
        this.agencyScope = agencyScope;
    }

    @GetMapping
    public PageResponse<AnalysisSummaryResponse> list(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "employeeNo", required = false) String employeeNo,
            @RequestParam(name = "classification", required = false) GapClassification classification,
            @RequestParam(name = "priority", required = false) String priority,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        EffectiveFeaturePermission effective = permission.require(GapPermissionGuard.GAP, PrimeHrAction.ACCESS, token);
        return service.list(agencyScope.resolveAgencyId(auth), employeeNo, classification, priority,
                page, size, auth.getName(), effective.dataScope());
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AnalysisResponse generate(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody GenerateRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.GAP, PrimeHrAction.ADD, token);
        return service.generate(agencyScope.resolveAgencyId(auth), request, token, auth.getName(), correlationId);
    }

    @GetMapping("/{analysisId}")
    public AnalysisResponse get(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                @PathVariable("analysisId") String analysisId) {
        EffectiveFeaturePermission effective = permission.require(GapPermissionGuard.GAP, PrimeHrAction.ACCESS, token);
        return service.get(agencyScope.resolveAgencyId(auth), analysisId, auth.getName(), effective.dataScope());
    }

    @GetMapping(value = "/{analysisId}/report.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> report(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable("analysisId") String analysisId) {
        EffectiveFeaturePermission effective = permission.require(GapPermissionGuard.GAP, PrimeHrAction.ACCESS, token);
        AnalysisResponse analysis = service.get(agencyScope.resolveAgencyId(auth), analysisId,
                auth.getName(), effective.dataScope());
        String employeeNo = analysis.employeeNo() == null ? "employee"
                : analysis.employeeNo().replaceAll("[^A-Za-z0-9._-]", "_");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("competency-gap-" + employeeNo + ".pdf").build().toString())
                .body(reportService.generate(analysis));
    }

    @GetMapping("/employees/{employeeNo}/latest")
    public AnalysisResponse latest(Authentication auth, @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                   @PathVariable("employeeNo") String employeeNo) {
        EffectiveFeaturePermission effective = permission.require(GapPermissionGuard.GAP, PrimeHrAction.ACCESS, token);
        return service.latest(agencyScope.resolveAgencyId(auth), employeeNo, auth.getName(), effective.dataScope());
    }

    @GetMapping("/employees/{employeeNo}/history")
    public PageResponse<AnalysisSummaryResponse> history(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable("employeeNo") String employeeNo,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        EffectiveFeaturePermission effective = permission.require(GapPermissionGuard.GAP, PrimeHrAction.ACCESS, token);
        return service.history(agencyScope.resolveAgencyId(auth), employeeNo, page, size,
                auth.getName(), effective.dataScope());
    }
}
