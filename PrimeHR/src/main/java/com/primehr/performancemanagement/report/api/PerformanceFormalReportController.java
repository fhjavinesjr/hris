package com.primehr.performancemanagement.report.api;

import com.primehr.performancemanagement.report.application.PerformanceFormalReportService;
import com.primehr.security.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/primehr/v1/performance-management/reports")
public class PerformanceFormalReportController {
    private final PerformanceFormalReportService service; private final AgencyScopeResolver agencies;
    private final PerformanceCommitmentPermissionGuard commitments; private final PerformanceRatingPermissionGuard ratings;
    private final PerformanceMonitoringPermissionGuard monitoring; private final PerformanceCoachingPermissionGuard coaching;
    private final PerformanceInterventionPermissionGuard interventions;
    private final PerformanceReportingPermissionGuard reports;
    public PerformanceFormalReportController(PerformanceFormalReportService service,AgencyScopeResolver agencies,
            PerformanceCommitmentPermissionGuard commitments,PerformanceRatingPermissionGuard ratings,
            PerformanceMonitoringPermissionGuard monitoring,PerformanceCoachingPermissionGuard coaching,
            PerformanceInterventionPermissionGuard interventions,PerformanceReportingPermissionGuard reports){this.service=service;this.agencies=agencies;this.commitments=commitments;this.ratings=ratings;this.monitoring=monitoring;this.coaching=coaching;this.interventions=interventions;this.reports=reports;}

    @GetMapping(value="/commitments/{id}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> planning(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id){String agency=agencies.resolveAgencyId(auth);var workflow=commitments.require(service.formTypeForCommitment(agency,id),PrimeHrAction.ACCESS,token);var scope=intersect(workflow,reports.requireScoped(PerformanceReportingPermissionGuard.FORM,token));return pdf("spms-planning-"+id,service.planningForm(agency,id,token,actor(auth),scope,correlation));}
    @GetMapping(value="/final-ratings/{id}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> rated(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id){String agency=agencies.resolveAgencyId(auth);var workflow=ratings.require(PrimeHrAction.ACCESS,token);var scope=intersect(workflow,reports.requireScoped(PerformanceReportingPermissionGuard.FORM,token));return pdf("spms-rated-"+id,service.ratedForm(agency,id,token,actor(auth),scope,correlation));}
    @GetMapping(value="/monitoring/{id}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> monitoring(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id){String agency=agencies.resolveAgencyId(auth);var workflow=monitoring.require(PrimeHrAction.ACCESS,token);var scope=intersect(workflow,reports.requireScoped(PerformanceReportingPermissionGuard.MONITORING,token));return pdf("spms-monitoring-"+id,service.monitoring(agency,id,token,actor(auth),scope,correlation));}
    @GetMapping(value="/coaching/{id}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> coaching(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id){String agency=agencies.resolveAgencyId(auth);var workflow=coaching.require(PrimeHrAction.ACCESS,token);var scope=intersect(workflow,reports.requireScoped(PerformanceReportingPermissionGuard.MONITORING,token));return pdf("spms-coaching-"+id,service.coaching(agency,id,token,actor(auth),scope,correlation));}
    @GetMapping(value="/interventions/{id}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> intervention(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id){return intervention(auth,token,correlation,id,false);}
    @GetMapping(value="/development-plans/{id}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> development(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id){return intervention(auth,token,correlation,id,true);}
    private ResponseEntity<byte[]> intervention(Authentication auth,String token,String correlation,String id,boolean development){String agency=agencies.resolveAgencyId(auth);var workflow=interventions.require(PrimeHrAction.ACCESS,token);var scope=intersect(workflow,reports.requireScoped(PerformanceReportingPermissionGuard.MONITORING,token));return pdf((development?"spms-development-plan-":"spms-intervention-")+id,service.intervention(agency,id,development,token,actor(auth),scope,correlation));}
    private static com.primehr.integration.administrative.PermissionDataScope intersect(com.primehr.integration.administrative.PermissionDataScope workflow,com.primehr.integration.administrative.PermissionDataScope report){if(workflow==report)return workflow;if(workflow==com.primehr.integration.administrative.PermissionDataScope.AGENCY_WIDE)return report;if(report==com.primehr.integration.administrative.PermissionDataScope.AGENCY_WIDE)return workflow;throw new org.springframework.security.access.AccessDeniedException("Workflow and report data scopes do not overlap");}
    private static String actor(Authentication auth){if(auth==null||auth.getName()==null||auth.getName().isBlank())throw new org.springframework.security.access.AccessDeniedException("Authenticated employee identity is required");return auth.getName();}
    private static ResponseEntity<byte[]> pdf(String name,byte[] body){String safe=name.replaceAll("[^A-Za-z0-9._-]","_")+".pdf";return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).cacheControl(CacheControl.noStore()).header("X-Content-Type-Options","nosniff").header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.inline().filename(safe).build().toString()).body(body);}
}
